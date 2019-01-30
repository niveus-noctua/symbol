package com.symbol.symbol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button signInButton;
    private Button registerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.signInButton);
        registerButton = findViewById(R.id.registerButton);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
}

    private void updateUI(FirebaseUser user) {
        if (user == null || !user.isEmailVerified()) {

            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(signInIntent);
                    overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                }
            });
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(registerIntent);
                    overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
                }
            });

        } else {
            Intent intent = new Intent(MainActivity.this, SymbolActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.horizontal_translate_shown, R.anim.horizontal_translate_gone);
            finish();
        }
    }
}
