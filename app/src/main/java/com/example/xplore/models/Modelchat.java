package com.example.xplore.models;

public class Modelchat {


    String message, reciever, sender, timestamp, type;
    boolean isseen;

    public Modelchat() {

    }

    public Modelchat(String message, String reciever, String sender, String timestamp, String type, boolean isseen) {
        this.message = message;
        this.reciever = reciever;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.isseen = isseen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
