package com.example.automatedpillworks.utils;

import android.content.Context;

import com.example.automatedpillworks.GlobalVar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class UserHandler {
    private Context context;
    FirebaseMessaging messaging;
    FirebaseAuth auth;

    public UserHandler(Context context){
        this.context = context;

        messaging = FirebaseMessaging.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void logOutUser(){
        //Removing FCM Topics

        //Removing User topic
        messaging.unsubscribeFromTopic(auth.getUid());

        //Removing Box Topics

        for(String boxId: GlobalVar.userData.userInfo.boxes){
            messaging.unsubscribeFromTopic(boxId);
        }

        GlobalVar.resetValues();
        auth.signOut();
        if(GoogleSignIn.getLastSignedInAccount(context) == null){

        }
    }
}
