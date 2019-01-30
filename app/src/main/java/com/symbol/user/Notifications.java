package com.symbol.user;

public class Notifications {

    private String type;
    private String userUid;
    private String senderUid;
    private String requestObjectName;
    private String message;

    public Notifications() {}

    public void setType(String type) {
        this.type = type;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public void setRequestObjectName(String requestObjectName) {
        this.requestObjectName = requestObjectName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getRequestObjectName() {
        return requestObjectName;
    }

    public String getMessage() {
        return message;
    }
}
