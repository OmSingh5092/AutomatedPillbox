package com.example.automatedpillworks.CloudMessaging;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.internal.$Gson$Preconditions;

public class HandleTopics {

    public static void unsubscribeFromAllTopics(Context context){
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        messaging.unsubscribeFromTopic("users");
        messaging.unsubscribeFromTopic("boxes");
    }
}
