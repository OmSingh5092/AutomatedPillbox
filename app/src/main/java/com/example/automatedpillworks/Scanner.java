package com.example.automatedpillworks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.automatedpillworks.UserInfo.UserAdditional;
import com.example.automatedpillworks.UserInfo.UserData;
import com.example.automatedpillworks.UserInfo.UserInfoModal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Result;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    ZXingScannerView scannerView;
    FirebaseDatabase database;
    //Firebase Objects
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    DatabaseReference reference;
    StorageReference storage;
    ProgressBar pb;
    Button signup,login;

    UserInfoModal infoModal;
    UserAdditional additional;


    void requestCameraPermissoin(){
        int MY_CAMERA_REQUEST_CODE = 100;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(Scanner.this, new String[] {Manifest.permission.CAMERA},MY_CAMERA_REQUEST_CODE);
        }

    }

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

    void switchToSignup(){
        Intent i = new Intent(Scanner.this,Signup.class);
        startActivity(i);
    }
    void switchToHome(){
        Intent i = new Intent(Scanner.this,Home.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
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
                    infoModal= documentSnapshot.toObject(UserInfoModal.class);
                    GlobalVar.userData = new UserData(infoModal);
                    loadAdditionalData();
                }else{
                    switchToSignup();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //User Have not filled the Signup Form completely

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //Refrencing
        scanner = findViewById(R.id.scanner_qr);
        view = findViewById(R.id.scanner_view);
        pb = findViewById(R.id.scanner_pb);
        signup = findViewById(R.id.scanner_signup);
        login = findViewById(R.id.scanner_login);
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

        //Asking Camera Permission
        checkPermission();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Scanner.this,RegisterActivity.class);
                startActivity(i);
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Scanner.this,LogInActivity.class);
                startActivity(i);
            }
        });


        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(scannerView);

            }
        });

    }

    private static final int REQUEST_CAMERA = 1;

    private boolean checkPermission(){
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);

    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();

                    }
                }
                break;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        scannerView = new ZXingScannerView(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {

        final String code = result.getText();

        Toast.makeText(getApplicationContext(),code,Toast.LENGTH_LONG).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        myRef.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    Intent i = new Intent(Scanner.this,Phone_Auth.class);
                    i.putExtra("boxname",code);
                    i.putExtra("status","1");
                    startActivity(i);

                }
                else{
                    Intent i = new Intent(Scanner.this,Phone_Auth.class);
                    i.putExtra("boxname",code);
                    i.putExtra("status","0");
                    startActivity(i);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
