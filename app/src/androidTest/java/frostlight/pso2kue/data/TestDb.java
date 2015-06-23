package frostlight.pso2kue.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

/**
 * TestDb
 * Test cases for all database functions
 * Created by Vincent on 5/19/2015.
 */
public class TestDb extends AndroidTestCase {

    /**
     * Inserts a set of ContentValues into a database and queries for the same values
     *
     * @param sqLiteDatabase The database to insert into
     * @param tableName      The name of the table to insert to
     * @param testValues     ContentValues consisting of the entry for insertion
     * @return ID of the row the entry was inserted to
     */
    private static long insertAndVerify(SQLiteDatabase sqLiteDatabase, String tableName,
                                        ContentValues testValues) {
        // Insert ContentValues into database and get a row ID back
        long locationRowId = sqLiteDatabase.insert(tableName, null, testValues);

        // Verify insertion was successful
        assertTrue("Error: Insertion into table " + tableName + " was unsuccessful",
                locationRowId != -1);

        // Query and verify query for the values that were just inserted
        verifyValues(sqLiteDatabase, tableName, testValues);

        return locationRowId;
    }

    /**
     * Verifies that an entry (according to a ContentValues) exists in the table
     * This test queries the database
     *
     * @param sqLiteDatabase The database to check
     * @param tableName      The name of the table to check
     * @param expectedValues ContentValues consisting of what to look for
     */
    private static void verifyValues(SQLiteDatabase sqLiteDatabase, String tableName,
                                     ContentValues expectedValues) {
        String whereClause = "";

        // Iterate through each ContentValue key-value pair to generate the WHERE clause
        // of the SQL query
        for (String key : expectedValues.keySet()) {
            Object value = expectedValues.get(key);

            if (!whereClause.isEmpty())
                whereClause += " AND ";
            whereClause += key + " = \"" + value.toString() + "\"";
        }

        // Query the table
        Cursor cursor = sqLiteDatabase.query(
                tableName,
                null,
                whereClause,
                null,
                null,
                null,
                null
        );

        // Cursor should not be empty
        assertFalse("Empty cursor returned for query on " + tableName, TestUtilitiesData.isCursorEmpty(cursor));
        cursor.close();
    }

    // Call this before each test to start clean
    private void deleteDb() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteDb();
    }

    // Test if the database can be properly created with all the tables
    public void testCreateDb() throws Throwable {
        // Delete and remake the database
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase sqLiteDatabase = new DbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, sqLiteDatabase.isOpen());

        // Check if the table was created properly
        Cursor cursor = sqLiteDatabase
                .rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", cursor.moveToFirst());

        // Verify that the tables have been created
        TestUtilitiesData.hashTest(cursor, "name", TestUtilitiesData.tableNames, "Error: Database was created without all entries");

        // Verify that each table contains all the required columns
        for (int i = 0; i < TestUtilitiesData.tableNames.length; i++) {
            cursor = sqLiteDatabase.rawQuery("PRAGMA table_info(" + TestUtilitiesData.tableNames[i] + ")", null);
            assertTrue("Error: Unable to query the database for table information",
                    cursor.moveToFirst());
            TestUtilitiesData.hashTest(cursor, "name", TestUtilitiesData.columnNames[i], "Error: The " + TestUtilitiesData.tableNames[i] +
                    " table does not contain all required columns");
        }
        cursor.close();
    }

    // Insert and query each database
    // Testing is handled by the database insertion function
    public void testInsert() {
        DbHelper dbHelper = new DbHelper(this.getContext());
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        // Insert and query calendar database
        insertAndVerify(sqLiteDatabase, KueContract.CalendarEntry.TABLE_NAME,
                TestUtilitiesData.createCalendarValues());

        // Insert and query twitter database
        insertAndVerify(sqLiteDatabase, KueContract.TwitterEntry.TABLE_NAME,
                TestUtilitiesData.createTwitterValues());

        // Insert and query translation database
        insertAndVerify(sqLiteDatabase, KueContract.TranslationEntry.TABLE_NAME,
                TestUtilitiesData.createTranslationValues());
    }
}
