package com.iosharp.android.ssplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class Utility {

    private boolean isDst() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    private Date adjustForDst(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    static Long convertDateToLong(String dateString) {
        SimpleDateFormat dateFormat;
        // For the start/end datetime
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
            // If we adjust justDate for DST, we could be an hour behind and the date is not correct.
//            if (isDst()) {
//                return adjustForDst(convertedDate);
//            }
            return convertedDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStreamUrl(Context c, int i) {
        // String format because the URL needs 01, 02, 03, etc when we have single digit integers
        String channel = String.format("%02d", i);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        String uid = sharedPreferences.getString(c.getString(R.string.pref_ss_uid_key), null);
        String password = sharedPreferences.getString(c.getString(R.string.pref_ss_password_key), null);
        String server = sharedPreferences.getString(c.getString(R.string.pref_server_key), null);
        String service = sharedPreferences.getString(c.getString(R.string.pref_service_key), null);
        boolean quality = sharedPreferences.getBoolean(c.getString(R.string.pref_quality_key), false);

        String streamQuality = null;

        if (quality) {
            streamQuality = "1";
        } else {
            streamQuality = "2";
        }

        String port = null;

        if (service.equals("live247")) {
            port = "12935";

        } else if (service.equals("mystreams")) {
            port = "29350";

        } else if (service.equals("starstreams")) {
            port = "3935";

        } else if (service.equals("mma-tv")) {
            port = "5540";
        }

        String SERVICE_URL_AND_PORT = server + ":" + port;
        String STREAM_CHANNEL_AND_QUALITY = String.format("ch%sq%s.stream", channel, streamQuality);
        String BASE_URL = "http://" + SERVICE_URL_AND_PORT + "/view/" + STREAM_CHANNEL_AND_QUALITY + "/playlist.m3u8";
        String UID_PARAM = "u";
        String PASSWORD_PARAM = "p";

        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(UID_PARAM, uid)
                .appendQueryParameter(PASSWORD_PARAM, password)
                .build();

        return uri.toString();
    }
}
