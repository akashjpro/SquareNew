package com.example.tmha.square.model;

/**
 * Created by tmha on 7/3/2017.
 */

public class User {
    private String mId;
    private String mName;
    private String mPhoto;
    private String mPermission;

    public User() {
    }

    public User(String mId, String mName,
                String mPhoto, String mPermission) {
        this.mId = mId;
        this.mName = mName;
        this.mPhoto = mPhoto;
        this.mPermission = mPermission;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPhoto() {
        return mPhoto;
    }

    public void setmPhoto(String mPhoto) {
        this.mPhoto = mPhoto;
    }

    public String getmPermission() {
        return mPermission;
    }

    public void setmPermission(String mPermission) {
        this.mPermission = mPermission;
    }
}
