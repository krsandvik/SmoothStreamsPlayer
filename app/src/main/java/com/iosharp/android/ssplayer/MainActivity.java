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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;


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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ChannelListFragment extends Fragment {

        private DbHelper mDatabase;
        private ChannelAdapter mAdapter;

        public ChannelListFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            FetchChannelTask fetchChannelTask = new FetchChannelTask(getActivity());
//            fetchChannelTask.execute();

            mDatabase = new DbHelper(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);


            Cursor cursor = mDatabase.getReadableDatabase().rawQuery("SELECT * FROM " + DbContract.ChannelEntry.TABLE_NAME, null);

            ListView listView = (ListView) rootView.findViewById(R.id.listview);
            mAdapter = new ChannelAdapter(getActivity(), cursor);
            listView.setAdapter(mAdapter);

            mDatabase.getAllEvents();

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

            System.out.println(cursor.getString(cursor.getColumnIndex("name")));;

        }


    }
}
