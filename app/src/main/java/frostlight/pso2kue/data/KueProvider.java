package frostlight.pso2kue.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * KueProvider
 * Manages the content provider for the app
 * Created by Vincent on 6/7/2015.
 */
public class KueProvider extends ContentProvider {

    // Matches URIs for content provider requests
    private static final UriMatcher sUriMatcher = uriMatcher();

    // Database helper object
    private DbHelper mDbHelper;

    // IDs for URI types
    static final int CALENDAR = 1;
    static final int TWITTER = 2;
    static final int TRANSLATION = 3;

    // Union of calendar and Twitter, only supports queries
    static final int EMERGENCYQUEST = 10;

    /**
     * Creates a UriMatcher for the content provider
     * @return UriMatcher with all the relevant tables
     */
    static UriMatcher uriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = KueContract.CONTENT_AUTHORITY;

        // Add all URIs for content
        uriMatcher.addURI(authority, KueContract.PATH_CALENDAR, CALENDAR);
        uriMatcher.addURI(authority, KueContract.PATH_TWITTER, TWITTER);
        uriMatcher.addURI(authority, KueContract.PATH_TRANSLATION, TRANSLATION);
        uriMatcher.addURI(authority, KueContract.PATH_EMERGENCYQUEST, EMERGENCYQUEST);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        // Initialise the database helper
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case CALENDAR:
                return KueContract.CalendarEntry.CONTENT_TYPE;
            case TWITTER:
                return KueContract.TwitterEntry.CONTENT_TYPE;
            case TRANSLATION:
                return KueContract.TranslationEntry.CONTENT_TYPE;
            case EMERGENCYQUEST:
                return KueContract.EmergencyQuest.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Error: Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Start by getting a readable database
        final SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();
        Cursor returnCursor;

        // Use the uriMatcher to match the URI's we are going to handle
        // If it doesn't match these, throw an UnsupportedOperationException
        switch(sUriMatcher.match(uri)) {
            case CALENDAR:
                returnCursor = sqLiteDatabase.query(
                        KueContract.CalendarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TWITTER:
                returnCursor = sqLiteDatabase.query(
                        KueContract.TwitterEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRANSLATION:
                returnCursor = sqLiteDatabase.query(
                        KueContract.TranslationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case EMERGENCYQUEST:
                // Union SELECT from both the calendar and Twitter tables
                String subQueryCalendar = SQLiteQueryBuilder.buildQueryString(true,
                        KueContract.CalendarEntry.TABLE_NAME, projection, selection, null, null,
                        null, null);
                String subQueryTwitter = SQLiteQueryBuilder.buildQueryString(true,
                        KueContract.TwitterEntry.TABLE_NAME, projection, selection, null, null,
                        null, null);
                String[] queryArray = {subQueryCalendar, subQueryTwitter};

                String unionQuery = new SQLiteQueryBuilder().buildUnionQuery(queryArray,
                        sortOrder, null);                returnCursor = sqLiteDatabase.rawQuery(unionQuery, null);
                break;
            default:
                throw new UnsupportedOperationException("Error: Unknown URI " + uri);
        }

        // Set the Uri to observe on the cursor for content observers
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor containing the results of the query
        return returnCursor;
    }



    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Start by getting a writable database
        final SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        Uri returnUri;
        long _id; // ID of the new database entry

        // Use the uriMatcher to match the URI's we are going to handle
        // If it doesn't match these, throw an UnsupportedOperationException
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch)
        {
            case CALENDAR:
                _id = sqLiteDatabase.insert(KueContract.CalendarEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = KueContract.CalendarEntry.buildCalendarUri(_id);
                else
                    throw new android.database.SQLException("Error: Failed to insert row into " + uri);
                break;
            case TWITTER:
                _id = sqLiteDatabase.insert(KueContract.TwitterEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = KueContract.TwitterEntry.buildTwitterUri(_id);
                else
                    throw new android.database.SQLException("Error: Failed to insert row into " + uri);
                break;
            case TRANSLATION:
                _id = sqLiteDatabase.insert(KueContract.TranslationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = KueContract.TranslationEntry.buildTranslationUri(_id);
                else
                    throw new android.database.SQLException("Error: Failed to insert row into " + uri);
                break;
            case EMERGENCYQUEST:
                throw new UnsupportedOperationException("Error: Insert is not support for URI " + uri);
            default:
                throw new UnsupportedOperationException("Error: Unknown URI " + uri);
        }

        // Notify the content observers
        getContext().getContentResolver().notifyChange(uri, null);

        // Also notify observers on the EMERGENCYQUEST Uri if the insertion was into
        // one of the unioned tables
        if (uriMatch == CALENDAR || uriMatch == TWITTER)
            getContext().getContentResolver().notifyChange(KueContract.EmergencyQuest.CONTENT_URI, null);

        // Return the Uri of the entry that was just inserted
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        int deleteCount; // Number of rows deleted

        // Use the uriMatcher to match the URI's we are going to handle
        // If it doesn't match these, throw an UnsupportedOperationException
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch)
        {
            case CALENDAR:
                deleteCount = sqLiteDatabase.delete(KueContract.CalendarEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TWITTER:
                deleteCount = sqLiteDatabase.delete(KueContract.TwitterEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TRANSLATION:
                deleteCount = sqLiteDatabase.delete(KueContract.TranslationEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case EMERGENCYQUEST:
                throw new UnsupportedOperationException("Error: Insert is not support for URI " + uri);
            default:
                throw new UnsupportedOperationException("Error: Unknown URI " + uri);
        }

        // If rows were deleted, notify the content observers
        if (deleteCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of rows deleted
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        int updateCount;

        // Use the uriMatcher to match the URI's we are going to handle
        // If it doesn't match these, throw an UnsupportedOperationException
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch)
        {
            case CALENDAR:
                updateCount = sqLiteDatabase.update(KueContract.CalendarEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case TWITTER:
                updateCount = sqLiteDatabase.update(KueContract.TwitterEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case TRANSLATION:
                updateCount = sqLiteDatabase.update(KueContract.TranslationEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case EMERGENCYQUEST:
                throw new UnsupportedOperationException("Error: Insert is not support for URI " + uri);
            default:
                throw new UnsupportedOperationException("Error: Unknown URI " + uri);
        }

        // If rows were updated, notify the content observers
        if (updateCount > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of rows updated
        return updateCount;
    }
}
