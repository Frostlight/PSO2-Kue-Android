package frostlight.pso2kue.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * TestUriMatcher
 * Tests the Uri matcher to see if it matches the correct ID for each Uri
 * Created by Vincent on 6/8/2015.
 */
public class TestUriMatcher extends AndroidTestCase {

    // content://frostlight.pso2kue/calendar
    private static final Uri TEST_CALENDAR_DIR = KueContract.CalendarEntry.CONTENT_URI;

    // content://frostlight.pso2kue/twitter
    private static final Uri TEST_TWITTER_DIR = KueContract.TwitterEntry.CONTENT_URI;

    // content://frostlight.pso2kue/translation
    private static final Uri TEST_TRANSLATION_DIR = KueContract.TranslationEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = KueProvider.uriMatcher();

        // Test calendar directory Uri
        assertEquals("Error: The calendar URI was matched incorrectly",
                testMatcher.match(TEST_CALENDAR_DIR), KueProvider.CALENDAR);

        // Test Twitter directory Uri
        assertEquals("Error: The Twitter URI was matched incorrectly",
                testMatcher.match(TEST_TWITTER_DIR), KueProvider.TWITTER);

        // Test translation directory Uri
        assertEquals("Error: The translation URI was matched incorrectly",
                testMatcher.match(TEST_TRANSLATION_DIR), KueProvider.TRANSLATION);
    }
}
