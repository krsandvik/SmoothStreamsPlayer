package com.iosharp.android.ssplayer;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iosharp.android.ssplayer.db.ChannelContract;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import static com.iosharp.android.ssplayer.db.ChannelContract.*;

class ChannelAdapter extends CursorAdapter {

    ImageView mIcon;

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
//        int channelId = cursor.getInt(cursor.getColumnIndex(ChannelContract.ChannelEntry._ID));
        showIcon(view, cursor);

        ((TextView) view.findViewById(R.id.textView1))
                .setText(cursor.getString(cursor.getColumnIndex(ChannelEntry.COLUMN_NAME)));
    }

    public void showIcon(View view, Cursor cursor) {
        String SMOOTHSTREAMS_ICON_BASE = "http://smoothstreams.tv/schedule/includes/images/uploads/";
        String channelIcon = cursor.getString(cursor.getColumnIndex(ChannelEntry.COLUMN_ICON));
        String SMOOTHSTREAMS_ICON_URL = SMOOTHSTREAMS_ICON_BASE + channelIcon;

        mIcon = (ImageView) view.findViewById(R.id.imageView1);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(SMOOTHSTREAMS_ICON_URL, mIcon, new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build());
    }
}