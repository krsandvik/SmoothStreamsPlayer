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

import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 20;
    private static final String[] CHANNEL_COLUMNS = {
            ChannelEntry._ID,
            ChannelEntry.COLUMN_NAME,
            ChannelEntry.COLUMN_ICON};
    private static final String[] EVENT_COLUMNS = {
            EventEntry._ID,
            EventEntry.COLUMN_KEY_CHANNEL,
            EventEntry.COLUMN_NETWORK,
            EventEntry.COLUMN_NAME,
            EventEntry.COLUMN_DESCRIPTION,
            EventEntry.COLUMN_START_DATE,
            EventEntry.COLUMN_END_DATE,
            EventEntry.COLUMN_RUNTIME,
            EventEntry.COLUMN_LANGUAGE,
            EventEntry.COLUMN_CATEGORY,
            EventEntry.COLUMN_QUALITY};
    private static String DATABASE_NAME = "smoothstreams.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + ChannelEntry.TABLE_NAME + " ( " +
                ChannelEntry._ID + " INTEGER PRIMARY KEY, " +
                ChannelEntry.COLUMN_NAME + " TEXT, " +
                ChannelEntry.COLUMN_ICON + " TEXT);";

        final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + " ( " +
                EventEntry._ID + " INTEGER PRIMARY KEY, " +
                EventEntry.COLUMN_KEY_CHANNEL + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_NETWORK + " TEXT, " +
                EventEntry.COLUMN_NAME + " TEXT, " +
                EventEntry.COLUMN_DESCRIPTION + " TEXT, " +
                EventEntry.COLUMN_START_DATE + " REAL, " +
                EventEntry.COLUMN_END_DATE + " REAL, " +
                EventEntry.COLUMN_RUNTIME + " REAL, " +
                EventEntry.COLUMN_LANGUAGE + " TEXT, " +
                EventEntry.COLUMN_CATEGORY + " TEXT, " +
                EventEntry.COLUMN_QUALITY + " TEXT, " +

                "FOREIGN KEY (" + EventEntry.COLUMN_KEY_CHANNEL + ") REFERENCES " +
                ChannelEntry.TABLE_NAME + " (" + ChannelEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_CHANNEL_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChannelEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

//    Channel CRUD

    public void addChannel(Channel channel) {
//        Log.d(TAG, "addChannel: " + channel.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChannelEntry._ID, channel.getId());
        values.put(ChannelEntry.COLUMN_NAME, channel.getName());
        values.put(ChannelEntry.COLUMN_ICON, channel.getIcon());

        db.insertWithOnConflict(ChannelEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE);

        db.close();
    }

    public Channel getChannel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ChannelEntry.TABLE_NAME,
                CHANNEL_COLUMNS,
                ChannelEntry._ID + " = ?",
                new String[]{String.valueOf(id)},
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

        return channel;
    }

    public List<Channel> getAllChannels() {
        List<Channel> channels = new LinkedList<Channel>();

        String query = "SELECT * FROM " + ChannelEntry.TABLE_NAME;

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

    public void deleteChannel(Channel channel) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ChannelEntry.TABLE_NAME,
                ChannelEntry._ID + " = ?",
                new String[]{String.valueOf(channel.getId())});

        db.close();

        Log.d(TAG, "deleteChannel: " + channel.toString());
    }

//    Event CRUD

    public void addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EventEntry._ID, event.getId());
        values.put(EventEntry.COLUMN_KEY_CHANNEL, event.getChannel());
        values.put(EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(EventEntry.COLUMN_NAME, event.getName());
        values.put(EventEntry.COLUMN_DESCRIPTION, event.getDescription());
        values.put(EventEntry.COLUMN_START_DATE, event.getStartDate());
        values.put(EventEntry.COLUMN_END_DATE, event.getEndDate());
        values.put(EventEntry.COLUMN_RUNTIME, event.getRuntime());
        values.put(EventEntry.COLUMN_LANGUAGE, event.getLanguage());
        values.put(EventEntry.COLUMN_NETWORK, event.getNetwork());
        values.put(EventEntry.COLUMN_QUALITY, event.getQuality());

        db.insertWithOnConflict(EventEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public Event getEvent(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(EventEntry.TABLE_NAME,
                EVENT_COLUMNS,
                EventEntry._ID + " = ? ",
                new String[]{String.valueOf(id)},
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

        String query = "SELECT * FROM " + EventEntry.TABLE_NAME + " WHERE " +
                EventEntry.COLUMN_KEY_CHANNEL + " = " + channelId + ";";

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

        Log.d(TAG, "getAllEvents(" + id + "): " + events.toString());

        return events;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new LinkedList<Event>();

        String query = "SELECT * FROM " + EventEntry.TABLE_NAME;

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


    public void deleteEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(EventEntry.TABLE_NAME,
                EventEntry._ID + " = ?",
                new String[]{String.valueOf(event.getId())});

        db.close();

        Log.d(TAG, "deleteEvent: " + event.toString());
    }
}
