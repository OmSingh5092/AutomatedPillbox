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
}
