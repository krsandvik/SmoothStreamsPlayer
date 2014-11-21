package com.iosharp.android.ssplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
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
        showIcon(view, cursor);
        setCurrentEvent(view, context, cursor);

        ((TextView) view.findViewById(R.id.textView1))
                .setText(cursor.getString(ChannelListFragment.COL_CHANNEL_NAME));
    }

    private void showIcon(View view, Cursor cursor) {
        String SMOOTHSTREAMS_ICON_BASE = "http://smoothstreams.tv/schedule/includes/images/uploads/";
        String channelIcon = cursor.getString(ChannelListFragment.COL_CHANNEL_ICON);
        String SMOOTHSTREAMS_ICON_URL = SMOOTHSTREAMS_ICON_BASE + channelIcon;

        mIcon = (ImageView) view.findViewById(R.id.imageView1);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(SMOOTHSTREAMS_ICON_URL, mIcon, new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build());
    }

    private void setCurrentEvent(View view, Context context, Cursor cursor) {
        String id = cursor.getString(ChannelListFragment.COL_EVENT_ID);
        TextView eventTitle = (TextView) view.findViewById(R.id.textView2);

        if (id != null) {
            Date now = new Date();
            String title = cursor.getString(ChannelListFragment.COL_EVENT_NAME);
            Date startDate = new Date(cursor.getLong(ChannelListFragment.COL_EVENT_START_DATE));
            Date endDate = new Date(cursor.getLong(ChannelListFragment.COL_EVENT_END_DATE));

            if (now.after(startDate) && now.before(endDate)) {
                String quality = cursor.getString(ChannelListFragment.COL_EVENT_QUALITY);

                if (quality.equalsIgnoreCase("720p")) {
                    int marker = R.drawable.rate_star_med_off_holo_dark;

                    SpannableString ssTitle = new SpannableString(title + " ");
                    ImageSpan imageSpan = new ImageSpan(context, marker, ImageSpan.ALIGN_BASELINE);
                    ssTitle.setSpan(imageSpan, ssTitle.length() - 1, ssTitle.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    eventTitle.setText(ssTitle);

                } else {
                    eventTitle.setText(title);
                }
            } else {
                // Set textview to nothing due to views being recycled
                eventTitle.setText("");
            }
        } else {
            // Set textview to nothing due to views being recycled
            eventTitle.setText("");
        }
    }
}