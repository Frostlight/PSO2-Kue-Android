package frostlight.pso2kue.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import frostlight.pso2kue.data.EqContract.CalendarEntry;
import frostlight.pso2kue.data.EqContract.TwitterEntry;
import frostlight.pso2kue.data.EqContract.TranslationEntry;
import twitter4j.Twitter;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * CalendarDbHelper
 * Manages a local database for emergency quest data.
 * Created by Vincent on 5/19/2015.
 */
public class EqDbHelper extends SQLiteOpenHelper {
    // If the database schema is changed, increment the database version
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "pso2.db";

    public EqDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
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
                TranslationEntry.COLUMN_ENGLISH + " TEXT NOT NULL );";
        sqLiteDatabase.execSQL(SQL_CREATE_TRANSLATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
