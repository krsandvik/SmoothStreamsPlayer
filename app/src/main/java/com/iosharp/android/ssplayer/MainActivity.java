package com.iosharp.android.ssplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.iosharp.android.ssplayer.service.SmoothService;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity {
    public static final String USER_AGENT = "SmoothStreamsPlayer";

    private VideoCastManager mCastManager;
    private Tracker mTracker;

    final String[] TAB_TITLES = {"Channels",
            "Events"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        if (!(Build.MODEL.contains("AFT") || Build.MANUFACTURER.equals("Amazon"))) {
            VideoCastManager.checkGooglePlayServices(this);
        }

        googleAnalytics();
        setupActionBar();
        setupTabs();

        mCastManager = PlayerApplication.getCastManager(this);
        if (mCastManager != null) {
            mCastManager.reconnectSessionIfPossible(this, false);

        }

        setAlarm();
    }

    private void setAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, SmoothService.AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
    }

    private void googleAnalytics() {
        mTracker = ((PlayerApplication)getApplication()).getTracker(
                PlayerApplication.TrackerName.APP_TRACKER);

        mTracker.setScreenName(TAB_TITLES[0]);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagertabstrip);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.SteelBlue));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTracker = ((PlayerApplication) getApplication()).getTracker(
                        PlayerApplication.TrackerName.APP_TRACKER);

                mTracker.setScreenName(TAB_TITLES[position]);
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                GoogleAnalytics.getInstance(getApplicationContext()).dispatchLocalHits();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void getChannels() {
        if (Utils.isInternetAvailable(this)) {
            Intent intent = new Intent(this, SmoothService.class);
            this.startService(intent);
        }
    }

    @Override
    protected void onResume() {
        getChannels();

        if (mCastManager != null) {
            mCastManager.incrementUiCounter();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCastManager != null) {
            mCastManager.clearContext(this);
        }
        super.onDestroy();
    }

    public void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (mCastManager != null) {
            mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            getChannels();
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {



        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChannelListFragment();
                case 1:
                    return new EventListFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position].toUpperCase();
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length ;
        }

        }

}
