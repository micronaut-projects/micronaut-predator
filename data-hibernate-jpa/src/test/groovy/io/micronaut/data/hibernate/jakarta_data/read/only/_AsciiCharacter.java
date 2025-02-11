/*
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

import jakarta.annotation.Generated;
import jakarta.data.Sort;
import jakarta.data.metamodel.SortableAttribute;
import jakarta.data.metamodel.StaticMetamodel;
import jakarta.data.metamodel.TextAttribute;

/**
 * This static metamodel class represents what an annotation processor-based approach
 * might generate.
 */
@Generated("ee.jakarta.tck.data.mock.generator")
@StaticMetamodel(AsciiCharacter.class)
public class _AsciiCharacter {
    public static final String ID = "id";
    public static final String HEXADECIMAL = "hexadecimal";
    public static final String NUMERICVALUE = "numericValue";

    public static final SortableAttribute<AsciiCharacter> id = new NumericAttr("id");
    public static final TextAttribute<AsciiCharacter> hexadecimal = new TextAttr("hexadecimal");
    public static final SortableAttribute<AsciiCharacter> isControl = new BooleanAttr("isControl");
    public static final SortableAttribute<AsciiCharacter> numericValue = new NumericAttr("numericValue");
    public static final TextAttribute<AsciiCharacter> thisCharacter = new TextAttr("thisCharacter");

    private static record BooleanAttr(String name, Sort<AsciiCharacter> asc, Sort<AsciiCharacter> desc)
            implements SortableAttribute<AsciiCharacter> {
        private BooleanAttr(String name) {
            this(name, Sort.asc(name), Sort.desc(name));
        }
    };

    private static record NumericAttr(String name, Sort<AsciiCharacter> asc, Sort<AsciiCharacter> desc)
            implements SortableAttribute<AsciiCharacter> {
        private NumericAttr(String name) {
            this(name, Sort.asc(name), Sort.desc(name));
        }
    };

    private static record TextAttr(String name, Sort<AsciiCharacter> asc, Sort<AsciiCharacter> ascIgnoreCase,
            Sort<AsciiCharacter> desc, Sort<AsciiCharacter> descIgnoreCase) implements TextAttribute<AsciiCharacter> {
        private TextAttr(String name) {
            this(name, Sort.asc(name), Sort.ascIgnoreCase(name), Sort.desc(name), Sort.descIgnoreCase(name));
        }
    };

    // Avoids the checkstyle error,
    // HideUtilityClassConstructor: Utility classes should not have a public or default constructor
    private _AsciiCharacter() {
    }
}
