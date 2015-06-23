package frostlight.pso2kue.data;

/**
 * TestProvider
 * Created by Vincent on 6/8/2015.
 */

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

public class TestProvider extends AndroidTestCase {

    /**
     * Inserts a set of ContentValues into a database and queries for the same values
     * [Inserts into sqLiteDatabase, queries using content provider]
     *
     * @param context        Context containing the content provider
     * @param sqLiteDatabase The database to insert into
     * @param tableName      The name of the table to insert to
     * @param testValues     ContentValues consisting of the entry for insertion
     * @param contentUri     Used for querying and checking if the notification Uri was set properly
     * @return ID of the row the entry was inserted to
     */
    private long insertAndVerify(Context context, SQLiteDatabase sqLiteDatabase, String tableName,
                         ContentValues testValues, Uri contentUri) {
        // Insert ContentValues into database and get a row ID back
        long locationRowId = sqLiteDatabase.insert(tableName, null, testValues);

        // Verify insertion was successful
        assertTrue("Error: Insertion into " + contentUri.toString() + " was unsuccessful",
                locationRowId != -1);

        // Query and verify query for the values that were just inserted
        verifyValues(context, testValues, contentUri);

        return locationRowId;
    }

    /**
     * Inserts a set of ContentValues into a database and queries for the same values
     * [Inserts and queries using content provider]
     *
     * @param context        Context containing the content provider
     * @param testValues     ContentValues consisting of the entry for insertion
     * @param contentUri     Used for querying and checking if the notification Uri was set properly
     * @return ID of the row the entry was inserted to
     */
    private long insertAndVerify(Context context, Uri contentUri, ContentValues testValues) {
        // Insert ContentValues into database and get a row ID back
        Uri uri = context.getContentResolver().insert(contentUri, testValues);
        long locationRowId = ContentUris.parseId(uri);

        // Verify insertion was successful
        assertTrue("Error: Insertion into " + contentUri.toString() + " was unsuccessful",
                locationRowId != -1);

        // Query and verify query for the values that were just inserted
        verifyValues(context, testValues, contentUri);

        return locationRowId;
    }

    /**
     * Verifies that an entry (according to a ContentValues) exists in the table
     * This test queries the ContentProvider instead of the database itself
     *
     * @param context        Context of the
     * @param expectedValues ContentValues consisting of what to look for
     * @param contentUri     Used for querying and checking if the notification Uri was set properly
     */
    private static void verifyValues(Context context, ContentValues expectedValues, Uri contentUri) {
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
        assertFalse("Empty cursor returned for query on " + contentUri.toString(),
                TestUtilitiesData.isCursorEmpty(cursor));
        cursor.close();
    }

    /**
     * Helper function that deletes all records from all database tables using the ContentProvider
     * It also queries the ContentProvider to make sure that the database has been successfully
     * deleted
     */
    private void deleteAllRecords() {
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
        insertAndVerify(mContext, sqLiteDatabase, KueContract.CalendarEntry.TABLE_NAME,
                TestUtilitiesData.createCalendarValues(), KueContract.CalendarEntry.CONTENT_URI);

        // Insert and query twitter database
        insertAndVerify(mContext, sqLiteDatabase, KueContract.TwitterEntry.TABLE_NAME,
                TestUtilitiesData.createTwitterValues(), KueContract.TwitterEntry.CONTENT_URI);

        // Insert and query translation database
        insertAndVerify(mContext, sqLiteDatabase, KueContract.TranslationEntry.TABLE_NAME,
                TestUtilitiesData.createTranslationValues(), KueContract.TranslationEntry.CONTENT_URI);
    }

    // Insert and update each column in the calendar table, and tests the content resolver by
    // registering a content observer with a cursor
    public void testInsertUpdate_CursorNotify() {
        DbHelper dbHelper = new DbHelper(this.getContext());
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        ContentValues contentValues = TestUtilitiesData.createCalendarValues();

        // Insert and verify calendar database
        long locationRowId = insertAndVerify(mContext, sqLiteDatabase,
                KueContract.CalendarEntry.TABLE_NAME, contentValues, KueContract.CalendarEntry.CONTENT_URI);

        // Create new ContentValues to update with
        ContentValues updatedValues = new ContentValues(contentValues);
        updatedValues.put(KueContract.CalendarEntry._ID, locationRowId);
        updatedValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, "Annihilator's Apparition");

        // Create and register the content observer with a cursor
        Cursor calendarCursor = mContext.getContentResolver().query(
                KueContract.CalendarEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        TestUtilitiesData.TestContentObserver testContentObserver = TestUtilitiesData.getTestContentObserver();
        calendarCursor.registerContentObserver(testContentObserver);

        // Perform the update and make sure it was successful
        int updateCount = mContext.getContentResolver().update(
                KueContract.CalendarEntry.CONTENT_URI, updatedValues, KueContract.CalendarEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(updateCount, 1);

        // Test to make sure the observer is called by waiting for a notification
        // from the content observer (caused by the update action)
        testContentObserver.waitForNotificationOrFail();

        // Unregister observer and close the cursor
        calendarCursor.unregisterContentObserver(testContentObserver);
        calendarCursor.close();

        // Verify that the ContentProvider was updated correctly (with the new ContentValues)
        verifyValues(mContext, updatedValues, KueContract.CalendarEntry.CONTENT_URI);
    }

    // Insert and update each column in the calendar table, and tests the content resolver by
    // registering a content observer with the content resolver
    public void testInsertUpdate_Resolver() {
        ContentValues contentValues = TestUtilitiesData.createCalendarValues();

        // Register a content observer with the content resolver
        TestUtilitiesData.TestContentObserver testContentObserver = TestUtilitiesData.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(KueContract.CalendarEntry.CONTENT_URI,
                true, testContentObserver);

        // Directly insert into the calendar URI and verify with a query
        long locationRowId = insertAndVerify(mContext, KueContract.CalendarEntry.CONTENT_URI,
                contentValues);

        // Test to make sure the observer is called by waiting for a notification
        // from the content observer (caused by the insert action)
        testContentObserver.waitForNotificationOrFail();

        // Create new ContentValues to update with
        ContentValues updatedValues = new ContentValues(contentValues);
        updatedValues.put(KueContract.CalendarEntry._ID, locationRowId);
        updatedValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, "Annihilator's Apparition");

        // Perform the update and make sure it was successful
        int updateCount = mContext.getContentResolver().update(
                KueContract.CalendarEntry.CONTENT_URI, updatedValues, KueContract.CalendarEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(updateCount, 1);

        // Test to make sure the observer is called by waiting for a notification
        // from the content observer (caused by the update action)
        testContentObserver.waitForNotificationOrFail();

        // Unregister the content observer
        mContext.getContentResolver().unregisterContentObserver(testContentObserver);

        // Query the calendar URI to verify the update
        verifyValues(mContext, updatedValues, KueContract.CalendarEntry.CONTENT_URI);
    }

    // Insert and update each column in the Twitter table, and tests the content resolver by
    // registering a content observer with the content resolver
    // Queries here are done through the EmergencyQuest URI, which is the union of the calendar
    // and Twitter tables
    public void testInsertUpdate_EmergencyQuest() {
        ContentValues contentValues = TestUtilitiesData.createTwitterValues();

        // Register a content observer with the content resolver
        TestUtilitiesData.TestContentObserver testContentObserver = TestUtilitiesData.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(KueContract.EmergencyQuest.CONTENT_URI,
                true, testContentObserver);

        // Insert and verify calendar URI
        Uri uri = mContext.getContentResolver().insert(KueContract.TwitterEntry.CONTENT_URI, contentValues);
        long locationRowId = ContentUris.parseId(uri);

        // Verify insertion was successful
        assertTrue("Error: Insertion into " + KueContract.TwitterEntry.CONTENT_URI.toString() +
                        " was unsuccessful", locationRowId != -1);

        // Verify the table by querying using the EmergencyQuest URI, which is the union of the
        // calendar and Twitter tables
        verifyValues(mContext, contentValues, KueContract.EmergencyQuest.CONTENT_URI);

        // Test to make sure the observer is called by waiting for a notification
        // from the content observer (caused by the insert action)
        testContentObserver.waitForNotificationOrFail();

        // Create new ContentValues to update with
        ContentValues updatedValues = new ContentValues(contentValues);
        updatedValues.put(KueContract.CalendarEntry._ID, locationRowId);
        updatedValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, "Annihilator's Apparition");

        // Perform the update and make sure it was successful
        int updateCount = mContext.getContentResolver().update(
                KueContract.TwitterEntry.CONTENT_URI, updatedValues, KueContract.TwitterEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(updateCount, 1);

        // Test to make sure the observer is called by waiting for a notification
        // from the content observer (caused by the update action)
        testContentObserver.waitForNotificationOrFail();

        // Unregister the content observer
        mContext.getContentResolver().unregisterContentObserver(testContentObserver);

        // Query the EmergencyQuest URI to verify the update
        verifyValues(mContext, updatedValues, KueContract.EmergencyQuest.CONTENT_URI);
    }
}
