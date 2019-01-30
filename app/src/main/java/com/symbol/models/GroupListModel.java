package com.symbol.models;

public class GroupListModel {

    private String groupName;
    private String groupThumbImage;
    private String groupUid;
    private String description;
    private int contestants;

    public GroupListModel() {}

    public GroupListModel(String groupName, String groupThumbImage) {
        this.groupName = groupName;
        this.groupThumbImage = groupThumbImage;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupThumbImage(String groupThumbImage) {
        this.groupThumbImage = groupThumbImage;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContestants(int contestants) {
        this.contestants = contestants;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupThumbImage() {
        return groupThumbImage;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public String getDescription() {
        return description;
    }

    public int getContestants() {
        return contestants;
    }
}
