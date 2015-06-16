package frostlight.pso2kue;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility
 * A set of helper functions for the App
 * Created by Vincent on 5/26/2015.
 */
public class Utility {

    /**
     * Matches an input string with a regular expression, and returns the first result
     *
     * @param input Input string to match with
     * @param regex Regex used to match
     * @return The first result of the pattern
     */
    public static String matchPattern(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        //noinspection ResultOfMethodCallIgnored
        matcher.find();
        return matcher.group();
    }

    /**
     * Returns a formatted date String corresponding to the user's timezone
     *
     * @param date Date in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long date) {
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yyyy HH:mm");
        return dateTime.withZone(DateTimeZone.getDefault()).toString(dateTimeFormatter);
    }

    /**
     * Returns a formatted time String corresponding to the user's timezone
     *
     * @param date Date in milliseconds
     * @return Formatted date string
     */
    public static String formatTime(long date) {
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm");
        return dateTime.withZone(DateTimeZone.getDefault()).toString(dateTimeFormatter);
    }

    /**
     * Rounds the date/time up to the nearest hour
     *
     * @param dateInMillis Date/time in milliseconds
     * @return Rounded date/time in milliseconds
     */
    public static long roundUpHour(long dateInMillis) {
        DateTime dateTime = new DateTime(dateInMillis);
        dateTime = dateTime.plusSeconds(60 - dateTime.getSecondOfMinute());
        dateTime = dateTime.plusMinutes(60 - dateTime.getMinuteOfHour());
        return dateTime.getMillis();
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "Today", "Tomorrow"
     * <p/>
     * If the day is not "today" or "tomorrow", just return the formatted month and day
     * E.g. "June 23", "August 15"
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return Name of the day
     */
    public static String getDayName(Context context, long dateInMillis) {
        // Calculate the difference in days between two days
        int daysBetween = Days.daysBetween(new DateTime(System.currentTimeMillis()).toLocalDate(),
                new DateTime(dateInMillis).toLocalDate()).getDays();

        if (daysBetween == 0) {
            return context.getString(R.string.today);
        } else if (daysBetween == 1) {
            return context.getString(R.string.tomorrow);
        } else {
            // Otherwise, convert millisecond date format to the format "Month day"
            return (DateTimeFormat.forPattern("MMMM dd")).print(dateInMillis);
        }
    }

    /**
     * Generates a tag for log entries based on the class name
     * Source: http://stackoverflow.com/questions/8355632/how-do-you-usually-tag-log-entries-android
     *
     * @return Tag to use with logging functions
     */
    public static String getTag() {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            if (ste[i].getMethodName().equals("getTag")) {
                tag = ste[i + 1].getClassName() + "_" + ste[i + 1].getLineNumber();
            }
        }
        return tag;
    }
}
