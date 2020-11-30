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
package io.micronaut.data.tck.repositories;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Version;
import io.micronaut.data.repository.reactive.RxJavaCrudRepository;
import io.micronaut.data.tck.entities.Student;
import io.reactivex.Completable;

public interface StudentReactiveRepository extends RxJavaCrudRepository<Student, Long> {

    Completable updateByIdAndVersion(@Id Long id, @Version Long version, @Parameter("name") String name);

    Completable updateById(@Id Long id, @Parameter("name") String name);

    Completable deleteByIdAndVersionAndName(@Id Long id, @Version Long version, String name);

    Completable deleteByIdAndVersion(@Id Long id, @Version Long version);

}
