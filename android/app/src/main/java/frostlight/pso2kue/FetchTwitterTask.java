package frostlight.pso2kue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import frostlight.pso2kue.data.KueContract;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * FetchTwitterTask
 * Async task to fetch tweets from Twitter
 *
 * Currently fetches:
 * 1) onResume
 * 2) every 5 minutes (does not include initially)
 *
 * FetchTwitterTask will not do anything if the EQ in the Twitter database is within one
 * hour of the current time
 *
 * Created by Vincent on 5/19/2015.
 */
public class FetchTwitterTask extends AsyncTask<Integer, Void, Void> {

    private final Context mContext;

    public FetchTwitterTask(Context context) {
        mContext = context;
    }

    /**
     * Creates an authentication token for application only authentication based on the
     * consumer key and secret provided in ConstGeneral.java
     *
     * @return The authentication token
     */
    private static OAuth2Token getOAuth2Token() {
        OAuth2Token token = null;
        ConfigurationBuilder configurationBuilder = getConfigurationBuilder();

        try {
            token = new TwitterFactory(configurationBuilder.build())
                    .getInstance().getOAuth2Token();
        } catch (TwitterException e) {
            // Hide errors since they trigger too often (no internet, etc.)
            //Log.e(Utility.getTag(), "Error: ", e);
            //e.printStackTrace();
            return null;
        }

        return token;
    }

    /**
     * Creates a ConfigurationBuilder based on the consumer key and secret provided in ConstGeneral.java
     *
     * @return The ConfigurationBuilder
     */
    private static ConfigurationBuilder getConfigurationBuilder() {
        ConfigurationBuilder configurationBuilder;

        configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(true);
        configurationBuilder.setOAuthConsumerKey(ConstKey.twitterKey);
        configurationBuilder.setOAuthConsumerSecret(ConstKey.twitterSecret);
        return configurationBuilder;
    }

    /**
     * Queries the calendar database for all entries that are scheduled since 30 minutes in the past
     * @return  Associated cursor for that query
     */
    private Cursor queryCalendar () {
        // Where clause: all events scheduled from 30 minutes in the past
        String whereClause = KueContract.CalendarEntry.COLUMN_DATE + " > " +
                Long.toString(System.currentTimeMillis() - 1800000);

        // Sort order: ascending by date
        String sortOrder = KueContract.CalendarEntry.COLUMN_DATE + " ASC";

        return mContext.getContentResolver().query(
                KueContract.CalendarEntry.CONTENT_URI,
                null,
                whereClause,
                null,
                sortOrder
        );
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

        // Look up the last entry in the Twitter database
        Cursor cursor = mContext.getContentResolver().query(
                KueContract.TwitterEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // If the Twitter entry exists, compare the Tweet date with the current date
        if (!Utility.isCursorEmpty(cursor)) {
            long lastDate = Long.parseLong(cursor.getString(
                    cursor.getColumnIndex(KueContract.TwitterEntry.COLUMN_DATE)));

            // If the difference is less than an hour (i.e. the EQ happens within 1 hour before to 1
            // hour after, then there is no need to Twitter fetch)
            // This should save some of the data overhead
            if (Math.abs(System.currentTimeMillis() - lastDate) < 60*60*1000)
                return null;
        }
        cursor.close();

        // Look up the first entry in the calendar database
        cursor = queryCalendar();

        // If the entry exists, compare the first calendar entry date with the current date
        if (!Utility.isCursorEmpty(cursor)) {
            String dateIndex = cursor.getString(
                    cursor.getColumnIndex(KueContract.CalendarEntry.COLUMN_DATE));

            if (dateIndex.equals("")) {
                // This means we failed to retrieve the date index
                return null;
            }

            long lastDate = Long.parseLong(dateIndex);

            // If the difference is less than an hour (i.e. the EQ happens within 1 hour before to 1
            // hour after, then it overlaps with the Twitter fetch -- there is no need to fetch)
            if (Math.abs(System.currentTimeMillis() - lastDate) < 60*60*1000)
                return null;
        }

        int ship = params[0];

        // Authentication with Twitter
        OAuth2Token token = getOAuth2Token();

        // Nothing to do, likely no network connection
        if (token == null)
            return null;

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

            if (eqName.equals("")) {
                // This means the Tweet isn't actually of an EQ
                return null;
            }

            String translatedEqName = TranslationHelper.getEqTranslation(mContext, eqName);

            // Calculate the EQ time from the time the Tweet was posted
            long eqTime = Utility.roundUpHour(response.getCreatedAt().getTime());

            // Wipe the Twitter database before inserting
            mContext.getContentResolver().delete(KueContract.TwitterEntry.CONTENT_URI, null, null);

            // Insert the element into database
            ContentValues contentValues = new ContentValues();
            contentValues.put(KueContract.TwitterEntry.COLUMN_EQNAME, translatedEqName);
            contentValues.put(KueContract.TwitterEntry.COLUMN_DATE, eqTime);
            mContext.getContentResolver().insert(KueContract.TwitterEntry.CONTENT_URI, contentValues);
        } catch (TwitterException e) {
            // Hide errors since they trigger too often (no internet, etc.)
            //Log.e(Utility.getTag(), "Error: ", e);
            //e.printStackTrace();
            cancel(true);
        }
        return null;
    }
}
