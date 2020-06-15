package com.example.automatedpillworks;

import android.content.Intent;

import com.example.automatedpillworks.adapters.ReminderRecyclerAdapter;
import com.example.automatedpillworks.Model.UserData;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class GlobalVar {
    public static String currentBox;
    public static Integer currentBoxIndex=0;
    public static UserData userData = new UserData();
    public static List<String> Boxes;

    public static void resetValues(){
        currentBox = null;
        userData = new UserData();
    }

    public static class Initialisation{
        public static String scannedBox;
    }

    //SomeNecessaryConstants
    //Notification Channel ID
    public static String CHANNEL_ID = "#8795";
}
