package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.automatedpillworks.CloudMessaging.AsyncTaskSubscribeToTopics;
import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.PatientInfoModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.ActivityAddBoxBinding;
import com.example.automatedpillworks.utils.BasicFunctions;
import com.example.automatedpillworks.utils.DateFormats;
import com.example.automatedpillworks.utils.RadioGroupItemAdder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddBoxActivity extends AppCompatActivity{
    ActivityAddBoxBinding binding;

    //Firebase Objects
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseDatabase database;

    //Selectable Data
    int bloodGroup,gender,weight;
    long dob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBoxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Setting Toolbar
        setSupportActionBar(binding.toolbar);

        //Initialising Firebase Instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //Checking Permission
        checkPermission();

        //RadioButtons
        initRadioButtons();

        //Dob
        initDobButton();

        binding.scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(AddBoxActivity.this)
                        .setOrientationLocked(false)
                        .setPrompt(getResources().getString(R.string.qr_scanner_prompt)).initiateScan();

            }
        });
    }

    void initDobButton(){
        binding.dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Date Picker Dialog
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(AddBoxActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR,year);
                        calendar.set(Calendar.MONTH,month);
                        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        dob = calendar.getTimeInMillis();

                        //Setting date on Button
                        binding.dob.setText(DateFormats.onlyDay(dob));
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                dialog.show();
            }
        });
    }

    void initRadioButtons(){
        RadioGroupItemAdder adder = new RadioGroupItemAdder(this);
        adder.addBloodGroups(binding.bloodGroup);
        adder.addGenders(binding.gender);

        binding.gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                gender = group.indexOfChild(findViewById(checkedId));
            }
        });

        binding.bloodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                bloodGroup = group.indexOfChild(findViewById(checkedId));
            }
        });

    }

    void onSubmit(){

        //Closing Keyboard
        BasicFunctions.hideKeyboard(AddBoxActivity.this);
        if(!isFilled()){
            return;
        }

        final String id = binding.boxid.getText().toString();
        //Checking if box is previously added
        if(GlobalVar.userData.userInfo.boxes.contains(id)){
            Snackbar.make(findViewById(R.id.doctor_name),"Box already added!",Snackbar.LENGTH_SHORT).show();
            return;
        }
        //Checking the permission
        database.getReference().child("boxes").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    addBox(id,binding.boxname.getText().toString());
                    pushInfo(id);
                }else{
                    Snackbar.make(findViewById(R.id.doctor_name),"Box Not Found!", Snackbar.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(findViewById(R.id.doctor_name),databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    void pushInfo(String boxId){
        PatientInfoModel model = new PatientInfoModel(binding.patientName.getText().toString(),
                binding.doctorName.getText().toString(),dob,gender,bloodGroup,weight);
        database.getReference("boxes").child(boxId).child("info").setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddBoxActivity.this, "Box successfully added", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    void addBox(String id, final String name){
        //Starting Async Task to subscribe to FCM topics
        List<String> topics = new ArrayList<String>(){{
            add(name);
        }};

        AsyncTaskSubscribeToTopics asyncTask = new AsyncTaskSubscribeToTopics(topics);
        asyncTask.execute();

        GlobalVar.userData.userInfo.boxes.add(id);
        GlobalVar.userData.userInfo.boxnames.put(id,name);
        firestore.collection(getResources().getString(R.string.firestor_base_user_collection))
                .document(auth.getUid()).update("boxnames",GlobalVar.userData.userInfo.boxnames);
        firestore.collection("users")
                .document(auth.getUid()).update("boxes",FieldValue.arrayUnion(id));
    }

    Boolean isFilled(){
        if(binding.boxname.getText().toString().length() ==0){
            return false;
        }else if(binding.boxid.getText().toString().length()==0){
            return false;
        }else if(binding.patientName.getText().toString().length() ==0){
            return false;
        }else if(binding.doctorName.getText().toString().length()==0){
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
                binding.boxid.setText(result.getContents());
                Toast.makeText(this, "Scanned: ", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_box_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.save){
            onSubmit();
        }
        return super.onOptionsItemSelected(item);
    }
}

