package com.android.doctorAppointment.Authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.myInterface.FirebaseEmailVerificationListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerification extends AppCompatActivity {
    private FirebaseAuth auth;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        auth = FirebaseAuth.getInstance();
        init();
    }

    private void init() {
        coordinatorLayout = findViewById(R.id.emailVerificationCoordRoot);
        FirebaseEmailVerificationListener firebaseEmailVerificationListener = success -> {
            if (success) {
                String message = "Email Verification link sent";
                setSnackBar(coordinatorLayout, message);
            } else {
                Toast.makeText(this, "Failed to Send Email Verification", Toast.LENGTH_SHORT).show();
            }
        };

        findViewById(R.id.buttonSendVerification).setOnClickListener(view -> {
            /*if (!Patterns.EMAIL_ADDRESS.matcher(textInputLayout.getEditText().getText())
            .matches())
                textInputLayout.setError("Invalid Email");
            else {*/
            sendEmailVerification(firebaseEmailVerificationListener);
            //}
        });


        findViewById(R.id.textViewLoginLinkVerification).setOnClickListener(view -> {
            startActivity(new Intent(this, SignIn.class));
            finish();
        });

        findViewById(R.id.textViewRegisterLinkVerification).setOnClickListener(view -> {
            startActivity(new Intent(this, SignUp.class));
            finish();
        });
    }

    private void sendEmailVerification(FirebaseEmailVerificationListener verificationListener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task ->
                    verificationListener.onResult(task.isSuccessful()))
                    .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(),
                            Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(this, "Failed to get Current User", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignIn.class));
            finish();
        }
    }

    private void setSnackBar(View root, String title) {
        Snackbar.make(root, title, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", view -> {
                    startActivity(new Intent(this, SignIn.class));
                    finish();
                })
                .show();
    }
}