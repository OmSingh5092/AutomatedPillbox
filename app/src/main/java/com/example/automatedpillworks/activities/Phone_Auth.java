package com.example.automatedpillworks.activities;

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

import com.example.automatedpillworks.R;
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





public class Phone_Auth extends AppCompatActivity {



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

                        if(status.equals("1")){
                            Intent i = new Intent(Phone_Auth.this,Home.class);
                            i.putExtra("boxname",getIntent().getStringExtra("boxname"));
                            startActivity(i);
                            finish();

                        }
                        else if(status.equals("2")){
                            Intent i = new Intent(Phone_Auth.this,Home.class);
                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("boxid"));
                            myRef.child("phonenumber").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                            startActivity(i);
                            finish();
                        }
                        else {
                            Intent i = new Intent(Phone_Auth.this,Signup.class);
                            i.putExtra("boxname",getIntent().getStringExtra("boxname"));
                            startActivity(i);
                            finish();
                        }





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
        setContentView(R.layout.activity_phone__auth);

        phone = findViewById(R.id.phone_auth_number);
        otp = findViewById(R.id.phone_auth_otp);
        cl = findViewById(R.id.phone_auth_mainview);
        spinner = findViewById(R.id.phone_auth_spinner);
        submit = findViewById(R.id.phone_auth_submit);
        verify = findViewById(R.id.phone_auth_verify);
        otpview = findViewById(R.id.phone_auth_otpview);


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


                if(getIntent().getStringExtra("status").equals("1")){

                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                    myRef.child(getIntent().getStringExtra("boxname")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            user_phone = dataSnapshot.child("phonenumber").getValue(String.class);

                            if(user_phone.equals(phonenumber)){
                                PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber,60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD,mCallBack);
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Please enter the registered phonenumber",Toast.LENGTH_SHORT).show();
                                spinner.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


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



    }
}
