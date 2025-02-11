/**
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package io.micronaut.data.hibernate.jakarta_data.entity;

import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * A repository that performs operations on different types of entities.
 */
@Repository
public interface MultipleEntityRepo { // Do not add a primary entity type.

    // Methods for Box entity:

    @Insert
    Box[] addAll(Box... boxes);

    @Query("DELETE FROM Box")
    long removeAll();

    @Query("UPDATE Box SET length = length + ?1, width = width - ?1, height = height * ?2")
    long resizeAll(int lengthIncrementWidthDecrement, int heightFactor);

    // Methods for Coordinate entity:

    @Insert
    Coordinate create(Coordinate c);

    @Query("DELETE FROM Coordinate WHERE test > 0.0d AND y > 0.0f")
    long deleteIfPositive();

    @Query("DELETE FROM Coordinate WHERE test > 0.0d AND y > 0.0f")
    void deleteIfPositiveWithoutReturnRecords();

    @Query("UPDATE Coordinate SET test = :newX, y = y / :yDivisor WHERE id = :id")
    boolean move(UUID id, double newX, float yDivisor);

    @Query("WHERE id = ?1")
    Optional<Coordinate> withUUID(UUID id);
}
