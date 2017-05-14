/**
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.blue.mytalk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blue.mytalk.Activity.LoginActivity;
import com.example.blue.mytalk.Activity.MainActivity;
import com.example.blue.mytalk.Database.DatabaseManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private FirebaseAuth mFirebaseAuth;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private Button mSignInButton;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private EditText mEmailField;
    private EditText mPasswordField;
    private CallbackManager mCallbackManager;
    // Firebase instance variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {

            finish();
            overridePendingTransition(0, 0);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_sign_in);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mSignInButton = (Button) findViewById(R.id.sign_in_button);

        mSignInButton.setOnClickListener(this);

        // Set click listeners


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        faceBookLogin();
        signInMail();
        resetPass();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                showProgressDialog();
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            signSucces();


                        }
                    }
                });
    }

    private void faceBookLogin() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        Button loginButton = (Button) findViewById(R.id.button_facebook_login);
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showProgressDialog();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);

            }
        });

loginButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("public_profile","email"));

    }
});
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            signSucces();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }


                    }
                });
    }

    private void signSucces() {
        final SaveLoad saveLoad = new SaveLoad(SignInActivity.this);
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        final String uid = firebaseUser.getUid();
        saveLoad.saveString(SaveLoad.UID, uid);
        DatabaseManager databaseManager = new DatabaseManager(this);
        try {

            databaseManager.creatNofriend(uid);
            databaseManager.creatUser(uid);
            Log.e("uid", uid);
        } catch (Exception ignored) {

        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(uid).child("idtoken");
        String token = FirebaseInstanceId.getInstance().getToken();
        databaseReference.setValue(token, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (saveLoad.loadString(SaveLoad.NAME + uid, "").equals("")) {

                    Intent intent = new Intent(SignInActivity.this, LoginActivity.class);

                    startActivity(intent);
                } else {
                    finish();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));

                }
            }
        });
    }

    private void createAccount(final String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();


        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            setErros(task);


                        } else {
                            openDialogCreat(email);
                            sendEmailVerification();
                        }
                        hideProgressDialog();

                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {

        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START sign_in_with_email]
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            setErros(task);

                            hideProgressDialog();
                        } else {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            assert firebaseUser != null;
                            if (firebaseUser.isEmailVerified()) {
                                signSucces();
                            } else {

                                openDialogVerify();
                            }

                        }


                    }
                });

    }

    private void sendEmailVerification() {


        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button


                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(SignInActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]

    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {

            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {

            valid = false;
        } else {

        }

        return valid;
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void signInMail() {
        Button buttonSign = (Button) findViewById(R.id.email_sign_in_button);
        Button buttonCreat = (Button) findViewById(R.id.email_create_account_button);
        buttonSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
        buttonCreat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });
    }

    private void openDialogVerify() {
        hideProgressDialog();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.xacnhanmail);

        alertDialogBuilder
                .setMessage(R.string.tinnhanxacnhan)
                .setCancelable(true)
                .setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                                mFirebaseAuth.signOut();
                            }
                        })

                .setNegativeButton(R.string.guimai,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                sendEmailVerification();
                                mFirebaseAuth.signOut();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    private void openDialogResetPass(final String email) {
        hideProgressDialog();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.xacnhanmail);

        alertDialogBuilder
                .setMessage(mEmailField.getText().toString())
                .setCancelable(true)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {


                                mFirebaseAuth.sendPasswordResetEmail(email);

                            }
                        })

                .setNegativeButton(R.string.No,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();

                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    private void resetPass() {
        TextView textpass = (TextView) findViewById(R.id.resetpass);
        textpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                if (!email.equals("")) {
                    openDialogResetPass(email);
                } else {
                    mEmailField.setError(getString(R.string.nhapmail));
                }
            }
        });
    }

    private void openDialogCreat(String mail) {
        hideProgressDialog();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setTitle(R.string.guimai);

        alertDialogBuilder
                .setMessage(getString(R.string.thongbaoxacnhan) + " "+mail)
                .setCancelable(true)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });


        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    private void setErros( Task<AuthResult> task){
        try {
            throw task.getException();
        } catch(FirebaseAuthWeakPasswordException e) {
            mPasswordField.setError(getString(R.string.loipass));
            mPasswordField.requestFocus();
        } catch(FirebaseAuthInvalidCredentialsException e) {
            Log.e("err",e.toString());
            if (e.toString().equals("com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted.")) {
                mEmailField.setError(getString(R.string.loiEmailSai));
                mEmailField.requestFocus();
            }else {
                mPasswordField.setError(getString(R.string.loipass));
                mPasswordField.requestFocus();
            }
        } catch (FirebaseAuthInvalidUserException e){
            mEmailField.setError(getString(R.string.loikhongCoMail));
            mEmailField.requestFocus();
        }
        catch(FirebaseAuthUserCollisionException e) {
            mEmailField.setError(getString(R.string.loiDaCoEmail));
            mEmailField.requestFocus();
        } catch(Exception e) {
           if (e.toString().equals("com.google.firebase.FirebaseException: An internal error has occurred. [ WEAK_PASSWORD  ]")){
               mPasswordField.setError(getString(R.string.minpass));
               mPasswordField.requestFocus();
           }else {
               Toast.makeText(SignInActivity.this, R.string.auth_failed,
                       Toast.LENGTH_SHORT).show();
           }

        }}
}

