package com.example.automatedpillworks.UserInfo;

import android.content.Intent;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class UserInfoModal implements Serializable {
    public String firstname;
    public String lastname;
    public Integer gender;
    public Integer blood;
    public Long dob;
    public String address;
    public Long weight;
    public List<String> boxes;

    public UserInfoModal(){
        this.boxes = new ArrayList<>();
    }

    public UserInfoModal(String firstname, String lastname, Integer blood, Long dob, String address, Long weight,Integer gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.blood = blood;
        this.dob = dob;
        this.address = address;
        this.weight = weight;
        this.gender = gender;
        this.boxes =new ArrayList<>();
    }
}
