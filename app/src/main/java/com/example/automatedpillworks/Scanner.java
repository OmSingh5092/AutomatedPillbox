package com.example.automatedpillworks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.automatedpillworks.UserInfo.UserInfoModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    ZXingScannerView scannerView;
    FirebaseDatabase database;
    UserInfoModal userData;
    FirebaseAuth auth;
    ProgressBar pb;
    Button signup,login;


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

    void loadUserData(){

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



        //Checking the existence of Firebase Users
        if(auth.getCurrentUser()!= null){
            loadUserData();
        }else{

        }
        toggleView();






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
