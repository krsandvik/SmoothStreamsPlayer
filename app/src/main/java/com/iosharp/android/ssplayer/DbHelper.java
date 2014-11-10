package com.iosharp.android.ssplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 4;
    private static String DATABASE_NAME = "smoothstreams.db";

    private static final String[] CHANNEL_COLUMNS = {DbContract.ChannelEntry._ID,
            DbContract.ChannelEntry.COLUMN_NAME, DbContract.ChannelEntry.COLUMN_NAME};

    private static final String[] EVENT_COLUMNS = {DbContract.EventEntry._ID, DbContract.EventEntry.COLUMN_KEY_CHANNEL,
            DbContract.EventEntry.COLUMN_NETWORK, DbContract.EventEntry.COLUMN_NAME, DbContract.EventEntry.COLUMN_DESCRIPTION,
            DbContract.EventEntry.COLUMN_START_DATE, DbContract.EventEntry.COLUMN_END_DATE,
            DbContract.EventEntry.COLUMN_RUNTIME, DbContract.EventEntry.COLUMN_LANGUAGE, DbContract.EventEntry.COLUMN_CATEGORY,
            DbContract.EventEntry.COLUMN_QUALITY};

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + DbContract.ChannelEntry.TABLE_NAME + " ( " +
                DbContract.ChannelEntry._ID + " INTEGER PRIMARY KEY, " +
                DbContract.ChannelEntry.COLUMN_NAME + " TEXT, " +
                DbContract.ChannelEntry.COLUMN_ICON + " TEXT);";

        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + DbContract.EventEntry.TABLE_NAME + " ( " +
                DbContract.EventEntry._ID + " INTEGER PRIMARY KEY, " +
                DbContract.EventEntry.COLUMN_KEY_CHANNEL + " INTEGER NOT NULL, " +
                DbContract.EventEntry.COLUMN_NETWORK + " TEXT, " +
                DbContract.EventEntry.COLUMN_NAME + " TEXT, " +
                DbContract.EventEntry.COLUMN_DESCRIPTION + " TEXT, " +
                DbContract.EventEntry.COLUMN_START_DATE + " REAL, " +
                DbContract.EventEntry.COLUMN_END_DATE + " REAL, " +
                DbContract.EventEntry.COLUMN_RUNTIME + " REAL, " +
                DbContract.EventEntry.COLUMN_LANGUAGE + " TEXT, " +
                DbContract.EventEntry.COLUMN_CATEGORY + " TEXT, " +
                DbContract.EventEntry.COLUMN_QUALITY + " TEXT, " +

                "FOREIGN KEY (" + DbContract.EventEntry.COLUMN_KEY_CHANNEL + ") REFERENCES " +
                DbContract.ChannelEntry.TABLE_NAME + " (" + DbContract.ChannelEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_CHANNEL_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DbContract.ChannelEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DbContract.EventEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

//    Channel CRUD

    public void addChannel(Channel channel) {
        Log.d(TAG, "addChannel: " + channel.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.ChannelEntry._ID, channel.getId());
        values.put(DbContract.ChannelEntry.COLUMN_NAME, channel.getName());
        values.put(DbContract.ChannelEntry.COLUMN_ICON, channel.getIcon());

        db.insertWithOnConflict(DbContract.ChannelEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE);

        db.close();
    }

    public Channel getChannel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DbContract.ChannelEntry.TABLE_NAME,
                CHANNEL_COLUMNS,
                " id = ? ",
                new String[] { String.valueOf(id) },
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Channel channel = new Channel();
        channel.setId(Integer.parseInt(cursor.getString(0)));
        channel.setName(cursor.getString(1));
        channel.setIcon(cursor.getString(2));

        Log.d(TAG, "getChannel(" + id +"): " + channel.toString());

        return channel;
    }

    public List<Channel> getAllChannels() {
        List<Channel> channels = new LinkedList<Channel>();

        String query = "SELECT * FROM " + DbContract.ChannelEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Channel channel = null;
        if (cursor.moveToFirst()) {
            do {
                channel = new Channel();
                channel.setId(Integer.parseInt(cursor.getString(0)));
                channel.setName(cursor.getString(1));
                channel.setIcon(cursor.getString(2));

                channels.add(channel);
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "getAllChannels(): " + channels.toString());

        return channels;
    }

    public int updateChannel(Channel channel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.ChannelEntry.COLUMN_NAME, channel.getName());
        values.put(DbContract.ChannelEntry.COLUMN_ICON, channel.getIcon());

        int i = db.update(DbContract.ChannelEntry.TABLE_NAME,
                values,
                DbContract.ChannelEntry._ID + " = ?",
                new String[] {String.valueOf(channel.getId()) });

        db.close();

        return i;
    }

    public void deleteChannel(Channel channel) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DbContract.ChannelEntry.TABLE_NAME,
                DbContract.ChannelEntry._ID + " = ?",
                new String[] { String.valueOf(channel.getId()) });

        db.close();

        Log.d(TAG, "deleteChannel: " + channel.toString());
    }

//    Event CRUD

    public void addEvent(Event event) {
        Log.d(TAG, "addEvent: " + event.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.EventEntry._ID, event.getId());
        values.put(DbContract.EventEntry.COLUMN_KEY_CHANNEL, event.getChannel());
        values.put(DbContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(DbContract.EventEntry.COLUMN_NAME, event.getName());
        values.put(DbContract.EventEntry.COLUMN_DESCRIPTION, event.getDescription());
        values.put(DbContract.EventEntry.COLUMN_START_DATE, event.getStartDate());
        values.put(DbContract.EventEntry.COLUMN_END_DATE, event.getEndDate());
        values.put(DbContract.EventEntry.COLUMN_RUNTIME, event.getRuntime());
        values.put(DbContract.EventEntry.COLUMN_LANGUAGE, event.getLanguage());
        values.put(DbContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(DbContract.EventEntry.COLUMN_QUALITY, event.getQuality());

        db.insertWithOnConflict(DbContract.EventEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public Event getEvent(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DbContract.EventEntry.TABLE_NAME,
                EVENT_COLUMNS,
                " id = ? ",
                new String[] { String.valueOf(id) },
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Event event = new Event();

        event.setId(Integer.parseInt(cursor.getString(0)));
        event.setChannel(Integer.parseInt(cursor.getString(1)));
        event.setNetwork(cursor.getString(2));
        event.setName(cursor.getString(3));
        event.setDescription(cursor.getString(4));
        event.setStartDate(Double.valueOf(cursor.getString(5)).longValue());
        event.setEndDate(Double.valueOf(cursor.getString(6)).longValue());
        event.setRuntime(Double.valueOf(cursor.getString(7)).longValue());
        event.setLanguage(cursor.getString(8));
        event.setNetwork(cursor.getString(9));
        event.setQuality(cursor.getString(10));

        Log.d(TAG, "getEvent(" + id + "): " + event.toString());

        return event;
    }

    public List<Event> getAllEvents(int id) {
        List<Event> events = new LinkedList<Event>();

        String channelId = String.valueOf(id);

        String query = "SELECT * FROM " + DbContract.EventEntry.TABLE_NAME + " WHERE " +
                DbContract.EventEntry.COLUMN_KEY_CHANNEL + " = " + channelId + ";";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Event event = null;
        if (cursor.moveToFirst()) {
            do {
                event = new Event();

                event.setId(Integer.parseInt(cursor.getString(0)));
                event.setChannel(Integer.parseInt(cursor.getString(1)));
                event.setNetwork(cursor.getString(2));
                event.setName(cursor.getString(3));
                event.setDescription(cursor.getString(4));
                event.setStartDate(Double.valueOf(cursor.getString(5)).longValue());
                event.setEndDate(Double.valueOf(cursor.getString(6)).longValue());
                event.setRuntime(Double.valueOf(cursor.getString(7)).longValue());
                event.setLanguage(cursor.getString(8));
                event.setNetwork(cursor.getString(9));
                event.setQuality(cursor.getString(10));

                events.add(event);
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "getAllEvents(" + id+ "): " + events.toString());

        return events;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new LinkedList<Event>();

        String query = "SELECT * FROM " + DbContract.EventEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Event event = null;
        if (cursor.moveToFirst()) {
            do {
                event = new Event();

                event.setId(Integer.parseInt(cursor.getString(0)));
                event.setChannel(Integer.parseInt(cursor.getString(1)));
                event.setNetwork(cursor.getString(2));
                event.setName(cursor.getString(3));
                event.setDescription(cursor.getString(4));
                event.setStartDate(Double.valueOf(cursor.getString(5)).longValue());
                event.setEndDate(Double.valueOf(cursor.getString(6)).longValue());
                event.setRuntime(Double.valueOf(cursor.getString(7)).longValue());
                event.setLanguage(cursor.getString(8));
                event.setNetwork(cursor.getString(9));
                event.setQuality(cursor.getString(10));

                events.add(event);
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "getAllEvents(): " + events.toString());

        return events;
    }

    public int updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.EventEntry._ID, event.getId());
        values.put(DbContract.EventEntry.COLUMN_KEY_CHANNEL, event.getChannel());
        values.put(DbContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(DbContract.EventEntry.COLUMN_NAME, event.getName());
        values.put(DbContract.EventEntry.COLUMN_DESCRIPTION, event.getDescription());
        values.put(DbContract.EventEntry.COLUMN_START_DATE, event.getStartDate());
        values.put(DbContract.EventEntry.COLUMN_END_DATE, event.getEndDate());
        values.put(DbContract.EventEntry.COLUMN_RUNTIME, event.getRuntime());
        values.put(DbContract.EventEntry.COLUMN_LANGUAGE, event.getLanguage());
        values.put(DbContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(DbContract.EventEntry.COLUMN_QUALITY, event.getQuality());

        int i = db.update(DbContract.EventEntry.TABLE_NAME,
                values,
                DbContract.EventEntry._ID + " = ?",
                new String[] {String.valueOf(event.getId()) });

        db.close();

        return i;
    }

    public void deleteEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DbContract.EventEntry.TABLE_NAME,
                DbContract.EventEntry._ID + " = ?",
                new String[] { String.valueOf(event.getId()) });

        db.close();

        Log.d(TAG, "deleteEvent: " + event.toString());
    }
}
