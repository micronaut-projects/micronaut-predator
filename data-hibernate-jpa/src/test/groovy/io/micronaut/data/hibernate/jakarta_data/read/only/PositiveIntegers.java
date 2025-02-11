/**
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
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

import io.micronaut.data.hibernate.jakarta_data.read.only.NaturalNumber.NumberType;
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is a read only repository that shares the same data (and entity type)
 * as the NaturalNumbers repository: the positive integers 1-100.
 * This repository is pre-populated at test startup and verified prior to running tests.
 */
@Repository
public interface PositiveIntegers extends BasicRepository<NaturalNumber, Long> {
    long countByIdLessThan(long number);

    boolean existsByIdGreaterThan(Long number);

    CursoredPage<NaturalNumber> findByFloorOfSquareRootNotAndIdLessThanOrderByNumBitsRequiredDesc(long excludeSqrt,
                                                                                                  long eclusiveMax,
                                                                                                  PageRequest pagination,
                                                                                                  Order<NaturalNumber> order);

    List<NaturalNumber> findByIsOddTrueAndIdLessThanEqualOrderByIdDesc(long max);

    List<NaturalNumber> findByIsOddFalseAndIdBetween(long min, long max);

    Stream<NaturalNumber> findByNumTypeInOrderByIdAsc(Set<NumberType> types, Limit limit);

    Stream<NaturalNumber> findByNumTypeOrFloorOfSquareRoot(NumberType type, long floor);

    @Find
    Page<NaturalNumber> findMatching(long floorOfSquareRoot, Short numBitsRequired, NumberType numType,
            PageRequest pagination, Sort<?>... sorts);

    @Find
    Optional<NaturalNumber> findNumber(long id);

    @Find
    List<NaturalNumber> findOdd(boolean isOdd, NumberType numType, Limit limit, Order<NaturalNumber> sorts);

    @Query("Select id Where isOdd = true and (id = :id or id < :exclusiveMax) Order by id Desc")
    List<Long> oddAndEqualToOrBelow(long id, long exclusiveMax);

    // Per the spec: The 'and' operator has higher precedence than 'or'.
    @Query("WHERE numBitsRequired = :bits OR numType = :type AND id < :xmax")
    CursoredPage<NaturalNumber> withBitCountOrOfTypeAndBelow(@Param("bits") short bitsRequired,
                                                             @Param("type") NumberType numberType,
                                                             @Param("xmax") long exclusiveMax,
                                                             Sort<NaturalNumber> sort1,
                                                             Sort<NaturalNumber> sort2,
                                                             PageRequest pageRequest);
}
