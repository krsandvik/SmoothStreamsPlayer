package com.iosharp.android.ssplayer.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Date;

public class ChannelProvider extends ContentProvider {
    private static final String TAG = ChannelProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int EVENT = 100;
    private static final int EVENT_WITH_CHANNEL = 101;
    private static final int EVENT_WITH_CHANNEL_AND_DATE = 102;
    private static final int CHANNEL = 300;
    private static final int CHANNEL_ID = 301;

    private static final SQLiteQueryBuilder sEventByChannelIdQueryBuilder;
    static {
        sEventByChannelIdQueryBuilder = new SQLiteQueryBuilder();
        sEventByChannelIdQueryBuilder.setTables(
                ChannelContract.EventEntry.TABLE_NAME + " INNER JOIN " +
                        ChannelContract.ChannelEntry.TABLE_NAME +
                        " ON " + ChannelContract.EventEntry.TABLE_NAME +
                        "." + ChannelContract.EventEntry.COLUMN_KEY_CHANNEL +
                        " = " + ChannelContract.ChannelEntry.TABLE_NAME +
                        "." + ChannelContract.ChannelEntry._ID);

    }

    private static final String sChannelIdSelection =
            ChannelContract.ChannelEntry.TABLE_NAME +
                    "." + ChannelContract.ChannelEntry._ID + " = ? ";
    private static final String sChannelIdWithStartDateSelection =
            ChannelContract.ChannelEntry.TABLE_NAME +
                    "." + ChannelContract.ChannelEntry._ID + " = ? AND " +
                    ChannelContract.EventEntry.COLUMN_START_DATE + " >= ?";
    private static final String sChannelIdAndDaySelection =
            ChannelContract.ChannelEntry.TABLE_NAME +
                    "." + ChannelContract.ChannelEntry._ID + " = ? AND " +
                    ChannelContract.EventEntry.COLUMN_START_DATE + " = ?";

    private DbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ChannelContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ChannelContract.PATH_EVENT, EVENT);
        matcher.addURI(authority, ChannelContract.PATH_EVENT + "/*", EVENT_WITH_CHANNEL);
        matcher.addURI(authority, ChannelContract.PATH_EVENT + "/*/*", EVENT_WITH_CHANNEL_AND_DATE);

        matcher.addURI(authority, ChannelContract.PATH_CHANNEL, CHANNEL);
        matcher.addURI(authority, ChannelContract.PATH_CHANNEL + "/#", CHANNEL_ID);

        return matcher;
    }

    private Cursor getEventByChannelId(Uri uri, String[] projection, String sortOrder) {
        String channel = ChannelContract.EventEntry.getChannelFromUri(uri);
        String startDate = ChannelContract.EventEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sChannelIdSelection;
            selectionArgs = new String[]{channel};
        } else {
            selectionArgs = new String[]{channel, startDate};
            selection = sChannelIdWithStartDateSelection;
        }

        return sEventByChannelIdQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getEventByChannelIdAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String channel = ChannelContract.EventEntry.getChannelFromUri(uri);
        String date = ChannelContract.EventEntry.getDateFromUri(uri);

        return sEventByChannelIdQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sChannelIdAndDaySelection,
                new String[]{channel, date},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "event/*/*"
            case EVENT_WITH_CHANNEL_AND_DATE: {
                retCursor = getEventByChannelIdAndDate(uri, projection, sortOrder);
                break;
            }
            // "event/*"
            case EVENT_WITH_CHANNEL: {
                retCursor = getEventByChannelId(uri, projection, sortOrder);
                break;
            }
            // "event"
            case EVENT: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        ChannelContract.EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "channel/*"
            case CHANNEL_ID: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        ChannelContract.ChannelEntry.TABLE_NAME,
                        projection,
                        ChannelContract.ChannelEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "channel"
            case CHANNEL: {
//                retCursor = mDbHelper.getWritableDatabase().rawQuery(
//                        "SELECT * FROM (SELECT * FROM " + ChannelContract.ChannelEntry.TABLE_NAME +
//                        " LEFT OUTER JOIN " + ChannelContract.EventEntry.TABLE_NAME +
//                        " ON " + ChannelContract.EventEntry.TABLE_NAME +
//                        "." + ChannelContract.EventEntry.COLUMN_KEY_CHANNEL +
//                        " = " + ChannelContract.ChannelEntry.TABLE_NAME +
//                        "." + ChannelContract.ChannelEntry._ID +
//                        " ORDER BY " + ChannelContract.ChannelEntry.TABLE_NAME +
//                        "." + ChannelContract.ChannelEntry._ID +
//                        ", " + ChannelContract.EventEntry.COLUMN_START_DATE +
//                        ") AS t GROUP BY t."+ ChannelContract.ChannelEntry._ID
//                        ,null);

                String table = ChannelContract.ChannelEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        ChannelContract.EventEntry.TABLE_NAME +
                        " ON " + ChannelContract.EventEntry.TABLE_NAME +
                        "." + ChannelContract.EventEntry.COLUMN_KEY_CHANNEL +
                        " = " + ChannelContract.ChannelEntry.TABLE_NAME +
                        "." + ChannelContract.ChannelEntry._ID;

                String[] columns = new String[] {
                        ChannelContract.ChannelEntry.TABLE_NAME + "." + ChannelContract.ChannelEntry._ID,
                        ChannelContract.ChannelEntry.TABLE_NAME + "." + ChannelContract.ChannelEntry.COLUMN_NAME,
                        ChannelContract.ChannelEntry.TABLE_NAME + "." + ChannelContract.ChannelEntry.COLUMN_ICON,
//                        ChannelContract.ChannelEntry.TABLE_NAME + "." + ChannelContract.ChannelEntry.COLUMN_ICON,
                        ChannelContract.EventEntry.TABLE_NAME + "." + ChannelContract.EventEntry.COLUMN_NAME,
                        "MIN(" + ChannelContract.EventEntry.TABLE_NAME + "." + ChannelContract.EventEntry.COLUMN_START_DATE +")",
                        ChannelContract.EventEntry.TABLE_NAME + "." + ChannelContract.EventEntry.COLUMN_END_DATE,
                        ChannelContract.EventEntry.TABLE_NAME + "." + ChannelContract.EventEntry.COLUMN_QUALITY
                };

                retCursor = mDbHelper.getReadableDatabase().query(table,
                        columns,
                        null,
                        null,
                        ChannelContract.ChannelEntry.TABLE_NAME + "." + ChannelContract.ChannelEntry._ID,
                        null,
                        ChannelContract.ChannelEntry.TABLE_NAME + "." + ChannelContract.ChannelEntry._ID
                                + ", " + ChannelContract.EventEntry.TABLE_NAME + "." + ChannelContract.EventEntry.COLUMN_START_DATE
                        );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case EVENT_WITH_CHANNEL_AND_DATE:
                return ChannelContract.EventEntry.CONTENT_ITEM_TYPE;
            case EVENT_WITH_CHANNEL:
                return ChannelContract.EventEntry.CONTENT_TYPE;
            case EVENT:
                return ChannelContract.EventEntry.CONTENT_TYPE;
            case CHANNEL:
                return ChannelContract.ChannelEntry.CONTENT_TYPE;
            case CHANNEL_ID:
                return ChannelContract.ChannelEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case EVENT: {
                long _id = db.insertWithOnConflict(ChannelContract.EventEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = ChannelContract.EventEntry.buildEventUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CHANNEL: {
                long _id = db.insertWithOnConflict(ChannelContract.ChannelEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = ChannelContract.ChannelEntry.buildChannelUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case EVENT:
                rowsDeleted = db.delete(
                        ChannelContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHANNEL:
                rowsDeleted = db.delete(
                        ChannelContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case EVENT:
                rowsUpdated = db.update(ChannelContract.EventEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CHANNEL:
                rowsUpdated = db.update(ChannelContract.ChannelEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
