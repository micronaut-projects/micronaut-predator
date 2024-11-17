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
package io.micronaut.data.connection.annotation;


import io.micronaut.data.connection.ConnectionDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to set client info for the connection.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Connectable
public @interface ConnectionClientInfo {

    /**
     * If this flag is not disabled then when connection is established {@link io.micronaut.data.connection.support.ConnectionClientInfoDetails}
     * will be populated in {@link ConnectionDefinition#connectionClientInfo()} using values from this annotation
     * or calculate default module and action from the class name and method name issuing the call.
     * Then this information can be used for example to {@link java.sql.Connection#setClientInfo(String, String)}.
     *
     * @return whether connection should set client info
     */
    boolean enabled() default true;

    /**
     * The module name for connection client info if {@link #enabled()} is set to true.
     * If not provided, then it will fall back to the name of the class currently being intercepted in {@link io.micronaut.data.connection.interceptor.ConnectableInterceptor}.
     *
     * @return the custom module name for connection client info
     */
    String module() default "";

    /**
     * The action name for connection client info if {@link #enabled()} is set to true.
     * If not provided, then it will fall back to the name of the method currently being intercepted in {@link io.micronaut.data.connection.interceptor.ConnectableInterceptor}.
     *
     * @return the custom action name for tracing
     */
    String action() default "";

    /**
     * Returns an array of additional attributes that will be included in the connection client info.
     * These attributes can provide extra context about the connection and its usage.
     *
     * @return an array of ConnectionClientInfoAttribute instances
     */
    ConnectionClientInfoAttribute[] clientInfoAttributes() default {};
}
