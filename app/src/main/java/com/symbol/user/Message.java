package com.symbol.user;


import java.util.Date;

public class Message {

    private String senderName;
    private String message;
    private Date timestamp;
    private String thumbImage;
    private String messageImage;
    private String uid;
    private Boolean seen;

    public Message() {}

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public Boolean getSeen() {
        return seen;
    }

    public String getUid() {
        return uid;
    }

    public String getMessageImage() {
        return messageImage;
    }
}
