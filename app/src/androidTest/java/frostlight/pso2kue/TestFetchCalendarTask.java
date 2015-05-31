package frostlight.pso2kue;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import frostlight.pso2kue.data.DbContract;
import frostlight.pso2kue.data.DbHelper;

/**
 * TestFetchCalendarTask
 * Tests the AsyncTask FetchCalendarTask
 * Created by Vincent on 5/19/2015.
 */
public class TestFetchCalendarTask extends InstrumentationTestCase {

    private static boolean called;
    private DbHelper mDbHelper;
    private SQLiteDatabase mSQLiteDatabase;

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
                    // Setup DbHelper and Database
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mDbHelper = new DbHelper(getInstrumentation().getTargetContext());
                        mSQLiteDatabase = mDbHelper.getWritableDatabase();
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Log.d(Utility.getTag(), "onPostExecute");

                        // Query the database the AsyncTask inserted into for the entries
                        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM "
                                + DbContract.CalendarEntry.TABLE_NAME, null);
                        assertTrue("Error: The database has not been created correctly",
                                cursor.moveToFirst());

                        // Log the Calendar entries from the database
                        do {
                            Log.v(Utility.getTag(), cursor.getColumnName(1) + ": " + cursor.getString(1));
                            Log.v(Utility.getTag(), cursor.getColumnName(2) + ": " + Utility.formatDate(
                                    Long.parseLong(cursor.getString(2))));
                        } while (cursor.moveToNext());
                        cursor.close();

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
