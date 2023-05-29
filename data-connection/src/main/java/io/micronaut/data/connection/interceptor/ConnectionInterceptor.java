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
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.connection.ConnectionOperationsRegistry;
import io.micronaut.data.connection.annotation.Connection;
import io.micronaut.data.connection.manager.ConnectionDefinition;
import io.micronaut.data.connection.manager.DefaultConnectionDefinition;
import io.micronaut.data.connection.manager.async.AsyncConnectionOperations;
import io.micronaut.data.connection.manager.reactive.ReactiveConnectionOperations;
import io.micronaut.data.connection.manager.synchronous.ConnectionOperations;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link Connection} interceptor.
 *
 * @author Denis stepanov
 * @since 4.0.0
 */
@Internal
@Singleton
@InterceptorBean(Connection.class)
public final class ConnectionInterceptor implements MethodInterceptor<Object, Object> {

    private final Map<TenantExecutableMethod, ConnectionInvocation> connectionInvocationMap = new ConcurrentHashMap<>(30);

    @NonNull
    private final ConnectionOperationsRegistry connectionOperationsRegistry;
//    @Nullable
//    private final TransactionDataSourceTenantResolver tenantResolver;

    private final ConversionService conversionService;

    /**
     * Default constructor.
     *
     * @param connectionOperationsRegistry The {@link ConnectionOperationsRegistry}
     * @param conversionService            The conversion service
     */
    public ConnectionInterceptor(@NonNull ConnectionOperationsRegistry connectionOperationsRegistry,
//                                 @Nullable TransactionDataSourceTenantResolver tenantResolver,
                                 ConversionService conversionService) {
        this.connectionOperationsRegistry = connectionOperationsRegistry;
//        this.tenantResolver = tenantResolver;
        this.conversionService = conversionService;
    }

    @Override
    public int getOrder() {
        return InterceptPhase.TRANSACTION.getPosition() - 10;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String tenantDataSourceName = null;
//        if (tenantResolver != null) {
//            tenantDataSourceName = tenantResolver.resolveTenantDataSourceName();
//        } else {
//            tenantDataSourceName = null;
//        }
        InterceptedMethod interceptedMethod = InterceptedMethod.of(context, conversionService);
        try {
            ExecutableMethod<Object, Object> executableMethod = context.getExecutableMethod();
            final ConnectionInvocation connectionInvocation = connectionInvocationMap
                .computeIfAbsent(new TenantExecutableMethod(tenantDataSourceName, executableMethod), ignore -> {
                    final String dataSource = tenantDataSourceName == null ? executableMethod.stringValue(Connection.class).orElse(null) : tenantDataSourceName;
                    final ConnectionDefinition connectionDefinition = getConnectionDefinition(executableMethod);

                    switch (interceptedMethod.resultType()) {
                        case PUBLISHER -> {
                            ReactiveConnectionOperations<?> operations = connectionOperationsRegistry.provideReactive(ReactiveConnectionOperations.class, dataSource);
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
                    ReactiveConnectionOperations<?> operations = Objects.requireNonNull(connectionInvocation.reactiveConnectionOperations);
                    return interceptedMethod.handleResult(
                        operations.withConnection(definition, (status) -> interceptedMethod.interceptResultAsPublisher())
                    );
                }
                case COMPLETION_STAGE -> {
                    AsyncConnectionOperations<?> operations = Objects.requireNonNull(connectionInvocation.asyncConnectionOperations);
//                    boolean isKotlinSuspended = interceptedMethod instanceof KotlinInterceptedMethod;
                    CompletionStage<?> result;
//                    if (isKotlinSuspended) {
//                        KotlinInterceptedMethod kotlinInterceptedMethod = (KotlinInterceptedMethod) interceptedMethod;
//                        result = operations.withConnection(definition, new KotlinInterceptedMethodAsyncResultSupplier<>(kotlinInterceptedMethod));
//                    } else {
                    result = operations.withConnection(definition, status -> interceptedMethod.interceptResultAsCompletionStage());
//                    }
                    return interceptedMethod.handleResult(result);
                }
                case SYNCHRONOUS -> {
                    ConnectionOperations<?> operations = Objects.requireNonNull(connectionInvocation.connectionManager);
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

    @NonNull
    public static ConnectionDefinition getConnectionDefinition(ExecutableMethod<Object, Object> executableMethod) {
        AnnotationValue<Connection> annotation = executableMethod.getAnnotation(Connection.class);
        if (annotation == null) {
            throw new IllegalStateException("No declared @Connection annotation present");
        }

        return new DefaultConnectionDefinition(
            executableMethod.getDeclaringType().getSimpleName() + "." + executableMethod.getMethodName(),
            annotation.enumValue("propagation", ConnectionDefinition.Propagation.class).orElse(ConnectionDefinition.PROPAGATION_DEFAULT),
            annotation.enumValue("transactionIsolation", ConnectionDefinition.TransactionIsolation.class).orElse(null),
            annotation.longValue("timeout").stream().mapToObj(Duration::ofSeconds).findFirst().orElse(null),
            annotation.booleanValue("readOnly").orElse(null)
        );
    }

    /**
     * Cached invocation associating a method with a definition a connection manager.
     */
    private record ConnectionInvocation(
        @Nullable ConnectionOperations<?> connectionManager,
        @Nullable ReactiveConnectionOperations<?> reactiveConnectionOperations,
        @Nullable AsyncConnectionOperations<?> asyncConnectionOperations,
        ConnectionDefinition definition) {

    }

    private record TenantExecutableMethod(String dataSource, ExecutableMethod<?, ?> method) {
    }

}