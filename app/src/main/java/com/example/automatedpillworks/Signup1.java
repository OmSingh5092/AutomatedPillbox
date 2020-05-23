package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.snapshot.BooleanNode;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Signup1 extends AppCompatActivity  {
    RadioGroup genderRadio,bloodgroupRadio;
    Button dobButton,next;
    TextInputEditText weightInput;
    TextView dobDisplay;
    Integer gender,blood;
    Long dob;
    Double weight;
    Calendar calendar ;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1);
        //Setting toolbar
        Toolbar toolbar = findViewById(R.id.signup1_toolbar);
        setSupportActionBar(toolbar);

        //Refrencing views;
        genderRadio = findViewById(R.id.signup1_radiogroup);
        bloodgroupRadio= findViewById(R.id.signup1_bloodgroup);
        dobButton = findViewById(R.id.signup1_dob);
        dobDisplay = findViewById(R.id.profile_blooddisplay);
        next = findViewById(R.id.signup1_next);
        weightInput = findViewById(R.id.signup1_weight);


        //Calendar object to get current time;
        calendar = Calendar.getInstance();

        //Populating the radio group
        initialiseRadioGroup();

        //Initialising firebase Instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();


        bloodgroupRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                blood = bloodgroupRadio.indexOfChild(group.findViewById(checkedId));
            }
        });

        genderRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                gender = genderRadio.indexOfChild(group.findViewById(checkedId));
            }
        });




        dobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(Signup1.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year,month,dayOfMonth);
                        Date date = new Date(cal.getTimeInMillis());
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
                        dobDisplay.setText(sdf.format(date));
                        dob = cal.getTimeInMillis();
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }
        });





        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFilled()){
                    GlobalVar.signUpTemp.userInfo.gender = gender;
                    GlobalVar.signUpTemp.userInfo.blood = blood;
                    GlobalVar.signUpTemp.userInfo.weight = weight;
                    GlobalVar.signUpTemp.userInfo.dob = dob;
                }
                uploadData();
            }
        });


    }

    void uploadData(){
        firestore.collection(getResources().getString(R.string.firestor_base_user_collection))
                .document(auth.getUid())
                .set(GlobalVar.signUpTemp.userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("firestore", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("firestore","Error uploading document",e);
            }
        });
    }

    Boolean isFilled(){
        if(weightInput.getText().length()==0){
            return false;
        }

        return true;
    }

    void initialiseRadioGroup(){

        String blood[] = this.getResources().getStringArray(R.array.bloodgroup);
        for(int i =0 ;i<blood.length; i++){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(blood[i]);
            bloodgroupRadio.addView(radioButton,i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth.signOut();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
