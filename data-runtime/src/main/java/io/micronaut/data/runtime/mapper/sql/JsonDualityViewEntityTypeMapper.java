/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.data.runtime.mapper.sql;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;

import java.util.function.BiFunction;

/**
 * The JSON duality view type mapper. Supported only for Oracle.
 *
 * @author radovanradic
 * @since 4.0.0.
 *
 * @param <T>  The entity type
 * @param <RS> The result set type
 * @param <R>  The result type
 */
public class JsonDualityViewEntityTypeMapper<T, RS, R> implements SqlTypeMapper<RS, R> {

    private final String columnName;
    private final RuntimePersistentEntity<T> entity;
    private final ResultReader<RS, String> resultReader;
    private final MediaTypeCodec jsonCodec;
    private final BiFunction<RuntimePersistentEntity<Object>, Object, Object> eventListener;

    public JsonDualityViewEntityTypeMapper(@NonNull String columnName, @NonNull RuntimePersistentEntity<T> entity, @NonNull ResultReader<RS, String> resultReader, @NonNull MediaTypeCodec jsonCodec,
                                           @Nullable BiFunction<RuntimePersistentEntity<Object>, Object, Object> eventListener) {
        this.columnName = columnName;
        this.entity = entity;
        this.resultReader = resultReader;
        new ArgumentUtils.ArgumentCheck<>(() -> jsonCodec.getMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE));
        this.jsonCodec = jsonCodec;
        this.eventListener = eventListener;
    }


    @Override
    public R map(RS object, Class<R> type) throws DataAccessException {
        String jsonViewData = resultReader.readString(object, columnName);
        R entityInstance = jsonCodec.decode(type, jsonViewData);
        if (entityInstance == null) {
            throw new DataAccessException("Unable to map result to entity of type [" + type.getName() + "]. Missing result data.");
        }
        return triggerPostLoad(entity, entityInstance);
    }

    @Override
    public Object read(RS object, String name) {
        throw new UnsupportedOperationException("Custom field read is not supported");
    }

    @Override
    public boolean hasNext(RS resultSet) {
        return resultReader.next(resultSet);
    }

    private <K> K triggerPostLoad(RuntimePersistentEntity<?> persistentEntity, K entity) {
        K finalEntity;
        if (eventListener != null && persistentEntity.hasPostLoadEventListeners()) {
            finalEntity = (K) eventListener.apply((RuntimePersistentEntity<Object>) persistentEntity, entity);
        } else {
            finalEntity = entity;
        }
        return finalEntity;
    }
}
