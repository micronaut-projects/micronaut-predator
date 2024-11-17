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
package io.micronaut.data.connection.jdbc.operations;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.connection.ConnectionDefinition;
import io.micronaut.data.connection.support.ConnectionClientInfoDetails;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A customizer for Oracle database connections that sets and clears client information.
 *
 * This customizer checks if the connection is an Oracle database connection and then sets the client information
 * (client ID, module, and action) after opening the connection. It also clears these properties before closing the connection.
 *
 * @since 4.10
 */
@Singleton
@Requires(property = ConnectionCustomizer.PREFIX + ".oracleclientinfo.enabled", value = "true", defaultValue = "false")
final class OracleConnectionClientInfoCustomizer implements ConnectionCustomizer {

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
    private static final List<String> RESERVED_CLIENT_INFO_NAMES = List.of(ORACLE_CLIENT_ID, ORACLE_MODULE, ORACLE_ACTION);

    private static final Logger LOG = LoggerFactory.getLogger(OracleConnectionClientInfoCustomizer.class);

    private final Map<Connection, Boolean> connectionSupportedMap = new ConcurrentHashMap<>(20);

    @Override
    public boolean supportsConnection(Connection connection, ConnectionDefinition connectionDefinition) {
        return connectionSupportedMap.computeIfAbsent(connection, this::isOracleConnection);
    }

    @Override
    public void afterOpen(Connection connection, ConnectionDefinition connectionDefinition) {
        // Set client info for connection if Oracle connection after connection is opened
        ConnectionClientInfoDetails connectionClientInfo = connectionDefinition.connectionClientInfo();
        if (connectionClientInfo != null) {
            LOG.trace("Setting connection tracing info to the Oracle connection");
            try {
                if (connectionClientInfo.appName() != null) {
                    connection.setClientInfo(ORACLE_CLIENT_ID, connectionClientInfo.appName());
                }
                connection.setClientInfo(ORACLE_MODULE, connectionClientInfo.module());
                connection.setClientInfo(ORACLE_ACTION, connectionClientInfo.action());
                for (Map.Entry<String, String> additionalInfo : connectionClientInfo.connectionClientInfoAttributes().entrySet()) {
                    String name = additionalInfo.getKey();
                    if (RESERVED_CLIENT_INFO_NAMES.contains(name)) {
                        LOG.debug("Connection client info attribute {} already set. Skip setting value from the arbitrary attributes.", name);
                    } else {
                        String value = additionalInfo.getValue();
                        connection.setClientInfo(name, value);
                    }
                }
            } catch (SQLClientInfoException e) {
                LOG.debug("Failed to set connection tracing info", e);
            }
        }
    }

    @Override
    public void beforeClose(Connection connection, ConnectionDefinition connectionDefinition) {
        // Clear client info for connection if it was Oracle connection and client info was set previously
        ConnectionClientInfoDetails connectionClientInfo = connectionDefinition.connectionClientInfo();
        if (connectionClientInfo != null) {
            try {
                connection.setClientInfo(ORACLE_CLIENT_ID, null);
                connection.setClientInfo(ORACLE_MODULE, null);
                connection.setClientInfo(ORACLE_ACTION, null);
                for (Map.Entry<String, String> additionalInfo : connectionClientInfo.connectionClientInfoAttributes().entrySet()) {
                    String name = additionalInfo.getKey();
                    if (!RESERVED_CLIENT_INFO_NAMES.contains(name)) {
                        connection.setClientInfo(name, null);
                    }
                }
            } catch (SQLClientInfoException e) {
                LOG.debug("Failed to clear connection tracing info", e);
            }
        }
    }

    @Override
    public String getName() {
        return "Oracle Connection Customizer";
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
}
