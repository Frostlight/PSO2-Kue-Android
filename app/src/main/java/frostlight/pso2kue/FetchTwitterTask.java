package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import frostlight.pso2kue.data.DbContract;
import frostlight.pso2kue.data.DbHelper;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * FetchTwitterTask
 * Async task to fetch tweets from Twitter
 * Created by Vincent on 5/19/2015.
 */
public class FetchTwitterTask extends AsyncTask<Integer, Void, Void> {

    private DbHelper mDbHelper;
    private SQLiteDatabase mSQLiteDatabase;

    /**
     * FetchTwitterTask, initialises database helper on the context
     *
     * @param context The context to instantiate
     */
    public FetchTwitterTask(Context context) {
        mDbHelper = new DbHelper(context);
        mSQLiteDatabase = mDbHelper.getWritableDatabase();
    }

    /**
     * Creates an authentication token for application only authentication based on the
     * consumer key and secret provided in ConstGeneral.java
     *
     * @return The authentication token
     */
    public static OAuth2Token getOAuth2Token() {
        OAuth2Token token = null;
        ConfigurationBuilder configurationBuilder = getConfigurationBuilder();

        try {
            token = new TwitterFactory(configurationBuilder.build())
                    .getInstance().getOAuth2Token();
        } catch (TwitterException e) {
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
        }

        return token;
    }

    /**
     * Creates a ConfigurationBuilder based on the consumer key and secret provided in ConstGeneral.java
     *
     * @return The ConfigurationBuilder
     */
    public static ConfigurationBuilder getConfigurationBuilder() {
        ConfigurationBuilder configurationBuilder;

        configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(true);
        configurationBuilder.setOAuthConsumerKey(ConstKey.twitterKey);
        configurationBuilder.setOAuthConsumerSecret(ConstKey.twitterSecret);
        return configurationBuilder;
    }

    /**
     * Checks if a cursor is empty
     *
     * @param cursor Cursor to check
     * @return True if the cursor is empty, False if the cursor is not empty
     */
    static boolean isCursorEmpty(Cursor cursor) {
        return !cursor.moveToFirst() || cursor.getCount() == 0;
    }

    /**
     * Translates a String from Japanese to English
     * @param japanese String in Japanese
     * @return String in English
     */
    public static String translateJpEng (String japanese) {
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

    /**
     * Query Twitter for updates, update the main thread with a callback if a new random
     * emergency quest was found. The Twitter ID for Tweets for each ship come in the form of
     * PSO2es_ship## [1-10]
     *
     * Note: Twitter ship bots are mapped in ConstGeneral.shipId to array indices 1 lower than the
     * actual ship (i.e. ship 1 = ConstGeneral.shipId[0], ship 10 = ConstGeneral.shipId[9])
     *
     * @param params params[0] A string representing which ship (server) to use [1-10]
     *               e.g. 1 = ship 1, 10 = ship 10
     */
    @Override
    protected Void doInBackground(Integer... params) {
        // If there are no servers selected, there's nothing to look up
        if (params.length == 0)
            return null;
        int ship = params[0];

        // Authentication with Twitter
        OAuth2Token token = getOAuth2Token();
        ConfigurationBuilder configurationBuilder = getConfigurationBuilder();
        configurationBuilder.setOAuth2TokenType(token.getTokenType());
        configurationBuilder.setOAuth2AccessToken(token.getAccessToken());

        // Get a Twitter object based on the authentication details
        Twitter twitter = new TwitterFactory(configurationBuilder.build()).getInstance();

        try {
            // For bot ID, subtract 1 from the ship number to get the array index
            Long bot_id = Long.parseLong(ConstGeneral.shipId[ship - 1][0]);

            // Only retrieve the latest Tweet (one tweet) from the bot
            Paging paging = new Paging();
            paging.setCount(1);

            // Perform the lookup here
            twitter4j.Status response = twitter.getUserTimeline(bot_id, paging).get(0);

            /**
             * Extract the EQ name from the Tweet
             * Original:    で緊急クエスト「市街地奪還作戦」が発生します
             * Result:      市街地奪還作戦
             */
            String eqName = Utility.matchPattern(response.getText(),
                    "(?<=で緊急クエスト「).*(?=」が発生します)");

            // Try to find the Japanese string in the translation database
            Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM "
                    + DbContract.TranslationEntry.TABLE_NAME + " WHERE "
                    + DbContract.TranslationEntry.COLUMN_JAPANESE + " = \"" + eqName + "\"", null);

            String translatedEqName;
            if (!isCursorEmpty(cursor)) {
                // If the Japanese entry exists in the translation database, just use that
                translatedEqName = cursor.getString(
                        cursor.getColumnIndex(DbContract.TranslationEntry.COLUMN_ENGLISH));
            } else {
                // If the Japanese entry doesn't exist in the translation database, translate it
                // to English with the Bing Translate API
                translatedEqName = translateJpEng(eqName);

                // Add the translation to the database
                ContentValues contentValues = new ContentValues();
                contentValues.put(DbContract.TranslationEntry.COLUMN_JAPANESE, eqName);
                contentValues.put(DbContract.TranslationEntry.COLUMN_ENGLISH, translatedEqName);
                mSQLiteDatabase.insert(DbContract.TranslationEntry.TABLE_NAME, null, contentValues);
            }

            // Calculate the EQ time from the time the Tweet was posted
            long eqTime = Utility.roundUpHour(response.getCreatedAt().getTime());

            // Wipe the Twitter database before inserting
            mSQLiteDatabase.delete(DbContract.TwitterEntry.TABLE_NAME, null, null);

            // Insert the element into database
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbContract.TwitterEntry.COLUMN_EQNAME, translatedEqName);
            contentValues.put(DbContract.TwitterEntry.COLUMN_DATE, eqTime);
            mSQLiteDatabase.insert(DbContract.TwitterEntry.TABLE_NAME, null, contentValues);
        } catch (TwitterException e) {
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
        }
        return null;
    }
}
