package com.example.automatedpillworks;

import android.content.Intent;

import com.example.automatedpillworks.adapters.ReminderRecyclerAdapter;
import com.example.automatedpillworks.Model.UserData;

import java.util.ArrayList;
import java.util.List;

public class GlobalVar {
    public static String currentBox;
    public static Integer currentBoxIndex=0;
    public static ArrayList<ReminderRecyclerAdapter.Data> reminderRecyclerdata = new ArrayList<>();
    public static UserData userData;
    public static List<String> Boxes;

    public static UserData signUpTemp = new UserData();

    public static void resetValues(){
        currentBox = null;
        reminderRecyclerdata = null;
        userData = null;
    }

    //SomeNecessaryConstants
    //Notification Channel ID
    public static String CHANNEL_ID = "#8795";
}
