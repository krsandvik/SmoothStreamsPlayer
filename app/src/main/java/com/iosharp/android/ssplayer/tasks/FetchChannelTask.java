package com.iosharp.android.ssplayer.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.EventListFragment;
import com.iosharp.android.ssplayer.MainActivity;
import com.iosharp.android.ssplayer.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;

import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;
import static com.iosharp.android.ssplayer.db.ChannelContract.getDbDateString;

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
                    String cleanEventName = null;
                    JSONObject e = (JSONObject) items.get(j);

                    int eventId = Integer.parseInt(e.getString(TAG_EVENT_ID));
                    String eventNetwork = e.getString(TAG_EVENT_NETWORK);
                    String eventName = Utils.getCleanTitle(e.getString(TAG_EVENT_NAME));
                    String eventDescription = e.getString(TAG_EVENT_DESCRIPTION);
                    long eventStartDate = Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE));
                    long eventEndDate = Utils.convertDateToLong(e.getString(TAG_EVENT_END_DATE));
                    int eventRuntime = Integer.parseInt(e.getString(TAG_EVENT_RUNTIME));
                    int eventChannel = Integer.parseInt(e.getString(TAG_EVENT_CHANNEL));
                    String eventLanguage = e.getString(TAG_EVENT_LANGUAGE);
                    String eventQuality = e.getString(TAG_EVENT_QUALITY);
                    String eventDate = getDbDateString(Utils.convertDateToLong(e.getString(TAG_EVENT_START_DATE)));

                    // Handle umlaute http://hootcook.blogspot.de/2009/04/java-charset-encoding-utf-8.html
                    try {
                        byte[] bytes = eventName.getBytes("ISO-8859-1");
                        cleanEventName = new String(bytes, "UTF-8");
                    } catch (UnsupportedEncodingException e1) {
                        Crashlytics.logException(e1);
                        e1.printStackTrace();
                    }

                    ContentValues eventValues = new ContentValues();
                    eventValues.put(EventEntry._ID, eventId);
                    eventValues.put(EventEntry.COLUMN_KEY_CHANNEL, eventChannel);
                    eventValues.put(EventEntry.COLUMN_NETWORK, eventNetwork);
                    eventValues.put(EventEntry.COLUMN_NAME, cleanEventName);
                    eventValues.put(EventEntry.COLUMN_DESCRIPTION, eventDescription);
                    eventValues.put(EventEntry.COLUMN_START_DATE, eventStartDate);
                    eventValues.put(EventEntry.COLUMN_END_DATE, eventEndDate);
                    eventValues.put(EventEntry.COLUMN_RUNTIME, eventRuntime);
                    eventValues.put(EventEntry.COLUMN_LANGUAGE, eventLanguage);
                    eventValues.put(EventEntry.COLUMN_QUALITY, eventQuality);
                    eventValues.put(EventEntry.COLUMN_DATE, eventDate);

                    mContext.getContentResolver().insert(EventEntry.CONTENT_URI, eventValues);
                }
            }
        }
        // Delete events that have already passed
        String now = Long.toString(new Date().getTime());
        mContext.getContentResolver().delete(EventEntry.CONTENT_URI, EventEntry.COLUMN_END_DATE + "< ?", new String[] {now});
    }



    private String getDateFormatFromLong(long date) {
        Date day = new Date(date);
        day.setHours(0);
        day.setMinutes(0);
        day.setSeconds(0);
        day.getTime();

        return getDbDateString(day);
    }


    @Override
    protected String doInBackground(Void... voids) {
        final OkHttpClient client = new OkHttpClient();
        String channelsJsonStr = null;

        try {
            final String SMOOTHSTREAMS_JSON_FEED = "http://cdn.smoothstreams.tv/schedule/feed.json";

            URL url = new URL(SMOOTHSTREAMS_JSON_FEED);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", MainActivity.USER_AGENT)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            channelsJsonStr = response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            return null;
        }

        try {
            getChannelDataFromJson(channelsJsonStr);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // Refresh eventlist after we get the latest info
        EventListFragment.updateEvents(mContext);
    }
}
