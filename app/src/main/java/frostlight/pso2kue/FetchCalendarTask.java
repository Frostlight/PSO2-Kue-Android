package frostlight.pso2kue;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


/**
 * FetchCalenderTask
 * Async task to fetch the emergency quest timetable from Google Calendars
 * Created by Vincent on 5/19/2015.
 */
public class FetchCalendarTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw XML response as a string.
        String calendarXml = null;

        try {
            URL url = new URL(ConstGeneral.googleUrl);

            // Create the request to Google calendar, and open the connection
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();

            // Nothing to do if input stream fails
            if (inputStream == null) {
                return null;
            }

            // Read input stream to get a list of entries
            try {
                List<XmlHelper.Entry> entryList = XmlHelper.parse(inputStream);

                // Print each list element out
                for (XmlHelper.Entry entry: entryList) {
                    Log.v(Utility.getTag(), "Title: " + entry.title);
                    Log.v(Utility.getTag(), "Summary: " + entry.summary);
                }
            } catch (XmlPullParserException e) {
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
            }

            // TODO: Store entries into database
        } catch (IOException e) {
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
        }
        return null;
    }
}
