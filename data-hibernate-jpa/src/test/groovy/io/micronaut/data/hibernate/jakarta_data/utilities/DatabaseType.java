/*
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
package io.micronaut.data.hibernate.jakarta_data.utilities;

import java.util.Arrays;

/**
 * This enum represents the configured DatabaseType based on the {@link TestProperty} databaseType
 */
public enum DatabaseType {
    OTHER(Integer.MAX_VALUE), //No database type was configured
    RELATIONAL(100),
    GRAPH(50),
    DOCUMENT(40),
    TIME_SERIES(30),
    COLUMN(20),
    KEY_VALUE(10);

    private int flexibility;

    private DatabaseType(int flexibility) {
        this.flexibility = flexibility;
    }

    public static DatabaseType valueOfIgnoreCase(String value) {
        return Arrays.stream(DatabaseType.values()).filter(type -> type.name().equalsIgnoreCase(value)).findAny().orElse(DatabaseType.OTHER);
    }

    public boolean isKeywordSupportAtOrBelow(DatabaseType benchmark) {
        return this.flexibility <= benchmark.flexibility;
    }
}
