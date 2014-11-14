package com.iosharp.android.ssplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.db.DbHelper;
import com.iosharp.android.ssplayer.model.Channel;
import com.iosharp.android.ssplayer.tasks.FetchChannelTask;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;

public class ChannelListFragment extends Fragment {

    private DbHelper mDatabase;
    private ChannelAdapter mAdapter;
    private MiniController mMini;
    private VideoCastManager mCastManager;

    public ChannelListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FetchChannelTask fetchChannelTask = new FetchChannelTask(getActivity());
        fetchChannelTask.execute();

        mDatabase = new DbHelper(getActivity());
    }

    public void handleNavigation(Context c, MediaInfo info) {
        if (mCastManager.isConnected()) {
            mCastManager.startCastControllerActivity(c, info, 0, true);
        } else {
            Intent intent = new Intent(c, VideoActivity.class);
            intent.putExtra("media", com.google.sample.castcompanionlibrary.utils.Utils.fromMediaInfo(info));
            c.startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mAdapter.changeCursor(mDatabase.getReadableDatabase().rawQuery("SELECT * FROM " + ChannelContract.ChannelEntry.TABLE_NAME, null));

        if (mCastManager != null) {
            mCastManager.incrementUiCounter();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        // MiniController
        mCastManager = CastApplication.getCastManager(getActivity());
        mMini = (MiniController) rootView.findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);


        Cursor cursor = mDatabase.getReadableDatabase().rawQuery("SELECT * FROM " + ChannelContract.ChannelEntry.TABLE_NAME, null);

        ListView listView = (ListView) rootView.findViewById(R.id.listview);
        mAdapter = new ChannelAdapter(getActivity(), cursor);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Get channel ID
                Cursor c = (Cursor) mAdapter.getItem(position);
                c.moveToPosition(position);
                int channelId = c.getInt(c.getColumnIndex(ChannelContract.ChannelEntry._ID));
                // Retrieve channel object from database
                Channel channel = mDatabase.getChannel(channelId);
                // Create MediaInfo based off channel object
                String url = Utils.getStreamUrl(getActivity(), channelId);
                MediaInfo mediaInfo = Utils.buildMediaInfo(channel.getName(), "SmoothStreams", url, channel.getIcon());

                // Pass to handleNavigation
                handleNavigation(getActivity(), mediaInfo);
            }
        });

        return rootView;
    }
}


