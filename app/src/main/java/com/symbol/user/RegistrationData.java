package com.symbol.user;

public class RegistrationData {

    private String displayName;
    private String firstName;
    private String lastName;
    private String image;
    private String thumbImage;
    private String uid;
    private String status;

    public RegistrationData() {}

    public RegistrationData setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public RegistrationData setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public RegistrationData setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public RegistrationData setImage(String image) {
        this.image = image;
        return this;
    }

    public RegistrationData setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
        return this;
    }

    public RegistrationData setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public RegistrationData setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getImage() {
        return image;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public String getUid() {
        return uid;
    }

    public String getStatus() {
        return status;
    }
}
