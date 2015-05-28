package frostlight.pso2kue.data;

import android.provider.BaseColumns;

/**
 * DbContract
 * Defines table and column names for the databases
 * Created by Vincent on 5/19/2015.
 */
public class DbContract {

    /**
     * Inner class that defines the table contents of the calendar table
     * The calendar table stores the emergency quest schedule obtained from Google calendar
     * Table: ID | EQ Name | Date/Time
     */
    public static final class CalendarEntry implements BaseColumns {
        public static final String TABLE_NAME = "calendar";

        // The name of the emergency quest
        public static final String COLUMN_EQNAME = "eq_name";

        // The time the emergency quest occurs
        public static final String COLUMN_DATE = "date";
    }

    /**
     * Inner class that defines the table contents of the Twitter table
     * The twitter table stores emergency quest alert tweets obtained from Twitter bots
     * Table: ID | EQ Name | Date/Time
     */
    public static final class TwitterEntry implements BaseColumns {
        public static final String TABLE_NAME = "twitter";

        // The name of the emergency quest
        public static final String COLUMN_EQNAME = "eq_name";

        // The time the emergency quest occurs
        public static final String COLUMN_DATE = "date";
    }

    /**
     * Inner class that defines the table contents of the translation table
     * The translation table stores the japanese/english pairs for the emergency quest names
     * Table: ID | EQ Name | Date/Time
     */
    public static final class TranslationEntry implements BaseColumns {
        public static final String TABLE_NAME = "translation";

        // The name of the emergency quest in Japanese
        public static final String COLUMN_JAPANESE = "japanese";

        // The corresponding english translation
        public static final String COLUMN_ENGLISH = "english";
    }
}
