package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.PatientInfoModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.ActivityPatientInfoBinding;
import com.example.automatedpillworks.utils.DateFormats;
import com.example.automatedpillworks.utils.RadioGroupItemAdder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class PatientInfoActivity extends AppCompatActivity {
    ActivityPatientInfoBinding binding;

    //Firebase Objects
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseDatabase database;

    PatientInfoModel data;

    //Selectable Data
    int bloodGroup,gender,weight;
    long dob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setting Toolbar
        setSupportActionBar(binding.toolbar);

        //Initialising Firebase Instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if(GlobalVar.currentBox == null){
            Toast.makeText(this, "No Box Added", Toast.LENGTH_SHORT).show();
            return;
        }
        //setting listeners

        //RadioButtons
        initRadioButtons();

        //Dob
        initDobButton();

        //Fill data
        loadData();
    }
    private void onDataLoaded(){

    }

    void loadData(){
        database.getReference("boxes").child(GlobalVar.currentBox).child("info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data= dataSnapshot.getValue(PatientInfoModel.class);
                if(data!=null){
                    fillData();
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.scrollView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void fillData(){
        binding.patientName.setText(data.getPatientName());
        binding.doctorName.setText(data.getDoctorName());
        binding.weight.setText(String.valueOf(data.getWeight()));
        binding.dob.setText(DateFormats.onlyDay(data.getDob()));
        binding.bloodGroup.check(binding.bloodGroup.getChildAt(data.getBloodGroup()).getId());
        binding.gender.check(binding.gender.getChildAt(data.getGender()).getId());
    }

    void initDobButton(){
        binding.dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Date Picker Dialog
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(PatientInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
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

    void pushInfo(String boxId){
        Snackbar.make(binding.getRoot(),"Updating...",Snackbar.LENGTH_INDEFINITE).show();
        PatientInfoModel model = new PatientInfoModel(binding.patientName.getText().toString(),
                binding.doctorName.getText().toString(),dob,gender,bloodGroup,Integer.valueOf(binding.weight.getText().toString()));
        database.getReference("boxes").child(boxId).child("info").setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PatientInfoActivity.this, "Box successfully added", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    Boolean isFilled(){
        if(binding.patientName.getText().toString().length() ==0){
            return false;
        }else if(binding.doctorName.getText().toString().length()==0){
            return false;
        }

        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patient_info_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.save){
            if(!isFilled()){
                return true;
            }
            pushInfo(GlobalVar.currentBox);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}