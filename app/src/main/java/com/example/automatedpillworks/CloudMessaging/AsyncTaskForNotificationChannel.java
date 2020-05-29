package com.example.automatedpillworks.CloudMessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.R;

public class AsyncTaskForNotificationChannel extends AsyncTask {
    Context context;

    public AsyncTaskForNotificationChannel(Context context){
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.channel_name);
                String description = context.getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(GlobalVar.CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }


            return null;
        }

    }
}
