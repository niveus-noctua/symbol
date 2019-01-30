package com.symbol.symbol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.symbol.firebase.FirebasePath;
import com.symbol.user.RegistrationData;
import com.symbol.validation.Validator;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText displayNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private Button registerButton;
    private ProgressDialog registrationDialog;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();




    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        registerButton = findViewById(R.id.registerButton);

        registrationDialog = new ProgressDialog(this);

        setListeners();


    }

    private void setListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = validateName();
                final String email = validateEmail();
                final String password = validatePassword();
                final String firstName = validateFirstName();
                final String lastName = validateLastName();
                if (name != null && email !=null && password != null && firstName != null && lastName != null) {
                    registrationDialog.setMessage("Please wait while we creating your account");
                    registrationDialog.setCancelable(false);
                    registrationDialog.show();
                    registerUser(name, email, password, firstName, lastName);
                }

            }
        });
    }

    private String validateLastName() {
        if (lastNameEditText.getText().toString().equals("")) {
            lastNameEditText.setError(getString(R.string.empty_field_error));
            return null;
        } else {
            if (Validator.validateName(lastNameEditText.getText().toString())) {
                return lastNameEditText.getText().toString();
            } else {
                lastNameEditText.setError(getString(R.string.wrong_name_error));
                return null;
            }

        }
    }

    private String validateFirstName() {
        if (firstNameEditText.getText().toString().equals("")) {
            firstNameEditText.setError(getString(R.string.empty_field_error));
            return null;
        } else {
            if (Validator.validateName(firstNameEditText.getText().toString())) {
                return firstNameEditText.getText().toString();
            } else {
                firstNameEditText.setError(getString(R.string.wrong_name_error));
                return null;
            }

        }
    }

    private String validateName() {
        if (displayNameEditText.getText().toString().equals("")) {
            displayNameEditText.setError(getString(R.string.empty_field_error));
            return null;
        } else
            return displayNameEditText.getText().toString();
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

    private void registerUser(final String name, String email, String password, final String firstName, final String lastName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.w(TAG, "Registration complete successfully");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveToFirebase(name, firstName, lastName, user);
                        } else {
                            registrationDialog.dismiss();
                            Log.w(TAG, "Registration failed", task.getException());
                        }
                    }
                });
    }

    private void updateUI() {
        Intent intent = new Intent(RegisterActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
        finish();
    }

    private void saveToFirebase(String nickname, String firstName, String lastName, final FirebaseUser user) {
        final String uid = user.getUid();

        RegistrationData regData = new RegistrationData();
        regData.setDisplayName(nickname)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setImage("default_profile")
                .setThumbImage("default_profile")
                .setStatus("Roses are red, violets are blue")
                .setUid(uid);

        db.collection("Users").document(uid).collection("RegistrationData").document(uid).set(regData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    registrationDialog.dismiss();
                    user.sendEmailVerification();
                    Toast.makeText(RegisterActivity.this,
                            "Registration complete successfully. Please verify your email",
                            Toast.LENGTH_LONG).show();
                    updateUI();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    registrationDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Registration failed",
                            Toast.LENGTH_SHORT).show();
                }
            });
        Map<String, String> inviteCount = new HashMap<>();
        inviteCount.put("count", "0");
        db.collection("Users")
                .document(uid)
                .collection("Invites")
                .document("invitesCount").set(inviteCount);
        db.collection("PublicProfileData").document(uid).set(regData);

    }


}
