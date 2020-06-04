package com.example.automatedpillworks.CloudMessaging;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.automatedpillworks.R;
import com.example.automatedpillworks.activities.Reminder;
import com.example.automatedpillworks.utils.DateFormats;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationService extends FirebaseMessagingService {
    public static String CHANNEL_ID = "#8795";
    public Context appContext;
    public SharedPreferences sharedPreferences;



    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //Setting up the references
        appContext = this.getApplicationContext();
        //Retrieving the shared preferences instance
        this.sharedPreferences = getSharedPreferences("BOX_NAMES",Context.MODE_PRIVATE);
        if(remoteMessage.getData().get("type").equals("reminder")){
            ReminderNotification(remoteMessage);
        }else if(remoteMessage.getData().get("type").equals("newbox")){
            newBoxNotification(remoteMessage);
        }else if(remoteMessage.getData().get("type").equals("deletebox")){
            newBoxNotification(remoteMessage);
        }

    }




    public void ReminderNotification(RemoteMessage message){
        Intent intent = new Intent(appContext, Reminder.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent, 0);

        //Setting title as the box name

        Map<String,String> data = message.getData();
        String title = data.get(sharedPreferences.getString(data.get("boxname"),"No Title"));

        //Generating date
        String dateString = DateFormats.dayWithTime(Long.valueOf(data.get("time")));

        //Setting body as formatted date
        String  body = "You have missed "+ data.get("name")+ "on "+dateString;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(100, builder.build());
    }

    private void newBoxNotification(RemoteMessage remoteMessage){

        Map<String,String> data = remoteMessage.getData();

        String title = "New box has been added";
        String body = "Box id: " + data.get("boxid");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(100, builder.build());
    }

    private void boxDeletedNotification(RemoteMessage remoteMessage){

        Map<String,String> data = remoteMessage.getData();

        String title = "A box has been deleted";
        String body = "Box id: " + data.get("boxid");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(100, builder.build());
    }
}
