package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Signup extends AppCompatActivity {
    ImageButton imageButton;
    Button next,gallery;
    ProgressBar spinner;
    TextInputEditText firstname,lastname,address;
    String boxname;

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
                imageButton.setImageBitmap(bitmap);

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
        setContentView(R.layout.activity_signup);


        next = findViewById(R.id.signup_next);
        imageButton = findViewById(R.id.signup_imagebutton);
        gallery = findViewById(R.id.signup_gallery);
        spinner = findViewById(R.id.signup_progessbar);
        firstname = findViewById(R.id.signup_firstname);
        lastname = findViewById(R.id.signup_lastname);
        address = findViewById(R.id.signup_address);

        boxname = getIntent().getStringExtra("boxname");


        spinner.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(Signup.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(Signup.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_READ_REQUET_CODE);
        }


        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!firstname.getText().toString().equals(null) && !lastname.getText().toString().equals(null) &&!address.getText().toString().equals(null)){


                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();

                    myRef.child(boxname).child("info").child("firstname").setValue(firstname.getText().toString());
                    myRef.child(boxname).child("info").child("lastname").setValue(lastname.getText().toString());
                    myRef.child(boxname).child("info").child("address").setValue(address.getText().toString());

                    Intent i = new Intent(Signup.this, Signup1.class);
                    i.putExtra("boxname",boxname);
                    startActivity(i);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please fill all the entries",Toast.LENGTH_SHORT).show();
                }

            }
        });


        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(boxname).child("phonenumber").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
    }
}
