package com.symbol.models;

public class UserListModel {

    private String displayName;
    private String status;
    private String thumbImage;

    public UserListModel() {}

    public UserListModel(String displayName, String status, String thumbImage) {
        this.displayName = displayName;
        this.status = status;
        this.thumbImage = thumbImage;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return status;
    }

    public String getThumbImage() {
        return thumbImage;
    }
}
