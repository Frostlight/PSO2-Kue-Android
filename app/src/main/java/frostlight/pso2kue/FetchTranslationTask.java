package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import frostlight.pso2kue.data.KueContract;


/**
 * FetchTranslationTask
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

                // Wipe the Translation database before inserting
                mContext.getContentResolver().delete(KueContract.TranslationEntry.CONTENT_URI, null, null);
                for(int i = 0 ; i < array.length() ; i++){
                    //list.add(array.getJSONObject(i).getString("interestKey"));
                    String japanese = array.getJSONObject(i).getJSONObject("gsx$japanese").getString("$t");
                    String english = array.getJSONObject(i).getJSONObject("gsx$english").getString("$t");

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(KueContract.TranslationEntry.COLUMN_JAPANESE, japanese);
                    contentValues.put(KueContract.TranslationEntry.COLUMN_ENGLISH, english);
                    mContext.getContentResolver().insert(KueContract.TranslationEntry.CONTENT_URI, contentValues);
                }
            } catch (Exception e) {
                // JSON failed to parse
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
                cancel(true);
            }
        } catch (IOException e) {
            // Hostname wasn't resolved properly, no internet?
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
