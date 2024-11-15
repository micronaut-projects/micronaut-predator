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

import io.micronaut.core.order.Ordered;
import io.micronaut.data.connection.ConnectionDefinition;

import java.sql.Connection;

/**
 * Customizes connections based on the provided {@link ConnectionDefinition}.
 *
 * Implementations of this interface can modify the behavior of connections created by Micronaut Data JDBC.
 *
 * @see ConnectionDefinition
 */
public interface ConnectionCustomizer extends Ordered {

    /**
     * Checks whether this customizer supports the given connection and connection definition.
     *
     * @param connection            the SQL connection to be customized
     * @param connectionDefinition the connection definition used to create the connection
     * @return true if this customizer supports the given connection and connection definition, false otherwise
     */
    boolean supportsConnection(Connection connection, ConnectionDefinition connectionDefinition);

    /**
     * Called after a connection is opened.
     *
     * This method allows implementations to perform additional setup or configuration on the connection.
     *
     * @param connection            the newly opened SQL connection
     * @param connectionDefinition the connection definition used to create the connection
     */
    void afterOpen(Connection connection, ConnectionDefinition connectionDefinition);

    /**
     * Called before a connection is closed.
     *
     * This method allows implementations to release any resources or perform cleanup tasks related to the connection.
     *
     * @param connection            the SQL connection about to be closed
     * @param connectionDefinition the connection definition used to create the connection
     */
    void beforeClose(Connection connection, ConnectionDefinition connectionDefinition);

    /**
     * Returns the name of this customizer. Used for logging purposes.
     *
     * @return the name of this customizer
     */
    String getName();
}
