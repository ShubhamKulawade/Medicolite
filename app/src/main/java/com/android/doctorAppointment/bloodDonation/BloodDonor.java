package com.android.doctorAppointment.bloodDonation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.User;
import com.android.doctorAppointment.utility.ErrorConstantString;
import com.android.doctorAppointment.utility.MyCurrentUser;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;

import static com.android.doctorAppointment.utility.MyConstants.USER;

public class BloodDonor extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_donor);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        init();
    }

    private void init() {
        String bloodGroup = "A+";
        String donationRate = "3 Months";
        ThemedToggleButtonGroup groupBloodType = findViewById(R.id.Blood);
        ThemedToggleButtonGroup groupDonationRate = findViewById(R.id.donationRate);

        groupBloodType.selectButton(R.id.btn1);
        groupDonationRate.selectButton(R.id.btn11);



        EditText address = findViewById(R.id.editTextTextPostalAddress);
        User user=MyCurrentUser.getUser();
        address.setText(user!=null?user.getLocation():"");


        Button buttonRegisterAsDonor = findViewById(R.id.buttonRegisterAsdonor);
        buttonRegisterAsDonor.setOnClickListener(v -> {
            if (validate(address.getText().toString())) {

                Map<String, String> map = new HashMap<>();
                map.put("bloodGroup", groupBloodType.getSelectedButtons().get(0).getSelectedText());
                map.put("donationRate", groupDonationRate.getSelectedButtons().get(0).getSelectedText());

                //Toast.makeText(this, "Map: "+map.toString(), Toast.LENGTH_SHORT).show();
                updateDb(map);
            }
        });
    }

    private void updateDb(Map<String, String> map) {
        db.collection(USER).document(auth.getUid()).set(map, SetOptions.merge()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(BloodDonor.this, "Successfully Registered as Blood Donor", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, ErrorConstantString.DB_FAILED, Toast.LENGTH_SHORT).show();
            }
        }).addOnCanceledListener(() -> Toast.makeText(BloodDonor.this, ErrorConstantString.CANCELLED_BY_USER, Toast.LENGTH_SHORT).show());
    }

    private boolean validate(String text) {
        if (text.length() < 10) {
            Toast.makeText(this, ErrorConstantString.ADDRESS_ERROR, Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

}