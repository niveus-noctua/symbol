package com.symbol.user;

public class Profile {

    private String uid;
    private String nickname;
    private String firstName;
    private String middleName;
    private String lastName;
    private String university;
    private String tokenId;
    private BirthDate birthDate;
    private int dayOfBirth;
    private int monthOfBirth;
    private int yearOfBirth;
    private int age;

    public Profile() {}

    public Profile setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Profile setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Profile setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Profile setMiddleName(String middleName) {
        this.middleName = middleName;
        return this;
    }

    public Profile setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Profile setBirthDate(int day, int month, int year) {
        this.dayOfBirth = day;
        this.monthOfBirth = month;
        this.yearOfBirth = year;
        this.birthDate = new BirthDate(day, month, year);
        age = this.birthDate.getAge();
        return this;
    }

    public String getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUniversity() {
        return university;
    }

    public int getAge() {
        return age;
    }

    public String getTokenId() {
        return tokenId;
    }
}
