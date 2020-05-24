package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

public class AddBoxActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    ImageButton qr;
    TextInputEditText boxidInput;
    MaterialButton submit;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    ZXingScannerView scannerView;
    ConstraintLayout cl;
    void addBox(String id){
        GlobalVar.userData.userInfo.boxes.add(id);
        firestore.collection(getResources().getString(R.string.firestor_base_user_collection))
                .document(auth.getUid()).update("boxes", FieldValue.arrayUnion(id));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_box);
        //Setting Toolbar
        MaterialToolbar toolbar = findViewById(R.id.add_box_toolbar);
        setSupportActionBar(toolbar);
        //Refrencing Objects

        qr = findViewById(R.id.add_box_qr);
        boxidInput = findViewById(R.id.add_box_boxid);
        submit = findViewById(R.id.add_box_submit);
        cl = findViewById(R.id.add_box_camera);

        //Initialising Firebase Instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFilled()){
                    scannerView = new ZXingScannerView(AddBoxActivity.this);
                    addBox(boxidInput.getText().toString());
                }
            }
        });

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();

            }
        });
    }

    Boolean isFilled(){
        if(boxidInput.getText().toString().length() ==0){
            return false;
        }

        return true;

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }



    void checkPermission(){
        int requestCode =100;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, requestCode);
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
        scannerView.stopCamera();
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
        //Fixing this code in TextInputEditText
        boxidInput.setText(code);
        setContentView(R.layout.activity_add_box);
    }
}

