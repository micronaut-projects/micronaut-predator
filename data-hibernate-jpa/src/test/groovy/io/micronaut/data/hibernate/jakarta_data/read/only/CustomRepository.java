/**
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

import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.Set;

/**
 * Do not add methods or inheritance to this interface.
 * Its purpose is to test that without inheriting from a built-in repository,
 * the lifecycle methods with the same entity class are what identifies the
 * primary entity class to use for the count and exist methods.
 */
@Repository
public interface CustomRepository {

    @Insert
    void add(List<NaturalNumber> list);

    long countByIdIn(Set<Long> ids);

    boolean existsByIdIn(Set<Long> ids);

    @Delete
    void remove(List<NaturalNumber> list);
}
