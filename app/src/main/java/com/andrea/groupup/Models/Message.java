package com.andrea.groupup.Models;

public class Message {
    private String text;
    private MemberData memberData;
    private boolean belongsToCurrentUser;

    public Message() {
    }
    public Message(String text, MemberData data, boolean belongsToCurrentUser) {
        this.text = text;
        this.memberData = data;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public MemberData getMemberData() {
        return memberData;
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
