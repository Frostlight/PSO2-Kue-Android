package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import frostlight.pso2kue.data.KueContract;


/**
 * FetchCalenderTask
 * Async task to fetch the emergency quest timetable from Google Calendars
 * Created by Vincent on 5/19/2015.
 */
public class FetchCalendarTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public FetchCalendarTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // If there is no network connection, nothing to do
        if (!Utility.isOnline(mContext))
            return null;


        // Some entries in the Google Calendar don't correspond to EQs
        // Build a RegEx expression here to filter out the non-EQ entries
        StringBuilder ignore_stringBuilder; // Used to build the ignore_string RegEx
        String ignore_string = ""; // Contains the RegEx itself

        try {
            // Get the list of Strings to ignore for calendar entries
            JSONArray array = GoogleSpreadsheetHelper.getJSONArray(ConstGeneral.ignoreStringsUrl);
            if (array == null) {
                cancel(false);
                return null;
            }

            // Allocate 13 character spaces for each String in the array
            // Reference:
            // "(Boost Day)|(Boost Period)|(Round.*Start)|(Round.*Ends)|(Ranking)|(Dance Festival)|(Maintenance)|(Stamp)|(Event)"
            // is 113 characters long, 12.55 characters for each entry on average;
            ignore_stringBuilder = new StringBuilder(array.length() * 13);

            // Construct a RegEx in the form of "(Ignore A)|(Ignore B)|...|(Ignore X)" for the ignore String
            for (int i = 0; i < array.length(); i++) {
                //list.add(array.getJSONObject(i).getString("interestKey"));
                String string = array.getJSONObject(i).getJSONObject("gsx$ignorestring").getString("$t");

                if (i != 0)
                    ignore_stringBuilder.append("|");
                ignore_stringBuilder.append("(").append(string).append(")");
            }
            ignore_string = ignore_stringBuilder.toString();
        } catch (Exception e) {
            // JSON failed to parse
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
            cancel(true);
        }

        // Declared outside try/catch block so it can be closed in the finally block
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain raw JSON for the response (with the calendar)
        String rawCalenderJSON = null;

        // Parameter values for querying
        // Start date in RFC3339 format (current time minus 30 minutes)
        String startDateRFC3339 = Utility.dateToRFC3339(System.currentTimeMillis() - 1800000);

        // Timezone to use (UTC)
        String timeZone = "UTC";

        try {
            final String KEY_PARAM = "key";
            final String TIMEZONE_PARAM = "timeZone";
            final String START_MIN_PARAM = "timeMin";

            // Build the URL using uri builder
            Uri built_uri = Uri.parse(ConstGeneral.googleUrl).buildUpon()
                    .appendQueryParameter(KEY_PARAM, ConstGeneral.googleKey)
                    .appendQueryParameter(TIMEZONE_PARAM, timeZone)
                    .appendQueryParameter(START_MIN_PARAM, startDateRFC3339)
                    .build();
            URL url = new URL(built_uri.toString());

            // Create the request to Google calendar, and open the connection
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            // Nothing to do if input stream fails
            if (inputStream == null) {
                cancel(false);
                return null;
            }

            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read each line from the JSON raw response
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            if (builder.length() == 0) {
                cancel(false);
                return null;
            }

            try {
                // Create a JSONObject from the raw response from Google Calendar
                JSONObject calendarJson = new JSONObject(builder.toString());
                JSONArray calendarItemsArray = calendarJson.getJSONArray("items");

                // Wipe the Calendar database before inserting
                mContext.getContentResolver().delete(KueContract.CalendarEntry.CONTENT_URI, null, null);

                // Using data from the JSON response, insert each EQ into the calendar database
                for(int i = 0; i < calendarItemsArray.length(); i++) {
                    JSONObject calendarItem = calendarItemsArray.getJSONObject(i);
                    String title = calendarItem.getString("summary");

                    // Date is in RFC 3339 Format, timezone UTC
                    // e.g. 2013-07-04T23:37:46.782Z
                    String dateRFC3339 = calendarItem.getJSONObject("start").getString("dateTime");

                    /**
                     * Find anything that isn't EQ related on the calendar entries
                     * If there are any matches, the event isn't EQ related, so it is not added to the database
                     *
                     * Examples below are separated by commas
                     * Original:    Limited Quest Boost Day, Black Nyack Boost Period, Round 10 Start, Round 10 Ends
                     * Result:      Boost Day, Boost Period, Round 10 Start, Round 10 Ends
                     */
                    if (Utility.matchPattern(title, ignore_string).length() > 0)
                        continue;

                    // Parse the RFC 3339 time and find the long milliseconds
                    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                    DateTime dateTime = dateTimeFormatter.parseDateTime(dateRFC3339);
                    String date = Long.toString(dateTime.getMillis());

                    // Insert each element into the database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, title);
                    contentValues.put(KueContract.CalendarEntry.COLUMN_DATE, date);
                    mContext.getContentResolver().insert(KueContract.CalendarEntry.CONTENT_URI, contentValues);
                }
            } catch (JSONException e) {
                // JSON failed to parse
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
                cancel(true);
            }
        } catch (IOException e) {
            // Hostname wasn't resolved properly, start date couldn't be encoded, etc.
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
            cancel(true);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(Utility.getTag(), "Error", e);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
