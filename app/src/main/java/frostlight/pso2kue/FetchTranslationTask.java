package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

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
            JSONArray array = Utility.GoogleSpreadsheetHelper.getJSONArray(ConstGeneral.translationUrl);
            if (array == null) {
                cancel(false);
                return null;
            }

            // Don't need to wipe, insert over database to avoid
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

    /**
     * Class of functions used for helping with translation
     * Created by Vincent on 8/10/2015.
     */
    public static class TranslationHelper {

        public static String getEqTranslation(Context context, String eqName) {
            // Try to find the Japanese string in the translation database
            Cursor cursor = context.getContentResolver().query(
                    KueContract.TranslationEntry.CONTENT_URI,
                    null,
                    KueContract.TranslationEntry.COLUMN_JAPANESE + " = \"" + eqName + "\"",
                    null,
                    null
            );

            String translatedEqName;
            if (cursor != null && !Utility.isCursorEmpty(cursor)) {
                // If the Japanese entry exists in the translation database, just use that
                translatedEqName = cursor.getString(
                        cursor.getColumnIndex(KueContract.TranslationEntry.COLUMN_ENGLISH));
            } else {
                // If the Japanese entry doesn't exist in the translation database, or if the query
                // attempt failed, translate it to English with the Bing Translate API
                translatedEqName = bingTranslateJpEng(eqName);

                // Add the translation to the database
                ContentValues contentValues = new ContentValues();
                contentValues.put(KueContract.TranslationEntry.COLUMN_JAPANESE, eqName);
                contentValues.put(KueContract.TranslationEntry.COLUMN_ENGLISH, translatedEqName);
                context.getContentResolver().insert(KueContract.TranslationEntry.CONTENT_URI, contentValues);
            }

            if (cursor != null) {
                cursor.close();
            }

            return translatedEqName;
        }

        /**
         * Translates a String from Japanese to English
         * @param japanese String in Japanese
         * @return String in English
         */
        private static String bingTranslateJpEng(String japanese) {
            // Set Bing authentication key and Secret
            Translate.setClientId(ConstKey.bingKey);
            Translate.setClientSecret(ConstKey.bingSecret);

            // Attempt to translate the text from Japanese to English
            try {
                return Translate.execute(japanese, Language.JAPANESE, Language.ENGLISH);
            } catch (Exception e) {
                Log.e(Utility.getTag(), "Error: ", e);
                e.printStackTrace();
            }
            return null;
        }
    }
}
