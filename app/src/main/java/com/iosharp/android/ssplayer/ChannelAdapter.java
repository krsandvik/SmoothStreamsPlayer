package com.iosharp.android.ssplayer;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Date;

class ChannelAdapter extends CursorAdapter {

    ImageView mIcon;

    public ChannelAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View retView = inflater.inflate(R.layout.channel_list_row, viewGroup, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        showIcon(view, context, cursor);
        setCurrentEvent(view, cursor);

        ((TextView) view.findViewById(R.id.textView1))
                .setText(cursor.getString(ChannelListFragment.COL_CHANNEL_NAME));
    }

    private void showIcon(View view, Context context, Cursor cursor) {
        String SMOOTHSTREAMS_ICON_BASE = "http://smoothstreams.tv/schedule/includes/images/uploads/";
        String channelIcon = cursor.getString(ChannelListFragment.COL_CHANNEL_ICON);
        String SMOOTHSTREAMS_ICON_URL = SMOOTHSTREAMS_ICON_BASE + channelIcon;

        mIcon = (ImageView) view.findViewById(R.id.imageView1);

        Picasso.with(context).load(SMOOTHSTREAMS_ICON_URL).into(mIcon);
    }

    private void setCurrentEvent(View view, Cursor cursor) {
        String id = cursor.getString(ChannelListFragment.COL_EVENT_ID);
        TextView eventTitle = (TextView) view.findViewById(R.id.textView2);

        if (id != null) {
            Date now = new Date();
            String title = cursor.getString(ChannelListFragment.COL_EVENT_NAME);
            Date startDate = new Date(cursor.getLong(ChannelListFragment.COL_EVENT_START_DATE));
            Date endDate = new Date(cursor.getLong(ChannelListFragment.COL_EVENT_END_DATE));
            String language = cursor.getString(ChannelListFragment.COL_EVENT_LANGUAGE);
            String quality = cursor.getString(ChannelListFragment.COL_EVENT_QUALITY);

            if (now.after(startDate) && now.before(endDate)) {
                SpannableString qualitySpannableString = new SpannableString("");
                SpannableString languageSpannableString = new SpannableString("");

                if (!language.equals("")) {
                    languageSpannableString = Utils.getLanguageImg(mContext, language);
                }
                if (quality.equalsIgnoreCase("720p")) {
                    qualitySpannableString = Utils.getHighDefBadge();
                }

                eventTitle.setText(TextUtils.concat(title, languageSpannableString, qualitySpannableString));
                eventTitle.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 50;
    }
}