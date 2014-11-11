package com.iosharp.android.ssplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iosharp.android.ssplayer.db.ChannelsContract;
import com.iosharp.android.ssplayer.db.DbHelper;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ChannelListFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public static class ChannelListFragment extends Fragment {

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);


            final Cursor cursor = mDatabase.getReadableDatabase().rawQuery("SELECT * FROM " + ChannelsContract.ChannelEntry.TABLE_NAME, null);

            ListView listView = (ListView) rootView.findViewById(R.id.listview);
            mAdapter = new ChannelAdapter(getActivity(), cursor);
            listView.setAdapter(mAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Cursor c = (Cursor) mAdapter.getItem(position);
                    c.moveToPosition(position);
                    int channelId = c.getInt(c.getColumnIndex(ChannelsContract.ChannelEntry._ID));

                    Intent i = new Intent(getActivity(), VideoActivity.class).putExtra(Intent.EXTRA_INTENT, channelId);
                    startActivity(i);
                }
            });

            return rootView;
        }
    }

    public static class ChannelAdapter extends CursorAdapter {
        public ChannelAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            View retView = inflater.inflate(R.layout.channel_list_item, viewGroup, false);

            return retView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.channel_list_item_textview))
                    .setText(cursor.getString(cursor.getColumnIndex("_id")));
        }


    }
}
