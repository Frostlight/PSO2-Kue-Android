package frostlight.pso2kue.backend;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

import static frostlight.pso2kue.backend.OfyService.ofy;

/**
 * Created by Vincent on 8/7/2015.
 */
public class TwitterServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(TwitterServlet.class.getName());

    /**
     * Api Keys can be obtained from the google cloud console
     */
    private static final String API_KEY = System.getProperty("gcm.api.key");

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
            e.printStackTrace();
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

    private static twitter4j.Twitter getTwitter(){
        // Authentication with TwitterServlet
        OAuth2Token token = getOAuth2Token();

        // Nothing to do, likely no network connection
        if (token == null)
            return null;

        ConfigurationBuilder configurationBuilder = getConfigurationBuilder();
        configurationBuilder.setOAuth2TokenType(token.getTokenType());
        configurationBuilder.setOAuth2AccessToken(token.getAccessToken());

        // Get a TwitterServlet object based on the authentication details
        return new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    private String fetchTwitter(Twitter twitter, int ship) {
        try {
            // For bot ID, subtract 1 from the ship number to get the array index
            Long bot_id = Long.parseLong(frostlight.pso2kue.backend.ConstGeneral.shipId[ship - 1][0]);

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

            // Otherwise, return the EQ name
            return eqName;

        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendShip(String message, int ship) {
        if (message == null || message.trim().length() == 0) {
            log.warning("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder().addData("message", message).build();
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).filter("ship ==", ship).list();
        for (RegistrationRecord record : records) {
            Result result = null;
            try {
                result = sender.send(msg, record.getRegId(), 5);
                if (result.getMessageId() != null) {
                    log.info("Message sent to " + record.getRegId());
                    String canonicalRegId = result.getCanonicalRegistrationId();
                    if (canonicalRegId != null) {
                        // if the regId changed, we have to update the datastore
                        log.info("Registration Id changed for " + record.getRegId() + " updating to " + canonicalRegId);
                        record.setRegId(canonicalRegId);
                        ofy().save().entity(record).now();
                    }
                } else {
                    String error = result.getErrorCodeName();
                    if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                        log.warning("Registration Id " + record.getRegId() + " no longer registered with GCM, removing from datastore");
                        // if the device is no longer registered with Gcm, remove it from the datastore
                        ofy().delete().entity(record).now();
                    } else {
                        log.warning("Error when sending message : " + error);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Authenticate and get a Twitter object
        twitter4j.Twitter twitter = getTwitter();

        if (twitter != null) {
            for (int ship = 1; ship <= 10; ship++) {
                String message = fetchTwitter(twitter, ship);
                sendShip(message, ship);
            }
        }
    }
}
