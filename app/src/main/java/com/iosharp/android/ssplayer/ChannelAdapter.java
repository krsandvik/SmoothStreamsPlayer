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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;

import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;

class ChannelAdapter extends CursorAdapter {

    ImageView mIcon;

    public ChannelAdapter(Context context, Cursor c) {
        super(context, c);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View retView = inflater.inflate(R.layout.list_row, viewGroup, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        System.out.println(Arrays.toString(cursor.getColumnNames()));
        showIcon(view, cursor);
        setCurrentEvent(view, cursor);

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

    public void setCurrentEvent(View view, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(ChannelContract.EventEntry.COLUMN_NAME));
        TextView eventTitle = (TextView) view.findViewById(R.id.textView2);


        // TODO: check this logic and clean up the column names
        if (title != null) {
            Date startDate = new Date(cursor.getLong(4));
            Date endDate = new Date(cursor.getLong(cursor.getColumnIndex(ChannelContract.EventEntry.COLUMN_END_DATE)));
            Date now = new Date();

            if (now.after(startDate) && now.before(endDate)) {
                eventTitle.setText(title);
            } else {
                eventTitle.setText("");
            }

        } else {
            // Set textview to nothing due to views being recycled
            eventTitle.setText("");
        }

    }
}