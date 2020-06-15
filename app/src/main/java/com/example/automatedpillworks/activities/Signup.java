package com.example.automatedpillworks.activities;

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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.automatedpillworks.GlobalVar;
import com.example.automatedpillworks.Model.UserAdditional;
import com.example.automatedpillworks.Model.UserData;
import com.example.automatedpillworks.Model.UserInfoModel;
import com.example.automatedpillworks.Model.UserProfileModel;
import com.example.automatedpillworks.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Signup extends AppCompatActivity {
    ImageView imageButton;
    Button next,gallery;
    ProgressBar pb;
    TextInputEditText firstname,lastname,address,phonenumber;
    String boxname;
    Bitmap profileImage;

    UserProfileModel profileData;
    UserInfoModel userInfo;
    UserAdditional userAdditional;
    UserData userData;

    //Firebase Objects
    StorageReference storage;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    Boolean isPhotoUploaded = false;

    Uri imageUri;

    int PICK_IMAGE =100 , STORAGE_WRITE_REQUEST_CODE=100,STORAGE_READ_REQUET_CODE=100;

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(300, 300)
                .withAspectRatio(5f, 5f)
                .start(this);
    }

    private File getImageFile() throws IOException {
        String currentPhotoPath = "";
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
                profileImage = BitmapFactory.decodeStream(inputStream);
                imageButton.setImageBitmap(profileImage);
                uploadImage();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void askStoragePermissoin(){

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(Signup.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(Signup.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_READ_REQUET_CODE);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Refrencing Views
        next = findViewById(R.id.signup_next);
        imageButton = findViewById(R.id.signup_imagebutton);
        gallery = findViewById(R.id.signup_gallery);
        pb = findViewById(R.id.singup_progress);
        firstname = findViewById(R.id.signup_firstname);
        lastname = findViewById(R.id.signup_lastname);
        address = findViewById(R.id.signup_address);
        phonenumber = findViewById(R.id.signup_phonenumber);

        //Firebase Instantation
        storage = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //Asking Storage Permission
        askStoragePermissoin();

        //Filling details in fields
        fillDetails();

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pictureIntent.setType("image/*");  // 1
                pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(pictureIntent,"Select Picture"), PICK_IMAGE);  // 4
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(isFilled()){
                profileData = new UserProfileModel(firstname.getText().toString(),lastname.getText().toString(),
                        phonenumber.getText().toString(),address.getText().toString(),auth.getCurrentUser().getEmail());
                userAdditional = new UserAdditional(profileImage);

                userInfo = new UserInfoModel();
                userInfo.setUserprofile(profileData);
                if(GlobalVar.Initialisation.scannedBox !=null){
                    userInfo.addNewBox(GlobalVar.Initialisation.scannedBox);
                }
                userData = new UserData();
                userData.setUserInfo(userInfo);
                userData.setUserAdditional(userAdditional);

                uploadData();
            }

            }
        });
    }

    void fillDetails(){
    }

    void swithActivity(){
        Intent i = new Intent(Signup.this,Scanner.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    void uploadImage(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = storage
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
                isPhotoUploaded= true;
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                long d = (taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount())*100;
                pb.setProgress((int)d);
            }
        });
    }

    void uploadData(){
        firestore.collection(getResources().getString(R.string.firestor_base_user_collection))
                .document(auth.getUid())
                .set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("firestore", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("firestore","Error uploading document",e);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                makeCollectionForTokens();
            }
        });
    }

    void makeCollectionForTokens(){
        Map<String, List<String>> tempdata = new HashMap<>();
        tempdata.put(("tokens"),new ArrayList<String>());
        firestore.collection("registrationToken").document(auth.getUid()).set(tempdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                GlobalVar.userData = userData;
                swithActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    Boolean isFilled(){
        if(firstname.getText().toString().length()==0){
            return false;
        }else if(!isPhotoUploaded){
            return false;
        }
        return true;
    }
}
