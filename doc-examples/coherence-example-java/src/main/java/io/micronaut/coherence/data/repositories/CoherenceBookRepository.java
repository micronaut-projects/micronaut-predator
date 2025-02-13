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
package io.micronaut.coherence.data.repositories;

import com.tangosol.util.UUID;
import io.micronaut.coherence.data.AbstractCoherenceRepository;
import io.micronaut.coherence.data.annotation.CoherenceRepository;
import io.micronaut.coherence.data.model.Book;

import java.util.List;

/**
 * A {@code Repository} extending {@link AbstractCoherenceRepository} to ensure this integration point is functional.
 */
@CoherenceRepository("book")
public abstract class CoherenceBookRepository extends AbstractCoherenceRepository<Book, UUID> {
    public abstract List<Book> findByTitleStartingWith(String keyword);
}

