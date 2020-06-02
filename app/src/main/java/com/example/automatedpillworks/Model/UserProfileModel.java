package com.example.automatedpillworks.Model;

import java.io.Serializable;

public class UserProfileModel implements Serializable {
    public String firstname;
    public String lastname;
    public Integer gender;
    public Integer blood;
    public Long dob;
    public String address;
    public Long weight;

    public UserProfileModel(){

    }

    public UserProfileModel(String firstname, String lastname, Integer gender, Integer blood, Long dob, String address, Long weight) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.gender = gender;
        this.blood = blood;
        this.dob = dob;
        this.address = address;
        this.weight = weight;
    }
}
