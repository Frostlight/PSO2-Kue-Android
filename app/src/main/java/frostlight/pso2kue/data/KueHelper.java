package frostlight.pso2kue.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import frostlight.pso2kue.data.KueContract.CalendarEntry;
import frostlight.pso2kue.data.KueContract.TranslationEntry;
import frostlight.pso2kue.data.KueContract.TwitterEntry;


/**
 * CalendarDbHelper
 * Manages a local database for emergency quest data.
 * Created by Vincent on 5/19/2015.
 */
public class KueHelper extends SQLiteOpenHelper {

    // If the database schema is changed, increment the database version
    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "pso2.db";

    public KueHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create each table with columns as specified in KueContract
        final String SQL_CREATE_CALENDAR_TABLE = "CREATE TABLE " + CalendarEntry.TABLE_NAME + " (" +
                CalendarEntry._ID + " INTEGER PRIMARY KEY," +
                CalendarEntry.COLUMN_EQNAME + " TEXT NOT NULL, " +
                CalendarEntry.COLUMN_DATE + " INTEGER NOT NULL );";
        sqLiteDatabase.execSQL(SQL_CREATE_CALENDAR_TABLE);

        final String SQL_CREATE_TWITTER_TABLE = "CREATE TABLE " + TwitterEntry.TABLE_NAME + " (" +
                TwitterEntry._ID + " INTEGER PRIMARY KEY," +
                TwitterEntry.COLUMN_EQNAME + " TEXT NOT NULL, " +
                TwitterEntry.COLUMN_DATE + " INTEGER NOT NULL );";
        sqLiteDatabase.execSQL(SQL_CREATE_TWITTER_TABLE);

        final String SQL_CREATE_TRANSLATION_TABLE = "CREATE TABLE " + TranslationEntry.TABLE_NAME + " (" +
                TranslationEntry._ID + " INTEGER PRIMARY KEY," +
                TranslationEntry.COLUMN_JAPANESE + " TEXT NOT NULL, " +
                TranslationEntry.COLUMN_ENGLISH + " TEXT NOT NULL, " +
                " UNIQUE (" + TranslationEntry.COLUMN_JAPANESE + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_TRANSLATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CalendarEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TwitterEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TranslationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
