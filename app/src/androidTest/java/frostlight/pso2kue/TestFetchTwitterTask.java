package frostlight.pso2kue;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import frostlight.pso2kue.data.DbHelper;
import frostlight.pso2kue.data.KueContract;

/**
 * TestFetchTwitterTask
 * Created by Vincent on 5/20/2015.
 * Tests the AsyncTask FetchTwitterTask
 */
public class TestFetchTwitterTask extends InstrumentationTestCase {

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
                // Execute FetchTwitterTask for Ship 2
                new FetchTwitterTask(getInstrumentation().getTargetContext()) {
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
                                + KueContract.TwitterEntry.TABLE_NAME, null);
                        assertTrue("Error: The database has not been created correctly",
                                cursor.moveToFirst());

                        // Log the Twitter entry from the database
                        Log.v(Utility.getTag(), cursor.getColumnName(1) + ": " + cursor.getString(1));
                        Log.v(Utility.getTag(), cursor.getColumnName(2) + ": " + Utility.formatDate(
                                Long.parseLong(cursor.getString(2))));

                        assertFalse("Error: The database should have only one entry",
                                cursor.moveToNext());
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
                }.execute(2);
            }
        });

	    /* The testing thread will wait here until the UI thread releases it
         * above with the countDown() or 10 seconds passes and it times out
	     */
        signal.await(10, TimeUnit.SECONDS);
        assertTrue(called);
    }
}