package frostlight.pso2kue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.Date;

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
     * FetchCalendarTask, initialises database helper on the context
     * @param context The context to instantiate
     */
    public FetchTwitterTask(Context context) {
        mDbHelper = new DbHelper(context);
        mSQLiteDatabase = mDbHelper.getWritableDatabase();
    }

    /**
     * Creates an authentication token for application only authentication based on the
     * consumer key and secret provided in ConstGeneral.java
     * @return The authentication token
     */
    public static OAuth2Token getOAuth2Token() {
        OAuth2Token token = null;
        ConfigurationBuilder configurationBuilder = getConfigurationBuilder();

        try {
            token = new TwitterFactory(configurationBuilder.build())
                    .getInstance().getOAuth2Token();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return token;
    }

    /**
     * Creates a ConfigurationBuilder based on the consumer key and secret provided in ConstGeneral.java
     * @return The ConfigurationBuilder
     */
    public static ConfigurationBuilder getConfigurationBuilder() {
        ConfigurationBuilder configurationBuilder;

        configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(true);
        configurationBuilder.setOAuthConsumerKey(ConstTwitterAuth.consumerKey);
        configurationBuilder.setOAuthConsumerSecret(ConstTwitterAuth.consumerSecret);
        return configurationBuilder;
    }

    /**
     * Query Twitter for updates, update the main thread with a callback if a new random
     * emergency quest was found. The Twitter ID for Tweets for each ship come in the form of
     * PSO2es_ship## [1-10]
     *
     * Note: Twitter ship bots are mapped in ConstGeneral.shipId to array indices 1 lower than the
     * actual ship (i.e. ship 1 = ConstGeneral.shipId[0], ship 10 = ConstGeneral.shipId[9])
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

            // Log the tweet information
            /**
             * Extract the EQ name from the Tweet
             * Original:    で緊急クエスト「市街地奪還作戦」が発生します
             * Result:      市街地奪還作戦
             */
            String eqName = Utility.matchPattern(response.getText(),
                    "(?<=で緊急クエスト「).*(?=」が発生します)");
            Log.v(Utility.getTag(), "EQ Name: " + eqName);

            // Calculate the EQ time from the time the Tweet was posted
            long time = response.getCreatedAt().getTime();
            Log.v(Utility.getTag(), "When: " + Utility.formatDate(Utility.roundUpHour(time)));
            // TODO: Store entry into database

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
