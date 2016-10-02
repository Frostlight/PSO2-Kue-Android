package frostlight.pso2kue;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * Utility
 * A set of helper functions for the App
 * Created by Vincent on 5/26/2015.
 */
public class Utility {

    /**
     * Checks if there is an active network connection
     *
     * @param context Context to use to verify network connection
     * @return True if there is a network connection, False otherwise
     */
    static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

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
     * Gets the notification setting from the preferences
     *
     * @param context Context to use for resource fetching
     * @return True or false depending on whether notifications are enabled or disabled
     */
    public static boolean getPreferenceNotifications(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getBoolean(context.getString(R.string.pref_notifystate_key),
                    Boolean.parseBoolean(context.getString(R.string.pref_notifystate_default)));
        } catch (Exception e) {
            return ConstGeneral.defaultNotify;
        }
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
    static int getPreferenceClock(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return Integer.parseInt(sharedPreferences.getString(
                    context.getString(R.string.pref_clock_key), context.getString(R.string.pref_clock_default)));
        } catch (Exception e) {
            return ConstGeneral.defaultClock;
        }
    }

    /**
     * Gets the quest name language setting from the preferences
     *
     * @param context Context to use for resource fetching
     * @return Either english or japanese
     */
    public static String getPreferenceQuestLanguage(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getString(
                    context.getString(R.string.pref_questlanguage_key), context.getString(R.string.pref_questlanguage_default));
        } catch (Exception e) {
            return ConstGeneral.defaultQuestLanguage;
        }
    }

    /**
     * Gets the filter toggle (on/off) from the preferences
     *
     * @param context Context to use for resource fetching
     * @return True or false depending on whether filters are enabled or disabled
     */
    public static boolean getPreferenceFilter(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getBoolean(context.getString(R.string.pref_filterstate_key),
                    Boolean.parseBoolean(context.getString(R.string.pref_filterstate_default)));
        } catch (Exception e) {
            return ConstGeneral.defaultFilter;
        }
    }

    /**
     * Gets the filter details (which options are enabled) from the preferences
     *
     * @param context Context to use for resource fetching
     * @return Contents of filter selection
     */
    public static Set<String> getPreferenceFilterDetails(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getStringSet(
                    context.getString(R.string.pref_filterdetails_key), null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the timezone setting from the preferences
     *
     * @param context Context to use for resource fetching
     * @return Timezone
     */
    static String getPreferenceTimezone(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getString(
                    context.getString(R.string.pref_timezone_key), context.getString(R.string.pref_timezone_default));
        } catch (Exception e) {
            return ConstGeneral.defaultTimezone;
        }
    }

    /**
     * Matches an input string with a regular expression, and returns the first result
     *
     * @param input Input string to match with
     * @param regex Regex used to match
     * @return The first result of the pattern
     */
    static String matchPattern(String input, String regex) {
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
    static long roundUpHour(long dateInMillis) {
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
    static String dateToRFC3339(long dateInMillis) {
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
    static String formatTimeForDisplay(long date, int timeDisplayPreference, String timezone) {
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter dateTimeFormatter;

        // 24 hour clock
        if (timeDisplayPreference == 24)
            dateTimeFormatter = DateTimeFormat.forPattern("HH:mm");
        // 12 hour clock
        else
            dateTimeFormatter = DateTimeFormat.forPattern("hh:mm aa");

        if (timezone.equals("default"))
            return dateTime.withZone(DateTimeZone.getDefault()).toString(dateTimeFormatter);
        else
            return dateTime.withZone(DateTimeZone.forID(timezone)).toString(dateTimeFormatter);
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
    static String getDayName(Context context, long dateInMillis) {
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
     static String getDayNameShort(Context context, long dateInMillis) {
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

    /**
     * GoogleSpreadsheetHelper
     * Methods that interact with Google Spreadsheets
     * Created by Vincent on 9/12/15.
     */
    static class GoogleSpreadsheetHelper {

        /**
         * Gets a JSONArray containing cell contents from a Google Spreadsheets uri
         * Top cells are assumed to be headers
         * @param uri Uri to extract JSONArray from
         * @return JSONArray containing cell contents
         */
        static JSONArray getJSONArray(String uri) {
            // Declared outside try/catch block so it can be closed in the finally block
            HttpsURLConnection urlConnection = null;

            // Return query as JSON instead
            String alternateResults="json";

            // JSONArray to return
            JSONArray array;

            try {
                final String ALT_PARAM = "alt";

                // Build the URL using uri builder
                Uri built_uri = Uri.parse(uri).buildUpon()
                        .appendQueryParameter(ALT_PARAM, alternateResults)
                        .build();
                URL url = new URL(built_uri.toString());

                // Create the request to Google spreadsheets, and open the connection
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();

                // Nothing to do if input stream fails
                if (inputStream == null) {
                    return null;
                }

                try {
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);
                    array = new JSONObject(responseStrBuilder.toString())
                            .getJSONObject("feed").getJSONArray("entry");
                } catch (Exception e) {
                    // JSON failed to parse
                    Log.e(getTag(), "Error: ", e);
                    e.printStackTrace();
                    return null;
                }
            } catch (IOException e) {
                // Hostname wasn't resolved properly, no internet?
                Log.e(getTag(), "Error: ", e);
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return array;
        }
    }

}
