package com.android.doctorAppointment.mainScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.adapter.AppointmentAdapter;
import com.android.doctorAppointment.model.Appointment;
import com.android.doctorAppointment.myInterface.AppointmentCancelListener;
import com.android.doctorAppointment.myInterface.AppointmentCompletedListener;
import com.android.doctorAppointment.myInterface.FirebaseListData;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.android.doctorAppointment.utility.MyConstants.APPOINTMENT;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.USER;
import static com.android.doctorAppointment.utility.MyConstants.USER_TYPE;

public class MyAppointments extends AppCompatActivity {

    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentArrayList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String TAG = "MyAppointments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        init();
    }

    private void init() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "Loading...");
        loadingDialog.show();
        SearchView searchView = findViewById(R.id.searchViewMyAppointments);
        TextView textViewNoAppointments = findViewById(R.id.textViewNoAppointment);
        Bundle extras = getIntent().getExtras();
        int type = extras.getInt(USER_TYPE);
        Button edit = findViewById(R.id.buttonEdit);
        edit.setOnClickListener(this::popUpMenu);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                ArrayList<Appointment> result = findIn(appointmentArrayList, s);
                if (result == null || result.size() == 0) {
                    Toast.makeText(MyAppointments.this, "Did not find any result",
                            Toast.LENGTH_SHORT).show();
                } else {
                    appointmentAdapter.setArraylist(result);
                    appointmentAdapter.notifyDataSetChanged();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        setUpRecyclerView(type);

        FirebaseListData firebaseListData = (list, success) -> {
            if (success) {
                if (list.size() == 0) {
                    textViewNoAppointments.setVisibility(View.VISIBLE);
                } else
                    textViewNoAppointments.setVisibility(View.INVISIBLE);


            } else {
                textViewNoAppointments.setVisibility(View.VISIBLE);
                //Toast.makeText(this, "Error while loading data", Toast.LENGTH_SHORT).show();
            }
            loadingDialog.dismiss();
        };

        getData(firebaseListData, type);


    }

    public ArrayList<Appointment> findIn(List<Appointment> appointmentCollection, String name) {
        List<Appointment> filtered =
                appointmentCollection.stream()
                        .filter(t -> t.getDoctorName().toLowerCase().contains(name.toLowerCase())
                                || t.getUserName().toLowerCase().contains(name.toLowerCase())
                                || t.getStatus().toLowerCase().contains(name.toLowerCase()))
                        .collect(Collectors.toList());
        return (ArrayList<Appointment>) filtered;
    }


    private void popUpMenu(View button) {
        PopupMenu popup = new PopupMenu(this, button);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popup_menu_appointment, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            String sortBy = item.getTitle().toString();
            if (sortBy.equals("View All")) {
                appointmentAdapter.setArraylist((ArrayList<Appointment>) appointmentArrayList);
            } else {
                ArrayList<Appointment> appointments = sort(sortBy);
                appointmentAdapter.setArraylist(appointments);
            }
            appointmentAdapter.notifyDataSetChanged();
            return true;
        });

        popup.show();//showing popup menu
    }


    private ArrayList<Appointment> sort(String status) {
        List<Appointment> appointment =
                appointmentArrayList
                        .stream()
                        .filter(t -> t.getStatus().toLowerCase().contains(status.toLowerCase()))
                        .collect(Collectors.toList());
        return (ArrayList<Appointment>) appointment;


    }


    private void setUpRecyclerView(int type) {
        //Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
        AppointmentCancelListener appointmentCancelListener = this::showDialog;

        AppointmentCompletedListener appointmentCompletedListener =
                new AppointmentCompletedListener() {
                    @Override
                    public void onComplete(Appointment appointment) {
                        Toast.makeText(MyAppointments.this, "Not Authorised for this action",
                                Toast.LENGTH_SHORT).show();
                    }
                };

        appointmentAdapter = new AppointmentAdapter(appointmentArrayList, this,
                appointmentCancelListener, appointmentCompletedListener, type == 0 ? false : true);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewMyAppointments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(appointmentAdapter);


    }

    private void getData(FirebaseListData firebaseListData, int type) {

        CollectionReference collectionReference =
                db.collection(type == 0 ? USER : DOCTOR).document(auth.getUid()).collection(APPOINTMENT);

        collectionReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (value != null && !value.isEmpty()) {
                appointmentArrayList.clear();
                for (DocumentSnapshot document : value.getDocuments()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Appointment appointment;
                    appointment = document.toObject(Appointment.class);
                    appointmentArrayList.add(appointment);


                }
                firebaseListData.dataReceived(appointmentArrayList, true);

                appointmentAdapter.notifyDataSetChanged();


            } else {
                Log.d(TAG, "Current data: null");
                firebaseListData.dataReceived(appointmentArrayList, false);
            }


        });



        /*
        db.collection(type == 0 ? USER : DOCTOR).document(auth.getUid()).collection(APPOINTMENT)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Appointment appointment;
                    appointment = document.toObject(Appointment.class);
                    appointmentArrayList.add(appointment);


                }
                firebaseListData.dataReceived(appointmentArrayList, true);
            } else {
                firebaseListData.dataReceived(null, false);

            }
            appointmentAdapter.notifyDataSetChanged();
        });*/
    }

    private void showDialog(Appointment appointment) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.cancel_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        TextInputLayout textInputLayout = dialog.findViewById(R.id.tilCancelReason);
        Button cancelAppointmentButton = dialog.findViewById(R.id.buttonDialogCancelAppointment);
        cancelAppointmentButton.setOnClickListener(view -> {
            if (textInputLayout.getEditText().getText().toString().length() < 10) {
                textInputLayout.setError("Reason for cancellation should be more than 10 " +
                        "character");
            } else {
                updateCancelReasonFirebase(textInputLayout.getEditText().getText().toString(),
                        appointment);
                dialog.dismiss();
                Toast.makeText(MyAppointments.this, "Appointment Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateCancelReasonFirebase(String text, Appointment appointment) {
        Log.d(TAG, "updateCancelReasonFirebase: called ");
        Log.d(TAG, "updateCancelReasonFirebase: appointment " + appointment.toString());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cancelReason", text);
        hashMap.put("status", "Cancelled");
        db.collection(DOCTOR).document(appointment.getDoctorUid()).collection(APPOINTMENT)
                .document(appointment.getDocumentUidDoctor()).update(hashMap);
        db.collection(USER).document(appointment.getUserUid()).collection(APPOINTMENT)
                .document(appointment.getDocumentUidUser()).update(hashMap);

    }
}