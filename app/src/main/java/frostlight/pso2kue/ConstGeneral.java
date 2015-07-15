package frostlight.pso2kue;

/**
 * ConstGeneral
 * Various constants used by the app
 * Created by Vincent on 5/25/2015.
 */
public class ConstGeneral {

    // Default ship if Preferences are not available
    // Ranges from Ship 1 to 10
    public static int defaultShip = 2;

    // Default time display setting if Preferences are not available
    // Either 24 or 12, corresponding to the type of clock
    public static int defaultClock = 24;

    // List of Twitter IDs and their associated handles
    public static final String[][] shipId = {
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

    // URL for the google calendar (XML format)
    public static final String googleUrl =
            "https://www.google.com/calendar/feeds/pso2emgquest%40gmail.com/public/basic?";

    // Time zone for Google calendar
    public static final String timeZone = "Japan";

    // URL for the translation table (querying)
    public static final String translationUrl =
            "https://spreadsheets.google.com/feeds/list/15W5QrDp3U8umUHxSUXJLd9dOFXGWv7gbqtWZk9qP5V8/od6/public/values?";
}
