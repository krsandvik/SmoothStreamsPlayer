package com.iosharp.android.ssplayer;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iosharp.android.ssplayer.db.ChannelContract;

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
                .setText(cursor.getString(cursor.getColumnIndex(ChannelContract.ChannelEntry.COLUMN_NAME)));
    }
}