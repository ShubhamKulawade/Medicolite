package com.android.doctorAppointment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.doctorAppointment.Authentication.EmailVerification;
import com.android.doctorAppointment.Authentication.SignIn;
import com.android.doctorAppointment.mainScreen.DoctorMainScreen;
import com.android.doctorAppointment.mainScreen.UserMainScreen;
import com.android.doctorAppointment.myInterface.MyFirebaseAuthorizationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Timer;
import java.util.TimerTask;

import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;

public class SplashScreen extends AppCompatActivity {

    private static final String TAG = "MySplashScreen";
    private FirebaseAuth auth;
    private Timer timer;
    private boolean isTaskRunning = false;
    private boolean isTimerFinished = false;
    private long SPLASH_DURATION = 2 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        auth = FirebaseAuth.getInstance();
        init();
    }

    private void init() {
        MyFirebaseAuthorizationCallback firebaseAuthorizationCallback = result -> {

            isTaskRunning = false;
            if (!isTimerFinished) {
                timer.cancel();
            }

            if (result) {
                startActivity(new Intent(this, DoctorMainScreen.class));
            } else {
                startActivity(new Intent(this, UserMainScreen.class));
            }
            finish();
        };


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isTimerFinished = true;
                if (!isTaskRunning) {
                    startActivity(new Intent(SplashScreen.this, SignIn.class));
                    finish();
                }

            }
        }, SPLASH_DURATION);


        if (auth.getCurrentUser() != null) {
            isTaskRunning = true;
            if (isVerified())
                isAuthorized(DOCTOR, firebaseAuthorizationCallback);
            else {
                timer.cancel();
                startActivity(new Intent(SplashScreen.this, EmailVerification.class));
                finish();
            }
        } else {
            timer.cancel();

            //startActivity(new Intent(this, MlTest.class));
            //startActivity(new Intent(this, ClassifierActivity.class));
            startActivity(new Intent(this, SignIn.class));
            finish();
        }
    }

    private void isAuthorized(String userType, MyFirebaseAuthorizationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(userType).document(auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        callback.isAuthorised(document.exists());
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                })
                .addOnCanceledListener(() -> Toast.makeText(this, "Log in Cancelled",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, SignIn.class));
                    finish();
                    Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isVerified()
    {

       return auth.getCurrentUser().isEmailVerified();
       //return true;
    }

    private interface MyIntentDecider{
        void getIntent(Intent intent);
    }
}