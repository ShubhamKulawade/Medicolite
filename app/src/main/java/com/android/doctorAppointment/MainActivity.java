/**package com.android.doctorAppointment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.doctorAppointment.Authentication.SignIn;
import com.android.doctorAppointment.Authentication.SignUp;
import com.android.doctorAppointment.mainScreen.DoctorMainScreen;
import com.android.doctorAppointment.mainScreen.UserMainScreen;
import com.android.doctorAppointment.myInterface.MyFirebaseAuthorizationCallback;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Timer;
import java.util.TimerTask;

import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    FirebaseAuth auth;
    private Timer timer;
    private boolean isTaskRunning = false;
    private boolean isTimerFinished = false;
    private long SPLASH_DURATION = 3 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        init();
        firebaseInit();

    }

    private void firebaseInit() {
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
        };


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isTimerFinished = true;
                if (!isTaskRunning) {
                    startActivity(new Intent(MainActivity.this, SignIn.class));
                }

            }
        }, SPLASH_DURATION);


        if (auth.getCurrentUser() != null) {
            isTaskRunning = true;
            isAuthorized(DOCTOR, firebaseAuthorizationCallback);
        } else {
            timer.cancel();
            startActivity(new Intent(this, SignIn.class));
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
                        Toast.LENGTH_SHORT).show());
    }


}**/