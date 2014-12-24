package com.iosharp.android.ssplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class StreamUrl {
    public static final int HTML5 = 0;
    public static final int RTMP = 1;
    public static final int RTSP = 2;



    public static String getUrl(Context context, int channel, int protocol) {
        // String format because the URL needs 01, 02, 03, etc when we have single digit integers
        String channelId = String.format("%02d", channel);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uid = sharedPreferences.getString(context.getString(R.string.pref_ss_uid_key), null);
        String password = sharedPreferences.getString(context.getString(R.string.pref_ss_password_key), null);
        String server = sharedPreferences.getString(context.getString(R.string.pref_server_key), null);
        String service = sharedPreferences.getString(context.getString(R.string.pref_service_key), null);
        boolean quality = sharedPreferences.getBoolean(context.getString(R.string.pref_quality_key), false);

        String streamQuality = null;

        if (quality) {
            streamQuality = "1";
        } else {
            streamQuality = "2";
        }

        String port = null;

        if (protocol == 0) {
            if (service.equals("live247")) {
                port = "12935";

            } else if (service.equals("mystreams")) {
                port = "29355";

            } else if (service.equals("starstreams")) {
                port = "39355";

            } else if (service.equals("mma-tv")) {
                port = "5545";
            }
        } else {
            if (service.equals("live247")) {
                port = "2935";

            } else if (service.equals("mystreams")) {
                port = "29350";

            } else if (service.equals("starstreams")) {
                port = "3935";

            } else if (service.equals("mma-tv")) {
                port = "5540";
            }
        }

        String SERVICE_URL_AND_PORT = server + ":" + port;
        String STREAM_CHANNEL_AND_QUALITY;
        String BASE_URL;
        String UID_PARAM = "u";
        String PASSWORD_PARAM = "p";
        Uri uri = null;

        switch (protocol) {
            case StreamUrl.HTML5:
                STREAM_CHANNEL_AND_QUALITY = String.format("ch%sq%s.stream", channelId, streamQuality);
                BASE_URL = "http://" + SERVICE_URL_AND_PORT + "/view/" + STREAM_CHANNEL_AND_QUALITY + "/playlist.m3u8";

                uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(UID_PARAM, uid)
                        .appendQueryParameter(PASSWORD_PARAM, password)
                        .build();
                break;
            case StreamUrl.RTMP:
                STREAM_CHANNEL_AND_QUALITY = String.format("ch%sq%s.stream", channelId, streamQuality);
                BASE_URL = "rtmp://" + SERVICE_URL_AND_PORT + "/view/" + STREAM_CHANNEL_AND_QUALITY;

                uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(UID_PARAM, uid)
                        .appendQueryParameter(PASSWORD_PARAM, password)
                        .build();
                break;
            case StreamUrl.RTSP:
                STREAM_CHANNEL_AND_QUALITY = String.format("ch%sq%s.stream", channelId, streamQuality);
                BASE_URL = "rtsp://" + SERVICE_URL_AND_PORT + "/view/" + STREAM_CHANNEL_AND_QUALITY;


                uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(UID_PARAM, uid)
                        .appendQueryParameter(PASSWORD_PARAM, password)
                        .build();
                break;
            default:
                throw new UnsupportedOperationException("Unknown protocol: " + protocol);
        }

        String url = null;

        // In case password has special characters
        try {
            url = URLDecoder.decode(uri.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }

        return url.toString();
    }
}
