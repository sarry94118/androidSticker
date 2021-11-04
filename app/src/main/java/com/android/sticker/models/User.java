package com.android.sticker.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String username;

    public User(String username) {
        this.username = username;
    }

}

