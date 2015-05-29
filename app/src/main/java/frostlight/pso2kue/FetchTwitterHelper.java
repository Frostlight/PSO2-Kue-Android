package frostlight.pso2kue;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * FetchTwitterHelper
 * A set of helper functions for FetchTwitterTask (twitter4j library)
 * Created by Vincent on 5/20/2015.
 */
public class FetchTwitterHelper {

    /**
     * Creates an authentication token for application only authentication based on the
     * consumer key and secret provided in ConstGeneral.java
     * @return The authentication token
     */
    public static OAuth2Token getOAuth2Token()
    {
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
    public static ConfigurationBuilder getConfigurationBuilder()
    {
        ConfigurationBuilder configurationBuilder;

        configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(true);
        configurationBuilder.setOAuthConsumerKey(ConstTwitterAuth.consumerKey);
        configurationBuilder.setOAuthConsumerSecret(ConstTwitterAuth.consumerSecret);
        return configurationBuilder;
    }
}
