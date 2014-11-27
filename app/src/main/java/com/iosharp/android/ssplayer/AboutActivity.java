package com.iosharp.android.ssplayer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AboutFragment())
                    .commit();
        }
    }

    public static class AboutFragment extends Fragment {

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);

            Tracker t = ((PlayerApplication) getActivity().getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
            t.setScreenName("About");
            t.send(new HitBuilders.ScreenViewBuilder().build());

            ((TextView) rootView.findViewById(R.id.about_app_version)).setText(getVersionInfo());
            ((TextView) rootView.findViewById(R.id.about_body)).setText(getString(R.string.about_body));

            return rootView;
        }

        public String getVersionInfo() {
            String strVersion = "v";

            PackageInfo packageInfo;
            try {
                packageInfo = getActivity().getPackageManager().getPackageInfo(
                        getActivity().getPackageName(), 0);
                strVersion += packageInfo.versionName;

            } catch (PackageManager.NameNotFoundException e) {
                strVersion += "Unknown";
            }

            return strVersion;
        }

    }


}
