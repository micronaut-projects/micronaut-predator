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

import java.util.UUID;

/**
 * This entity includes some field types that aren't covered elsewhere in the TCK.
 */
@jakarta.persistence.Entity
public class Coordinate {
    @jakarta.persistence.Id
    public UUID id;

    public double x;

    public float y;

    public static Coordinate of(String id, double x, float y) {
        Coordinate c = new Coordinate();
        c.id = UUID.nameUUIDFromBytes(id.getBytes());
        c.x = x;
        c.y = y;
        return c;
    }

    @Override
    public String toString() {
        return "Coordinate@" + Integer.toHexString(hashCode()) + "(" + x + "," + y + ")" + ":" + id;
    }
}
