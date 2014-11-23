package com.iosharp.android.ssplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.tasks.FetchChannelTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class MainActivity extends ActionBarActivity {

    private VideoCastManager mCastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!(Build.MODEL.contains("AFT") || Build.MANUFACTURER.equals("Amazon"))) {
            VideoCastManager.checkGooglePlayServices(this);
        }

        FetchChannelTask fetchChannelTask = new FetchChannelTask(this);
        fetchChannelTask.execute();


        setupActionBar();
        imageLoaderInit();

        mCastManager = PlayerApplication.getCastManager(this);
        if (mCastManager != null){
            mCastManager.reconnectSessionIfPossible(this, false);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ChannelListFragment())
                    .commit();
        }

        Uri uri = ChannelContract.EventEntry.buildEventWithDate("20141123");
        System.out.println(getContentResolver().query(uri, null, null, null, null).getCount());

    }

    @Override
    protected void onResume() {
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

    private void imageLoaderInit() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        if (ImageLoader.getInstance().isInited() == false) {
            ImageLoader.getInstance().init(config);
        }
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

        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
