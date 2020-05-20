package com.example.automatedpillworks.BasicFunctions;

import android.content.Context;

import com.example.automatedpillworks.CloudMessaging.Notifications;

public class Startup {

    public static void firebase_init(Context context){
        Notifications.getFirebaseInstance();
        Notifications.appContext = context.getApplicationContext();
        Notifications.createNotificationChannel(context);
        Notifications.subscribeToTopic();

    }
}