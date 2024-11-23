/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.data.connection.jdbc.oracle;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.connection.ConnectionDefinition;
import io.micronaut.data.connection.ConnectionStatus;
import io.micronaut.data.connection.annotation.ConnClientInfoAttr;
import io.micronaut.data.connection.annotation.ConnClientInfo;
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import io.micronaut.data.connection.support.AbstractConnectionOperations;
import io.micronaut.data.connection.support.ConnectionListener;
import io.micronaut.runtime.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A customizer for Oracle database connections that sets client information after opening and clears before closing.
 *
 * This customizer checks if the connection is an Oracle database connection and then sets the client information
 * (client ID, module, and action) after opening the connection. It also clears these properties before closing the connection.
 *
 * @author radovanradic
 * @since 4.11
 */
@EachBean(DataSource.class)
@Requires(condition = OracleClientInfoCondition.class)
@Context
@Internal
final class OracleClientInfoConnectionListener implements ConnectionListener<Connection> {

    private static final String NAME_MEMBER = "name";
    private static final String VALUE_MEMBER = "value";
    private static final String INTERCEPTED_SUFFIX = "$Intercepted";

    /**
     * Constant for the Oracle connection client info client ID property name.
     */
    private static final String ORACLE_CLIENT_ID = "OCSID.CLIENTID";
    /**
     * Constant for the Oracle connection client info module property name.
     */
    private static final String ORACLE_MODULE = "OCSID.MODULE";
    /**
     * Constant for the Oracle connection client info action property name.
     */
    private static final String ORACLE_ACTION = "OCSID.ACTION";
    /**
     * Constant for the Oracle connection database product name.
     */
    private static final String ORACLE_CONNECTION_DATABASE_PRODUCT_NAME = "Oracle";

    private static final Logger LOG = LoggerFactory.getLogger(OracleClientInfoConnectionListener.class);

    private static final Map<Class<?>, String> MODULE_CLASS_MAP = new ConcurrentHashMap<>(100);

    @Nullable
    private final String applicationName;

    OracleClientInfoConnectionListener(@NonNull DataSource dataSource,
                                       @NonNull @Parameter AbstractConnectionOperations<Connection> connectionOperations,
                                       @Nullable ApplicationConfiguration applicationConfiguration) {
        this.applicationName = applicationConfiguration != null ? applicationConfiguration.getName().orElse(null) : null;
        try {
            Connection connection = DelegatingDataSource.unwrapDataSource(dataSource).getConnection();
            if (isOracleConnection(connection)) {
                connectionOperations.addConnectionListener(this);
            }
        } catch (SQLException e) {
            LOG.error("Failed to get connection for oracle connection listener", e);
        }
    }

    @Override
    public void afterOpen(@NonNull ConnectionStatus<Connection> connectionStatus) {
        ConnectionDefinition connectionDefinition = connectionStatus.getDefinition();
        // Set client info for connection if Oracle connection after connection is opened
        Map<String, String> connectionClientInfo = getConnectionClientInfo(connectionDefinition);
        if (connectionClientInfo != null && !connectionClientInfo.isEmpty()) {
            Connection connection = connectionStatus.getConnection();
            LOG.trace("Setting connection tracing info to the Oracle connection");
            try {
                for (Map.Entry<String, String> additionalInfo : connectionClientInfo.entrySet()) {
                    String name = additionalInfo.getKey();
                    String value = additionalInfo.getValue();
                    connection.setClientInfo(name, value);
                }
            } catch (SQLClientInfoException e) {
                LOG.debug("Failed to set connection tracing info", e);
            }
        }
    }

    @Override
    public void beforeClose(@NonNull ConnectionStatus<Connection> connectionStatus) {
        // Clear client info for connection if it was Oracle connection and client info was set previously
        ConnectionDefinition connectionDefinition = connectionStatus.getDefinition();
        Map<String, String> connectionClientInfo = getConnectionClientInfo(connectionDefinition);
        if (connectionClientInfo != null && !connectionClientInfo.isEmpty()) {
            try {
                Connection connection = connectionStatus.getConnection();
                for (String key : connectionClientInfo.keySet()) {
                    connection.setClientInfo(key, null);
                }
            } catch (SQLClientInfoException e) {
                LOG.debug("Failed to clear connection tracing info", e);
            }
        }
    }

    @Override
    public String getName() {
        return "Oracle Connection Client Info Customizer";
    }

    /**
     * Checks whether current connection is Oracle database connection.
     *
     * @param connection The connection
     * @return true if current connection is Oracle database connection
     */
    private boolean isOracleConnection(Connection connection) {
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return StringUtils.isNotEmpty(databaseProductName) && databaseProductName.equalsIgnoreCase(ORACLE_CONNECTION_DATABASE_PRODUCT_NAME);
        } catch (SQLException e) {
            LOG.debug("Failed to get database product name from the connection", e);
            return false;
        }
    }

    /**
     * Gets connection client info from the {@link ConnClientInfoAttr} annotation.
     *
     * @param connectionDefinition The connection definition
     * @return The connection client info or null if not configured to be used
     */
    private @Nullable Map<String, String> getConnectionClientInfo(@NonNull ConnectionDefinition connectionDefinition) {
        AnnotationMetadata annotationMetadata = connectionDefinition.getAnnotationMetadata();
        AnnotationValue<ConnClientInfo> annotation = annotationMetadata.getAnnotation(ConnClientInfo.class);
        if (annotation == null) {
            return null;
        }
        List<AnnotationValue<ConnClientInfoAttr>> clientInfoAttributes = annotation.getAnnotations(VALUE_MEMBER);
        Map<String, String> additionalClientInfoAttributes = new LinkedHashMap<>(clientInfoAttributes.size());
        for (AnnotationValue<ConnClientInfoAttr> clientInfoAttribute : clientInfoAttributes) {
            String name = clientInfoAttribute.getRequiredValue(NAME_MEMBER, String.class);
            String value = clientInfoAttribute.getRequiredValue(VALUE_MEMBER, String.class);
            additionalClientInfoAttributes.put(name, value);
        }
        if (StringUtils.isNotEmpty(applicationName)) {
            additionalClientInfoAttributes.putIfAbsent(ORACLE_CLIENT_ID, applicationName);
        }
        if (annotationMetadata instanceof MethodInvocationContext methodInvocationContext) {
            additionalClientInfoAttributes.putIfAbsent(ORACLE_MODULE,
                MODULE_CLASS_MAP.computeIfAbsent(methodInvocationContext.getTarget().getClass(),
                    clazz -> clazz.getName().replace(INTERCEPTED_SUFFIX, ""))
            );
            additionalClientInfoAttributes.putIfAbsent(ORACLE_ACTION, methodInvocationContext.getName());
        }
        return additionalClientInfoAttributes;
    }
}
