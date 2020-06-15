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
    public void setUserprofile(UserProfileModel userprofile) {
        this.userprofile = userprofile;
    }

    public void addNewBox(String boxId){
        boxes.add(boxId);
        boxnames.put(boxId,"My Box");
    }
}
