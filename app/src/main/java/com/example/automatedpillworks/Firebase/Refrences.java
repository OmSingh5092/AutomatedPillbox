package com.example.automatedpillworks.Firebase;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Refrences {
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();

    static public class StorageRefrences{
        static public DatabaseReference reminder;

    }
    public static FirebaseAuth auth = FirebaseAuth.getInstance();
    public static FirebaseStorage storageRef = FirebaseStorage.getInstance();





    public static void initialiseRefrences(String boxname){
        StorageRefrences.reminder = database.getReference().child(boxname).child("reminder");
    }



}
