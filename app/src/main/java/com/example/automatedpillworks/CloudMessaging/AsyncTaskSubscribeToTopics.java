package com.example.automatedpillworks.CloudMessaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class AsyncTaskSubscribeToTopics extends AsyncTask<String,String,String> {
    String TAG = "fcmErrorMessage:";
    private List<String> topics;
    public AsyncTaskSubscribeToTopics(List<String> topics){
        this.topics = topics;
    }
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected String doInBackground(String... strings) {
        //Initialising Firebase instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            for (String topic : topics) {
                messaging.subscribeToTopic(topic).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Subscription Failure",e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("Success","Subscription Successful");
                    }
                });
            }
            //Subscribing to client group (Topic = UID)
            messaging.subscribeToTopic(auth.getUid());

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(!task.isSuccessful()){
                        Log.d("Error:",task.getException().getMessage());
                        return;
                    }
                    String token = task.getResult().getToken();
                    Log.d("RegistrationToken",token);
                    uploadToken(token);
                }
            });

            Log.d("Successfull" , "SubscriptionSuccessfull");

        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }

        return null;
    }

    void uploadToken(final String s){
        firestore.collection("registrationToken").document(auth.getUid())
                .update("tokens", FieldValue.arrayUnion(s)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("New Token",s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Failure:",e.getMessage());
            }
        });
    }
}
