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
package io.micronaut.data.hibernate.jakarta_data.entity;

import io.micronaut.core.annotation.Introspected;

@jakarta.persistence.Entity
@Introspected(accessKind = Introspected.AccessKind.FIELD)
public class Box {
    @jakarta.persistence.Id
    public String boxIdentifier;

    public int length;

    public int width;

    public int height;

    public static Box of(String id, int length, int width, int height) {
        Box box = new Box();
        box.boxIdentifier = id;
        box.length = length;
        box.width = width;
        box.height = height;
        return box;
    }

    @Override
    public String toString() {
        return "Box@" + Integer.toHexString(hashCode()) + ":" + length + "x" + width + "x" + height + ":" + boxIdentifier;
    }
}
