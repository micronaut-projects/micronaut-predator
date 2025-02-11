package io.micronaut.data.jakarta.tck;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectMethod;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

public class TestSuite {

    @Suite
    @SelectPackages("ee.jakarta.tck.data")
    @IncludeClassNamePatterns("ee.jakarta.tck.data.standalone.entity.*")
    public static class EntityTests {
    }

    @Suite
    @SelectPackages("ee.jakarta.tck.data")
    @IncludeClassNamePatterns("ee.jakarta.tck.data.standalone.persistence.*")
    public static class PersistenceTests {
    }

    @Suite
    @SelectMethod(type = ee.jakarta.tck.data.standalone.entity.EntityTests.class, name = "testFindFirst3")
    public static class SpecificTest {
    }

}
