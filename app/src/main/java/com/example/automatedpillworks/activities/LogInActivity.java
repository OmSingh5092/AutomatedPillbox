package com.example.automatedpillworks.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.automatedpillworks.R;
import com.example.automatedpillworks.databinding.ActivityLogInBinding;
import com.example.automatedpillworks.utils.BasicFunctions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogInActivity extends AppCompatActivity {

    ActivityLogInBinding binding;
    TextInputEditText username,password;
    ImageButton google,phone;
    Button submit;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    int RC_SIGN_IN = 100;
    String TAG = "GoogleSignin";

    void switchActivity(){
        Intent i =new Intent(LogInActivity.this,Scanner.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    void generateUser(String user, String pass){
        auth.createUserWithEmailAndPassword(user,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                switchActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    void LoginWithUsernamePassword(final String user, final String pass){
        auth.signInWithEmailAndPassword(user,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if(authResult.getAdditionalUserInfo().isNewUser()){
                    generateUser(user,pass);
                    return;
                }
                Toast.makeText(LogInActivity.this, getResources().getText(R.string.login_successfull), Toast.LENGTH_SHORT).show();
                switchActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Generate Users
                generateUser(user,pass);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Setting Toolbar
        MaterialToolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        //Refrencing
        username = findViewById(R.id.login_username);
        password = findViewById(R.id.login_password);
        google = findViewById(R.id.login_google);
        submit =findViewById(R.id.login_submit);
        //Firebase Instance
        auth = FirebaseAuth.getInstance();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasicFunctions.hideKeyboard(LogInActivity.this);
                if(isfilled()){
                    LoginWithUsernamePassword(username.getText().toString(), password.getText().toString());
                }

            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGoogleClient();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    void getGoogleClient(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signIn();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
            mGoogleSignInClient.signOut();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Signing out of google account
                            // Sign in success, update UI with the signed-in user's information
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                isNewUser();
                            }
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            switchActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    void isNewUser(){
        Intent i = new Intent(this,Signup.class);
        startActivity(i);
    }

    void updateUI(FirebaseUser user){

    }

    Boolean isfilled(){
        if(username.getText().length()==0){
            return false;
        }


        return true;

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
