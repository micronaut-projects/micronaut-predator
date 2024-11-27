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
package io.micronaut.data.jdbc.connection;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import io.micronaut.data.jdbc.config.DataJdbcConfiguration;
import io.micronaut.data.jdbc.connection.annotation.ClientInfo;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.runtime.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A customizer for Oracle database connections that sets client information before and after issuing JDBC call for given connection.
 *
 * This customizer checks if the connection is an Oracle database connection and then sets the client information
 * (client ID, module, and action) before issuing JDBC call for the connection. It also clears these properties after the JDBC call for given connection.
 *
 * @author radovanradic
 * @since 4.11
 */
@EachBean(DataSource.class)
//@Requires(condition = OracleClientInfoCondition.class)
final class OracleClientInfoConnectionCustomizer implements ConnectionCustomizer {

    private static final String NAME_MEMBER = "name";
    private static final String VALUE_MEMBER = "value";
    private static final String INTERCEPTED_SUFFIX = "$Intercepted";
    private static final String ORACLE_CLIENT_INFO_ENABLED = "enable-oracle-client-info";

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

    private static final Logger LOG = LoggerFactory.getLogger(OracleClientInfoConnectionCustomizer.class);

    private static final Map<Class<?>, String> MODULE_CLASS_MAP = new ConcurrentHashMap<>(100);

    @Nullable
    private final String applicationName;
    private final String dataSourceName;

    private final boolean enabled;

    OracleClientInfoConnectionCustomizer(@NonNull DataSource dataSource,
                                         @Parameter DataJdbcConfiguration jdbcConfiguration,
                                         ApplicationContext applicationContext,
                                         @Nullable ApplicationConfiguration applicationConfiguration) {
        this.applicationName = applicationConfiguration != null ? applicationConfiguration.getName().orElse(null) : null;
        this.enabled = isEnabled(jdbcConfiguration, applicationContext, dataSource);
        this.dataSourceName = jdbcConfiguration.getName();
    }

    private boolean isEnabled(DataJdbcConfiguration dataJdbcConfiguration, ApplicationContext applicationContext, DataSource dataSource) {
        if (dataJdbcConfiguration.getDialect() != Dialect.ORACLE) {
            return false;
        }
        String property = "datasources." + dataJdbcConfiguration.getName() + "." + ORACLE_CLIENT_INFO_ENABLED;
        if (!applicationContext.getProperty(property, Boolean.class).orElse(false)) {
            return false;
        }
        boolean customizerEnabled;
        try {
            Connection connection = DelegatingDataSource.unwrapDataSource(dataSource).getConnection();
            customizerEnabled = isOracleConnection(connection);
        } catch (SQLException e) {
            LOG.error("Failed to get connection for oracle connection customizer", e);
            customizerEnabled = false;
        }
        return customizerEnabled;
    }

    @Override
    public void beforeCall(@NonNull Connection connection, @NonNull MethodInfo methodInfo) {
        // Set client info for the connection if Oracle connection before JDBC call
        Map<String, String> connectionClientInfo = getConnectionClientInfo(methodInfo);
        if (CollectionUtils.isNotEmpty(connectionClientInfo)) {
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
    public void afterCall(@NonNull Connection connection) {
        // Clear client info for connection if it was Oracle connection and client info was set previously
        Properties properties = null;
        try {
            properties = connection.getClientInfo();
        } catch (SQLException e) {
            LOG.debug("Failed to get connection client info", e);
        }
        if (properties != null && !properties.isEmpty()) {
            try {
                for (String key : properties.stringPropertyNames()) {
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

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getDataSourceName() {
        return dataSourceName;
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
     * Gets connection client info from the {@link ClientInfo} annotation.
     *
     * @param methodInfo The method info
     * @return The connection client info or null if not configured to be used
     */
    private @NonNull Map<String, String> getConnectionClientInfo(@NonNull MethodInfo methodInfo) {
        AnnotationMetadata annotationMetadata = methodInfo.annotationMetadata();
        AnnotationValue<ClientInfo> annotation = annotationMetadata.getAnnotation(ClientInfo.class);
        List<AnnotationValue<ClientInfo.Attribute>> clientInfoValues = annotation != null ? annotation.getAnnotations(VALUE_MEMBER) : Collections.emptyList();
        Map<String, String> clientInfoAttributes = new LinkedHashMap<>(clientInfoValues.size());
        if (CollectionUtils.isNotEmpty(clientInfoValues)) {
            for (AnnotationValue<ClientInfo.Attribute> clientInfoValue : clientInfoValues) {
                String name = clientInfoValue.getRequiredValue(NAME_MEMBER, String.class);
                String value = clientInfoValue.getRequiredValue(VALUE_MEMBER, String.class);
                clientInfoAttributes.put(name, value);
            }
        }
        // Fallback defaults if not provided in the annotation
        if (StringUtils.isNotEmpty(applicationName)) {
            clientInfoAttributes.putIfAbsent(ORACLE_CLIENT_ID, applicationName);
        }
        clientInfoAttributes.putIfAbsent(ORACLE_MODULE,
            MODULE_CLASS_MAP.computeIfAbsent(methodInfo.clazz(),
                clazz -> clazz.getName().replace(INTERCEPTED_SUFFIX, ""))
        );
        clientInfoAttributes.putIfAbsent(ORACLE_ACTION, methodInfo.methodName());
        return clientInfoAttributes;
    }
}
