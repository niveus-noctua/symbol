package com.symbol.user;

import java.sql.Timestamp;
import java.util.Date;

public class Group {

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


    public Group() {

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
}
