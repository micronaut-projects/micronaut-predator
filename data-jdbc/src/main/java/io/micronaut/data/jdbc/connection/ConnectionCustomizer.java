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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.core.order.Ordered;

import java.sql.Connection;

/**
 * Handles before and after JDBC call for given connection {@link Connection}.
 *
 * Implementations of this interface can modify the behavior of connections created by Micronaut Data
 * or do what might be needed before or after JDBC call for given connection.
 *
 * @author radovanradic
 * @since 4.11
 */
public interface ConnectionCustomizer extends Named, Ordered {

    /**
     * Called before JDBC call is issued for given connection.
     *
     * This method allows implementations to perform additional setup or configuration on the connection.
     *
     * @param connection The JDBC connection
     * @param methodInfo The method info
     */
    void beforeCall(@NonNull Connection connection, @NonNull MethodInfo methodInfo);

    /**
     * Called after JDBC call for given connection has been issued.
     *
     * This method allows implementations to release any resources or perform cleanup tasks related to the connection.
     *
     * @param connection The JDBC connection
     */
    void afterCall(@NonNull Connection connection);

    /**
     * Returns the name of this listener. Used for logging purposes. By default, returns class simple name.
     *
     * @return the name of this customizer
     */
    @Override
    @NonNull
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Indicates whether this customizer is enabled.
     *
     * By default, all customizers are enabled. Subclasses may override this method to provide dynamic enabling/disabling logic.
     *
     * @return true if this customizer is enabled, false otherwise
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Returns the name of the data source associated with this customizer.
     *
     * This method provides access to the name of the data source that this customizer is configured for.
     *
     * @return the name of the data source
     */
    String getDataSourceName();
}
