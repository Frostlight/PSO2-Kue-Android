package frostlight.pso2kue;

import android.util.Xml;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XMLHelper
 * Parses the received XML from an input stream (Google calendar XML)
 * Created by Vincent on 5/30/2015.
 */
public class XmlHelper {

    // Parse returns a list of these objects
    public static class Entry {
        public final String title;      // Title of the entry (EQ)
        public final String summary;    // The date and time of the EQ

        private Entry(String title, String summary) {
            this.title = title;
            this.summary = summary;
        }
    }

    /**
     * Parses an InputStream for XML data
     * @param inputStream InputStream to read from
     * @return List of Entry objects read from the XML
     */
    public static List<Entry> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            inputStream.close();
        }
    }

    /**
     * Seeks for entry tags and continues down another level
     * @param parser Parser that reads data from the input stream
     * @return List of Entry objects read from the XML
     */
    private static List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();

        parser.require(XmlPullParser.START_TAG, null, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    /**
     * Reads parser from each entry tag and returns an Entry object with the tags we're
     * interested in. If it encounters a title or summary tag, hands them off to their respective
     * "read" methods for processing
     * @param parser Parser that reads data from the input stream
     * @return Entry object with the tags we're interested in
     */
    private static Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "entry");
        String title = null;
        String summary = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    title = readTitle(parser);
                    break;
                case "summary":
                    summary = readSummary(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new Entry(title, summary);
    }

    /**
     * Processes title tags in the feed
     * @param parser Parser that reads data from the input stream
     * @return Readable title string consisting of the EQ name
     */
    private static String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "title");
        return title;
    }

    /**
     * Processes summary tags in the feed
     * @param parser Parser that reads data from the input stream
     * @return Readable summary string consisting of the EQ date and time
     */
    private static String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "summary");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "summary");

        /**
         * Get only the date and starting time
         * Original:    When: Sat 30 May 2015 1:00 to 1:30
         * Result:      30 May 2015 1:00
         */
        Pattern pattern = Pattern.compile("(?<=When: ....).*(?= to)");
        Matcher matcher = pattern.matcher(summary);

        //noinspection ResultOfMethodCallIgnored
        matcher.find();
        summary = matcher.group();

        // Parse the time with Japan timezone
        DateTime dateTime = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yyyy HH:mm");
        dateTime = dateTimeFormatter.withZone(DateTimeZone.forID(ConstGeneral.timeZone))
                .parseDateTime(summary);

        // Return the time in milliseconds
        return Long.toString(dateTime.getMillis());
    }

    /**
     * Extracts text values from tags (for read methods)
     * @param parser Parser that reads data from the input stream
     * @return The text in between their respective tags
     */
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Ignores a tag, moves on to the next one
     * @param parser Parser that reads data from the input stream
     */
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
