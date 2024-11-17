/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.connection.interceptor;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.InvocationContext;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.connection.ConnectionDefinition;
import io.micronaut.data.connection.ConnectionOperations;
import io.micronaut.data.connection.ConnectionOperationsRegistry;
import io.micronaut.data.connection.DefaultConnectionDefinition;
import io.micronaut.data.connection.annotation.Connectable;
import io.micronaut.data.connection.annotation.ConnectionClientInfoAttribute;
import io.micronaut.data.connection.async.AsyncConnectionOperations;
import io.micronaut.data.connection.reactive.ReactiveStreamsConnectionOperations;
import io.micronaut.data.connection.reactive.ReactorConnectionOperations;
import io.micronaut.data.connection.support.ConnectionClientInfoDetails;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link Connectable} interceptor.
 *
 * @author Denis stepanov
 * @since 4.0.0
 */
@Internal
@Singleton
@InterceptorBean(Connectable.class)
public final class ConnectableInterceptor implements MethodInterceptor<Object, Object> {

    private static final String ENABLED = "enabled";
    private static final String MODULE_MEMBER = "module";
    private static final String ACTION_MEMBER = "action";
    private static final String NAME_MEMBER = "name";
    private static final String VALUE_MEMBER = "value";
    private static final String CLIENT_INFO_ATTRIBUTES_MEMBER = "clientInfoAttributes";
    private static final String INTERCEPTED_SUFFIX = "$Intercepted";

    private final Map<TenantExecutableMethod, ConnectionInvocation> connectionInvocationMap = new ConcurrentHashMap<>(30);

    @NonNull
    private final ConnectionOperationsRegistry connectionOperationsRegistry;
    @Nullable
    private final ConnectionDataSourceTenantResolver tenantResolver;

    private final ConversionService conversionService;

    @Nullable
    private final String appName;

    /**
     * Default constructor.
     *
     * @param connectionOperationsRegistry The {@link ConnectionOperationsRegistry}
     * @param tenantResolver               The tenant resolver
     * @param conversionService            The conversion service
     */
    ConnectableInterceptor(@NonNull ConnectionOperationsRegistry connectionOperationsRegistry,
                           @Nullable ConnectionDataSourceTenantResolver tenantResolver,
                           ApplicationConfiguration applicationConfiguration,
                           ConversionService conversionService) {
        this.connectionOperationsRegistry = connectionOperationsRegistry;
        this.tenantResolver = tenantResolver;
        this.appName = applicationConfiguration.getName().orElse(null);
        this.conversionService = conversionService;
    }

    @Override
    public int getOrder() {
        return InterceptPhase.TRANSACTION.getPosition() - 10;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String tenantDataSourceName;
        if (tenantResolver != null) {
            tenantDataSourceName = tenantResolver.resolveTenantDataSourceName();
        } else {
            tenantDataSourceName = null;
        }
        InterceptedMethod interceptedMethod = InterceptedMethod.of(context, conversionService);
        try {
            ExecutableMethod<Object, Object> executableMethod = context.getExecutableMethod();
            final ConnectionInvocation connectionInvocation = connectionInvocationMap
                .computeIfAbsent(new TenantExecutableMethod(tenantDataSourceName, executableMethod), ignore -> {
                    final String dataSource = tenantDataSourceName == null ? executableMethod.stringValue(Connectable.class).orElse(null) : tenantDataSourceName;
                    final ConnectionDefinition connectionDefinition = getConnectionDefinition(context, executableMethod, appName);

                    switch (interceptedMethod.resultType()) {
                        case PUBLISHER -> {
                            ReactiveStreamsConnectionOperations<?> operations = connectionOperationsRegistry.provideReactive(ReactiveStreamsConnectionOperations.class, dataSource);
                            return new ConnectionInvocation(null, operations, null, connectionDefinition);
                        }
                        case COMPLETION_STAGE -> {
                            AsyncConnectionOperations<?> operations = connectionOperationsRegistry.provideAsync(AsyncConnectionOperations.class, dataSource);
                            return new ConnectionInvocation(null, null, operations, connectionDefinition);
                        }
                        default -> {
                            ConnectionOperations<?> operations = connectionOperationsRegistry.provideSynchronous(ConnectionOperations.class, dataSource);
                            return new ConnectionInvocation(operations, null, null, connectionDefinition);
                        }
                    }
                });

            final ConnectionDefinition definition = connectionInvocation.definition;
            switch (interceptedMethod.resultType()) {
                case PUBLISHER -> {
                    ReactiveStreamsConnectionOperations<?> operations = Objects.requireNonNull(connectionInvocation.reactiveStreamsConnectionOperations);
                    if (connectionInvocation.reactorConnectionOperations != null) {
                        ReactorConnectionOperations<?> reactorConnectionOperations = connectionInvocation.reactorConnectionOperations;
                        if (context.getExecutableMethod().getReturnType().isSingleResult()) {
                            return reactorConnectionOperations.withConnectionMono(definition, status -> Mono.from(interceptedMethod.interceptResultAsPublisher()));
                        }
                        return reactorConnectionOperations.withConnectionFlux(definition, status -> Flux.from(interceptedMethod.interceptResultAsPublisher()));
                    }
                    return interceptedMethod.handleResult(
                        operations.withConnection(definition, status -> interceptedMethod.interceptResultAsPublisher())
                    );
                }
                case COMPLETION_STAGE -> {
                    AsyncConnectionOperations<?> operations = Objects.requireNonNull(connectionInvocation.asyncConnectionOperations);
                    return interceptedMethod.handleResult(
                        operations.withConnection(definition, status -> interceptedMethod.interceptResultAsCompletionStage())
                    );
                }
                case SYNCHRONOUS -> {
                    ConnectionOperations<?> operations = Objects.requireNonNull(connectionInvocation.connectionOperations);
                    return operations.execute(definition, connection -> context.proceed());
                }
                default -> {
                    return interceptedMethod.unsupported();
                }
            }
        } catch (Exception e) {
            return interceptedMethod.handleException(e);
        }
    }

    /**
     * Retrieves the connection definition based on the provided executable method and application name.
     *
     * This method is deprecated since version 4.10.4 and marked for removal in future versions.
     *
     * @param executableMethod the executable method to retrieve the connection definition for
     * @param appName the application name
     * @return the connection definition
     * @deprecated Since 4.10.4, use {@link #getConnectionDefinition(InvocationContext, ExecutableMethod, String)} instead
     */
    @NonNull
    @Deprecated(since = "4.10.4", forRemoval = true)
    public static ConnectionDefinition getConnectionDefinition(ExecutableMethod<Object, Object> executableMethod, String appName) {
        return getConnectionDefinition(null, executableMethod, appName);
    }

    /**
     * Retrieves the connection definition based on the provided executable method and application name.
     *
     * This method examines the annotations present on the executable method to determine the connection definition.
     * It looks for the presence of the {@link Connectable} annotation and uses its attributes to construct the connection definition.
     * Additionally, it checks for the presence of the {@link io.micronaut.data.connection.annotation.ConnectionClientInfo} annotation to obtain connection tracing information.
     *
     * @param context      the invocation context, may be null
     * @param executableMethod the executable method to retrieve the connection definition for
     * @param appName       the application name
     * @return the connection definition
     */
    @NonNull
    public static ConnectionDefinition getConnectionDefinition(@Nullable InvocationContext<Object, Object> context,
                                                               ExecutableMethod<Object, Object> executableMethod,
                                                               String appName) {
        AnnotationValue<Connectable> annotation = executableMethod.getAnnotation(Connectable.class);
        if (annotation == null) {
            throw new IllegalStateException("No declared @Connectable annotation present");
        }
        AnnotationValue<io.micronaut.data.connection.annotation.ConnectionClientInfo> connectionClientInfoAnnotationValue = executableMethod.getAnnotation(io.micronaut.data.connection.annotation.ConnectionClientInfo.class);
        ConnectionClientInfoDetails connectionClientInfoDetails = connectionClientInfoAnnotationValue == null ? null : getConnectionClientInfo(connectionClientInfoAnnotationValue, context, executableMethod, appName);
        return new DefaultConnectionDefinition(
            executableMethod.getDeclaringType().getSimpleName() + "." + executableMethod.getMethodName(),
            annotation.enumValue("propagation", ConnectionDefinition.Propagation.class).orElse(ConnectionDefinition.PROPAGATION_DEFAULT),
            annotation.longValue("timeout").stream().mapToObj(Duration::ofSeconds).findFirst().orElse(null),
            annotation.booleanValue("readOnly").orElse(null),
            connectionClientInfoDetails
        );
    }

    /**
     * Gets connection client info from the {@link io.micronaut.data.connection.annotation.ConnectionClientInfo} annotation.
     *
     * @param annotation The {@link io.micronaut.data.connection.annotation.ConnectionClientInfo} annotation value
     * @param executableMethod The method being executed
     * @param appName The micronaut application name, null if not set
     * @return The connection client info or null if not configured to be used
     */
    private static @Nullable ConnectionClientInfoDetails getConnectionClientInfo(AnnotationValue<io.micronaut.data.connection.annotation.ConnectionClientInfo> annotation,
                                                                                 @Nullable InvocationContext<Object, Object> context,
                                                                                 ExecutableMethod<Object, Object> executableMethod,
                                                                                 String appName) {
        boolean connectionClientInfoEnabled = annotation.booleanValue(ENABLED).orElse(true);
        if (!connectionClientInfoEnabled) {
            return null;
        }
        String module = annotation.stringValue(MODULE_MEMBER).orElse(null);
        String action = annotation.stringValue(ACTION_MEMBER).orElse(null);
        if (module == null) {
            if (context != null) {
                Class<?> clazz = context.getTarget().getClass();
                module = clazz.getName().replace(INTERCEPTED_SUFFIX, "");
            } else {
                module = executableMethod.getDeclaringType().getName();
            }
        }
        if (action == null) {
            action = executableMethod.getMethodName();
        }
        List<AnnotationValue<ConnectionClientInfoAttribute>> clientInfoAttributes = annotation.getAnnotations(CLIENT_INFO_ATTRIBUTES_MEMBER, ConnectionClientInfoAttribute.class);
        Map<String, String> additionalClientInfoAttributes = new HashMap<>(clientInfoAttributes.size());
        for (AnnotationValue<ConnectionClientInfoAttribute> clientInfoAttribute : clientInfoAttributes) {
            String name = clientInfoAttribute.getRequiredValue(NAME_MEMBER, String.class);
            String value = clientInfoAttribute.getRequiredValue(VALUE_MEMBER, String.class);
            additionalClientInfoAttributes.put(name, value);
        }
        return new ConnectionClientInfoDetails(appName, module, action, additionalClientInfoAttributes);
    }

    /**
     * Cached invocation associating a method with a definition a connection manager.
     *
     * @param connectionOperations                The connection operations
     * @param reactorConnectionOperations         The reactor connection operations
     * @param reactiveStreamsConnectionOperations The reactive connection operations
     * @param asyncConnectionOperations           The async connection operations
     * @param definition                          The connection definition
     */
    private record ConnectionInvocation(
        @Nullable ConnectionOperations<?> connectionOperations,
        @Nullable ReactorConnectionOperations<?> reactorConnectionOperations,
        @Nullable ReactiveStreamsConnectionOperations<?> reactiveStreamsConnectionOperations,
        @Nullable AsyncConnectionOperations<?> asyncConnectionOperations,
        ConnectionDefinition definition) {

        ConnectionInvocation(
            @Nullable ConnectionOperations<?> connectionOperations,
            @Nullable ReactiveStreamsConnectionOperations<?> reactiveStreamsConnectionOperations,
            @Nullable AsyncConnectionOperations<?> asyncConnectionOperations, ConnectionDefinition definition) {

            this(connectionOperations,
                reactiveStreamsConnectionOperations instanceof ReactorConnectionOperations<?> reactorReactiveConnectionOperations ? reactorReactiveConnectionOperations : null,
                reactiveStreamsConnectionOperations,
                asyncConnectionOperations,
                definition);
        }
    }

    /**
     * The tenant executable method.
     *
     * @param dataSource The datasource name
     * @param method     The method
     */
    private record TenantExecutableMethod(String dataSource, ExecutableMethod<?, ?> method) {
    }

}
