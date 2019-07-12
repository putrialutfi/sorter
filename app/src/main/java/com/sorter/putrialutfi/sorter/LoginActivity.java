package com.sorter.putrialutfi.sorter;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sorter.putrialutfi.sorter.Session.SharedPrefs;

import java.util.Collections;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int REQUEST_CODE = 1;
    private static final String TAG_LOG = "reports";
    private String username;

    private FirebaseAuth mFirebaseAuth;
    private SignInButton loginButton;
    private ProgressBar progressBar;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    SharedPrefs mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Fabric.with(this, new Crashlytics());

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(this);

        mSharedPreferences = SharedPrefs.getInstance();

        if (mSharedPreferences.getLogedIn(LoginActivity.this)) {
            Intent NextScreen = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(NextScreen);
            finish();
        }

        mFirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null) {
                    goToMainPage();
                    Log.d(TAG_LOG, "user is available, go to main activity");
                }
                else {
                    Log.d(TAG_LOG, "user is none");
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthStateListener != null){
            FirebaseAuth.getInstance().signOut();
        }
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                logIn();
                progressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void logIn() {
        Intent signInIntent =  Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            Log.d(TAG_LOG, "SignIn Success");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            Log.d(TAG_LOG, "Login Success");
            GoogleSignInAccount acc = result.getSignInAccount();
            username = acc.getDisplayName();
            Toast.makeText(this, "Masuk sebagai " + acc.getDisplayName(), Toast.LENGTH_SHORT).show();
            AuthCredential credential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
            firebaseAuthWithGoogle(credential);
        }
        else {
            Log.d(TAG_LOG, "Login Failed:( " + result);
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG_LOG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            Log.d(TAG_LOG, "task is successful");
                            goToMainPage();
                        }else{
                            Log.w(TAG_LOG, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void goToMainPage() {
        mSharedPreferences.isLogedIn(this, true);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("username", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Reports : ", "Connection Failed");
    }
}
