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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AsciiCharactersPopulator implements Populator<AsciiCharacters> {

    public static AsciiCharactersPopulator get() {
        return new AsciiCharactersPopulator();
    }

    @Override
    public void populationLogic(AsciiCharacters repo) {
        List<AsciiCharacter> dictonary = new ArrayList<>();

        IntStream.range(1, 128) // Some databases don't support ASCII NULL character (0)
            .forEach(value -> {
                AsciiCharacter inst = new AsciiCharacter();

                inst.setId(value);
                inst.setNumericValue(value);
                inst.setHexadecimal(Integer.toHexString(value));
                inst.setThisCharacter((char) value);
                inst.setControl(Character.isISOControl((char) value));

                dictonary.add(inst);
            });

        repo.saveAll(dictonary);
    }

    @Override
    public boolean isPopulated(AsciiCharacters repo) {
        return repo.countByHexadecimalNotNull() == 127L;
    }
}
