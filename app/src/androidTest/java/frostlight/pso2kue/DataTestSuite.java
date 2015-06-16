package frostlight.pso2kue;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * DataTestSuite
 * Run all the tests under the data package
 * Created by Vincent on 6/1/2015.
 */
public class DataTestSuite extends TestSuite {
    public DataTestSuite() {
        super();
    }

    public static Test suite() {
        TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder(DataTestSuite.class);
        testSuiteBuilder.includePackages("frostlight.pso2kue.data");
        return testSuiteBuilder.build();
    }
}