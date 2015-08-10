package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

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
        // Declared outside try/catch block so it can be closed in the finally block
        HttpsURLConnection urlConnection = null;

        // Parameter values for querying
        // Maximum results to return from calendar
        int maxResults = 99;

        // Start date in RFC3339 format (current time minus 30 minutes)
        String startDateRFC3339 = Utility.dateToRFC3339(System.currentTimeMillis() - 1800000);

        try {
            final String START_MIN_PARAM = "start-min";
            final String MAX_RESULTS_PARAM = "max-results";

            // Build the URL using uri builder
            Uri built_uri = Uri.parse(ConstGeneral.googleUrl).buildUpon()
                    .appendQueryParameter(START_MIN_PARAM, startDateRFC3339)
                    .appendQueryParameter(MAX_RESULTS_PARAM, Integer.toString(maxResults))
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
                cancel(true);
                return null;
            }

            try {
                // Read input stream to get a list of entries
                List<XmlParse.Entry> entryList = XmlParse.parse(inputStream);

                // Wipe the Calendar database before inserting
                mContext.getContentResolver().delete(KueContract.CalendarEntry.CONTENT_URI, null, null);
                for (XmlParse.Entry entry : entryList) {
                    /**
                     * Find anything that isn't EQ related on the calendar entries
                     * If there are any matches, the event isn't EQ related, so it is not added to the database
                     *
                     * Examples below are separated by commas
                     * Original:    Limited Quest Boost Day, Black Nyack Boost Period, Round 10 Start, Round 10 Ends
                     * Result:      Boost Day, Boost Period, Round 10 Start, Round 10 Ends
                     */
                    if (Utility.matchPattern(entry.title,
                            "(Boost Day)|(Boost Period)|(Round.*Start)|(Round.*Ends)" +
                                    "|(Ranking)|(Dance Festival)|(Maintenance)|(Stamp)").length() > 0)
                        continue;

                    // Insert each element into the database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, entry.title);
                    contentValues.put(KueContract.CalendarEntry.COLUMN_DATE, entry.summary);
                    mContext.getContentResolver().insert(KueContract.CalendarEntry.CONTENT_URI, contentValues);
                }
            } catch (XmlPullParserException e) {
                // XML failed to parse
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
        }
        return null;
    }
}
