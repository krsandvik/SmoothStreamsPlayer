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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.db.DbHelper;
import com.iosharp.android.ssplayer.model.Channel;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;

public class ChannelListFragment extends Fragment {

    private DbHelper mDatabase;
    private ChannelAdapter mAdapter;

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
        Intent intent = new Intent(c, VideoActivity.class);
        intent.putExtra("media", Utils.fromMediaInfo(info));
        c.startActivity(intent);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        final Cursor cursor = mDatabase.getReadableDatabase().rawQuery("SELECT * FROM " + ChannelContract.ChannelEntry.TABLE_NAME, null);

        ListView listView = (ListView) rootView.findViewById(R.id.listview);
        mAdapter = new ChannelAdapter(getActivity(), cursor);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                c.moveToPosition(position);
                int channelId = c.getInt(c.getColumnIndex(ChannelContract.ChannelEntry._ID));

                Channel channel = mDatabase.getChannel(channelId);

                String url = Utility.getStreamUrl(getActivity(), channelId);
                MediaInfo mediaInfo = Utility.buildMediaInfo(channel.getName(), "SmoothStreams", url, channel.getIcon());

                handleNavigation(getActivity(), mediaInfo);
            }
        });

        return rootView;
    }
}

class ChannelAdapter extends CursorAdapter {
    public ChannelAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View retView = inflater.inflate(R.layout.list_row, viewGroup, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.textView1))
                .setText(cursor.getString(cursor.getColumnIndex("name")));


    }


}
