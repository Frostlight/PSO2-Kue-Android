package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import frostlight.pso2kue.data.DbContract;
import frostlight.pso2kue.data.DbHelper;


/**
 * FetchCalenderTask
 * Async task to fetch the emergency quest timetable from Google Calendars
 * Created by Vincent on 5/19/2015.
 */
public class FetchCalendarTask extends AsyncTask<Void, Void, Void> {

    private DbHelper mDbHelper;
    private SQLiteDatabase mSQLiteDatabase;

    /**
     * FetchCalendarTask, initialises database helper on the context
     * @param context The context to instantiate
     */
    public FetchCalendarTask(Context context) {
        mDbHelper = new DbHelper(context);
        mSQLiteDatabase = mDbHelper.getWritableDatabase();
    }


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


            try {
                // Read input stream to get a list of entries
                List<XmlHelper.Entry> entryList = XmlHelper.parse(inputStream);

                // Wipe the Calendar database before inserting
                mSQLiteDatabase.delete(DbContract.CalendarEntry.TABLE_NAME, null, null);
                for (XmlHelper.Entry entry: entryList) {
                    // Insert each element into the database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DbContract.CalendarEntry.COLUMN_EQNAME, entry.title);
                    contentValues.put(DbContract.CalendarEntry.COLUMN_DATE, entry.summary);
                    mSQLiteDatabase.insert(DbContract.CalendarEntry.TABLE_NAME, null, contentValues);
                }
            } catch (XmlPullParserException e) {
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
        }
        return null;
    }
}
