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

    // Default notification option if Preferences are not available
    // true or false
    public static boolean defaultNotify = true;

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

    // Reference:
    // https://www.googleapis.com/calendar/v3/calendars/pso2emgquest@gmail.com/events?
    // key=AIzaSyAEy3bKbdodcRrHisd5y3Z8qE022qRoyBA&timeMin=2015-11-20T21:45:01.563Z

    // URL for the google calendar (JSON format)
    public static final String googleUrl =
            "https://www.googleapis.com/calendar/v3/calendars/pso2emgquest%40gmail.com/events?";

    // Key for Android access with package frostlight.pso2kue
    public static final String googleKey =
            "AIzaSyAsVB6u2_PTC0hdnoIqxlJSIHL5FnP2FD0";

    // Time zone for Google calendar
    public static final String timeZone = "Japan";

    // URL for the translation table (querying)
    public static final String translationUrl =
            "https://spreadsheets.google.com/feeds/list/15W5QrDp3U8umUHxSUXJLd9dOFXGWv7gbqtWZk9qP5V8/default/public/values?";

    // URL for the ignore strings table (strings to ignore for calendar fetching)
    public static final String ignoreStringsUrl =
            "https://spreadsheets.google.com/feeds/list/1snJ_ZF--8W-0As7PT6nZjaQi_ofTZyvMXR9yZsQVUt4/default/public/values?";
}
