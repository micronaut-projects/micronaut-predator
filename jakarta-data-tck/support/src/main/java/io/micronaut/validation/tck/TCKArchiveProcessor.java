/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
package io.micronaut.validation.tck;

import ee.jakarta.tck.data.framework.junit.anno.Platform;
import ee.jakarta.tck.data.framework.junit.anno.ReadOnlyTest;
import ee.jakarta.tck.data.framework.junit.anno.Signature;
import ee.jakarta.tck.data.framework.junit.anno.Web;
import ee.jakarta.tck.data.framework.read.only.Populator;
import ee.jakarta.tck.data.framework.servlet.TestServlet;
import ee.jakarta.tck.data.framework.signature.DataSignatureTestRunner;
import ee.jakarta.tck.data.standalone.entity.EntityTests;
import ee.jakarta.tck.data.standalone.entity.MultipleEntityRepo;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;


public class TCKArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(TCKArchiveProcessor.class.getCanonicalName());

    private static final Package servletPackage = TestServlet.class.getPackage();
    private static final Package readOnlyPackage = Populator.class.getPackage();

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof ClassContainer) {
            if(testClass.getName().equals(EntityTests.class.getName())) {
                ((ClassContainer<?>) applicationArchive).addClass(MultipleEntityRepo.class);
            }
        }
    }
}
