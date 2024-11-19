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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to set client info for the connection.
 *
 * @author radovanradic
 * @since 4.10
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Connectable
public @interface ConnectionClientInfo {

    /**
     * @return whether connection should set client info
     */
    boolean enabled() default true;

    /**
     * Returns an array of additional attributes that will be included in the connection client info.
     * These attributes can provide extra context about the connection and its usage.
     *
     * @return an array of ConnectionClientInfoAttribute instances
     */
    ConnectionClientInfoAttribute[] clientInfoAttributes() default {};
}
