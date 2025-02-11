/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package io.micronaut.data.hibernate.jakarta_data.read.only;

import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Save;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

//FIXME - Are user defined repository interfaces like this allowed via the Specification?
// Currently failing in test environment
//   java.lang.IllegalArgumentException: @Repository ee.jakarta.tck.data.framework.readonly.NaturalNumbers does not specify an entity class.
//   To correct this, have the repository interface extend DataRepository or another built-in repository interface and supply the entity class as the first parameter.
@Deprecated //Not currently in use
public interface ReadOnlyRepository<T, K> extends DataRepository<T, K>{

    // WRITE - default method
    // Necessary for pre-population
    @Save
    <S extends T> List<S> saveAll(List<S> entities);

    // READ - default methods
    Optional<T> findById(K id);

    boolean existsById(K id);

    Stream<T> findAll();

    Stream<T> findByIdIn(List<K> ids);

    long count();

}
