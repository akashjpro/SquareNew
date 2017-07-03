package com.example.tmha.square.activity;

import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tmha.square.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



/*
 * Classname: LoginActivity
 *
 * Version information
 *
 * Date:06/07/2017
 *
 * Copyright
 *
 * Created by tmha on 06/7/2017
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TAG";
    private final int RC_SIGN_IN = 123;
    private Button btnLogin, btnGoogle, btnFacebook;
    private ImageView mImgIcon;
    private TextView mTxtTitle;
    private DatabaseReference mDatabase;
    private LinearLayout mLayout;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //add cotrols
        addControls();
        addEvents();

    }

    private void addControls() {

        mFirebaseAuth = FirebaseAuth.getInstance();

        btnLogin    = (Button) findViewById(R.id.buttonLogin);
        btnGoogle   = (Button) findViewById(R.id.buttonGoogle);
        btnFacebook = (Button) findViewById(R.id.buttonFacebook);
        mImgIcon    = (ImageView) findViewById(R.id.imgIconApp);
        mTxtTitle   = (TextView) findViewById(R.id.txtTitle);
        mLayout     = (LinearLayout) findViewById(R.id.activity_login);

        getSupportActionBar().hide();
        final TransitionDrawable transitionDrawable = (TransitionDrawable) mImgIcon.getDrawable();
        transitionDrawable.startTransition(2000);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transitionDrawable.reverseTransition(2000);
            }
        },2000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImgIcon.setVisibility(View.GONE);
                mTxtTitle.setVisibility(View.GONE);
                mLayout.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("Login");
                getSupportActionBar().show();
            }
        }, 4000);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Hello").setValue("KKKKKKKKKKKKKKKK");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
//                        .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if (user != null){
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
                }
            }
        };
    }

    private void addEvents() {
        btnLogin.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonLogin:
                    login();
                break;
            case R.id.buttonGoogle:
                loginWithGoogle();
                break;
            case R.id.buttonFacebook:
                break;
        }
    }

    private void loginWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void login(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this, "Fail authenticate with Google", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
