package frostlight.pso2kue;

/**
 * Const
 * Various constants used by the app
 * Created by Vincent on 5/25/2015.
 */
public class Const {
    // Authentication keys for using Twitter API (read permission only)
    static final String consumerKey = "YOUR CONSUMER KEY HERE";
    static final String consumerSecret = "YOUR CONSUMER SECRET HERE=";

    // Endpoint URL for oauth2 authentication with Twitter
    static final String twitterAuthUrl = "https://api.twitter.com/oauth2/token";

    // URL for GET statuses/user_timeline API request
    static final String twitterTimelineUrl =
            "https://api.twitter.com/1.1/statuses/user_timeline.json?";

    // List of Twitter IDs and their associated handles
    static final String[][] shipId = {
            {"2753540587", "PSO2es_ship01"},    // Ship 1
            {"2791287498", "PSO2es_ship02"},    // Ship 2
            {"2791359409", "PSO2es_ship03"},    // Ship 3
            {"2791298382", "PSO2es_ship04"},    // Ship 4
            {"2791375477", "PSO2es_ship05"},    // Ship 5
            {"2791309260", "PSO2es_ship06"},    // Ship 6
            {"2791380121", "PSO2es_ship07"},    // Ship 7
            {"2791316526", "PSO2es_ship08"},    // Ship 8
            {"2791388557", "PSO2es_ship09"},    // Ship 9
            {"2791357028", "PSO2es_ship10"}     // Ship 10
    };
}
