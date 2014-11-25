package com.iosharp.android.ssplayer;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applidium.headerlistview.HeaderListView;
import com.applidium.headerlistview.SectionAdapter;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.iosharp.android.ssplayer.db.ChannelContract.*;

public class EventListFragment extends Fragment {
    private static final String TAG = EventListFragment.class.getSimpleName();

    private static ArrayList<ArrayList<Event>> mDateEvents;
    private static ArrayList<String> mDate;
    private static EventAdapter mAdapter;
    private VideoCastManager mCastManager;
    private MiniController mMini;

    public EventListFragment() {

    }

    private static void getDateEvents(Context context, ArrayList<String> dates, ArrayList<ArrayList<Event>> events) {
        Uri uri = EventEntry.buildEventDate();
        Cursor dateCursor = context.getContentResolver().query(uri, null, null, null, null);

        String date;
        if (dateCursor != null) {
            while (dateCursor.moveToNext()) {
                ArrayList<Event> channelEvents = new ArrayList<Event>();
                date = dateCursor.getString(dateCursor.getColumnIndex(EventEntry.COLUMN_DATE));

                dates.add(date);

                Uri u = EventEntry.buildEventWithDate(date);
                String selection = EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_END_DATE +
                        " <= ?";
                String now = Long.toString(new Date().getTime());
                String[] selectionArgs = new String[]{now};
                String sortOrder = EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE +
                        ", " + EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_KEY_CHANNEL;

                Cursor eventCursor = context.getContentResolver().query(u, null, selection, selectionArgs, sortOrder);

                if (eventCursor != null) {
                    while (eventCursor.moveToNext()) {
                        String name = eventCursor.getString(eventCursor.getColumnIndex(EventEntry.COLUMN_NAME));
                        int channel = eventCursor.getInt(eventCursor.getColumnIndex(EventEntry.COLUMN_KEY_CHANNEL));
                        String quality = eventCursor.getString(eventCursor.getColumnIndex(EventEntry.COLUMN_QUALITY));
                        long startDate = eventCursor.getLong(eventCursor.getColumnIndex(EventEntry.COLUMN_START_DATE));
                        String language = eventCursor.getString(eventCursor.getColumnIndex(EventEntry.COLUMN_LANGUAGE));

                        Event e = new Event();
                        e.setName(name);
                        e.setChannel(channel);
                        e.setQuality(quality);
                        e.setStartDate(startDate);
                        e.setLanguage(language);

                        channelEvents.add(e);
                    }
                }
                events.add(channelEvents);
                eventCursor.close();
            }
        }
        dateCursor.close();
    }

    public static void updateEvents(Context context) {

        getDateEvents(context, mDate, mDateEvents);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = PlayerApplication.getCastManager(getActivity());

        // Init array lists before they are passed to getDateEvents() and populated
        mDate = new ArrayList<String>();
        mDateEvents = new ArrayList<ArrayList<Event>>();
   }

    @Override
    public void onResume() {
        super.onResume();
        updateEvents(getActivity());
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
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        //MiniController
        if (mCastManager != null) {
            mMini = (MiniController) rootView.findViewById(R.id.miniController_event);
            mCastManager.addMiniController(mMini);
        }

        HeaderListView list = (HeaderListView) rootView.findViewById(R.id.channel_list_view);
        // This can be removed when HeaderListView fixes a bug https://github.com/applidium/HeaderListView/issues/28
        list.setId(2);
        mAdapter = new EventAdapter();
        list.setAdapter(mAdapter);

        return rootView;
    }

    class EventAdapter extends SectionAdapter {
        @Override
        public int numberOfSections() {
            return mDateEvents.size();
        }

        @Override
        public int numberOfRows(int section) {
            if (section > -1) {
                return mDateEvents.get(section).size();
            }
            return 1;
        }

        @Override
        public View getRowView(int section, int row, View convertView, ViewGroup parent) {
            Event e = getRowItem(section, row);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.event_item_row, null);
            }

            SpannableString qualitySpannableString = new SpannableString("");
            SpannableString languageSpannableString = new SpannableString("");
            String channel = String.format("%02d", e.getChannel());
            String quality = e.getQuality();
            String language = e.getLanguage();

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String time = sdf.format(new Date(e.getStartDate()));

            SpannableString title = new SpannableString(channel + "\t\t" +time + "\t\t" + e.getName());

            if (quality.equalsIgnoreCase("720p")) {
                qualitySpannableString = Utils.getHighDefBadge();
            }if (!language.equals("")) {
                languageSpannableString = Utils.getLanguageBadge(language.toUpperCase());
            }

            ((TextView) convertView.findViewById(R.id.event_item_row_title))
                    .setText(TextUtils.concat(title, qualitySpannableString, languageSpannableString));

            return convertView;
        }

        @Override
        public Event getRowItem(int section, int row) {
            return mDateEvents.get(section).get(row);
        }

        @Override
        public boolean hasSectionHeaderView(int section) {
            return true;
        }

        @Override
        public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.event_header_row, null);
            }

            String date = getSectionHeaderItem(section);

            ((TextView) convertView.findViewById(R.id.event_header_row_title))
                    .setText(date);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public String getSectionHeaderItem(int section) {
            return getFormattedDate(mDate.get(section));
        }
    }

    private String getFormattedDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ChannelContract.DATE_FORMAT);
            Date newDate = sdf.parse(date);

            SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String newDateString = desiredDateFormat.format(newDate);

            return newDateString;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}


