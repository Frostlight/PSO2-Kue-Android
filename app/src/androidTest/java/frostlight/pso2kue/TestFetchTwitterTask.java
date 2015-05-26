package frostlight.pso2kue;

import android.content.res.Configuration;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;

/**
 * TestFetchTwitterTask
 * Created by Vincent on 5/20/2015.
 * Tests the AsyncTask FetchTwitterTask
 */
public class TestFetchTwitterTask extends InstrumentationTestCase {
    private static boolean called;

    protected void setUp() throws Exception {
        super.setUp();
        called = false;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testSuccessfulFetch() throws Throwable {
        // Create a signal to let us know when our task is done.
        final CountDownLatch signal = new CountDownLatch(1);

        // Execute the async task on the UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                new FetchTwitterTask() {
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Log.d(App.getTag(), "onPostExecute");

                        /* Normally we would use some type of listener to notify the activity
                         * that the async call was finished.
                         *
                         * In our test method we would subscribe to that and signal from
                         * there instead
                         */
                        called = true;
                        signal.countDown();
                    }
                }.execute(2);
            }
        });

	    /* The testing thread will wait here until the UI thread releases it
	     * above with the countDown() or 30 seconds passes and it times out.
	     */
        signal.await(10, TimeUnit.SECONDS);
        assertTrue(called);
    }
}