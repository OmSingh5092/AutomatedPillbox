package com.example.automatedpillworks.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoModel implements Serializable {
    public UserProfileModel userprofile = new UserProfileModel();
    public Map<String,String> boxnames = new HashMap<>();
    public List<String> boxes = new ArrayList<>();
    public List<String> newboxes = new ArrayList<>();

    public UserInfoModel(){
    }

    public UserInfoModel(String firstname, String lastname, Integer blood, Long dob, String address, Long weight, Integer gender,String email) {
        this.userprofile.firstname = firstname;
        this.userprofile.lastname = lastname;
        this.userprofile.blood = blood;
        this.userprofile.dob = dob;
        this.userprofile.address = address;
        this.userprofile.weight = weight;
        this.userprofile.gender = gender;
        this.userprofile.email = email;
    }
}
