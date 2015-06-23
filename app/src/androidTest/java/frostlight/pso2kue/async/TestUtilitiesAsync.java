package frostlight.pso2kue.async;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * TestUtilitiesData
 * Helper functions and constants for TestAsync
 * Created by Vincent on 5/29/2015.
 */
public class TestUtilitiesAsync {

    /**
     * Returns a formatted date String corresponding to the user's timezone
     * Used for logging dates in TestFetchCalendarTask and TestFetchTwitterTask
     *
     * @param date Date in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long date) {
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yyyy HH:mm");
        return dateTime.withZone(DateTimeZone.getDefault()).toString(dateTimeFormatter);
    }
}
