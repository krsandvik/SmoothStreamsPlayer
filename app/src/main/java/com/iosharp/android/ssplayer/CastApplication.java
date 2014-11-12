package com.iosharp.android.ssplayer;

import android.app.Application;
import android.content.Context;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

public class CastApplication extends Application {
    private static String APPLICATION_ID = "1586DC79";
    private static VideoCastManager mCastMgr = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static VideoCastManager getCastManager(Context context) {
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
}
