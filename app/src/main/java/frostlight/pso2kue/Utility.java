package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import frostlight.pso2kue.data.KueContract;

/**
 * Utility
 * A set of helper functions for the App
 * Created by Vincent on 5/26/2015.
 */
public class Utility {

    /**
     * Checks if a cursor is empty
     *
     * @param cursor Cursor to check
     * @return True if the cursor is empty, False if the cursor is not empty
     */
    public static boolean isCursorEmpty(Cursor cursor) {
        return !cursor.moveToFirst() || cursor.getCount() == 0;
    }

    /**
     * Gets the ship (server) number from the preferences
     *
     * @param context Context to use for resource fetching
     * @return The ship number from preferences, or a default (ship 2) if no ship number is specified
     */
    public static int getPreferenceShip(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return Integer.parseInt(sharedPreferences.getString(
                    context.getString(R.string.pref_ship_key), context.getString(R.string.pref_ship_default)));
        } catch (Exception e) {
            return ConstGeneral.defaultShip;
        }
    }

    /**
     * Gets the time display setting from the preferences
     *
     * @param context Context to use for resource fetching
     * @return Either 24 or 12, corresponding to the type of clock
     */
    public static int getPreferenceClock(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return Integer.parseInt(sharedPreferences.getString(
                    context.getString(R.string.pref_clock_key), context.getString(R.string.pref_clock_default)));
        } catch (Exception e) {
            return ConstGeneral.defaultClock;
        }
    }

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

        String matchedString;
        // If there are no matches, matcher.group() throws an exception
        // In that case, we just return an empty string
        try {
            matchedString = matcher.group();
        } catch (Exception e) {
            matchedString = "";
        }

        return matchedString;
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
     * Converts a long date to a date string in RFC3339 format
     * Used in the query for Google calendar, in the start-date parameter
     * (which accepts a date in RFC3339 format)
     *
     * @param dateInMillis Date in milliseconds
     * @return Date in RFC3339 format
     */
    public static String dateToRFC3339 (long dateInMillis) {
        Date date = new Date(dateInMillis);

        // Apply RFC3339 format using Joda-Time
        DateTime dateTime = new DateTime(date.getTime(), DateTimeZone.UTC);
        DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
        return dateFormatter.print(dateTime);
    }

    /**
     * Returns a formatted time String corresponding to the user's timezone
     * Used to display times in the MainActivity's ListView
     *
     * @param date Date in milliseconds
     * @param timeDisplayPreference Type of clock to use, either 24 or 12
     * @return Formatted date string
     */
    public static String formatTimeForDisplay(long date, int timeDisplayPreference) {
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter dateTimeFormatter;

        // 24 hour clock
        if (timeDisplayPreference == 24)
            dateTimeFormatter = DateTimeFormat.forPattern("HH:mm");
        // 12 hour clock
        else
            dateTimeFormatter = DateTimeFormat.forPattern("hh:mm aa");
        return dateTime.withZone(DateTimeZone.getDefault()).toString(dateTimeFormatter);
    }

    /**
     * Given a day, a name to use for that day
     * Used to display day names in the MainActivity's ListView
     * E.g "Today, July 15", "Tomorrow, June 28"
     *
     * If the day is not "today" or "tomorrow", just return the formatted day of week, month and day
     * E.g. "Tuesday, June 23", "Wednesday, August 15"
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return Name of the day
     */
    public static String getDayName(Context context, long dateInMillis) {
        // Calculate the difference in days between two days
        int daysBetween = Days.daysBetween(new DateTime(System.currentTimeMillis()).toLocalDate(),
                new DateTime(dateInMillis).toLocalDate()).getDays();

        if (daysBetween == 0) {
            return context.getString(R.string.today) + ", "
                    + (DateTimeFormat.forPattern("MMMM dd")).print(dateInMillis);
        } else if (daysBetween == 1) {
            return context.getString(R.string.tomorrow) + ", "
                    + (DateTimeFormat.forPattern("MMMM dd")).print(dateInMillis);
        } else {
            // Otherwise, convert millisecond date format to the format "Month day"
            return (DateTimeFormat.forPattern("EEEE, MMMM dd")).print(dateInMillis);
        }
    }

    /**
     * Given a day, a name to use for that day
     * Like getDayName but returns shorter versions of the dates
     * E.g "Today", "Tomorrow"
     *
     * If the day is not "today" or "tomorrow", just return the formatted month and day
     * E.g. "June 23", "August 15"
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return Name of the day
     */
    public static String getDayNameShort(Context context, long dateInMillis) {
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
