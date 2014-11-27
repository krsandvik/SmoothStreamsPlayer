package com.iosharp.android.ssplayer;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

import java.util.HashMap;

public class PlayerApplication extends Application {
    private static String APPLICATION_ID = "1586DC79";
    private static String PROPERTY_ID = "UA-57141244-1";
    private static VideoCastManager mCastMgr = null;

    public static VideoCastManager getCastManager(Context context) {
        if (!(Build.MODEL.contains("AFT") || Build.MANUFACTURER.equals("Amazon"))) {
            if (null == mCastMgr) {
                mCastMgr = VideoCastManager.initialize(context, APPLICATION_ID, null, null);
                mCastMgr.enableFeatures(
                        VideoCastManager.FEATURE_NOTIFICATION |
                                VideoCastManager.FEATURE_LOCKSCREEN |
                                VideoCastManager.FEATURE_WIFI_RECONNECT |
                                VideoCastManager.FEATURE_DEBUGGING);

            }
            mCastMgr.setContext(context);
            return mCastMgr;
        }
        return null;
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
    }
}
