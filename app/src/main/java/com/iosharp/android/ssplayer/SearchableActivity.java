package com.iosharp.android.ssplayer;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;


public class SearchableActivity extends ActionBarActivity {
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        setupActionBar();

        mListView = (ListView) findViewById(R.id.search_listview);

        handleIntent(getIntent());
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_searchable));
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        String wildcaseQuery = "%" + query + "%";

        String[] EVENT_COLUMNS = new String[] {
                EventEntry.TABLE_NAME + "." + EventEntry._ID,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_KEY_CHANNEL,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NETWORK,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NAME,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_DESCRIPTION,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_LANGUAGE,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_CATEGORY,
        };

        String selection = EVENT_COLUMNS[0] + " LIKE ? OR " +
                EVENT_COLUMNS[1] + " LIKE ? OR " +
                EVENT_COLUMNS[2] + " LIKE ? OR " +
                EVENT_COLUMNS[3] + " LIKE ? OR " +
                EVENT_COLUMNS[4] + " LIKE ? OR " +
                EVENT_COLUMNS[5] + " LIKE ? OR " +
                EVENT_COLUMNS[6] + " LIKE ?";

        String[] queryArgs = new String[] {wildcaseQuery,
                wildcaseQuery, wildcaseQuery, wildcaseQuery
                , wildcaseQuery, wildcaseQuery, wildcaseQuery};

        Cursor cursor = this.getContentResolver().query(EventEntry.CONTENT_URI, null, selection, queryArgs, null);



        if (cursor == null) {
            // No results. Tell them.
            System.out.println("No results");
        } else {
            String[] resultColumns = new String[] {EventEntry.COLUMN_NAME};
            int[] resultViews = new int[] {R.id.search_result_name};

            SimpleCursorAdapter events = new SimpleCursorAdapter(this, R.layout.search_result_row, cursor, resultColumns, resultViews);
            mListView.setAdapter(events);
        }


    }




//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_searchable, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
