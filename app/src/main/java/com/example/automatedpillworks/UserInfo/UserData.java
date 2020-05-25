package com.example.automatedpillworks.UserInfo;

public class UserData {
    public UserInfoModal userInfo = new UserInfoModal();
    public UserAdditional userAdditional = new UserAdditional();

    public UserData(UserInfoModal userInfo, UserAdditional userAdditional) {
        this.userInfo = userInfo;
        this.userAdditional = userAdditional;
    }
    public UserData(UserInfoModal userInfo){
        this.userInfo = userInfo;
    }

    public UserData(UserAdditional userAdditional){
        this.userAdditional = userAdditional;
    }
    public UserData(){

    }
}
