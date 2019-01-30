package com.symbol.models;

import java.util.Date;

public class Chat {

    private String senderUid;
    private String receiverUid;
    private String message;
    private String senderThumbImage;
    private String chatThumbImage;
    private String displayName;
    private String seen;
    private Date timestamp;

    public Chat() {}

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderThumbImage(String senderThumbImage) {
        this.senderThumbImage = senderThumbImage;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public void setChatThumbImage(String chatThumbImage) {
        this.chatThumbImage = chatThumbImage;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderThumbImage() {
        return senderThumbImage;
    }

    public String getChatThumbImage() {
        return chatThumbImage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getSeen() {
        return seen;
    }
}
