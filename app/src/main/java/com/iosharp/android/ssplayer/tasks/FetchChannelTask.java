package com.iosharp.android.ssplayer.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iosharp.android.ssplayer.Utils;
import com.iosharp.android.ssplayer.db.DbHelper;
import com.iosharp.android.ssplayer.model.Channel;
import com.iosharp.android.ssplayer.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchChannelTask extends AsyncTask<Void, Void, String> {
    private final static String TAG = FetchChannelTask.class.getSimpleName();

    private Context mContext;
    private DbHelper mDatabase;

    public FetchChannelTask(Context context) {
        mContext = context;
        mDatabase = new DbHelper(mContext);
    }

    private void getChannelDataFromJson(String channelJsonStr) throws JSONException {

        // Channel information
        final String TAG_CHANNEL_NAME = "name";
        final String TAG_CHANNEL_ICON = "img";

        // Event information. Each event is an element of the 'items' array
        final String TAG_CHANNEL_ITEMS = "items";

        final String TAG_PROGRAMME_ID = "id";
        final String TAG_PROGRAMME_NETWORK = "network";
        final String TAG_PROGRAMME_NAME = "name";
        final String TAG_PROGRAMME_DESCRIPTION = "description";
        final String TAG_PROGRAMME_START_DATE = "time";
        final String TAG_PROGRAMME_END_DATE = "end_time";
        final String TAG_PROGRAMME_RUNTIME = "runtime";
        final String TAG_PROGRAMME_CHANNEL = "channel";
        final String TAG_PROGRAMME_LANGUAGE = "language";
        final String TAG_PROGRAMME_QUALITY = "quality";

        // First purge database of all stale events

        JSONObject channelList = new JSONObject(channelJsonStr);

        for (int i = 1; i < channelList.length() + 1; i++) {
            // Channel starts at 1, not zero

            // For every channel..
            JSONObject c = channelList.getJSONObject(Integer.toString(i));

            // Create a new channel
            Channel channel = new Channel();

            // Set ID, name and Icon..
            channel.setId(i);
            channel.setName(c.getString(TAG_CHANNEL_NAME));
            channel.setIcon(c.getString(TAG_CHANNEL_ICON));

            mDatabase.addChannel(channel);

            // Get the channels' events, if any are there.
            if (c.has(TAG_CHANNEL_ITEMS)) {
                JSONArray items = c.getJSONArray(TAG_CHANNEL_ITEMS);

                for (int j = 0; j < items.length(); j++) {
                    JSONObject e = (JSONObject) items.get(j);
                    Event event = new Event();
                    event.setId(Integer.parseInt(e.getString(TAG_PROGRAMME_ID)));
                    event.setNetwork(e.getString(TAG_PROGRAMME_NETWORK));
                    event.setName(e.getString(TAG_PROGRAMME_NAME));
                    event.setDescription(e.getString(TAG_PROGRAMME_DESCRIPTION));
                    event.setStartDate(Utils.convertDateToLong(e.getString(TAG_PROGRAMME_START_DATE)));
                    event.setEndDate(Utils.convertDateToLong(e.getString(TAG_PROGRAMME_END_DATE)));
                    event.setRuntime(Integer.parseInt(e.getString(TAG_PROGRAMME_RUNTIME)));
                    event.setChannel(Integer.parseInt(e.getString(TAG_PROGRAMME_CHANNEL)));
                    event.setLanguage(e.getString(TAG_PROGRAMME_LANGUAGE));
                    event.setQuality(e.getString(TAG_PROGRAMME_QUALITY));

                    mDatabase.addEvent(event);
                }
            }
        }
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
