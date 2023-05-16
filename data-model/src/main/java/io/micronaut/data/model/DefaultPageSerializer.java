/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.data.model;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serializer;

import java.io.IOException;

/**
 * Custom serializer for {@link DefaultPage} as a workaround for https://github.com/micronaut-projects/micronaut-serialization/issues/307.
 *
 * @author Denis Stepanov
 * @since 4.0.0
 */
@Prototype
@Internal
final class DefaultPageSerializer implements Serializer<DefaultPage<Object>> {

    @Override
    public void serialize(Encoder encoder, EncoderContext context, Argument<? extends DefaultPage<Object>> type, DefaultPage<Object> page) throws IOException {
        PageSerializer.serializePage(encoder, context, type, page);
    }
}
