package frostlight.pso2kue.data;

/**
 * TestProvider
 * Created by Vincent on 6/8/2015.
 */

import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import frostlight.pso2kue.Utility;

public class TestProvider extends AndroidTestCase {

    /**
     * Inserts a set of ContentValues into a database and queries for the same values
     *
     * @param sqLiteDatabase The database to insert into
     * @param tableName      The name of the table to insert to
     * @param testValues     ContentValues consisting of the entry for insertion
     * @param contentUri     Used for querying and checking if the notification Uri was set properly
     * @return ID of the row the entry was inserted to
     */
    long insertQueryProvider(SQLiteDatabase sqLiteDatabase, String tableName,
                                    ContentValues testValues, Uri contentUri) {
        // Insert ContentValues into database and get a row ID back
        long locationRowId = sqLiteDatabase.insert(tableName, null, testValues);

        // Verify insertion was successful
        assertTrue("Error: Insertion into table " + tableName + " was unsuccessful",
                locationRowId != -1);

        // Query and verify query for the values that were just inserted
        verifyValuesProvider(mContext, tableName, testValues, contentUri);

        return locationRowId;
    }

    /**
     * Verifies that an entry (according to a ContentValues) exists in the table
     * This test queries the ContentProvider instead of the database itself
     *
     * @param context        Context of the
     * @param tableName      The name of the table to check
     * @param expectedValues ContentValues consisting of what to look for
     * @param contentUri     Used for querying and checking if the notification Uri was set properly
     */
    static void verifyValuesProvider(Context context, String tableName,
                                     ContentValues expectedValues, Uri contentUri) {
        String whereClause = "";

        // Iterate through each ContentValue key-value pair to generate the WHERE clause
        // of the SQL query
        for (String key : expectedValues.keySet()) {
            Object value = expectedValues.get(key);

            if (!whereClause.isEmpty())
                whereClause += " AND ";
            whereClause += key + " = \"" + value.toString() + "\"";
        }

        // Query the content resolver
        Cursor cursor = context.getContentResolver().query(
                contentUri,
                null,
                whereClause,
                null,
                null
        );

        // Check if the NotificationUri has been set correctly (API level 19 minimum)
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    cursor.getNotificationUri(), contentUri);
        }

        // Cursor should not be empty
        assertFalse("Empty cursor returned for query on " + tableName, TestUtilities.isCursorEmpty(cursor));
        cursor.close();
    }

    /**
     * Helper function that deletes all records from all database tables using the ContentProvider
     * It also queries the ContentProvider to make sure that the database has been successfully
     * deleted
     */
    public void deleteAllRecords() {
        // Delete all records from database tables
        mContext.getContentResolver().delete(
                KueContract.CalendarEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                KueContract.TwitterEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                KueContract.TranslationEntry.CONTENT_URI,
                null,
                null
        );

        // Query each table to verify the database records have been deleted properly
        Cursor cursor = mContext.getContentResolver().query(
                KueContract.CalendarEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from calendar table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                KueContract.TwitterEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Twitter table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                KueContract.TranslationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from translation table during delete", 0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords in setUp
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    // Verify that the content provider has been registered correctly
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // Define the component name based on the package name from the context and the KueProvider class
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                KueProvider.class.getName());

        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: KueProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + KueContract.CONTENT_AUTHORITY,
                    providerInfo.authority, KueContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: KueProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    // Verify that the ContentProvider returns the correct type for each URI it can handle
    public void testGetType() {
        // content://frostlight.pso2kue/calendar
        String type = mContext.getContentResolver().getType(KueContract.CalendarEntry.CONTENT_URI);
        // vnd.android.cursor.dir/frostlight.pso2kue/calendar
        assertEquals("Error: the CalendarEntry CONTENT_URI should return Calendar.CONTENT_TYPE",
                KueContract.CalendarEntry.CONTENT_TYPE, type);

        // content://frostlight.pso2kue/twitter
        type = mContext.getContentResolver().getType(KueContract.TwitterEntry.CONTENT_URI);
        // vnd.android.cursor.dir/frostlight.pso2kue/calendar
        assertEquals("Error: the CalendarEntry CONTENT_URI should return Calendar.CONTENT_TYPE",
                KueContract.TwitterEntry.CONTENT_TYPE, type);

        // content://frostlight.pso2kue/translation
        type = mContext.getContentResolver().getType(KueContract.TranslationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/frostlight.pso2kue/calendar
        assertEquals("Error: the CalendarEntry CONTENT_URI should return Calendar.CONTENT_TYPE",
                KueContract.TranslationEntry.CONTENT_TYPE, type);
    }

    // Insert and query each database
    // Testing is handled by the database insertion function
    public void testInsert() {
        DbHelper dbHelper = new DbHelper(this.getContext());
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        // Insert and query calendar database
        insertQueryProvider(sqLiteDatabase, KueContract.CalendarEntry.TABLE_NAME,
                TestUtilities.createCalendarValues(), KueContract.CalendarEntry.CONTENT_URI);

        // Insert and query twitter database
        insertQueryProvider(sqLiteDatabase, KueContract.TwitterEntry.TABLE_NAME,
                TestUtilities.createTwitterValues(), KueContract.TwitterEntry.CONTENT_URI);

        // Insert and query translation database
        insertQueryProvider(sqLiteDatabase, KueContract.TranslationEntry.TABLE_NAME,
                TestUtilities.createTranslationValues(), KueContract.TranslationEntry.CONTENT_URI);
    }
}
