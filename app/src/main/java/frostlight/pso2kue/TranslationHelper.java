package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import frostlight.pso2kue.data.KueContract;

/**
 * Created by Vincent on 8/10/2015.
 */
public class TranslationHelper {

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
        if (!Utility.isCursorEmpty(cursor)) {
            // If the Japanese entry exists in the translation database, just use that
            translatedEqName = cursor.getString(
                    cursor.getColumnIndex(KueContract.TranslationEntry.COLUMN_ENGLISH));
        } else {
            // If the Japanese entry doesn't exist in the translation database, translate it
            // to English with the Bing Translate API
            translatedEqName = bingTranslateJpEng(eqName);

            // Add the translation to the database
            ContentValues contentValues = new ContentValues();
            contentValues.put(KueContract.TranslationEntry.COLUMN_JAPANESE, eqName);
            contentValues.put(KueContract.TranslationEntry.COLUMN_ENGLISH, translatedEqName);
            context.getContentResolver().insert(KueContract.TranslationEntry.CONTENT_URI, contentValues);
        }

        cursor.close();
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
