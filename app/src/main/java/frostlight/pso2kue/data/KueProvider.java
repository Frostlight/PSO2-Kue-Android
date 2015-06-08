package frostlight.pso2kue.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Vincent on 6/7/2015.
 */
public class KueProvider extends ContentProvider {

    // Matches URIs for content provider requests
    private static final UriMatcher sUriMatcher = uriMatcher();

    // Database helper object
    private KueHelper mDbHelper;

    // IDs for URI types
    static final int CALENDAR = 1;
    static final int TWITTER = 2;
    static final int TRANSLATION = 3;

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

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new KueHelper(getContext());
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
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
