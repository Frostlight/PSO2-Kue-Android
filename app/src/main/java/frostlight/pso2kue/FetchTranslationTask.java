package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

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
        // If there is no network connection, nothing to do
        if (!Utility.isOnline(mContext))
            return null;

        try {
            JSONArray array = GoogleSpreadsheetHelper.getJSONArray(ConstGeneral.translationUrl);
            if (array == null) {
                cancel(false);
                return null;
            }

            // Wipe the Translation database before inserting
            mContext.getContentResolver().delete(KueContract.TranslationEntry.CONTENT_URI, null, null);
            for (int i = 0; i < array.length(); i++) {
                //list.add(array.getJSONObject(i).getString("interestKey"));
                String japanese = array.getJSONObject(i).getJSONObject("gsx$japanese").getString("$t");
                String english = array.getJSONObject(i).getJSONObject("gsx$english").getString("$t");

                // Log the Japanese and English names of each Emergency Quest on the table
                //Log.v(Utility.getTag(), "Japanese: " + japanese + " English: " + english);

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
        return null;
    }
}
