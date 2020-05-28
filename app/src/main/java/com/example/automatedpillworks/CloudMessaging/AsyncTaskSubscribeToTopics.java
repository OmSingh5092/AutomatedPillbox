package com.example.automatedpillworks.CloudMessaging;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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


    private List<String> topics = GlobalVar.userData.userInfo.boxes;
    private Context context;

    public AsyncTaskSubscribeToTopics(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            for (String topic : topics) {
                messaging.subscribeToTopic(topic);
            }

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(!task.isSuccessful()){
                        Log.d("Error:",task.getException().getMessage());
                        return;
                    }
                    String token = task.getResult().getToken();
                    String initialtoken = GlobalVar.userData.userInfo.registrationtoken.get("android");
                    updateTokenToFirestore(token);
                    Log.d("RegistrationToken",token);
                }
            });

            Log.d("Successfull" , "SubscriptionSuccessfull");

        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }

        return null;
    }

    void updateTokenToFirestore(String token){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Map<String,String> updates = new HashMap<>();
        updates.put("android",token);
        FirebaseFirestore.getInstance().collection("users").document(auth.getUid()).update("registrationtoken",updates);

    }
}
