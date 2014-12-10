package com.iosharp.android.ssplayer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import static com.iosharp.android.ssplayer.db.ChannelContract.EventEntry;


public class SearchableActivity extends ActionBarActivity {
    String[] EVENT_COLUMNS = new String[] {
            EventEntry.TABLE_NAME + "." + EventEntry._ID,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_KEY_CHANNEL,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NAME,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_END_DATE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_LANGUAGE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_QUALITY,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_CATEGORY,
    };

    public static final int COL_EVENT_ID = 0;
    public static final int COL_EVENT_CHANNEL = 1;
    public static final int COL_EVENT_NAME = 2;
    public static final int COL_EVENT_START_DATE = 3;
    public static final int COL_EVENT_END_DATE = 4;
    public static final int COL_EVENT_LANGUAGE = 5;
    public static final int COL_EVENT_QUALITY = 6;
    public static final int COL_EVENT_CATEGORY = 7;

    private ListView mListView;
    private Tracker mTracker;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        setupActionBar();

        mListView = (ListView) findViewById(R.id.search_listview);

        mTracker = ((PlayerApplication) getApplication()).getTracker(PlayerApplication.TrackerName.APP_TRACKER);
        mTracker.setScreenName(getString(R.string.ga_screen_search));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

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
            mQuery = query;
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.ga_events_category_search))
                        .setAction(getString(R.string.ga_events_action_search))
                        .build());

        TextView noResults = (TextView) findViewById(R.id.no_results);

        String wildcaseQuery = "%" + query + "%";

        String[] SEARCH_COLUMNS = new String[] {
                EventEntry.TABLE_NAME + "." + EventEntry._ID,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_KEY_CHANNEL,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NETWORK,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NAME,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_DESCRIPTION,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_LANGUAGE,
                EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_CATEGORY,
        };

        String selection = SEARCH_COLUMNS[0] + " LIKE ? OR " +
                SEARCH_COLUMNS[1] + " LIKE ? OR " +
                SEARCH_COLUMNS[2] + " LIKE ? OR " +
                SEARCH_COLUMNS[3] + " LIKE ? OR " +
                SEARCH_COLUMNS[4] + " LIKE ? OR " +
                SEARCH_COLUMNS[5] + " LIKE ? OR " +
                SEARCH_COLUMNS[6] + " LIKE ?";

        // Query is going to be the arg for each ?
        String[] queryArgs = new String[] {wildcaseQuery,
                wildcaseQuery, wildcaseQuery, wildcaseQuery
                , wildcaseQuery, wildcaseQuery, wildcaseQuery};

        Cursor cursor = this.getContentResolver().query(EventEntry.CONTENT_URI, EVENT_COLUMNS, selection, queryArgs, null);

        if (cursor.getCount() == 0) {
            mListView.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            ResultAdapter events = new ResultAdapter(this, cursor, false);
            mListView.setAdapter(events);
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_searchable, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(mQuery, false);

        return true;
    }
}
