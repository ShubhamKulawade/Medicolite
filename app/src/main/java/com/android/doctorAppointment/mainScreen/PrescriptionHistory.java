package com.android.doctorAppointment.mainScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.adapter.PrescriptionAdapter;
import com.android.doctorAppointment.model.Appointment;
import com.android.doctorAppointment.model.Prescription;
import com.android.doctorAppointment.myInterface.FirebaseListData;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.android.doctorAppointment.utility.MyConstants.APPOINTMENT;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.PRESCRIPTION;
import static com.android.doctorAppointment.utility.MyConstants.USER;

public class PrescriptionHistory extends AppCompatActivity {

    private static final String TAG = "MyPrescriptionHistory";
    private PrescriptionAdapter prescriptionAdapter;
    private ArrayList<Prescription> prescriptionArrayList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_history);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        init();


    }

    private void init() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "Loading...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        TextView textViewNoPrescription = findViewById(R.id.textViewNoPrescription);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPrescription);
        prescriptionArrayList = new ArrayList<>();
        prescriptionAdapter = new PrescriptionAdapter(prescriptionArrayList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(prescriptionAdapter);


        FirebaseListData firebaseListData = (list, success) -> {
            if (success) {
                if (list.size() == 0) {
                    textViewNoPrescription.setVisibility(View.VISIBLE);
                } else
                    textViewNoPrescription.setVisibility(View.INVISIBLE);

                loadingDialog.dismiss();
            } else {
                textViewNoPrescription.setVisibility(View.VISIBLE);
                //Toast.makeText(this, "Error while loading data", Toast.LENGTH_SHORT).show();
            }
            loadingDialog.dismiss();

        };

        getFriebasePrescription(firebaseListData);

    }

    private void getFriebasePrescription(FirebaseListData firebaseListData) {
        CollectionReference collectionReference =
                db.collection(USER).document(auth.getUid()).collection(PRESCRIPTION);

        collectionReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (value != null && !value.isEmpty()) {
                prescriptionArrayList.clear();
                for (DocumentSnapshot document : value.getDocuments()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Prescription prescription;
                    prescription = document.toObject(Prescription.class);
                    prescriptionArrayList.add(prescription);


                }
                firebaseListData.dataReceived(prescriptionArrayList, true);

                prescriptionAdapter.notifyDataSetChanged();


            } else {
                Log.d(TAG, "Current data: null");
                firebaseListData.dataReceived(prescriptionArrayList, false);
            }


        });

    }


}