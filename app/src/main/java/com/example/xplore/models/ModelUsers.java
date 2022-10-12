package com.example.xplore.models;



public class ModelUsers {
    boolean isBlocked=false;
    String name;



    String email;

    String image;

    String uid,onlinestatus,typingTo;

    public ModelUsers() {
    }

    public ModelUsers(boolean isBlocked, String name, String email, String image, String uid, String onlinestatus, String typingTo) {
        this.isBlocked = isBlocked;
        this.name = name;
        this.email = email;
        this.image = image;
        this.uid = uid;
        this.onlinestatus = onlinestatus;
        this.typingTo = typingTo;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlinestatus() {
        return onlinestatus;
    }

    public void setOnlinestatus(String onlinestatus) {
        this.onlinestatus = onlinestatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}

