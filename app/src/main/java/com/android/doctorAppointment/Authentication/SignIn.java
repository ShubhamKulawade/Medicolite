package com.android.doctorAppointment.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.myInterface.MyFirebaseAuthorizationCallback;
import com.android.doctorAppointment.profile.DoctorProfile;
import com.android.doctorAppointment.profile.UserProfile;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import static com.android.doctorAppointment.utility.ErrorConstantString.CANCELLED_BY_USER;
import static com.android.doctorAppointment.utility.ErrorConstantString.EMPTY_FIELD;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.USER;

public class SignIn extends AppCompatActivity {
    private static final String TAG = "SignIn";
    private FirebaseAuth mAuth;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private LoadingDialog loadingDialog;
    private String userType;
    private FirebaseFirestore db;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this, "Logging in...");
        loadingDialog.setCancelable(false);
        userType = USER;
        init();
    }

    private void init() {
        TextInputLayout email = findViewById(R.id.inputEmail);
        TextInputLayout password = findViewById(R.id.inputPassword);
        findViewById(R.id.textViewRegisterLink).setOnClickListener(
                view -> startActivity(new Intent(this, SignUp.class)));
        findViewById(R.id.buttonLogin).setOnClickListener(view -> {
            if (validate(email, password))
                signIn(email, password);
        });
        findViewById(R.id.textView4).setOnClickListener(view -> {
            startActivity(new Intent(SignIn.this, ForgotPassword.class));
        });

        coordinatorLayout =findViewById(R.id.signInCoordRoot);

        materialButtonToggleGroup =
                findViewById(R.id.materialButtonToggleGroup);

       /* materialButtonToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (group.getCheckedButtonId()==R.id.buttonUserSignIn){
                Log.d(TAG, "DOCTOR BUTTON");
            }
            else
                Log.d(TAG, "USER BUTTON");
            Log.d(TAG, "group: "+group);
            Log.d(TAG, "checkedId: "+checkedId);
            Log.d(TAG, "isChecked: "+isChecked);
        });*/


    }

    private boolean validate(TextInputLayout email, TextInputLayout password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern p = Pattern.compile(regex);

        if (email != null) {
            if (TextUtils.isEmpty(email.getEditText().getText())) {
                email.setError(EMPTY_FIELD);
                return false;
            } else
                email.setError(null);

        } else if (password != null) {
            if (TextUtils.isEmpty(password.getEditText().getText())) {
                password.setError(EMPTY_FIELD);
                return false;
            } else
                password.setError(null);
        }
        return true;

    }


    private void signIn(TextInputLayout email, TextInputLayout password) {
        loadingDialog.show();
        MyFirebaseAuthorizationCallback callback = result -> {
            if (result) {
                if (userType.equals(USER)) {
                    Intent intent = new Intent(this, UserProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if (userType.equals(DOCTOR)) {
                    Intent intent = new Intent(this, DoctorProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "signIn: Else");
                }
            } else {
                Toast.makeText(this, this.getResources().getText(R.string.NotAuthorisedSignin),
                        Toast.LENGTH_SHORT).show();
            }
        };


        setUserAccount(materialButtonToggleGroup);
        String emailString = email.getEditText().getText().toString();
        String passwordString = password.getEditText().getText().toString();

        mAuth.signInWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (isVerified())
                            isAuthorized(userType, callback);
                        else{
                            Toast.makeText(this, "Please check your email for Verification Link",
                                    Toast.LENGTH_LONG).show();
                            String title="Email not Verified";
                            setSnackBar(coordinatorLayout,title);
                        }

                    } else {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                })
                .addOnCanceledListener(() -> {
                    Toast.makeText(this, CANCELLED_BY_USER, Toast.LENGTH_SHORT).show();
                });

    }

    private void setUserAccount(MaterialButtonToggleGroup group) {
        if (group.getCheckedButtonId() == R.id.buttonDoctorSignIn) {
            Log.d(TAG, "DOCTOR BUTTON");
            userType = DOCTOR;
        } else {
            userType = USER;
            Log.d(TAG, "USER BUTTON");
        }
    }

    private void isAuthorized(String userType, MyFirebaseAuthorizationCallback callback) {
        if (mAuth.getCurrentUser().getUid() == null) {
            Toast.makeText(this, "Failed to verify", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection(userType).document(mAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                callback.isAuthorised(document.exists());
            } else {
                Log.d(TAG, "Failed with: ", task.getException());
            }
        })
                .addOnCanceledListener(() -> Toast.makeText(this, "Log in Cancelled",
                        Toast.LENGTH_SHORT).show());
    }

    private boolean isVerified() {
        return mAuth.getCurrentUser().isEmailVerified();
        //return true;
    }

    private void setSnackBar(View root, String title) {
        Snackbar.make(root, title, Snackbar.LENGTH_INDEFINITE)
                .setAction("Verify", view -> {
                    startActivity(new Intent(SignIn.this,EmailVerification.class));
                    finish();
                })
                .show();
    }


}