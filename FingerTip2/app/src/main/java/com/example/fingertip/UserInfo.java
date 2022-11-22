package com.example.fingertip;

public class UserInfo {


    String userUID;
    String email;
    String phoneNumber;
    String nickname;

    public UserInfo(){} // 생성자 메서드

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserInfo(String userUID, String email, String phoneNumber, String nickname){
        this.userUID = userUID;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
    }
}