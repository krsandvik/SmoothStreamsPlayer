package com.iosharp.android.ssplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.iosharp.android.ssplayer.model.Channel;
import com.iosharp.android.ssplayer.model.Event;

import java.util.LinkedList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 4;
    private static String DATABASE_NAME = "smoothstreams.db";

    private static final String[] CHANNEL_COLUMNS = {ChannelsContract.ChannelEntry._ID,
            ChannelsContract.ChannelEntry.COLUMN_NAME, ChannelsContract.ChannelEntry.COLUMN_NAME};

    private static final String[] EVENT_COLUMNS = {ChannelsContract.EventEntry._ID, ChannelsContract.EventEntry.COLUMN_KEY_CHANNEL,
            ChannelsContract.EventEntry.COLUMN_NETWORK, ChannelsContract.EventEntry.COLUMN_NAME, ChannelsContract.EventEntry.COLUMN_DESCRIPTION,
            ChannelsContract.EventEntry.COLUMN_START_DATE, ChannelsContract.EventEntry.COLUMN_END_DATE,
            ChannelsContract.EventEntry.COLUMN_RUNTIME, ChannelsContract.EventEntry.COLUMN_LANGUAGE, ChannelsContract.EventEntry.COLUMN_CATEGORY,
            ChannelsContract.EventEntry.COLUMN_QUALITY};

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + ChannelsContract.ChannelEntry.TABLE_NAME + " ( " +
                ChannelsContract.ChannelEntry._ID + " INTEGER PRIMARY KEY, " +
                ChannelsContract.ChannelEntry.COLUMN_NAME + " TEXT, " +
                ChannelsContract.ChannelEntry.COLUMN_ICON + " TEXT);";

        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + ChannelsContract.EventEntry.TABLE_NAME + " ( " +
                ChannelsContract.EventEntry._ID + " INTEGER PRIMARY KEY, " +
                ChannelsContract.EventEntry.COLUMN_KEY_CHANNEL + " INTEGER NOT NULL, " +
                ChannelsContract.EventEntry.COLUMN_NETWORK + " TEXT, " +
                ChannelsContract.EventEntry.COLUMN_NAME + " TEXT, " +
                ChannelsContract.EventEntry.COLUMN_DESCRIPTION + " TEXT, " +
                ChannelsContract.EventEntry.COLUMN_START_DATE + " REAL, " +
                ChannelsContract.EventEntry.COLUMN_END_DATE + " REAL, " +
                ChannelsContract.EventEntry.COLUMN_RUNTIME + " REAL, " +
                ChannelsContract.EventEntry.COLUMN_LANGUAGE + " TEXT, " +
                ChannelsContract.EventEntry.COLUMN_CATEGORY + " TEXT, " +
                ChannelsContract.EventEntry.COLUMN_QUALITY + " TEXT, " +

                "FOREIGN KEY (" + ChannelsContract.EventEntry.COLUMN_KEY_CHANNEL + ") REFERENCES " +
                ChannelsContract.ChannelEntry.TABLE_NAME + " (" + ChannelsContract.ChannelEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_CHANNEL_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChannelsContract.ChannelEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChannelsContract.EventEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

//    Channel CRUD

    public void addChannel(Channel channel) {
//        Log.d(TAG, "addChannel: " + channel.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChannelsContract.ChannelEntry._ID, channel.getId());
        values.put(ChannelsContract.ChannelEntry.COLUMN_NAME, channel.getName());
        values.put(ChannelsContract.ChannelEntry.COLUMN_ICON, channel.getIcon());

        db.insertWithOnConflict(ChannelsContract.ChannelEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE);

        db.close();
    }

    public Channel getChannel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ChannelsContract.ChannelEntry.TABLE_NAME,
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

        String query = "SELECT * FROM " + ChannelsContract.ChannelEntry.TABLE_NAME;

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
        values.put(ChannelsContract.ChannelEntry.COLUMN_NAME, channel.getName());
        values.put(ChannelsContract.ChannelEntry.COLUMN_ICON, channel.getIcon());

        int i = db.update(ChannelsContract.ChannelEntry.TABLE_NAME,
                values,
                ChannelsContract.ChannelEntry._ID + " = ?",
                new String[] {String.valueOf(channel.getId()) });

        db.close();

        return i;
    }

    public void deleteChannel(Channel channel) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ChannelsContract.ChannelEntry.TABLE_NAME,
                ChannelsContract.ChannelEntry._ID + " = ?",
                new String[] { String.valueOf(channel.getId()) });

        db.close();

        Log.d(TAG, "deleteChannel: " + channel.toString());
    }

//    Event CRUD

    public void addEvent(Event event) {
//        Log.d(TAG, "addEvent: " + event.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChannelsContract.EventEntry._ID, event.getId());
        values.put(ChannelsContract.EventEntry.COLUMN_KEY_CHANNEL, event.getChannel());
        values.put(ChannelsContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(ChannelsContract.EventEntry.COLUMN_NAME, event.getName());
        values.put(ChannelsContract.EventEntry.COLUMN_DESCRIPTION, event.getDescription());
        values.put(ChannelsContract.EventEntry.COLUMN_START_DATE, event.getStartDate());
        values.put(ChannelsContract.EventEntry.COLUMN_END_DATE, event.getEndDate());
        values.put(ChannelsContract.EventEntry.COLUMN_RUNTIME, event.getRuntime());
        values.put(ChannelsContract.EventEntry.COLUMN_LANGUAGE, event.getLanguage());
        values.put(ChannelsContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(ChannelsContract.EventEntry.COLUMN_QUALITY, event.getQuality());

        db.insertWithOnConflict(ChannelsContract.EventEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public Event getEvent(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ChannelsContract.EventEntry.TABLE_NAME,
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

        String query = "SELECT * FROM " + ChannelsContract.EventEntry.TABLE_NAME + " WHERE " +
                ChannelsContract.EventEntry.COLUMN_KEY_CHANNEL + " = " + channelId + ";";

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

        String query = "SELECT * FROM " + ChannelsContract.EventEntry.TABLE_NAME;

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
        values.put(ChannelsContract.EventEntry._ID, event.getId());
        values.put(ChannelsContract.EventEntry.COLUMN_KEY_CHANNEL, event.getChannel());
        values.put(ChannelsContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(ChannelsContract.EventEntry.COLUMN_NAME, event.getName());
        values.put(ChannelsContract.EventEntry.COLUMN_DESCRIPTION, event.getDescription());
        values.put(ChannelsContract.EventEntry.COLUMN_START_DATE, event.getStartDate());
        values.put(ChannelsContract.EventEntry.COLUMN_END_DATE, event.getEndDate());
        values.put(ChannelsContract.EventEntry.COLUMN_RUNTIME, event.getRuntime());
        values.put(ChannelsContract.EventEntry.COLUMN_LANGUAGE, event.getLanguage());
        values.put(ChannelsContract.EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(ChannelsContract.EventEntry.COLUMN_QUALITY, event.getQuality());

        int i = db.update(ChannelsContract.EventEntry.TABLE_NAME,
                values,
                ChannelsContract.EventEntry._ID + " = ?",
                new String[] {String.valueOf(event.getId()) });

        db.close();

        return i;
    }

    public void deleteEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ChannelsContract.EventEntry.TABLE_NAME,
                ChannelsContract.EventEntry._ID + " = ?",
                new String[] { String.valueOf(event.getId()) });

        db.close();

        Log.d(TAG, "deleteEvent: " + event.toString());
    }
}
