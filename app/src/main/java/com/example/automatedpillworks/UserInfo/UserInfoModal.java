package com.example.automatedpillworks.UserInfo;

import android.content.Intent;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoModal implements Serializable {
    public UserProfileModal userprofile = new UserProfileModal();
    public Map<String,String> boxnames = new HashMap<>();
    public List<String> boxes = new ArrayList<>();
    public Map<String,String>registrationtoken = new HashMap<>();

    public UserInfoModal(){
    }

    public UserInfoModal(String firstname, String lastname, Integer blood, Long dob, String address, Long weight,Integer gender) {
        this.userprofile.firstname = firstname;
        this.userprofile.lastname = lastname;
        this.userprofile.blood = blood;
        this.userprofile.dob = dob;
        this.userprofile.address = address;
        this.userprofile.weight = weight;
        this.userprofile.gender = gender;
    }
}
