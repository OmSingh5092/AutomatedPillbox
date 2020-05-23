package com.example.automatedpillworks;

import com.example.automatedpillworks.RecyclerViewAdapter.ReminderRecyclerAdapter;
import com.example.automatedpillworks.UserInfo.UserAdditional;
import com.example.automatedpillworks.UserInfo.UserData;
import com.example.automatedpillworks.UserInfo.UserInfoModal;

import java.util.ArrayList;
import java.util.List;

public class GlobalVar {
    public static String currentBox;
    public static ArrayList<ReminderRecyclerAdapter.Data> reminderRecyclerdata = new ArrayList<>();
    public static UserData userData;
    public static List<String> Boxes;

    public static UserData signUpTemp;
}
