package com.iosharp.android.ssplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.applidium.headerlistview.HeaderListView;
import com.applidium.headerlistview.SectionAdapter;

import java.util.ArrayList;


public class EventListFragment extends Fragment {
    public EventListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        HeaderListView headerListView = (HeaderListView) view.findViewById(R.id.listview);
        headerListView.setAdapter(new SectionAdapter() {
            @Override
            public int numberOfSections() {
                return 0;
            }

            @Override
            public int numberOfRows(int section) {
                return 0;
            }

            @Override
            public View getRowView(int section, int row, View convertView, ViewGroup parent) {
                return null;
            }

            @Override
            public Object getRowItem(int section, int row) {
                return null;
            }
        });
    }
}
