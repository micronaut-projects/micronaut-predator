package x;

import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;
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
//    @SelectPackages("ee.jakarta.tck.data")
//    @IncludeClassNamePatterns("ee.jakarta.tck.data.standalone.persistence.*")
    @SelectMethod(type = PersistenceEntityTests.class, name = "testMultipleInsertUpdateDelete")
    public static class PersistenceTests {
    }

}
