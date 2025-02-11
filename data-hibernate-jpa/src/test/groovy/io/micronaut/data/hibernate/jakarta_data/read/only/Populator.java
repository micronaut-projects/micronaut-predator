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

import java.util.logging.Logger;

/**
 * Aids in the population of repositories with entities for read-only testing.
 *
 * @param <T> A repository
 */
public interface Populator<T> {
    // INTERFACE METHODS

    /**
     * The logic that adds one or more entities to this repository.
     *
     * @param repo - this repository
     */
    void populationLogic(T repo);

    /**
     * A logical test that can verify if a repository is already populated or not.
     * Typically, this is by verifying the count of entities saved in the repository.
     *
     * @param repo - this repository
     *
     * @return true if the repository is populated, false otherwise.
     */
    boolean isPopulated(T repo);

    //DEFAULT METHODS

    public static final Logger log = Logger.getLogger(Populator.class.getCanonicalName());

    /**
     * Short circuiting method to to populate a repository that is not already populated.
     * Uses the isPopulated() method to determine if a repository is populated or not.
     *
     * @param repo - this repository
     */
    public default void populate(T repo) {
        if(isPopulated(repo)) {
            return;
        }

        final String repoName = repo.getClass().getSimpleName();

        log.info(repoName + " populating");
        populationLogic(repo);

        log.info(repoName + " waiting for eventual consistency");

        log.info(repoName + " verifying");
        if(! isPopulated(repo)) {
            throw new RuntimeException("Repository " + repoName + " was not populated");
        }

        log.info(repoName + " populated");
    }

}
