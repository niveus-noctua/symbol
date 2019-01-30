package com.symbol.user;

import java.util.Date;

public class Invite {

    private String type;
    private String status;
    private String senderUid;
    private String creatorUid;
    private String creatorDisplayName;
    private String groupName;
    private String groupUid;
    private String groupProfileImage;
    private String groupThumbImage;
    private String groupProfileBackground;
    private String description;
    private int contestants;
    private Date timestamp;
    //TODO public Message message

    public Invite() {}

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addGroup(Group group) {
        fillLocalGroup(group);
    }

    private void fillLocalGroup(Group group) {
        this.creatorDisplayName = group.getCreatorDisplayName();
        this.groupUid = group.getGroupUid();
        this.creatorUid = group.getCreatorUid();
        this.groupName = group.getGroupName();
        this.groupProfileImage = group.getGroupProfileImage();
        this.groupThumbImage = group.getGroupThumbImage();
        this.contestants = group.getContestants();
        this.timestamp = group.getTimestamp();
        this.groupProfileBackground = group.getGroupProfileBackground();
        this.description = group.getDescription();
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }
    public void setCreatorDisplayName(String creatorDisplayName) {
        this.creatorDisplayName = creatorDisplayName;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupProfileImage(String groupProfileImage) {
        this.groupProfileImage = groupProfileImage;
    }

    public void setGroupProfileBackground(String groupProfileBackground) {
        this.groupProfileBackground = groupProfileBackground;
    }

    public void setGroupThumbImage(String groupThumbImage) {
        this.groupThumbImage = groupThumbImage;
    }

    public void setContestants(int contestants) {
        this.contestants = contestants;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatorDisplayName() {
        return creatorDisplayName;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public String getGroupName() {
        return groupName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public String getGroupProfileImage() {
        return groupProfileImage;
    }

    public String getGroupThumbImage() {
        return groupThumbImage;
    }

    public String getDescription() {
        return description;
    }

    public int getContestants() {
        return contestants;
    }

    public String getGroupProfileBackground() {
        return groupProfileBackground;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public Group retrieveGroup() {
        Group retrievedGroup = new Group();
        retrievedGroup.setCreatorDisplayName(getCreatorDisplayName());
        retrievedGroup.setGroupUid(getGroupUid());
        retrievedGroup.setCreatorUid(getCreatorUid());
        retrievedGroup.setGroupName(getGroupName());
        retrievedGroup.setGroupProfileImage(getGroupProfileImage());
        retrievedGroup.setGroupThumbImage(getGroupThumbImage());
        retrievedGroup.setGroupProfileBackground(getGroupProfileBackground());
        return retrievedGroup;
    }



    public String getSenderUid() {
        return senderUid;
    }
}
