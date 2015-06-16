package frostlight.pso2kue.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.HashSet;

import frostlight.pso2kue.util.PollingCheck;

/**
 * TestUtilities
 * Helper functions and constants for TestDb
 * Created by Vincent on 5/29/2015.
 */
public class TestUtilities extends AndroidTestCase {

    // Test values to use when inserting into the tables
    static final String TEST_EQ_JAPANESE = "平穏を引き裂く混沌";
    static final String TEST_EQ_ENGLISH = "Chaotic Tranquility";
    static final long TEST_DATE = 1420165680L; // January 1, 2015

    // The names for each of the tables
    final static String[] tableNames = {
            KueContract.CalendarEntry.TABLE_NAME,
            KueContract.TwitterEntry.TABLE_NAME,
            KueContract.TranslationEntry.TABLE_NAME
    };
    // Corresponding to tableNames, the names of all the columns for each table
    final static String[][] columnNames = {
            {
                    // Calendar Table
                    KueContract.CalendarEntry._ID,
                    KueContract.CalendarEntry.COLUMN_EQNAME,
                    KueContract.CalendarEntry.COLUMN_DATE
            },
            {
                    // Twitter Table
                    KueContract.TwitterEntry._ID,
                    KueContract.TwitterEntry.COLUMN_EQNAME,
                    KueContract.TwitterEntry.COLUMN_DATE
            },
            {
                    // Translation Table
                    KueContract.TranslationEntry._ID,
                    KueContract.TranslationEntry.COLUMN_JAPANESE,
                    KueContract.TranslationEntry.COLUMN_ENGLISH
            }
    };

    /**
     * Checks if a cursor is empty
     *
     * @param cursor Cursor to check
     * @return True if the cursor is empty, False if the cursor is not empty
     */
    static boolean isCursorEmpty(Cursor cursor) {
        return !cursor.moveToFirst() || cursor.getCount() == 0;
    }

    /**
     * Creates a set of test entry values for the calendar table.
     *
     * @return Sample ContentValues for the calendar table
     */
    static ContentValues createCalendarValues() {
        ContentValues calendarValues = new ContentValues();
        calendarValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, TEST_EQ_ENGLISH);
        calendarValues.put(KueContract.CalendarEntry.COLUMN_DATE, TEST_DATE);
        return calendarValues;
    }

    /**
     * Creates a set of test entry values for the Twitter table.
     *
     * @return Sample ContentValues for the Twitter table
     */
    static ContentValues createTwitterValues() {
        ContentValues twitterValues = new ContentValues();
        twitterValues.put(KueContract.TwitterEntry.COLUMN_EQNAME, TEST_EQ_ENGLISH);
        twitterValues.put(KueContract.TwitterEntry.COLUMN_DATE, TEST_DATE);
        return twitterValues;
    }

    /**
     * Creates a set of test entry values for the translation table.
     *
     * @return Sample ContentValues for the translation table
     */
    static ContentValues createTranslationValues() {
        ContentValues translationValues = new ContentValues();
        translationValues.put(KueContract.TranslationEntry.COLUMN_JAPANESE, TEST_EQ_JAPANESE);
        translationValues.put(KueContract.TranslationEntry.COLUMN_ENGLISH, TEST_EQ_ENGLISH);
        return translationValues;
    }

    /**
     * Cross-checks a cursor with a provided column name and contents
     * (what's supposed to be in the database)
     * E.g. Master table (names column) - Check if all the tables have been created
     * Table info (names column) - Check if all the table columns have been created
     *
     * @param cursor         Associated cursor
     * @param columnName     Name of the column of the provided string array
     * @param columnContents Array of what's supposed to be in the column, will be crosschecked
     *                       with the cursor
     * @param errorMessage   Message to display if an error occurs
     */
    static void hashTest(Cursor cursor, String columnName, String[] columnContents, String errorMessage) {
        // Create a HashSet of all the column contents
        HashSet<String> hashSet = new HashSet<>();
        hashSet.addAll(Arrays.asList(columnContents));

        // Get the index of the provided column name, use it to crosscheck provided data with cursor
        int columnIndex = cursor.getColumnIndex(columnName);
        do {
            hashSet.remove(cursor.getString(columnIndex));
        } while (cursor.moveToNext());

        // HashSet should be empty after removing everything
        assertTrue(errorMessage, hashSet.isEmpty());
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    // A mock ContentObserver class used for testing purposes
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHandlerThread;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread handlerThread) {
            super(new Handler(handlerThread.getLooper()));
            mHandlerThread = handlerThread;
        }

        static TestContentObserver getTestContentObserver() {
            HandlerThread handlerThread = new HandlerThread("ContentObserverThread");
            handlerThread.start();
            return new TestContentObserver(handlerThread);
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Source: Android CTS (Compatibility Test Suite)
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHandlerThread.quit();
        }
    }
}
