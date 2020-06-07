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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.UserMetaDataModel;
import com.example.automatedpillworks.Model.UserProfileModel;
import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.ActivityEditProfileBinding;
import com.example.automatedpillworks.utils.BasicFunctions;
import com.example.automatedpillworks.utils.DateFormats;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    UserProfileModel model;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseStorage storage;
    Integer PICK_IMAGE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setting up toolbar
        setSupportActionBar(binding.toolbar);

        //Setting up Radio groups
        initRadioGroups();

        //Loading Data
        loadData();

        //Initiating Firebase Instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        //Setting up Email
        binding.email.setText(GlobalVar.userData.userInfo.userprofile.email);


        binding.dobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDatePickerDialog();
            }
        });

        binding.radiogroupBlood.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                model.blood = binding.radiogroupBlood.indexOfChild(findViewById(checkedId));
            }
        });

        binding.radiogroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                model.gender = binding.radiogroupGender.indexOfChild(findViewById(checkedId));
            }
        });

    }

    void loadData(){
        model = GlobalVar.userData.userInfo.userprofile;

        binding.profileImage.setImageBitmap(GlobalVar.userData.userAdditional.profileImage);
        binding.firstname.setText(model.firstname);
        binding.lastname.setText(model.lastname);
        binding.dobButton.setText(
                DateFormats.onlyDay(model.dob)
        );
        binding.address.setText(model.address);
        binding.weight.setText(model.weight.toString());

        binding.radiogroupGender.check(
                binding.radiogroupGender.getChildAt(model.gender)
                        .getId()
        );
        binding.radiogroupBlood.check(
                binding.radiogroupBlood.getChildAt(model.blood)
                        .getId()
        );
        binding.changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfilePhoto();
            }
        });
    }

    void saveData(){
        model.firstname = binding.firstname.getText().toString();
        model.lastname = binding.lastname.getText().toString();
        model.address = binding.address.getText().toString();
        model.weight= Long.valueOf(binding.weight.getText().toString());

        if(isFilled()){
            GlobalVar.userData.userInfo.userprofile = model;

            //Pushing Data on FireStore
            pushData();
        }

    }

    void pushData(){
        final Snackbar snackbar = Snackbar.make(binding.getRoot(),"Updating Profile",Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        firestore.collection("users").document(auth.getUid()).update("userprofile",GlobalVar.userData.userInfo.userprofile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        snackbar.dismiss();
                        Snackbar.make(binding.getRoot(),"Profile Updated Successfully",Snackbar.LENGTH_SHORT).show();
                        //Updating Metadata
                        changeMetaData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(binding.getRoot(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    void changeMetaData(){
        UserMetaDataModel metaData = new UserMetaDataModel(
                model.email,
                model.firstname + " " + model.lastname
        );
        firestore.collection("usersMetadata").document(auth.getUid()).set(metaData);
    }



    void initRadioGroups(){
        int i=0;
        for(String blood: getResources().getStringArray(R.array.bloodgroup)){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(blood);
            binding.radiogroupBlood.addView(radioButton,i);
        }

    }

    void initDatePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar tempCalendar = Calendar.getInstance();
                tempCalendar.set(Calendar.YEAR, year);
                tempCalendar.set(Calendar.DAY_OF_MONTH,month);
                tempCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                model.dob = tempCalendar.getTimeInMillis();

                //Setting text on Button
                binding.dobButton.setText(DateFormats.onlyDay(model.dob));
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    boolean isFilled(){
        if(model.firstname.length() ==0){
            Toast.makeText(this, "Please enter FirstName", Toast.LENGTH_SHORT).show();
            return false;
        }else if(model.lastname.length() ==0){
            Toast.makeText(this, "Please enter Lastname", Toast.LENGTH_SHORT).show();
            return false;
        }else if(binding.weight.getText().toString().length() ==0){
            Toast.makeText(this, "Please Enter weight", Toast.LENGTH_SHORT).show();
            return false;
        }else if(model.address.length() ==0){
            Toast.makeText(this, "Please Enter Address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    void changeProfilePhoto(){

        //Setting up intent
        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pictureIntent.setType("image/*");  // 1
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(pictureIntent,"Select Picture"), PICK_IMAGE);  // 4


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == PICK_IMAGE){
            Uri sourceUri = data.getData();
            try{
                File file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);
                openCropActivity(sourceUri, destinationUri);
            }catch (Exception e){
                askStoragePermissoin();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            setUpImage(UCrop.getOutput(data));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(300, 300)
                .withAspectRatio(5f, 5f)
                .start(this);
    }

    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        return file;
    }

    private void setUpImage(Uri imageUri){
        File file = new File(imageUri.getPath());
        try {
            InputStream inputStream = new FileInputStream(file);
            Bitmap profileImage = BitmapFactory.decodeStream(inputStream);

            //Setting image
            GlobalVar.userData.userAdditional.profileImage= profileImage;

            updateImage(profileImage);
            binding.profileImage.setImageBitmap(profileImage);
        }catch (Exception e) {
            e.printStackTrace();
            askStoragePermissoin();
        }
    }

    private void updateImage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = storage.getReference()
                .child(getResources().getString(R.string.firebase_storage_user_dir))
                .child(auth.getCurrentUser().getUid())
                .child(getResources().getString(R.string.firebase_storage_user_profile_image_name))
                .putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar.make(binding.getRoot(),"Upload Successfull!",Snackbar.LENGTH_SHORT).show();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    void askStoragePermissoin(){
        int STORAGE_WRITE_REQUEST_CODE = 100;
        int STORAGE_READ_REQUET_CODE = 100;

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_READ_REQUET_CODE);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_edit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        saveData();
        BasicFunctions.hideKeyboard(this);
        return super.onOptionsItemSelected(item);
    }
}