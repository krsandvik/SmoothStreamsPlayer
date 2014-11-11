package com.iosharp.android.ssplayer.model;

public class Channel {
    private int mId;
    private String mName;
    private String mIcon;

    public Channel() {
    }

    public Channel(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", mIcon='" + mIcon + '\'' +
                '}';
    }
}
