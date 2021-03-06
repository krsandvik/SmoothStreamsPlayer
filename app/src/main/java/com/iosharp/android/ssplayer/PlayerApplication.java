package com.iosharp.android.ssplayer;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import java.util.HashMap;

public class PlayerApplication extends Application {
    private static String sApplicationId;
    private static String PROPERTY_ID = "UA-57141244-1";
    private static VideoCastManager sCastMgr = null;

    private void initializeCastManager() {
        if (!(Build.MODEL.contains("AFT") || Build.MANUFACTURER.equals("Amazon"))) {
            sCastMgr = VideoCastManager.initialize(getApplicationContext(), sApplicationId, null, null);
        }

        if (sCastMgr != null ) {
                sCastMgr.enableFeatures(
                        VideoCastManager.FEATURE_NOTIFICATION |
                                VideoCastManager.FEATURE_LOCKSCREEN |
                                VideoCastManager.FEATURE_WIFI_RECONNECT |
                                VideoCastManager.FEATURE_DEBUGGING);
        }
    }

    public static VideoCastManager getCastManager() {
        if (sCastMgr == null) {
            throw new IllegalStateException("Application has not been started");
        }
        return sCastMgr;
    }

    public static String getUserAgent(Context context) {
        return "SmoothStreamsPlayer " + getVersion(context);
    }

    public static String getVersion(Context context) {
        String strVersion = "v";

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            strVersion += packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
            strVersion += "Unknown";
        }

        return strVersion;
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationId = getString(R.string.chromecast_app_id);
        initializeCastManager();
    }
}
