package com.example.automatedpillworks.UserInfo;

import android.content.Intent;

import java.io.Serializable;
import java.io.StringReader;

public class UserInfoModal implements Serializable {
    public String firstname;
    public String lastname;
    public Integer gender;
    public Integer blood;
    public Long dob;
    public String address;
    public Double weight;

    public UserInfoModal(){

    }

    public UserInfoModal(String firstname, String lastname, Integer blood, Long dob, String address, Double weight,Integer gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.blood = blood;
        this.dob = dob;
        this.address = address;
        this.weight = weight;
        this.gender = gender;
    }
}
