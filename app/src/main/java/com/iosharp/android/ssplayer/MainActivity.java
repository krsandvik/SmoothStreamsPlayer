package com.iosharp.android.ssplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.widgets.MiniController;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoCastManager mCastManager;
    private IVideoCastConsumer mCastConsumer;
    private MenuItem mediaRouteMenuItem;
    private MiniController mMini;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.activity_main);

        mCastManager = CastApplication.getCastManager(this);

        // -- Adding MiniController
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);

        mCastManager.reconnectSessionIfPossible(this, false);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ChannelListFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = CastApplication.getCastManager(this);
        if (mCastManager != null) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");
        if (mCastManager != null) {
//            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            mCastManager.clearContext(this);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
            mediaRouteMenuItem = mCastManager.
                    addMediaRouterButton(menu, R.id.media_route_menu_item);

            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
