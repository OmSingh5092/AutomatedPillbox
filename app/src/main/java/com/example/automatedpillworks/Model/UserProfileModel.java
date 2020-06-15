package com.example.automatedpillworks.Model;

import java.io.Serializable;

public class UserProfileModel implements Serializable {
    public String firstname;
    public String lastname;
    public String phone;
    public String address;
    public String email;

    public UserProfileModel(){

    }

    public UserProfileModel(String firstname,String lastname,String phone, String address,String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }
}
