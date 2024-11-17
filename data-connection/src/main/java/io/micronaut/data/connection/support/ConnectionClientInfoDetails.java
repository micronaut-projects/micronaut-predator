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

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.connection.annotation.ConnectionClientInfo;

import java.util.Map;

/**
 * The connection client info details that can be used to set to {@link java.sql.Connection#setClientInfo(String, String)}.
 * Currently used only for Oracle database connections.
 *
 * @param appName  The app name corresponding to the micronaut.application.name config value and can be null
 * @param module   The module (if not supplied in {@link ConnectionClientInfo#module()}
 *                 then by default the name of the class issuing database call)
 * @param action   The action (if not supplied in {@link ConnectionClientInfo#action()}
 *                 then by default the name of the method issuing database call)
 * @param connectionClientInfoAttributes The arbitrary connection client info attributes to be set to {@link java.sql.Connection#setClientInfo(String, String)}.
 */
@Experimental
public record ConnectionClientInfoDetails(@Nullable String appName, @NonNull String module, @NonNull String action,
                                          @NonNull Map<String, String> connectionClientInfoAttributes) {
}
