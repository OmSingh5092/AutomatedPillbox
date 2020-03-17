package com.example.automatedpillworks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class PhoneChange extends AppCompatActivity {

    TextInputEditText phone,otp;
    ConstraintLayout cl,otpview;
    ProgressBar spinner;
    Button submit,verify;
    ImageButton back;

    String user_phone;

    String verificatonid,status;


    public void signInwithCredentials(final PhoneAuthCredential credential) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Intent i = new Intent(PhoneChange.this,Home.class);
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("boxid"));
                    myRef.child("phonenumber").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                    startActivity(i);
                    finish();





                }
                else{
                    Toast.makeText(getApplicationContext(),"The OTP entered is wrong",Toast.LENGTH_SHORT).show();
                    otp.setText("");

                }


            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_change);

        phone = findViewById(R.id.phone_change_number);
        otp = findViewById(R.id.phone_change_otp);
        cl = findViewById(R.id.phone_change_mainview);
        spinner = findViewById(R.id.phone_change_spinner);
        submit = findViewById(R.id.phone_change_submit);
        verify = findViewById(R.id.phone_change_verify);
        otpview = findViewById(R.id.phone_change_otpview);
        back = findViewById(R.id.phone_change_back);

        cl.removeView(otpview);
        //cl.removeView(back);

        spinner.setVisibility(View.GONE);

        status = getIntent().getStringExtra("status");

        final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInwithCredentials(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(),"Verification Failed",Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(),"OTP Sent",Toast.LENGTH_SHORT).show();

                verificatonid = s;
                cl.addView(otpview);
                spinner.setVisibility(View.GONE);
                cl.removeView(verify);
            }
        };



        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                final String phonenumber = "+91" +phone.getText().toString();

                if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().equals(phonenumber)){
                    Toast.makeText(getApplicationContext(),"Enter another phone number",Toast.LENGTH_SHORT).show();
                    spinner.setVisibility(View.GONE);
                }
                else{
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber,60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD,mCallBack);
                }








            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp_number = otp.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificatonid,otp_number);
                signInwithCredentials(credential);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneChange.this.onBackPressed();
            }
        });
    }
}
