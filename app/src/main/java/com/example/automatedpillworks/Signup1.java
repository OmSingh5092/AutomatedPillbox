package com.example.automatedpillworks;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Signup1 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner bloodgroup;
    RadioGroup rg;
    Button dob,next;
    TextInputEditText weight;
    TextView dob_display;
    String boxname;
    String gender,blood="Blood Group";
    Boolean gender_flag = false;
    Boolean bloodgroup_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1);

        bloodgroup = findViewById(R.id.signup1_spinner);
        rg = findViewById(R.id.signup1_radiogroup);
        dob = findViewById(R.id.signup1_dob);
        dob_display = findViewById(R.id.profile_blooddisplay);
        next = findViewById(R.id.signup1_next);
        weight = findViewById(R.id.signup1_weight);
        boxname = getIntent().getStringExtra("boxname");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.bloodgroup,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        bloodgroup.setAdapter(adapter);


        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();


        bloodgroup.setOnItemSelectedListener(this);



        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(Signup1.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                        String dob =String.valueOf(dayOfMonth)+"/"+String.valueOf(month)+"/"+String.valueOf(year);
                        myRef.child(boxname).child("info").child("dob").setValue(dob);

                        dob_display.setText(dob);



                    }
                },2000,1,1);
                datePickerDialog.show();
            }
        });


        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId==(R.id.signup1_radio_male)){
                    gender = "m";

                }
                else if(checkedId == (R.id.signup1_radio_female)){
                    gender  = "f";
                }
                else if(checkedId == (R.id.signup1_radio_others)){
                    gender  = "o";
                }

            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!weight.getText().toString().equals(null)&&!dob_display.getText().toString().equals(null)&& !gender.equals(null) && !blood.equals("Blood Group")){
                    myRef.child(boxname).child("info").child("gender").setValue(gender);
                    myRef.child(boxname).child("info").child("weight").setValue(weight.getText().toString());
                    myRef.child(boxname).child("info").child("blood").setValue(blood);
                    Intent i = new Intent(Signup1.this,Home.class);
                    i.putExtra("boxname",boxname);
                    startActivity(i);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please fill all the entries",Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        blood = parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
