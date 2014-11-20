package com.iosharp.android.ssplayer.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iosharp.android.ssplayer.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;

public class FetchChannelTask extends AsyncTask<Void, Void, String> {
    private final static String TAG = FetchChannelTask.class.getSimpleName();

    private Context mContext;

    public FetchChannelTask(Context context) {
        mContext = context;
    }

    private void getChannelDataFromJson(String channelJsonStr) throws JSONException {

        // Channel information
        final String TAG_CHANNEL_NAME = "name";
        final String TAG_CHANNEL_ICON = "img";

        // Event information. Each event is an element of the 'items' array
        final String TAG_CHANNEL_ITEMS = "items";

        final String TAG_EVENT_ID = "id";
        final String TAG_EVENT_NETWORK = "network";
        final String TAG_EVENT_NAME = "name";
        final String TAG_EVENT_DESCRIPTION = "description";
        final String TAG_EVENT_START_DATE = "time";
        final String TAG_EVENT_END_DATE = "end_time";
        final String TAG_EVENT_RUNTIME = "runtime";
        final String TAG_EVENT_CHANNEL = "channel";
        final String TAG_EVENT_LANGUAGE = "language";
        final String TAG_EVENT_QUALITY = "quality";

        JSONObject channelList = new JSONObject(channelJsonStr);

        for (int i = 1; i < channelList.length() + 1; i++) {
            // Channel starts at 1, not zero

            // For every channel..
            JSONObject c = channelList.getJSONObject(Integer.toString(i));

            int channelId = i;
            String channelName = c.getString(TAG_CHANNEL_NAME);
            String channelIcon = c.getString(TAG_CHANNEL_ICON);

            ContentValues channelValues = new ContentValues();
            channelValues.put(ChannelEntry._ID, channelId);
            channelValues.put(ChannelEntry.COLUMN_NAME, channelName);
            channelValues.put(ChannelEntry.COLUMN_ICON, channelIcon);

            mContext.getContentResolver().insert(ChannelEntry.CONTENT_URI, channelValues);

            // Get the channels' events, if any are there.
            if (c.has(TAG_CHANNEL_ITEMS)) {
                JSONArray items = c.getJSONArray(TAG_CHANNEL_ITEMS);

                for (int j = 0; j < items.length(); j++) {
                    JSONObject e = (JSONObject) items.get(j);

                    int eventId = Integer.parseInt(e.getString(TAG_EVENT_ID));
                    String eventNetwork = e.getString(TAG_EVENT_NETWORK);
                    String eventName = e.getString(TAG_EVENT_NAME);
                    String eventDescription = e.getString(TAG_EVENT_DESCRIPTION);
                    long eventStartDate = Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE));
                    long eventEndDate = Utils.convertDateToLong(e.getString(TAG_EVENT_END_DATE));
                    int eventRuntime = Integer.parseInt(e.getString(TAG_EVENT_RUNTIME));
                    int eventChannel = Integer.parseInt(e.getString(TAG_EVENT_CHANNEL));
                    String eventLanguage = e.getString(TAG_EVENT_LANGUAGE);
                    String eventQuality = e.getString(TAG_EVENT_QUALITY);

                    ContentValues eventValues = new ContentValues();
                    eventValues.put(EventEntry._ID, eventId);
                    eventValues.put(EventEntry.COLUMN_KEY_CHANNEL, eventChannel);
                    eventValues.put(EventEntry.COLUMN_NETWORK, eventNetwork);
                    eventValues.put(EventEntry.COLUMN_NAME, eventName);
                    eventValues.put(EventEntry.COLUMN_DESCRIPTION, eventDescription);
                    eventValues.put(EventEntry.COLUMN_START_DATE, eventStartDate);
                    eventValues.put(EventEntry.COLUMN_END_DATE, eventEndDate);
                    eventValues.put(EventEntry.COLUMN_RUNTIME, eventRuntime);
                    eventValues.put(EventEntry.COLUMN_LANGUAGE, eventLanguage);
                    eventValues.put(EventEntry.COLUMN_QUALITY, eventQuality);

                    mContext.getContentResolver().insert(EventEntry.CONTENT_URI, eventValues);
                }
            }
        }
        // Delete events that have already passed
        String now = Long.toString(new Date().getTime());
        mContext.getContentResolver().delete(EventEntry.CONTENT_URI, EventEntry.COLUMN_END_DATE + "< ?", new String[] {now});
    }


    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String channelsJsonStr = null;

        try {
            final String SMOOTHSTREAMS_JSON_FEED = "http://cdn.smoothstreams.tv/schedule/feed.json";

            URL url = new URL(SMOOTHSTREAMS_JSON_FEED);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // For easier debugging when printing to log.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            channelsJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getChannelDataFromJson(channelsJsonStr);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
