package com.example.automatedpillworks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Signup1 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    RadioGroup rg,bloodgroup;
    RadioButton bloodselected;
    Button dob,next;
    TextInputEditText weight;
    TextView dob_display;
    String boxname;
    String gender,blood="Blood Group";
    Calendar calendar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1);

        rg = findViewById(R.id.signup1_radiogroup);
        bloodgroup = findViewById(R.id.signup1_bloodgroup);
        dob = findViewById(R.id.signup1_dob);
        dob_display = findViewById(R.id.profile_blooddisplay);
        next = findViewById(R.id.signup1_next);
        weight = findViewById(R.id.signup1_weight);
        boxname = getIntent().getStringExtra("boxname");
        calendar = Calendar.getInstance();

        initialiseRadioGroup();




        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();


        bloodgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                bloodselected = group.findViewById(checkedId);
            }
        });




        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(Signup1.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year,month,dayOfMonth);
                        Date date = new Date(cal.getTimeInMillis());
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
                        myRef.child(boxname).child("info").child("dob").setValue(cal.getTimeInMillis());

                        dob_display.setText(sdf.format(date));



                    }
                },Calendar.YEAR-20, Calendar.MONTH, Calendar.DAY_OF_MONTH);
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
                blood = bloodselected.getText().toString();
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

    void initialiseRadioGroup(){

        String blood[] = this.getResources().getStringArray(R.array.bloodgroup);
        for(int i =0 ;i<blood.length; i++){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(blood[i]);
            bloodgroup.addView(radioButton,i);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        blood = parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
