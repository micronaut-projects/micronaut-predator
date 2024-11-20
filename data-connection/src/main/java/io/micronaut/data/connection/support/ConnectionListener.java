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
package io.micronaut.data.connection.support;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.core.order.Ordered;
import io.micronaut.data.connection.ConnectionStatus;

/**
 * Handles connection after open or before close events based on the provided {@link ConnectionStatus}.
 *
 * Implementations of this interface can modify the behavior of connections created by Micronaut Data
 * or do what might be needed after connection open or before close.
 *
 * @see ConnectionStatus
 * @param <C> The connection type
 *
 * @author radovanradic
 * @since 4.11
 */
public interface ConnectionListener<C> extends Named, Ordered {

    /**
     * Called after a connection is opened.
     *
     * This method allows implementations to perform additional setup or configuration on the connection.
     *
     * @param connectionStatus            The newly opened connection
     */
    void afterOpen(@NonNull ConnectionStatus<C> connectionStatus);

    /**
     * Called before a connection is closed.
     *
     * This method allows implementations to release any resources or perform cleanup tasks related to the connection.
     *
     * @param connectionStatus            The connection statucs about to be closed
     */
    void beforeClose(@NonNull ConnectionStatus<C> connectionStatus);

    /**
     * Returns the name of this listener. Used for logging purposes. By default, returns class simple name.
     *
     * @return the name of this listener
     */
    @Override
    @NonNull
    default String getName() {
        return getClass().getSimpleName();
    }
}
