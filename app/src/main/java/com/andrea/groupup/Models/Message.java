package com.andrea.groupup.Models;

import android.graphics.Bitmap;

public class Message {
    private String text;
    private MemberData memberData;
    private Bitmap bitmap;
    private boolean belongsToCurrentUser;

    public Message() {
    }
    public Message(String text, MemberData data, Bitmap bitmap, boolean belongsToCurrentUser) {
        this.text = text;
        this.memberData = data;
        this.bitmap = bitmap;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public MemberData getMemberData() {
        return memberData;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }
/*
    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", isBelongsToCurrentUser=" + belongsToCurrentUser +
                ", timestamp=" + timestamp +
                ", clientID='" + clientID + '\'' +
                ", member=" + member +
                '}';
    }*/
}
