package frostlight.pso2kue;

import junit.framework.Test;
import junit.framework.TestSuite;

import frostlight.pso2kue.async.TestFetchCalendarTask;
import frostlight.pso2kue.async.TestFetchTwitterTask;

/**
 * AsyncTestSuite
 * Run all the tests that asynchronously fetch data
 * Created by Vincent on 6/1/2015.
 */

public class AsyncTestSuite extends TestSuite {
    public AsyncTestSuite() {
        super();
    }

    public static Test suite() {
        TestSuite testSuite = new TestSuite();
        testSuite.addTestSuite(TestFetchCalendarTask.class);
        testSuite.addTestSuite(TestFetchTwitterTask.class);
        return testSuite;
    }
}
        