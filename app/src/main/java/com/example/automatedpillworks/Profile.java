package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Profile extends AppCompatActivity {

    ImageView profileimage;
    Button gallery,dob,bloodgroup;
    TextView dob_display,blood_display;
    EditText firstname,lastname,weight,address;
    ImageButton edit,back;
    String boxname;
    RadioGroup rg;
    ProgressBar spinner;
    RadioButton male,female,others;
    FloatingActionButton submit;
    ConstraintLayout cl;

    String bloodstring,dobstring,genderstring;

    File profile_photo;

    Uri imageUri;

    int PICK_IMAGE =100 , STORAGE_WRITE_REQUEST_CODE=100,STORAGE_READ_REQUET_CODE=100;


    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(300, 300)
                .withAspectRatio(5f, 5f)
                .start(this);
    }

    String currentPhotoPath = "";

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
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE){


            Uri sourceUri = data.getData(); // 1
            File file = null; // 2

            try {

                file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);  // 3
                openCropActivity(sourceUri, destinationUri);  // 4

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

        else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            imageUri = UCrop.getOutput(data);

            File file = new File(imageUri.getPath());


            try {
                InputStream inputStream = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileimage.setImageBitmap(bitmap);

                spinner.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Uploading Photo....",Toast.LENGTH_SHORT).show();


                StorageReference storage = FirebaseStorage.getInstance().getReference();

                storage.child("users/"+boxname+"/profilephoto.jpg").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        spinner.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"Profile Photo Successfully Uploaded",Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        spinner.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"Upload Unsuccessful",Toast.LENGTH_SHORT).show();

                    }
                });




            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileimage = findViewById(R.id.profile_imageview);
        gallery = findViewById(R.id.profile_gallery);
        dob = findViewById(R.id.profile_dob);
        bloodgroup = findViewById(R.id.profile_blood);
        dob_display= findViewById(R.id.profile_dobdisplay);
        blood_display = findViewById(R.id.profile_blooddisplay);
        firstname = findViewById(R.id.profile_firstname);
        lastname = findViewById(R.id.profile_lastname);
        weight = findViewById(R.id.profile_weight);
        address = findViewById(R.id.profile_address);
        edit = findViewById(R.id.profile_edit);
        spinner = findViewById(R.id.profile_progessbar);
        rg = findViewById(R.id.profile_radiogroup);
        male = findViewById(R.id.profile_male);
        female = findViewById(R.id.profile_female);
        others = findViewById(R.id.profile_others);
        submit = findViewById(R.id.profile_submit);
        cl = findViewById(R.id.profile_parent_cl);
        back = findViewById(R.id.profile_back);

        DatabaseReference boxref = FirebaseDatabase.getInstance().getReference();
        boxref.orderByChild("phonenumber").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String fns = null,lns= null,ws= null,ads= null,dobs= null,bs= null,gender= null;
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    boxname = snap.getKey();
                    fns = snap.child("info").child("firstname").getValue(String.class);
                    lns = snap.child("info").child("lastname").getValue(String.class);
                    ws =  snap.child("info").child("weight").getValue(String.class);
                    ads = snap.child("info").child("address").getValue(String.class);
                    dobs = snap.child("info").child("dob").getValue(String.class);
                    bs = snap.child("info").child("blood").getValue(String.class);
                    gender = snap.child("info").child("gender").getValue(String.class);

                }

                firstname.setText(fns);
                lastname.setText(lns);
                weight.setText(ws);
                address.setText(ads);
                dob_display.setText(dobs);
                blood_display.setText(bs);

                if(gender.equals("m")){
                    male.setChecked(true);
                }
                else if(gender.equals("f")){
                    female.setChecked(true);
                }
                else if(gender.equals("o")){
                    others.setChecked(true);
                }

                try {
                    profile_photo = File.createTempFile("profile","jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                storageReference.child("users/"+boxname+"/profilephoto.jpg").getFile(profile_photo).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        InputStream inputStream = null;
                        try {
                            inputStream = new FileInputStream(profile_photo);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        spinner.setVisibility(View.GONE);

                        profileimage.setImageBitmap(bitmap);

                    }
                });

                //Toast.makeText(getApplicationContext(),boxname,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        firstname.setEnabled(false);
        lastname.setEnabled(false);
        weight.setEnabled(false);
        address.setEnabled(false);
        dob.setEnabled(false);
        bloodgroup.setEnabled(false);
        weight.setEnabled(false);
        rg.setEnabled(false);
        cl.removeView(submit);







        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(Profile.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_REQUEST_CODE);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(Profile.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_READ_REQUET_CODE);
                }

                Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pictureIntent.setType("image/*");  // 1
                pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);  // 2
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String[] mimeTypes = new String[]{"image/jpeg", "image/png"};  // 3
                    pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                }
                startActivityForResult(Intent.createChooser(pictureIntent,"Select Picture"), PICK_IMAGE);  // 4

            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstname.setEnabled(true);
                lastname.setEnabled(true);
                weight.setEnabled(true);
                address.setEnabled(true);
                dob.setEnabled(true);
                bloodgroup.setEnabled(true);
                weight.setEnabled(true);
                rg.setEnabled(true);
                edit.setEnabled(false);

                //if(condition)
                cl.addView(submit);

            }
        });



        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(male.isChecked()){
                    genderstring = "m";
                }
                else if(female.isChecked()){
                    genderstring ="f";
                }
                else if(others.isChecked()){
                    genderstring = "o";
                }
            }
        });

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(Profile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                        dobstring =String.valueOf(dayOfMonth)+"/"+String.valueOf(month)+"/"+String.valueOf(year);


                        dob_display.setText(dobstring);



                    }
                },2000,1,1);
                datePickerDialog.show();
            }
        });

        bloodgroup.setOnClickListener(new View.OnClickListener() {

            String[] blood_list;
            @Override
            public void onClick(View v) {

                blood_list = getResources().getStringArray(R.array.bloodgroup1);
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Select BloodGroup").setAdapter(new ArrayAdapter(Profile.this, android.R.layout.simple_list_item_1, blood_list),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                bloodstring = blood_list[which];
                                blood_display.setText(bloodstring);



                            }
                        });
                builder.create();
                builder.show();
            }
        });
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if(dobstring.equals(null) || bloodstring.equals(null) || firstname.getText().toString().equals(null) || lastname.getText().toString().equals(null) ||address.getText().toString().equals(null)|| weight.getText().toString().equals(null) || genderstring.equals(null)){
                    Toast.makeText(getApplicationContext(),"Please fill the entries properly", Toast.LENGTH_LONG).show();
                }v  */
                myRef.child(boxname).child("info").child("dob").setValue(dobstring);
                myRef.child(boxname).child("info").child("blood").setValue(bloodstring);
                myRef.child(boxname).child("info").child("firstname").setValue(firstname.getText().toString());
                myRef.child(boxname).child("info").child("lastname").setValue(lastname.getText().toString());
                myRef.child(boxname).child("info").child("address").setValue(address.getText().toString());
                myRef.child(boxname).child("info").child("weight").setValue(weight.getText().toString());
                myRef.child(boxname).child("info").child("gender").setValue(genderstring);

                Toast.makeText(getApplicationContext(),"Changes Have Been Done.",Toast.LENGTH_SHORT).show();
                edit.setEnabled(true);

                firstname.setEnabled(false);
                lastname.setEnabled(false);
                weight.setEnabled(false);
                address.setEnabled(false);
                dob.setEnabled(false);
                bloodgroup.setEnabled(false);
                weight.setEnabled(false);
                rg.setEnabled(false);
                cl.removeView(submit);



            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Profile.this,Home.class);
                startActivity(i);
                finish();

            }
        });







    }
}
