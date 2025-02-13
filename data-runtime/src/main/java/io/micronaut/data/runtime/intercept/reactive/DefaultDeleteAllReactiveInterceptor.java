/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.DeleteAllReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

import java.util.Optional;

/**
 * Default implementation of {@link DeleteAllReactiveInterceptor}.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public class DefaultDeleteAllReactiveInterceptor extends AbstractCountOrEntityPublisherInterceptor implements DeleteAllReactiveInterceptor<Object, Object> {
    /**
     * Default constructor.
     *
     * @param operations The operations
     */
    protected DefaultDeleteAllReactiveInterceptor(@NonNull RepositoryOperations operations) {
        super(operations);
    }

    @Override
    public Publisher<?> interceptPublisher(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
        Optional<Iterable<Object>> deleteEntities = findEntitiesParameter(context, Object.class);
        Optional<Object> deleteEntity = findEntityParameter(context, Object.class);
        if (deleteEntity.isEmpty() && deleteEntities.isEmpty()) {
            PreparedQuery<?, Number> preparedQuery = prepareQuery(methodKey, context);
            return reactiveOperations.executeDelete(preparedQuery);
        } else if (deleteEntity.isPresent()) {
            return reactiveOperations.delete(getDeleteOperation(context, deleteEntity.get()));
        }
        return reactiveOperations.deleteAll(getDeleteBatchOperation(context, deleteEntities.get()));
    }
}

