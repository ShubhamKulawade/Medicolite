package com.android.doctorAppointment.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import static com.android.doctorAppointment.utility.ErrorConstantString.EMAIL_ERROR;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
    }

    private void init(){
        TextInputLayout textInputLayout=findViewById(R.id.inputEmailForgot);
        //EditText email=textInputLayout.getEditText();

        findViewById(R.id.textViewLoginLink3).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.textViewRegisterLink3).setOnClickListener(view -> startActivity(new Intent(this,SignUp.class)));

        findViewById(R.id.buttonSendPassword).setOnClickListener(view -> {
            if (!Patterns.EMAIL_ADDRESS.matcher(textInputLayout.getEditText().getText()).matches()) {
                textInputLayout.setError(EMAIL_ERROR);
            }
            else
            {
                sendPasswordResetEmail(textInputLayout.getEditText().getText().toString());
            }
        });

       /* email.addTextChangedListener(new EditTextValidator(email) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    textView.setError(EMAIL_ERROR);
                }
            }
        });*/
    }

    private void sendPasswordResetEmail(String email)
    {
        FirebaseAuth auth=FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> Toast.makeText(this, "Password Reset Link sent to "+email, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
            String errorMessage=e.getMessage();
            switch(errorMessage)
            {
                case "ERROR_QUOTA_EXCEEDED":
                {
                    Toast.makeText(this, "Quota Exceeded", Toast.LENGTH_SHORT).show();
                    break;
                }

                case "ERROR_RETRY_LIMIT_EXCEEDED":
                {
                    Toast.makeText(this, "Please Wait before Requesting Another Email",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case "ERROR_CANCELED":
                {
                    Toast.makeText(this, "Email not Sent", Toast.LENGTH_SHORT).show();
                    break;
                }


            }
        });
    }

}