package com.example.automatedpillworks.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.automatedpillworks.CloudMessaging.AsyncTaskForNotificationChannel;
import com.example.automatedpillworks.CloudMessaging.AsyncTaskSubscribeToTopics;
import com.example.automatedpillworks.CloudMessaging.NotificationService;
import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.UserMetaDataModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.Model.UserAdditional;
import com.example.automatedpillworks.Model.UserData;
import com.example.automatedpillworks.Model.UserInfoModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CAMERA;

public class Scanner extends AppCompatActivity{

    //Firebase Objects
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    DatabaseReference reference;
    StorageReference storage;
    ProgressBar pb;
    Button signup,login;

    UserInfoModel infoModal;
    UserAdditional additional;
    UserMetaDataModel metadata;

    ImageButton scanner;
    ConstraintLayout view;


    void toggleView(){
        //Removing Progressbar
        pb.setVisibility(View.GONE);
        //Making the view Visible with animation
        if(view.getVisibility() != View.VISIBLE){
            view.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0,
                    0,
                    view.getHeight(),
                    0);
            animate.setDuration(5000);
            animate.setFillAfter(true);
            view.startAnimation(animate);
        } else {
            view.setVisibility(View.INVISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0,
                    0,
                    0,
                    view.getHeight());
            animate.setDuration(5000);
            animate.setFillAfter(true);
            view.startAnimation(animate);
        }
    }

    //Should be called when user Data has been loaded
    void executeFCMTasks(){

        //Setting the retrieved boxes names in shared preferences to be used by messaging service
        SharedPreferences sharedPreferences = getSharedPreferences("BOX_NAMES", Context.MODE_PRIVATE);
        for(Map.Entry<String,String> entry: GlobalVar.userData.userInfo.boxnames.entrySet()){
            sharedPreferences.edit().putString(entry.getKey(),entry.getValue());
        }

        //Starting the Async Tasks
        AsyncTaskForNotificationChannel channelTask = new AsyncTaskForNotificationChannel(getApplicationContext());
        AsyncTaskSubscribeToTopics task = new AsyncTaskSubscribeToTopics(GlobalVar.userData.userInfo.boxes);
        task.execute();
        channelTask.execute();
    }

    void switchToSignup(){
        Intent i = new Intent(Scanner.this,Signup.class);
        startActivity(i);
        finish();
    }

    void switchToHome(){
        Intent i = new Intent(Scanner.this,Home.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    void loadAdditionalData(){
        try {
            final File tempFile = File.createTempFile("image","jpeg");
            storage.child(getResources().getString(R.string.firebase_storage_user_dir))
                    .child(auth.getUid())
                    .child(getResources().getString(R.string.firebase_storage_user_profile_image_name))
                    .getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap temp = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                    GlobalVar.userData.userAdditional = new UserAdditional(temp);

                    Toast.makeText(Scanner.this, "Welcome!", Toast.LENGTH_SHORT).show();
                    switchToHome();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void loadUserData(){
        firestore.collection(getResources().getString(R.string.firestor_base_user_collection))
                .document(auth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    infoModal= documentSnapshot.toObject(UserInfoModel.class);
                    GlobalVar.userData.userInfo = infoModal;
                    executeFCMTasks();
                    loadAdditionalData();
                }else{
                    switchToSignup();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Scanner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //Referencing
        scanner = findViewById(R.id.scanner_qr);
        view = findViewById(R.id.scanner_view);
        pb = findViewById(R.id.scanner_pb);
        signup = findViewById(R.id.scanner_signup);
        //login = findViewById(R.id.scanner_login);
        //Instancing Auth
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();


        //Checking the existence of Firebase Users
        if(auth.getCurrentUser()!= null){
            loadUserData();
        }else{
            toggleView();
        }

        //Asking All Permission
        checkPermission();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Scanner.this,LogInActivity.class);
                startActivity(i);
            }
        });


        /*login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Scanner.this,LogInActivity.class);
                startActivity(i);
            }
        });  */


        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(Scanner.this)
                        .setOrientationLocked(false)
                        .setPrompt(getResources().getString(R.string.qr_scanner_prompt))
                        .initiateScan();

            }
        });

    }

    private static final int REQUEST_CAMERA = 1;

    private boolean checkPermission(){
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);

    }

    void switchActivity(){
        Intent i = new Intent(this,RegisterActivity.class);
        startActivity(i);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Setting boxid
                GlobalVar.signUpTemp.userInfo.boxes.add(result.getContents());
                GlobalVar.signUpTemp.userInfo.boxnames.put(result.getContents(),getResources().getString(R.string.default_box_name));
                Toast.makeText(this, "Scanned:"+result.getContents(), Toast.LENGTH_LONG).show();
                switchActivity();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
