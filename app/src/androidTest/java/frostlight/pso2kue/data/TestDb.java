package frostlight.pso2kue.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.HashSet;

/**
 * TestDb
 * Test cases for all database functions
 * Created by Vincent on 5/19/2015.
 */
public class TestDb extends AndroidTestCase {
    // The names for each of the tables
    final static String[] tableNames = {
            DbContract.CalendarEntry.TABLE_NAME,
            DbContract.TwitterEntry.TABLE_NAME,
            DbContract.TranslationEntry.TABLE_NAME
    };

    // Corresponding to tableNames, the names of all the columns for each table
    final static String[][] columnNames = {
            {
                    // Calendar Table
                    DbContract.CalendarEntry._ID,
                    DbContract.CalendarEntry.COLUMN_EQNAME,
                    DbContract.CalendarEntry.COLUMN_DATE
            },
            {
                    // Twitter Table
                    DbContract.TwitterEntry._ID,
                    DbContract.TwitterEntry.COLUMN_EQNAME,
                    DbContract.TwitterEntry.COLUMN_DATE
            },
            {
                    // Translation Table
                    DbContract.TranslationEntry._ID,
                    DbContract.TranslationEntry.COLUMN_JAPANESE,
                    DbContract.TranslationEntry.COLUMN_ENGLISH
            }
    };

    // Call this before each test to start clean
    void deleteDb() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    /**
     * Cross-checks a cursor with a provided column name and contents
     * (what's supposed to be in the database)
     * E.g. Master table (names column) - Check if all the tables have been created
     *      Table info (names column) - Check if all the table columns have been created
     * @param cursor Associated cursor
     * @param columnName Name of the column of the provided string array
     * @param columnContents Array of what's supposed to be in the column, will be crosschecked
     *                       with the cursor
     * @param errorMessage Message to display if an error occurs
     */
    void hashTest(Cursor cursor, String columnName, String[] columnContents, String errorMessage)
    {
        // Create a HashSet of all the column contents
        HashSet<String> hashSet = new HashSet<>();
        hashSet.addAll(Arrays.asList(columnContents));

        // Get the index of the provided column name, use it to crosscheck provided data with cursor
        int columnIndex = cursor.getColumnIndex(columnName);
        do {
            hashSet.remove(cursor.getString(columnIndex));
        } while( cursor.moveToNext() );

        // HashSet should be empty after removing everything
        assertTrue(errorMessage, hashSet.isEmpty());
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
        hashTest(cursor, "name", tableNames, "Error: Database was created without all entries");

        // Verify that each table contains all the required columns
        for (int i = 0; i < tableNames.length; i++)
        {
            cursor = sqLiteDatabase.rawQuery("PRAGMA table_info(" + tableNames[i] + ")", null);
            assertTrue("Error: Unable to query the database for table information",
                    cursor.moveToFirst());
            hashTest(cursor, "name", columnNames[i], "Error: The " + tableNames[i] +
                    " table does not contain all required columns");
        }
    }
}
