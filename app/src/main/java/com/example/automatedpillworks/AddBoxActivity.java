package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.snackbar.Snackbar;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AddBoxActivity extends AppCompatActivity{
    ImageButton qr;
    TextInputEditText boxinput,boxname;
    MaterialButton submit;
    FirebaseFirestore firestore;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ConstraintLayout cl;



    void addBox(String id,String name){
        GlobalVar.userData.userInfo.boxes.add(id);
        GlobalVar.userData.userInfo.boxnames.put(id,name);
        firestore.collection(getResources().getString(R.string.firestor_base_user_collection))
                .document(auth.getUid()).update("boxnames",GlobalVar.userData.userInfo.boxnames);
        firestore.collection("users")
                .document(auth.getUid()).update("boxes",FieldValue.arrayUnion(id));
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
        boxinput = findViewById(R.id.add_box_boxid);
        submit = findViewById(R.id.add_box_submit);
        boxname = findViewById(R.id.add_box_boxname);

        //Initialising Firebase Instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //Checking Permission
        checkPermission();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFilled()){
                    final String id = boxinput.getText().toString();
                    //Checking if box is previously added
                    if(GlobalVar.userData.userInfo.boxes.contains(id)){
                        Snackbar.make(findViewById(R.id.add_box_layout),"Box already added!",Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    //Checking the permission
                    database.getReference().child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                addBox(id,boxname.getText().toString());
                            }else{
                                Snackbar.make(findViewById(R.id.add_box_layout),"Box Not Found!", Snackbar.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Snackbar.make(findViewById(R.id.add_box_layout),databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
                        }
                    });


                }
            }
        });

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new IntentIntegrator(AddBoxActivity.this)
                        .setOrientationLocked(false)
                        .setPrompt(getResources().getString(R.string.qr_scanner_prompt)).initiateScan();

            }
        });
    }

    Boolean isFilled(){
        if(boxinput.getText().toString().length() ==0){
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Setting boxid in the edit text
                boxinput.setText(result.getContents());
                Toast.makeText(this, "Scanned: ", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

