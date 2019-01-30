package com.symbol.symbol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.symbol.validation.Validator;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button signInButton;
    private ProgressDialog signInDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DocumentReference tokenRef;

    private String uid;

    private final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        emailEditText = findViewById(R.id.emailSignInEditText);
        passwordEditText = findViewById(R.id.passwordSignInEditText);
        signInButton = findViewById(R.id.signInButton);
        signInDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        setListeners();

    }

    private void setListeners() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = validateEmail();
                final String password = validatePassword();
                if (email !=null && password != null) {
                    signInDialog.setMessage("Please wait while we checking your credentials");
                    signInDialog.setCancelable(false);
                    signInDialog.show();
                    signInUser(email, password);
                }

            }
        });
    }

    private String validateEmail() {
        if (emailEditText.getText().toString().equals("")) {
            emailEditText.setError(getString(R.string.empty_field_error));
            return null;
        } else {
            if (Validator.validateEmail(emailEditText.getText().toString())) {
                return emailEditText.getText().toString();
            } else {
                emailEditText.setError(getString(R.string.wrong_email_error));
                return null;
            }
        }
    }

    private String validatePassword() {
        if (passwordEditText.getText().toString().equals("")) {
            passwordEditText.setError(getString(R.string.empty_field_error));
            return null;
        } else {
            if (Validator.validatePassword(passwordEditText.getText().toString())) {
                return passwordEditText.getText().toString();
            } else {
                passwordEditText.setError(getString(R.string.wrong_password_error));
                return null;
            }
        }
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    if (user.isEmailVerified()) {
                        uid = user.getUid();
                        tokenRef  = FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(uid)
                                .collection("RegistrationData")
                                .document(uid);
                        Log.w(TAG, "Log in was succeed");
                        signInDialog.dismiss();
                        pushTokenId();
                        updateUI();
                    } else {
                        signInDialog.dismiss();
                        Log.w(TAG, "Log in was not succeed");
                        Toast.makeText(SignInActivity.this,
                                "Could not log in. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    signInDialog.dismiss();
                    Log.w(TAG, "Log in was not succeed");
                    Toast.makeText(SignInActivity.this,
                            "Could not log in. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void pushTokenId() {

        String tokenId = FirebaseInstanceId.getInstance().getToken();
        tokenRef.update("tokenId", tokenId);
    }

    private void updateUI() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        Intent intent = new Intent(SignInActivity.this,SymbolActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
        finish();

    }
}
