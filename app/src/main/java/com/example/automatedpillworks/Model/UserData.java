package com.example.automatedpillworks.Model;

public class UserData {
    public UserInfoModel userInfo = new UserInfoModel();
    public UserAdditional userAdditional = new UserAdditional();

    public UserData(UserInfoModel userInfo, UserAdditional userAdditional) {
        this.userInfo = userInfo;
        this.userAdditional = userAdditional;
    }
    public UserData(UserInfoModel userInfo){
        this.userInfo = userInfo;
    }

    public UserData(UserAdditional userAdditional){
        this.userAdditional = userAdditional;
    }
    public UserData(){

    }

    public void setUserInfo(UserInfoModel userInfo){
        this.userInfo = userInfo;
    }
    public void setUserAdditional(UserAdditional userAdditional){
        this.userAdditional = userAdditional;
    }
}
