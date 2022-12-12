package com.android.doctorAppointment.Authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.myInterface.FirebaseEmailVerificationListener;
import com.android.doctorAppointment.profile.DoctorProfile;
import com.android.doctorAppointment.profile.UserProfile;
import com.android.doctorAppointment.utility.EditTextValidator;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import static com.android.doctorAppointment.utility.ErrorConstantString.CANCELLED_BY_USER;
import static com.android.doctorAppointment.utility.ErrorConstantString.EMPTY_FIELD;
import static com.android.doctorAppointment.utility.ErrorConstantString.PASSWORD_NOT_MATCH;
import static com.android.doctorAppointment.utility.ErrorConstantString.PASSWORD_VALIDATION;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.USER;


@SuppressLint("LogNotTimber")
public class SignUp extends AppCompatActivity {
    private static final String TAG = "Sign Up";
    private TextInputLayout email, password, confirmPassword;
    private FirebaseAuth mAuth;
    private String userType;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this, "Creating Account...");
        loadingDialog.setCancelable(false);
        init();
    }

    private void init() {
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        materialButtonToggleGroup = findViewById(R.id.materialButtonToggleGroup2);
        coordinatorLayout = findViewById(R.id.signUpCoordRoot);

        email.getEditText().addTextChangedListener(new EditTextValidator(email.getEditText()) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches())
                    textView.setError("Invalid Email");
            }
        });
        findViewById(R.id.textViewLoginLink).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.buttonRegister).setOnClickListener(view -> {
            loadingDialog.show();
            if (validate(email, password, confirmPassword))
                signUp(email, password);
            else
                loadingDialog.dismiss();

        });

    }

    private void signUp(TextInputLayout email, TextInputLayout password) {
        setUserAccount(materialButtonToggleGroup);
        String usernameString = email.getEditText().getText().toString();
        String passwordString = password.getEditText().getText().toString();

        FirebaseEmailVerificationListener firebaseEmailVerificationListener = success -> {
            if (success) {
                String message = "Email Verification sent to " + usernameString;
                setSnackBar(coordinatorLayout, message);
                if (userType.equals(USER)) {
                    createEmptyCollection(USER);
                    Intent intent = new Intent(this, UserProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(intent);
                    //finish();
                } else if (userType.equals(DOCTOR)) {
                    createEmptyCollection(DOCTOR);
                    Intent intent = new Intent(this, DoctorProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(intent);
                    //finish();
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "signUp: Else");
                }
            } else {
                Toast.makeText(this, "Failed to Send Email Verification", Toast.LENGTH_SHORT).show();
            }
        };


        mAuth.createUserWithEmailAndPassword(usernameString, passwordString)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        sendEmailVerification(firebaseEmailVerificationListener);
                    } else {
                        Toast.makeText(this, "" + task.getException().getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCanceledListener(() -> {
                    Toast.makeText(this, CANCELLED_BY_USER,
                            Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                });
    }

   /* private boolean validate() {
        if (email != null && !TextUtils.isEmpty(email.getEditText().getText())) {
            return true;
        } else if (password != null && !TextUtils.isEmpty(password.getEditText().getText())) {
            return true;
        } else
            return password != null && password.getEditText().getText().equals(confirmPassword
            .getEditText().getText());
    }*/


    private void sendEmailVerification(FirebaseEmailVerificationListener verificationListener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task ->
                    verificationListener.onResult(task.isSuccessful()))
                    .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(),
                            Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(this, "Failed to get Current User", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignIn.class));
            finish();
        }
    }

    private boolean validate(TextInputLayout email, TextInputLayout password,
                             TextInputLayout confirmPassword) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern p = Pattern.compile(regex);

        boolean test =
                confirmPassword.getEditText().getText().toString().trim().equals(password.getEditText().getText().toString().trim());
        String passwordStr=password.getEditText().getText().toString();
        if (email != null) {
            if (TextUtils.isEmpty(email.getEditText().getText())) {
                email.setError(EMPTY_FIELD);
                return false;
            } else
                email.setError(null);

        } else return false;
        if (password != null) {
            if (TextUtils.isEmpty(password.getEditText().getText())) {
                password.setError(EMPTY_FIELD);
                return false;
            }else if(!p.matcher(passwordStr).matches()){
                password.setError(null);
                password.setError(PASSWORD_VALIDATION);
                return false;
            }

            else
                password.setError(null);
        } else return false;
        if (confirmPassword != null) {
            if (TextUtils.isEmpty(confirmPassword.getEditText().getText())) {
                confirmPassword.setError(EMPTY_FIELD);
                return false;
            } else if (!test) {
                confirmPassword.setError(PASSWORD_NOT_MATCH);
                return false;
            } else
                confirmPassword.setError(null);
        } else return false;


        return true;

    }


    private void setUserAccount(MaterialButtonToggleGroup group) {
        if (group.getCheckedButtonId() == R.id.buttonDoctorSignUp) {
            Log.d(TAG, "DOCTOR BUTTON");
            userType = DOCTOR;
        } else {
            userType = USER;
            Log.d(TAG, "USER BUTTON");
        }
    }

    private void createEmptyCollection(String userType) {
        Map<String, Object> data = new HashMap<>();
        data.put("junk", true);
        db = FirebaseFirestore.getInstance();
        db.collection(userType).document(mAuth.getUid()).set(data);
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