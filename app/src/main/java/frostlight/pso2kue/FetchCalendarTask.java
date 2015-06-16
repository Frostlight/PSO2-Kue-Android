package frostlight.pso2kue;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import frostlight.pso2kue.data.DbHelper;
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

        try {
            URL url = new URL(ConstGeneral.googleUrl);

            // Create the request to Google calendar, and open the connection
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
                // Read input stream to get a list of entries
                List<XmlParse.Entry> entryList = XmlParse.parse(inputStream);

                // Wipe the Calendar database before inserting
                mContext.getContentResolver().delete(KueContract.CalendarEntry.CONTENT_URI, null, null);
                for (XmlParse.Entry entry : entryList) {
                    // Insert each element into the database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, entry.title);
                    contentValues.put(KueContract.CalendarEntry.COLUMN_DATE, entry.summary);
                    mContext.getContentResolver().insert(KueContract.CalendarEntry.CONTENT_URI, contentValues);
                }
            } catch (XmlPullParserException e) {
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
