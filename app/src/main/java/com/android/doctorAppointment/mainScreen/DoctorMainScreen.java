package com.android.doctorAppointment.mainScreen;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorAppointment.Authentication.SignIn;


import com.android.doctorAppointment.Mi_model;
import com.android.doctorAppointment.MlTest.History;
import com.android.doctorAppointment.MlTest.MlTest;
import com.android.doctorAppointment.R;
import com.android.doctorAppointment.adapter.AppointmentAdapter;
import com.android.doctorAppointment.message.MessagingService;
import com.android.doctorAppointment.model.Appointment;
import com.android.doctorAppointment.model.Doctor;
import com.android.doctorAppointment.model.Prescription;
import com.android.doctorAppointment.myInterface.AppointmentCancelListener;
import com.android.doctorAppointment.myInterface.AppointmentCompletedListener;
import com.android.doctorAppointment.myInterface.FirebaseDataUploadCallback;
import com.android.doctorAppointment.myInterface.FirebaseListData;
import com.android.doctorAppointment.myInterface.ProfileDataReceived;
import com.android.doctorAppointment.profile.DoctorProfile;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.android.doctorAppointment.utility.MyConstants;
import com.android.doctorAppointment.utility.MyCurrentUser;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.android.doctorAppointment.utility.MyConstants.APPOINTMENT;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.FCM_TOKEN;
import static com.android.doctorAppointment.utility.MyConstants.PRESCRIPTION;
import static com.android.doctorAppointment.utility.MyConstants.USER;
import static com.android.doctorAppointment.utility.MyConstants.USER_TYPE;

public class DoctorMainScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DoctorMainScreen";
    private FirebaseFirestore db;
    private Doctor currentUser;
    private FirebaseAuth auth;
    private DrawerLayout drawer;
    private final ArrayList<Appointment> appointmentArrayList = new ArrayList<>();
    private AppointmentAdapter appointmentAdapter;
    private FirebaseDataUploadCallback firebaseDataUploadCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main_screen);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        init();
        setGreetings();
        initNavigation();
        initFCM();
    }

    private void setGreetings() {
        TextView greeting = findViewById(R.id.textViewGreetingsDoc);
        LocalTime localTime = new LocalTime();
        int hour = localTime.getHourOfDay();
        if (hour >= 0 && hour < 12) {
            greeting.setText(R.string.good_morning);
        } else if (hour >= 12 && hour < 16) {
            greeting.setText(R.string.good_afternoon);
        } else if (hour >= 16 && hour <= 23) {
            greeting.setText(R.string.good_evening);
        }

    }


    private void init() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "Loading...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        setUpRecyclerView();

        NavigationView navigationView = findViewById(R.id.navigationDoctor);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        TextView navigationName = headerView.findViewById(R.id.textViewHeaderName);
        TextView navigationEmail = headerView.findViewById(R.id.textViewHeaderEmail);
        ImageView navigationImage = headerView.findViewById(R.id.imageViewHeaderProfile);
        TextView userName = findViewById(R.id.textViewDoctorNameMain);
        Button edit = findViewById(R.id.buttonEdit);
        edit.setOnClickListener(this::popUpMenu);
        ProfileDataReceived profileDataReceived = (object, success) -> {
            loadingDialog.dismiss();
            Doctor doctor = (Doctor) object;
            if (!success) {
                Toast.makeText(this, "Failed to retrieve Data", Toast.LENGTH_SHORT).show();
            } else if (doctor != null && !doctor.getName().trim().isEmpty()) {
                MyCurrentUser.setUser(doctor);//setting to global
                userName.setText(doctor.getName());
                navigationName.setText(doctor.getName());
                navigationEmail.setText(doctor.getEmail());
                Glide.with(this).load(doctor.getImgUrl()).into(navigationImage);
            }
        };
        SearchView searchView = findViewById(R.id.searchViewDoc);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                ArrayList<Appointment> result = findIn(appointmentArrayList, s);
                if (result == null || result.size() == 0) {
                    Toast.makeText(DoctorMainScreen.this, "Did not find any result",
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
        TextView textViewNoDoctors = findViewById(R.id.textViewNoAppointmentDoc);
        FirebaseListData firebaseListData = (list, success) -> {
            if (success) {
                if (list.size() == 0) {
                    //Log.d(TAG, "init: " + Arrays.toString(list.toArray()));
                    textViewNoDoctors.setVisibility(View.VISIBLE);
                } else
                    textViewNoDoctors.setVisibility(View.INVISIBLE);

                loadingDialog.dismiss();
            } else {
                Toast.makeText(this, "Error while loading data", Toast.LENGTH_SHORT).show();
            }
        };


        getCurrentUserData(profileDataReceived, firebaseListData);


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
                appointmentAdapter.setArraylist(appointmentArrayList);
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


    private void initNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);


        CardView cardViewMenu = findViewById(R.id.cardview_menu);
        cardViewMenu.setOnClickListener(view -> drawer.openDrawer(GravityCompat.START));
    }

    private void initFCM() {
        setFcm(MessagingService.getToken(this));
    }

    private void setFcm(String token) {
       // Log.d(TAG, "setFcm: " + token);
        db.collection(DOCTOR).document(auth.getCurrentUser().getUid())
                .update(FCM_TOKEN, token)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Failed to setup FCM token" + task.getException().toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void removeFcm() {
        db.collection(DOCTOR).document(auth.getUid())
                .update(FCM_TOKEN, null)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Failed to setup FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setUpRecyclerView() {
        //.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
        //For Cancellation (Cancellation Dialog)
        AppointmentCancelListener appointmentCancelListener = this::showDialog;

        //For completion (completion dialog -> Add Prescription)
        AppointmentCompletedListener appointmentCompletedListener = this::showPrescriptionDialog;


        appointmentAdapter = new AppointmentAdapter(appointmentArrayList, this,
                appointmentCancelListener, appointmentCompletedListener, true);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewAppointments);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(appointmentAdapter);


    }


    //for Complete appointment action
    //Not in use: now uses showPrescriptionDialog
    private void showAlertBox(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.completeAppointmentMessage)
                .setTitle(R.string.Appointment_complete_title)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    updateFirebaseAppointmentComplete(appointment);
                    Toast.makeText(this, "Changed Status to COMPLETED", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, id) -> {
                    //  Action for 'NO' Button
                    dialog.cancel();

                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showPrescriptionDialog(Appointment appointment){
        Dialog dialog = new Dialog(this);
        firebaseDataUploadCallback = isSuccess -> {
            if(isSuccess)
            {
                dialog.dismiss();
                updateFirebaseAppointmentComplete(appointment);
                Toast.makeText(DoctorMainScreen.this, "Prescription Added Successfully",
                        Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(DoctorMainScreen.this, "Failed to Add Prescription",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_add_prescription);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        TextView patientName=dialog.findViewById(R.id.textViewPrescriptionPatientName);
        patientName.setText(appointment.getUserName());
        TextInputLayout textInputLayout = dialog.findViewById(R.id.tilPrescription);
        Button buttonAddPrescription = dialog.findViewById(R.id.buttonDialogAddPrescription);
        buttonAddPrescription.setOnClickListener(view -> {
            if (textInputLayout.getEditText().getText().length() < 4) {
                textInputLayout.setError("Prescription should be more than 4 " +
                        "character");
            } else {

                Prescription prescription=appointmentToPrescription(appointment);
                prescription.setPrescription(textInputLayout.getEditText().getText().toString());
                prescription.setCreationTimeStamp(System.currentTimeMillis());
                addPrescriptionToFirebase(prescription,firebaseDataUploadCallback);
            }


        });
    }

    private Prescription appointmentToPrescription(Appointment appointment){
        Prescription prescription=new Prescription();
        prescription.setDoctorName(appointment.getDoctorName());
        prescription.setUserName(appointment.getUserName());
        prescription.setDoctorUid(appointment.getDoctorUid());
        prescription.setUserUid(appointment.getUserUid());
        return prescription;
    }


    private void addPrescriptionToFirebase(Prescription prescription, FirebaseDataUploadCallback firebaseDataUploadCallback)
    {
        DocumentReference userRef =
                db.collection(MyConstants.USER).document(prescription.getUserUid())
                        .collection(PRESCRIPTION).document();
        prescription.setDocumentUidUser(userRef.getId());

        userRef.set(prescription).addOnCompleteListener(task -> {
            //Callback method
            firebaseDataUploadCallback.isSuccess(task.isSuccessful());
        });
    }


    private void updateFirebaseAppointmentComplete(Appointment appointment) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("status", "Completed");
        db.collection(DOCTOR).document(appointment.getDoctorUid()).collection(APPOINTMENT)
                .document(appointment.getDocumentUidDoctor()).update(hashMap);
        db.collection(USER).document(appointment.getUserUid()).collection(APPOINTMENT)
                .document(appointment.getDocumentUidUser()).update(hashMap);
    }


    //for cancel appointment action
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
            if (textInputLayout.getEditText().getText().length() < 10) {
                textInputLayout.setError("Reason should be more than 10 " +
                        "character");

            } else {
                updateCancelReasonFirebase(textInputLayout.getEditText().getText().toString(),
                        appointment);
                dialog.dismiss();
                Toast.makeText(DoctorMainScreen.this, "Appointment Cancelled",
                        Toast.LENGTH_SHORT).show();
            }


        });

    }

    private void updateCancelReasonFirebase(String text, Appointment appointment) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cancelReason", text);
        hashMap.put("status", "Cancelled");
        db.collection(DOCTOR).document(appointment.getDoctorUid()).collection(APPOINTMENT)
                .document(appointment.getDocumentUidDoctor()).update(hashMap);
        db.collection(USER).document(appointment.getUserUid()).collection(APPOINTMENT)
                .document(appointment.getDocumentUidUser()).update(hashMap);
    }

    private void getData(FirebaseListData firebaseListData) {
        CollectionReference collectionReference =
                db.collection(DOCTOR).document(auth.getCurrentUser().getUid()).collection(APPOINTMENT);

        collectionReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                //Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (value != null && !value.isEmpty()) {
                appointmentArrayList.clear();
                for (DocumentSnapshot document : value.getDocuments()) {
                    //Log.d(TAG, document.getId() + " => " + document.getData());
                    Appointment appointment;
                    appointment = document.toObject(Appointment.class);
                    appointmentArrayList.add(appointment);


                }
                firebaseListData.dataReceived(appointmentArrayList, true);

                appointmentAdapter.notifyDataSetChanged();


            } else {
                //Log.d(TAG, "Current data: null");
                firebaseListData.dataReceived(appointmentArrayList, true);
            }


        });



                /*.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Appointment appointment;
                    appointment = document.toObject(Appointment.class);
                    appointmentArrayList.add(appointment);


                }
                profileDataReceived.dataReceived(null, true);
            } else {
                profileDataReceived.dataReceived(null, false);

            }
            appointmentAdapter.notifyDataSetChanged();
        });*/
    }


    private void getCurrentUserData(ProfileDataReceived profileDataReceived,
                                    FirebaseListData firebaseListData) {
        db.collection(DOCTOR).document(auth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = task.getResult().toObject(Doctor.class);
                        if (currentUser == null || currentUser.getName().isEmpty()) {
                            profileDataReceived.dataReceived(null, false);
                            return;
                        } else {
                            profileDataReceived.dataReceived(currentUser, true);
                        }
                        getData(firebaseListData);
                    } else {
                        profileDataReceived.dataReceived(null, false);
                        Toast.makeText(this, "Failed to Retrieve User Data " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menuProfile:
                startActivity(new Intent(this, DoctorProfile.class));
                break;
            case R.id.menuAppointment:
                Intent intent = new Intent(this, MyAppointments.class);
                intent.putExtra(USER_TYPE, 1);
                startActivity(intent);
                break;

            case R.id.menu_mi_model:
                startActivity(new Intent(this, Mi_model.class));
                break;
            case R.id.menuDetect:
                startActivity(new Intent(this, MlTest.class));
                break;
            case R.id.menuHistory:
                startActivity(new Intent(this, History.class));
                break;
            case R.id.menuSignOut:
                removeFcm();
                auth.signOut();
                startActivity(new Intent(this, SignIn.class));
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}