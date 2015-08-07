package frostlight.pso2kue.backend;

import sun.rmi.runtime.Log;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Vincent on 8/7/2015.
 */
public class TwitterFetch {

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


    public String fetchTwitter() {
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

            // Try to find the Japanese string in the translation database
            cursor = mContext.getContentResolver().query(
                    KueContract.TranslationEntry.CONTENT_URI,
                    null,
                    KueContract.TranslationEntry.COLUMN_JAPANESE + " = \"" + eqName + "\"",
                    null,
                    null
            );

            String translatedEqName;
            if (!isCursorEmpty(cursor)) {
                // If the Japanese entry exists in the translation database, just use that
                translatedEqName = cursor.getString(
                        cursor.getColumnIndex(KueContract.TranslationEntry.COLUMN_ENGLISH));
                cursor.close();
            } else {
                // If the Japanese entry doesn't exist in the translation database, translate it
                // to English with the Bing Translate API
                translatedEqName = translateJpEng(eqName);

                // Add the translation to the database
                ContentValues contentValues = new ContentValues();
                contentValues.put(KueContract.TranslationEntry.COLUMN_JAPANESE, eqName);
                contentValues.put(KueContract.TranslationEntry.COLUMN_ENGLISH, translatedEqName);
                mContext.getContentResolver().insert(KueContract.TranslationEntry.CONTENT_URI, contentValues);
            }

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
            Log.e(Utility.getTag(), "Error: ", e);
            e.printStackTrace();
        }
        return null;
    }
    }
}
