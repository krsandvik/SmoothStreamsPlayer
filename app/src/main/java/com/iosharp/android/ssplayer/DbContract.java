package com.iosharp.android.ssplayer;

import android.provider.BaseColumns;

public class DbContract {

    public static final class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "event";

        public static final String COLUMN_KEY_CHANNEL = "channel_id";
        public static final String COLUMN_NETWORK = "network";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        // time since Unix epoch stored as a long
        public static final String COLUMN_START_DATE = "start_date";
        // time since Unix epoch stored as a long
        public static final String COLUMN_END_DATE = "end_date";
        // runtime stored as long
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_QUALITY = "quality";
    }

    public static final class ChannelEntry implements BaseColumns {
        public static final String TABLE_NAME = "channel";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ICON = "icon";


    }
}
