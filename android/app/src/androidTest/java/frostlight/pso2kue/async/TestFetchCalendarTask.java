package frostlight.pso2kue.async;

import android.database.Cursor;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import frostlight.pso2kue.FetchCalendarTask;
import frostlight.pso2kue.Utility;
import frostlight.pso2kue.data.KueContract;

/**
 * TestFetchCalendarTask
 * Tests the AsyncTask FetchCalendarTask
 * Created by Vincent on 5/19/2015.
 */
public class TestFetchCalendarTask extends InstrumentationTestCase {

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
                // Execute FetchCalendarTask
                new FetchCalendarTask(getInstrumentation().getTargetContext()) {
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        Log.d(Utility.getTag(), "Debug: onPostExecute");

                        // Check if database is empty
                        // E.g. No future events/calendar has not been updated yet
                        Cursor cursor = getInstrumentation().getTargetContext().getContentResolver()
                                .query(
                                        KueContract.CalendarEntry.CONTENT_URI,
                                        new String[] {"count(*)"},
                                        null,
                                        null,
                                        null
                                );
                        assertTrue("Error: The database has not been created correctly",
                                cursor.moveToFirst());
                        int rowCount = cursor.getInt(0);
                        cursor.close();

                        if (rowCount > 0) {
                            // If CalendarTable is not empty, query the database the AsyncTask
                            // inserted into for the entries
                            cursor = getInstrumentation().getTargetContext().getContentResolver()
                                    .query(
                                            KueContract.CalendarEntry.CONTENT_URI,
                                            null,
                                            null,
                                            null,
                                            null
                                    );
                            assertTrue("Error: The database has not been created correctly",
                                    cursor.moveToFirst());

                            // Log the Calendar entries from the database
                            do {
                                Log.v(Utility.getTag(), cursor.getColumnName(1) + ": " + cursor.getString(1));
                                Log.v(Utility.getTag(), cursor.getColumnName(2) + ": " + TestUtilitiesAsync.formatDate(
                                        Long.parseLong(cursor.getString(2))));
                            } while (cursor.moveToNext());
                            cursor.close();
                        } else {
                            Log.d(Utility.getTag(), "Debug: CalendarTable is empty");
                        }

                        /* Normally we would use some type of listener to notify the activity
                         * that the async call was finished
                         *
                         * In our test method we would subscribe to that and signal from
                         * there instead
                         */
                        called = true;
                        signal.countDown();
                    }
                }.execute();
            }
        });

	    /* The testing thread will wait here until the UI thread releases it
         * above with the countDown() or 10 seconds passes and it times out
	     */
        signal.await(10, TimeUnit.SECONDS);
        assertTrue(called);
    }
}
