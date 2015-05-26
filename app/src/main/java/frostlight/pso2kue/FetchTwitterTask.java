package frostlight.pso2kue;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

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

    /**
     * Query Twitter for updates, update the main thread with a callback if a new random
     * emergency quest was found. The Twitter ID for Tweets for each ship come in the form of
     * PSO2es_ship## [1-10]
     *
     * Note: Twitter ship bots are mapped in Const.shipId to array indices 1 lower than the
     * actual ship (i.e. ship 1 = Const.shipId[0], ship 10 = Const.shipId[9])
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
        OAuth2Token token = FetchTwitterHelper.getOAuth2Token();
        ConfigurationBuilder configurationBuilder = FetchTwitterHelper.getConfigurationBuilder();
        configurationBuilder.setOAuth2TokenType(token.getTokenType());
        configurationBuilder.setOAuth2AccessToken(token.getAccessToken());

        // Get a Twitter object based on the authentication details
        Twitter twitter = new TwitterFactory(configurationBuilder.build()).getInstance();

        try {
            // For bot screen name, subtract 1 from the ship number to get the array index
            Long screen_name = Long.parseLong(Const.shipId[ship - 1][0]);

            // Only retrieve the latest Tweet (one tweet) from the bot
            Paging paging = new Paging();
            paging.setCount(1);

            // Perform the lookup here
            twitter4j.Status response = twitter.getUserTimeline(screen_name, paging).get(0);

            // Log the tweet
            Log.v(App.getTag(), "Text: " + response.getText());
            Log.v(App.getTag(), "Time: " + response.getCreatedAt().toString());
            Log.v(App.getTag(), "Time since: " + Utility.getMinuteDifference(new Date(),
                    response.getCreatedAt()) + " minutes");
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
