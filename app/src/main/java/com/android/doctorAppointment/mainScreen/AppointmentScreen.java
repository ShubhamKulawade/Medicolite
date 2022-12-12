package com.android.doctorAppointment.mainScreen;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.Invitations.OutgoingInvitation;
import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.Appointment;
import com.android.doctorAppointment.model.Doctor;
import com.android.doctorAppointment.model.User;
import com.android.doctorAppointment.myInterface.ProfileDataReceived;
import com.android.doctorAppointment.myInterface.UserListener;
import com.android.doctorAppointment.utility.ErrorConstantString;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.android.doctorAppointment.utility.MyConstants;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import static com.android.doctorAppointment.utility.MyConstants.APPOINTMENT;
import static com.android.doctorAppointment.utility.MyConstants.CURRENT_USER;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;
import static com.android.doctorAppointment.utility.MyConstants.INTENT_EXTRA_UID;
import static com.android.doctorAppointment.utility.MyConstants.MEETING_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.USER;

@SuppressLint("LogNotTimber")
public class AppointmentScreen extends AppCompatActivity implements UserListener {
    private FirebaseFirestore db;
    private String TAG = "Appointment Screen";
    private Doctor doctorGlobal;
    FirebaseAuth auth;
    User currentUser;
    LoadingDialog loadingDialog;
    private Date currentSelectedDate = Calendar.getInstance().getTime();
Button buttonAudioCall,buttonVideoCall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_screen);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this, "Taking Appointment...");
        init();
    }

    private void init() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "Loading...");
        Bundle extras = getIntent().getExtras();
        String uid = extras.getString(INTENT_EXTRA_UID).trim();
        currentUser = (User) extras.get(CURRENT_USER);


        Log.d(TAG, "init: UID "+uid.toString());


        TextView doctorName = findViewById(R.id.textViewDoctorNameAppointment);
        TextView doctorSpeciality = findViewById(R.id.textViewSpecialityDoctorAppointment);
        ChipGroup chipGroup = findViewById(R.id.chipgroupTimeAppointment);
        ImageView doctorImage = findViewById(R.id.imageView2);
        CalendarView calendarView = findViewById(R.id.calendarView);
        Button buttonAppoint = findViewById(R.id.buttonAppoint);

        buttonAudioCall = (Button) findViewById(R.id.buttonAudioCall);
        buttonAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:7838705526"));
                startActivity(callIntent);
            }
        });

        buttonVideoCall = findViewById(R.id.buttonVideoCall);

        buttonVideoCall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                gotoUrl("https://meet.google.com/wah-xdan-sdz");


            }

            private void gotoUrl(String s) {
                Uri uri = Uri.parse(s);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });




        LocalDate start = new LocalDate();
        LocalDate end = new LocalDate().plusDays(2);

        calendarView.setMinDate(start.toDate().getTime());
        calendarView.setMaxDate(end.toDate().getTime());


        buttonAppoint.setOnClickListener(view -> {
            takeAppointment(currentUser, chipGroup);
        });


        calendarView.setOnDateChangeListener((calendarView1, i, i1, i2) -> {
            List<String> doctorDays = doctorGlobal.getDays();
            Calendar calendar = Calendar.getInstance();
            calendar.set(i, i1, i2);
            Date date = calendar.getTime();
            currentSelectedDate = date;

            if (!isDateValid(date, doctorDays)) {
                Toast.makeText(this, "Doctor Schedule Mismatch", Toast.LENGTH_SHORT).show();
            }
        });


        ProfileDataReceived profileDataReceived = (object, success) -> {
            Doctor doctor = (Doctor) object;
            doctorGlobal = doctor;
            //doctorGlobal.setUid(uid);

            if (success && object != null) {
                if (doctor.getName() != null) {
                    Log.d(TAG, "init: " + doctor);
                    doctorName.setText(doctor.getName());
                    doctorSpeciality.setText(doctor.getSpeciality());
                    Glide.with(this).load(doctor.getImgUrl()).into(doctorImage);
                    String[] timeRange;
                    for (String time : doctor.getTime()) {
                        timeRange = time.split("-");
                        Log.d(TAG, "init: Time range: " + Arrays.toString(timeRange));
                        List<String> list = convertRangeToList(timeRange[0], timeRange[1]);
                        addChipsFromList(list, chipGroup);
                    }


                }

            } else {
                Toast.makeText(this, "Doctor does not exists", Toast.LENGTH_SHORT).show();
            }
        };

        getData(profileDataReceived, uid);
    }

    private void validate(User currentUser, ChipGroup chipGroup) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        Date date = currentSelectedDate;
        String dateStr = sdf.format(currentSelectedDate);
        Log.d(TAG, "validate: Date:" + dateStr);
        Appointment appointment = new Appointment();
        appointment.setDate(dateStr);
        appointment.setDoctorUid(doctorGlobal.getUid());
        appointment.setDoctorName(doctorGlobal.getName());
        appointment.setUserName(currentUser.getName());
        appointment.setUserUid(auth.getUid());
        appointment.setStatus("Active");

        FirestoreDoctorAppointment firestoreDoctorAppointment= (result, alreadyHaveAppointment,appointment1) -> {
            loadingDialog.dismiss();
            if(!alreadyHaveAppointment) {
                if (!result) {
                    AppointmentScreen.this.addDataToFirestore(appointment);
                } else {

                    Toast.makeText(AppointmentScreen.this, ErrorConstantString.DOCTOR_APPOINTMENT_LIMIT_EXCEED,
                            Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                String message="Already have Appointment of "+appointment1.getDoctorName()
                        +" on "+appointment1.getTime()+" , "+appointment1.getDate()+".";

                showAlertBoxWithSingleButton(message);
            }
        };

        if (isDateValid(date, doctorGlobal.getDays())) {
            if ((chipGroup.getCheckedChipIds()).size() > 0) {
                String chipText = getChipText(chipGroup);
                appointment.setTime(chipText);
                appointment.setKey();
                appointmentLimitExceed(dateStr, chipText, firestoreDoctorAppointment);

            } else {
                loadingDialog.dismiss();
                Toast.makeText(this, "Please Select Appointment's time", Toast.LENGTH_SHORT).show();
            }
        } else {
            loadingDialog.dismiss();
            Toast.makeText(this, "Doctor Schedule Mismatch", Toast.LENGTH_SHORT).show();
        }

    }


    private void showAlertBoxWithSingleButton(String Message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                    //do things
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private String getChipText(ChipGroup chipGroup) {
        int chipId = chipGroup.getCheckedChipId();
        Chip chip = chipGroup.findViewById(chipId);
        return chip.getText().toString();


    }

    private void appointmentLimitExceed(String date, String time,
                                        FirestoreDoctorAppointment firestoreDoctorAppointment) {
        // Checks if doctor already has more than 2 appoinment at same time frame
        String uid=auth.getUid();
        Log.d(TAG, "appointmentLimitExceed: docotrGlobal " + doctorGlobal.toString());
        db.collection(DOCTOR).document(doctorGlobal.getUid()).collection(APPOINTMENT)
                .whereEqualTo("key", date + "_" + time)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int count = 0;
                for (DocumentSnapshot ignored : task.getResult()) {
                    count++;
                    Appointment appointment=ignored.toObject(Appointment.class);
                    if (appointment.getUserUid().equals(uid))
                    {
                        firestoreDoctorAppointment.limitExceeded(count > 2,true,appointment);
                        return;
                    }
                }
                Log.d(TAG, "appointmentLimitExceed: count:" + count);
                firestoreDoctorAppointment.limitExceeded(count > 2,false,null);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
                Toast.makeText(this, "Error Occurred while retrieving Data", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }

    private void addDataToFirestore(Appointment appointment) {
        DocumentReference ref =
                db.collection(DOCTOR).document(appointment.getDoctorUid())
                        .collection(APPOINTMENT).document();

        DocumentReference userRef =
                db.collection(MyConstants.USER).document(auth.getUid())
                        .collection(APPOINTMENT).document();

        String documentUid = ref.getId();
        String documentUserUid = userRef.getId();
        appointment.setDocumentUidDoctor(documentUid);
        appointment.setDocumentUidUser(documentUserUid);
        ref.set(appointment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                userRef.set(appointment).addOnCompleteListener(task1 -> {

                    if (task1.isSuccessful())
                        Toast.makeText(this, "Appointment Successful", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Failed to take Appointment", Toast.LENGTH_SHORT).show();

                });

            } else {
                Toast.makeText(this, "Failed to take Appointment", Toast.LENGTH_SHORT).show();
            }
            loadingDialog.dismiss();
        });
    }


    private void takeAppointment(User currentUser, ChipGroup chipGroup) {
        loadingDialog.show();
        validate(currentUser, chipGroup);
    }

    private void getData(ProfileDataReceived profileDataReceived, String uid) {
        DocumentReference docRef = db.collection(DOCTOR).document(uid);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Doctor doc = documentSnapshot.toObject(Doctor.class);
            if (doc != null && !doc.getName().isEmpty())
                profileDataReceived.dataReceived(doc, true);
            else
                profileDataReceived.dataReceived(doc, false);

        });

    }


    private boolean isDateValid(Date date, List<String> doctorsDay) {

        // full name form of the day
        String selectedDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        return doctorsDay.contains(selectedDay);
    }


    private List<String> convertRangeToList(String start, String end) {
        //joda time
        DateTimeFormatter dtf = DateTimeFormat.forPattern("hh:mm a");
        LocalTime startTime = dtf.parseLocalTime(start.trim());
        LocalTime endTime = dtf.parseLocalTime(end.trim());

        ArrayList<String> times = new ArrayList<>();
        while (startTime.isBefore(endTime)) {
            times.add(dtf.print(startTime));
            startTime = startTime.plusMinutes(30);
        }
        return times;
    }


    private void addChipsFromList(List<String> chipList, ChipGroup chipGroup) {
        for (String value : chipList) {
            addNewChipWithoutSelectAction(value, chipGroup);
        }
    }


    private void addNewChipWithoutSelectAction(String value, ChipGroup chipGroup) {
        Log.d(TAG, "addNewChip: " + value);
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.single_chip_layout, chipGroup,
                false);
        chip.setId(ViewCompat.generateViewId());
        chip.setText(value);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setCloseIconVisible(false);

        chipGroup.addView(chip);
    }

    private void initiateMeeting(int type) {
        getToken(this, type);

    }

    private void getToken(UserListener userListener, int type) {
        db.collection(DOCTOR).document(doctorGlobal.getUid()).get().addOnCompleteListener(task -> {
            Doctor doctor = task.getResult().toObject(Doctor.class);

            if (type == 0) {
                userListener.initiateAudioMeeting(doctor, doctor.getToken());
            } else if (type == 1) {
                userListener.initiateVideoMeeting(doctor, doctor.getToken());
            }

        });
    }


    @Override
    public void initiateVideoMeeting(User user, String token) {
        Log.d(TAG, "initiateVideoMeeting: User: " + user);
        if (token == null && token.trim().isEmpty()) {
            Toast.makeText(this, user.getName() + " is not available for Meeting.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, OutgoingInvitation.class);
            intent.putExtra(USER, user);
            intent.putExtra(CURRENT_USER, currentUser);
            intent.putExtra(MEETING_TYPE, "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user, String token) {
        Log.d(TAG, "initiateAudioMeeting: User: " + user);
        if (token == null && token.trim().isEmpty()) {
            Toast.makeText(this, user.getName() + " is not available for Meeting.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, OutgoingInvitation.class);
            intent.putExtra(USER, user);
            intent.putExtra(CURRENT_USER, currentUser);
            intent.putExtra(MEETING_TYPE, "audio");
            startActivity(intent);
        }
    }


    interface FirestoreDoctorAppointment {
        void limitExceeded(boolean result,boolean alreadyHaveAppointment,Appointment appointment);

    }


}