package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import frostlight.pso2kue.data.KueContract;


/**
 * FetchCalenderTask
 * Async task to fetch the translation timetable from Google Spreadsheets
 * Created by Vincent on 5/19/2015.
 */
public class FetchTranslationTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public FetchTranslationTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Declared outside try/catch block so it can be closed in the finally block
        HttpsURLConnection urlConnection = null;

        // Return query as JSON instead
        String alternateResults="json";

        try {
            final String ALT_PARAM = "alt";

            // Build the URL using uri builder
            Uri built_uri = Uri.parse(ConstGeneral.translationUrl).buildUpon()
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
                cancel(true);
                return null;
            }

            try {
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                JSONArray array = new JSONObject(responseStrBuilder.toString())
                        .getJSONObject("feed").getJSONArray("entry");


                List<String> list = new ArrayList<String>();
                //JSONArray array = jsonObject.getJSONArray("feed");
                for(int i = 0 ; i < array.length() ; i++){
                    //list.add(array.getJSONObject(i).getString("interestKey"));
                    String japanese = array.getJSONObject(i).getJSONObject("title").getString("$t");
                    String english = array.getJSONObject(i).getJSONObject("content").getString("$t");

                    Log.v(Utility.getTag(), "JP: " + japanese + " ENG: " + english);
                }


//                // Read input stream to get a list of entries
//                List<XmlParse.Entry> entryList = XmlParse.parse(inputStream);
//
//                // Wipe the Calendar database before inserting
//                mContext.getContentResolver().delete(KueContract.CalendarEntry.CONTENT_URI, null, null);
//                for (XmlParse.Entry entry : entryList) {
//                    /**
//                     * Find anything that isn't EQ related on the calendar entries
//                     * If there are any matches, the event isn't EQ related, so it is not added to the database
//                     *
//                     * Examples below are separated by commas
//                     * Original:    Limited Quest Boost Day, Black Nyack Boost Period, Round 10 Start, Round 10 Ends
//                     * Result:      Boost Day, Boost Period, Round 10 Start, Round 10 Ends
//                     */
//                    if (Utility.matchPattern(entry.title,
//                            "(Boost Day)|(Boost Period)|(Round.*Start)|(Round.*Start)" +
//                                    "|(Round.*Ends)|(Time Attack Ranking)").length() > 0)
//                        continue;
//
//                    // Insert each element into the database
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put(KueContract.CalendarEntry.COLUMN_EQNAME, entry.title);
//                    contentValues.put(KueContract.CalendarEntry.COLUMN_DATE, entry.summary);
//                    mContext.getContentResolver().insert(KueContract.CalendarEntry.CONTENT_URI, contentValues);
//                }
            } catch (Exception e) {
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
