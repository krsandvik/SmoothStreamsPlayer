package com.iosharp.android.ssplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class Utils {

    public static Long convertDateToLong(String dateString) {
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

    public static boolean checkForSetServiceCredentials(Context c) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        String uid = sharedPreferences.getString(c.getString(R.string.pref_ss_uid_key), null);
        String password = sharedPreferences.getString(c.getString(R.string.pref_ss_password_key), null);

        if (uid != null && password != null) {
            return true;
        }

        return false;
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

    public static MediaInfo buildMediaInfo(String channel, String studio, String url, String iconUrl) {
        final String SMOOTHSTREAMS_ICON_PREFIX = "http://smoothstreams.tv/schedule/includes/images/uploads/";
        final String SMOOTHSTREAMS_LOGO =
                "https://pbs.twimg.com/profile_images/378800000147953484/7af5bfc30ff182f852da32be5af79dfd.jpeg";
        final String CONTENT_TYPE = "application/x-mpegurl";

//        Debug only
//        url = "http://www.corsproxy.com/devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, channel);
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        mediaMetadata.addImage(new WebImage(Uri.parse(SMOOTHSTREAMS_ICON_PREFIX + iconUrl)));
        mediaMetadata.addImage(new WebImage(Uri.parse(SMOOTHSTREAMS_LOGO)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(CONTENT_TYPE)
                .setMetadata(mediaMetadata)
                .build();
    }

    private boolean isDst() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    private Date adjustForDst(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    public static SpannableString getHighDefBadge() {
        String highDefStr = " HD";
        final StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);
        SpannableString highDefSpannableString = new SpannableString(highDefStr);

        // Starting at one to not bold the space
        highDefSpannableString.setSpan(boldStyleSpan, 1, highDefStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return highDefSpannableString;
    }

    public static SpannableString getLanguageImg(Context context, String language) {
        int flag = R.drawable.gb;
        String lng = language.toLowerCase();

        if (lng.equals("eng")) {
            flag = R.drawable.gb;
        } else if (lng.equals("nl")) {
            flag = R.drawable.nl;
        } else if (lng.equals("es")) {
            flag = R.drawable.es;
        } else if (lng.equals("fr")) {
            flag = R.drawable.fr;
        } else if (lng.equals("de")) {
            flag = R.drawable.de;
        }

        String languageStr = " f";
        ImageSpan imageSpan = new ImageSpan(context, flag, DynamicDrawableSpan.ALIGN_BASELINE);
        SpannableString languageSpannableString = new SpannableString(languageStr);

        // Starting at one to not italize the space
        languageSpannableString.setSpan(imageSpan, 1, languageStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return languageSpannableString;
    }

    public static String getCleanTitle(String title) {
        String cleanTitle = title.replace("&amp;", "&");
        return cleanTitle;
    }
}
