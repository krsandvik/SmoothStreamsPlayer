package com.iosharp.android.ssplayer.videoplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.iosharp.android.ssplayer.CastApplication;
import com.iosharp.android.ssplayer.R;

import java.io.IOException;


public class VideoActivity extends ActionBarActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {

    private SurfaceView mSurfaceView;
    private MediaPlayer mPlayer;
    private VideoControllerView mController;
    private String mURL;
    private SurfaceHolder mSurfaceHolder;
    private int mCurrBuffer;
    private VideoCastManager mCastManager;
    private PlaybackLocation mLocation;
    private MediaInfo mSelectedMedia;
    private IVideoCastConsumer mCastConsumer;

    public static enum PlaybackLocation {
        LOCAL,
        REMOTE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = CastApplication.getCastManager(this);
        setContentView(R.layout.activity_video);
        loadViews();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mSelectedMedia = Utils.toMediaInfo(getIntent().getBundleExtra("media"));
            mURL = mSelectedMedia.getContentId();

            if (mCastManager.isConnected()) {
                updatePlaybackLocation(PlaybackLocation.REMOTE);
                System.out.println("******IS CONNECTED******");
                loadRemoteMedia();
            } else {
                mSurfaceHolder = mSurfaceView.getHolder();
                mSurfaceHolder.addCallback(this);
                mPlayer = new MediaPlayer();
                mController = new VideoControllerView(this, false);
                updatePlaybackLocation(PlaybackLocation.LOCAL);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        findViewById(R.id.progress).setVisibility(View.VISIBLE);

        if (!mCastManager.isConnected()) {
            setupLocalPlayback();
        }


        mCastManager.incrementUiCounter();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mController.show();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.release();
        }
        mCastManager.decrementUiCounter();
    }

    /**
     * SurfaceHolder.Callback
     */

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!mCastManager.isConnected()) {
            mPlayer.setDisplay(mSurfaceHolder);
            mPlayer.prepareAsync();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    // End SurfaceHolder.Callback

    /**
     * MediaPlayer.OnPreparedListener
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        System.out.println("IS PREPARED");
        mController.setMediaPlayer(this);
        mController.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        //TODO: figure out why this doesn't work
//        handleAspectRatio();
        mPlayer.start();
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
    // End MediaPlayer.OnPreparedListener

    /**
     * MediaPlayer.OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e(getPackageName(), String.format("Error(%s%s)", what, extra));

        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            mPlayer.reset();
        } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            mPlayer.reset();
        }
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnBufferingUpdateListener(this);

        return true;
    }

    /**
     * Handle aspect ratio
     */
    private void handleAspectRatio() {
        int videoWidth = mPlayer.getVideoWidth();
        int videoHeight = mPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        android.view.ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        mSurfaceView.setLayoutParams(lp);
    }

    /**
     * Buffering updates listening
     *
     * @param mediaPlayer
     * @param i
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mCurrBuffer = i;
        Log.i("Buffer:", mCurrBuffer + "%");
    }

    /**
     * VideoMediaController.MediaPlayerControl
     *
     * @return
     */
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public void seekTo(int i) {
        mPlayer.seekTo(i);
    }

    @Override
    public void start() {
        mPlayer.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    // End VideoMediaController.MediaPlayerControl

    private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onFailed(int resourceId, int statusCode) {
                super.onFailed(resourceId, statusCode);
            }
        };
    }


    private void updatePlaybackLocation(PlaybackLocation location) {
        this.mLocation = location;
        if (location == PlaybackLocation.LOCAL) {

        }
    }

    private void loadRemoteMedia() {
        mCastManager.startCastControllerActivity(this, mSelectedMedia, 0, true);
    }

    private void setupLocalPlayback() {
        try {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(mURL);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnBufferingUpdateListener(this);
            mPlayer.setOnErrorListener(this);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadViews() {
        mSurfaceView = (SurfaceView) findViewById(R.id.videoSurface);
    }

}
