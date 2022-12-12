package com.android.doctorAppointment.mainScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.Authentication.SignIn;
import com.android.doctorAppointment.bmi.BMI_Test_page;
import com.android.doctorAppointment.Mi_model;
import com.android.doctorAppointment.MlTest.History;
import com.android.doctorAppointment.MlTest.MlTest;
import com.android.doctorAppointment.R;
import com.android.doctorAppointment.adapter.DoctorCategoryAdapter;
import com.android.doctorAppointment.adapter.DoctorListAppointment;
import com.android.doctorAppointment.bloodDonation.BloodDonor;
import com.android.doctorAppointment.message.MessagingService;
import com.android.doctorAppointment.model.Doctor;
import com.android.doctorAppointment.model.DoctorCategory;
import com.android.doctorAppointment.model.User;
import com.android.doctorAppointment.myInterface.FirebaseListData;
import com.android.doctorAppointment.myInterface.MyRecyclerClickListener;
import com.android.doctorAppointment.myInterface.ProfileDataReceived;
import com.android.doctorAppointment.profile.UserProfile;
import com.android.doctorAppointment.utility.Haversine;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.android.doctorAppointment.utility.MyCurrentUser;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.android.doctorAppointment.utility.MyConstants.CURRENT_USER;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.FCM_TOKEN;
import static com.android.doctorAppointment.utility.MyConstants.INTENT_EXTRA_UID;
import static com.android.doctorAppointment.utility.MyConstants.USER;
import static com.android.doctorAppointment.utility.MyConstants.USER_TYPE;

@SuppressLint("LogNotTimber")
public class UserMainScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UserMainScreen";
    private DoctorListAppointment doctorListAppointmentAdapter;
    private ArrayList<Doctor> doctorArrayList;
    private FirebaseFirestore db;
    private User currentUser;
    private FirebaseAuth auth;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        init();
        initNavigation();
        initFCM();
        setGreetings();
        initDocCategories();

    }

    private void initFCM() {
        setFcm(MessagingService.getToken(this));
    }

    private void setGreetings() {
        TextView greeting = findViewById(R.id.textViewGreetings);
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

    private void setFcm(String token) {
        db.collection(USER).document(Objects.requireNonNull(auth.getUid()))
                .update(FCM_TOKEN, token)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Failed to setup FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeFcm() {
        db.collection(USER).document(Objects.requireNonNull(auth.getUid()))
                .update(FCM_TOKEN, null)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Failed to setup FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void init() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "Loading...");
        loadingDialog.show();
        setUpRecyclerView();

        NavigationView navigationView = findViewById(R.id.navigationUser);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        Button edit = findViewById(R.id.buttonEditUser);
        edit.setOnClickListener(this::popUpMenu);
        TextView navigationName = headerView.findViewById(R.id.textViewHeaderName);
        TextView userName = findViewById(R.id.textViewUserName);
        TextView navigationEmail = headerView.findViewById(R.id.textViewHeaderEmail);
        ImageView navigationImage = headerView.findViewById(R.id.imageViewHeaderProfile);
        SearchView searchView = findViewById(R.id.searchView);
        ProfileDataReceived profileDataReceived = (object, success) -> {
            loadingDialog.dismiss();
            User user = (User) object;
            if (!success) {
                Toast.makeText(this, "Failed to retrieve Data", Toast.LENGTH_SHORT).show();
            } else if (user != null && !user.getName().trim().isEmpty()) {
                MyCurrentUser.setUser(user);//setting to global
                navigationName.setText(user.getName());
                navigationEmail.setText(user.getEmail());
                Glide.with(this).load(user.getImgUrl()).into(navigationImage);
                userName.setText(user.getName());
            }
        };

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                ArrayList<Doctor> result = findIn(doctorArrayList, s);
                if (result == null || result.size() == 0) {
                    Toast.makeText(UserMainScreen.this, "Did not find any result",
                            Toast.LENGTH_SHORT).show();
                } else {
                    doctorListAppointmentAdapter.setArrayList(result);
                    doctorListAppointmentAdapter.notifyDataSetChanged();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        TextView textViewNoDoctors = findViewById(R.id.textViewNoDoctors);
        FirebaseListData firebaseListData = (list, success) -> {
            if (success) {
                if ((list.size() == 0)) {
                    Log.d(TAG, "init: " + Arrays.toString(list.toArray()));
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


    private void popUpMenu(View button) {
        PopupMenu popup = new PopupMenu(this, button);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popup_menu_user, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            String sortBy = item.getTitle().toString();
            if (sortBy.equals("View All")) {
                doctorListAppointmentAdapter.setDoctorArrayList(doctorArrayList);
            }
            doctorListAppointmentAdapter.notifyDataSetChanged();
            return true;
        });

        popup.show();//showing popup menu
    }


    public ArrayList<Doctor> findIn(List<Doctor> doctorCollection, String name) {
        List<Doctor> filtered =
                doctorCollection.stream()
                        .filter(t -> t.getName().toLowerCase().contains(name.toLowerCase())
                                || t.getSpeciality().toLowerCase().contains(name.toLowerCase())
                                || t.getGender().toLowerCase().contains(name.toLowerCase()))
                        .collect(Collectors.toList());
        return (ArrayList<Doctor>) filtered;
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

    private void setUpRecyclerView() {
        View.OnClickListener onClickListener = view -> {

            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            Doctor doctor = doctorArrayList.get(position);
            Log.d(TAG, "onClick: " + doctor.toString());
            startActivityWithAdditionalData(doctor);

        };
        doctorArrayList = new ArrayList<>();
        doctorListAppointmentAdapter = new DoctorListAppointment(this, doctorArrayList);
        doctorListAppointmentAdapter.setOnItemClickListener(onClickListener);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDoctor);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(doctorListAppointmentAdapter);


    }

    private void startActivityWithAdditionalData(Doctor doctor) {
        Intent intent = new Intent(this, AppointmentScreen.class);
        intent.putExtra(INTENT_EXTRA_UID, doctor.getUid());
        intent.putExtra(CURRENT_USER, currentUser);
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private void getData(FirebaseListData firebaseListData) {
        Haversine haversine = new Haversine();
        db.collection(DOCTOR).
                orderBy("name")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Doctor doctor;
                    doctor = document.toObject(Doctor.class);
                    double distance =
                            Double.parseDouble(haversine.getHaversine(currentUser.getLatLong(),
                                    doctor.getLatLong()));
                    doctor.setDistanceFromUser(Double.parseDouble(String.format("%.2f", distance)));
                    doctorArrayList.add(doctor);


                }
                firebaseListData.dataReceived(doctorArrayList, true);
            } else {
                firebaseListData.dataReceived(null, false);

            }
            mySort(doctorArrayList);
            doctorListAppointmentAdapter.notifyDataSetChanged();
        });
    }


    private void mySort(ArrayList<Doctor> doctorArrayList) {
        doctorArrayList.sort((doctor, t1) -> Double.compare(doctor.getDistanceFromUser(),
                t1.getDistanceFromUser()));
    }

    private void getCurrentUserData(ProfileDataReceived profileDataReceived,
                                    FirebaseListData firebaseListData) {
        db.collection(USER).document(Objects.requireNonNull(auth.getUid())).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser = task.getResult().toObject(User.class);
                        if (currentUser == null || currentUser.getName().isEmpty()) {
                            profileDataReceived.dataReceived(null, false);
                            return;
                        } else {
                            profileDataReceived.dataReceived(currentUser, true);
                        }
                        getData(firebaseListData);
                    } else {
                        profileDataReceived.dataReceived(null, false);
                        Toast.makeText(this, "Failed to Retrieve User Data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initDocCategories() {
        ArrayList<DoctorCategory> arrayList = new ArrayList<>();
        arrayList.add(new DoctorCategory(R.drawable.ic__hospitalist, "Family Physician\n"));
        arrayList.add(new DoctorCategory(R.drawable.ic_anesthesiologist, "Anesthesiologist\n"));
        arrayList.add(new DoctorCategory(R.drawable.ic__cardiologist, "Cardiologist\n(Heart)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__dermatologist, "Dermatologist\n(Skin)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__nephrologist, "Nephrologist\n(Kidney)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__neurosurgeon, "Neurologist\n(Brain)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__gynecologist, "Gynecologist\n"));
        arrayList.add(new DoctorCategory(R.drawable.ic__oncologist, "Oncologist\n(Cancer)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__opthalmologist, "Ophthalmologist\n(Eye)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__otolaryngologist, "Otolaryngologist\n(Ear)"));
        arrayList.add(new DoctorCategory(R.drawable.ic__pediatrician, "Pediatrician\n"));
        arrayList.add(new DoctorCategory(R.drawable.ic__psychiatrist, "Psychiatrist\n"));
        arrayList.add(new DoctorCategory(R.drawable.ic__radiologist, "Radiologist\n"));
        arrayList.add(new DoctorCategory(R.drawable.ic__surgeon, "Surgeon\n"));

        MyRecyclerClickListener clickListener = position -> {
            //Toast.makeText(this, "Clicked "+position, Toast.LENGTH_SHORT).show();
            ArrayList<Doctor> result = findIn(doctorArrayList, arrayList.get(position).getTitle());
            if (result == null || result.size() == 0) {
                Toast.makeText(UserMainScreen.this, "Did not find any result",
                        Toast.LENGTH_SHORT).show();
            } else {
                doctorListAppointmentAdapter.setArrayList(result);
                doctorListAppointmentAdapter.notifyDataSetChanged();
            }

        };

        DoctorCategoryAdapter doctorCategoryAdapter = new DoctorCategoryAdapter(arrayList, this, clickListener);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDocCategory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(doctorCategoryAdapter);
        doctorCategoryAdapter.notifyDataSetChanged();


    }

    public void becomeBloodDonor(View view) {
        startActivity(new Intent(this, BloodDonor.class));
    }


    private void nearbyHospitalIntent() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=hospital");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menuProfile:
                startActivity(new Intent(this, UserProfile.class));
                break;

            case R.id.menu_mi_model:
                startActivity(new Intent(this, Mi_model.class));
                break;
            case R.id.BMI_Test:
                startActivity(new Intent(this, BMI_Test_page.class));
                break;

            case R.id.menuAppointment:
                Intent intent = new Intent(this, MyAppointments.class);
                intent.putExtra(USER_TYPE, 0);
                startActivity(intent);
                break;
            case R.id.menuDetect:
                startActivity(new Intent(this, MlTest.class));
                break;
            case R.id.menuHistory:
                startActivity(new Intent(this, History.class));
                break;

            case R.id.chat:
                gotoUrl("https://api.whatsapp.com/send?phone=917838705526&text=hello%20can%20i%20help%20you?");
                break;


            case R.id.menuPrescription:
                //Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, PrescriptionHistory.class));
                break;
            case R.id.menuHospital:
                nearbyHospitalIntent();
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

    private void gotoUrl(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}