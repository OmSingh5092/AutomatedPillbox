package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.ActivityProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Setting toolbar
        setSupportActionBar(binding.toolbar);

        //Display Data
        displayData();

    }

    void displayData(){

        //Setting General Info
        binding.infoAddress.setText(GlobalVar.userData.userInfo.userprofile.address);
        //Setting Med Info
        binding.medInfoBlood.setText(getResources().getStringArray(R.array.bloodgroup)[GlobalVar.userData.userInfo.userprofile.blood]);
        binding.medInfoGender.setText(getResources().getStringArray(R.array.gender)[GlobalVar.userData.userInfo.userprofile.gender]);
        binding.medInfoWeight.setText(GlobalVar.userData.userInfo.userprofile.weight.toString());

        //Setting User Profile Picture
        binding.image.setImageBitmap(GlobalVar.userData.userAdditional.profileImage);

        //Setting Email
        binding.email.setText(GlobalVar.userData.userInfo.userprofile.email);

        binding.toolbar.setTitle(GlobalVar.userData.userInfo.userprofile.firstname+ " "
                + GlobalVar.userData.userInfo.userprofile.lastname);
        binding.collapseBar.setTitle(GlobalVar.userData.userInfo.userprofile.firstname+ " "
                + GlobalVar.userData.userInfo.userprofile.lastname);

        setSupportActionBar(binding.toolbar);
    }





    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.profile_nav_edit){
            Intent i = new Intent(this, EditProfileActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_nav,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostResume() {


        displayData();
        super.onPostResume();
    }
}