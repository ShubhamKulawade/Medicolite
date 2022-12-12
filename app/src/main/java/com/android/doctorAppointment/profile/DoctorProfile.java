package com.android.doctorAppointment.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.mainScreen.DoctorMainScreen;
import com.android.doctorAppointment.model.Doctor;
import com.android.doctorAppointment.myInterface.FirebaseImageUploadCallback;
import com.android.doctorAppointment.myInterface.ProfileDataReceived;
import com.android.doctorAppointment.myInterface.TimeDialogOnSave;
import com.android.doctorAppointment.myInterface.TimePickerCallback;
import com.android.doctorAppointment.utility.EditTextValidator;
import com.android.doctorAppointment.utility.LoadingDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.android.doctorAppointment.utility.ErrorConstantString.EMAIL_ERROR;
import static com.android.doctorAppointment.utility.ErrorConstantString.NAME_ERROR;
import static com.android.doctorAppointment.utility.ErrorConstantString.PHONE_ERROR;
import static com.android.doctorAppointment.utility.MyConstants.DOCTOR;

public class DoctorProfile extends AppCompatActivity {

    private final String[] myPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int RequestCode = 101;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private static final int PICK_IMAGE_REQUEST = 73;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LoadingDialog loadingDialog, loadingDialogSave;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String TAG = "UserProfile";
    private EditText locationEdittext, name, email, phone, dob;
    private Spinner speciality, gender;
    private CircleImageView profile_pic;
    private String time;
    private Doctor doctor;
    private String latLong;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    ChipGroup chipGroupProfileTiming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        loadingDialog = new LoadingDialog(this, "Retrieving Location...");
        loadingDialogSave = new LoadingDialog(this, "Uploading Data...");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        init();
    }


    private void init() {
        List<EditText> editTextList = new ArrayList<>();
        chipGroupProfileTiming = findViewById(R.id.chipGrpTiming);
        Button editTime = findViewById(R.id.buttonEditTiming);
        Button saveProfile = findViewById(R.id.buttonProfileSaveDoc);
        doctor = new Doctor();
        TimeDialogOnSave timeDialogOnSave = (days, times) -> {
            doctor.setDays(days);
            doctor.setTime(times);

            for (String value : days) {
                addNewChipWithoutSelectAction(value, chipGroupProfileTiming);
            }


            for (String value : times) {
                addNewChipWithoutSelectAction(value, chipGroupProfileTiming);
            }
            Log.d(TAG, "init: days" + Arrays.toString(days.toArray()));
            Log.d(TAG, "init: time" + Arrays.toString(times.toArray()));
        };

        editTime.setOnClickListener(view -> showDialog(timeDialogOnSave));


        name = findViewById(R.id.editTextNameDoc);
        email = findViewById(R.id.editTextEmailDoc);
        dob = findViewById(R.id.editTextDobDoc);
        speciality = findViewById(R.id.spinnerSpecialityDoc);
        gender = findViewById(R.id.spinnerGenderDoc);
        phone = findViewById(R.id.editTextPhoneDoc);
        locationEdittext = findViewById(R.id.editTextLocationDoc);
        profile_pic = findViewById(R.id.profile_imageDoc);

        editTextList.add(name);
        editTextList.add(email);
        editTextList.add(dob);
        editTextList.add(phone);
        editTextList.add(locationEdittext);


        name.addTextChangedListener(new EditTextValidator(name) {
            @Override
            public void validate(TextView textView, String text) {
                if (text.length() < 3) {
                    textView.setError(NAME_ERROR);
                }
            }
        });

        email.addTextChangedListener(new EditTextValidator(email) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    textView.setError(EMAIL_ERROR);
                }
            }
        });

        phone.addTextChangedListener(new EditTextValidator(phone) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.PHONE.matcher(text).matches()) {
                    textView.setError(PHONE_ERROR);
                }
            }
        });


        dob.setOnClickListener(view -> datePicker(dob));
        profile_pic.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Profile picture"),
                    PICK_IMAGE_REQUEST);
        });

        locationEdittext.setOnClickListener(view -> myPermission());


        //callback interface when image url is returned
        FirebaseImageUploadCallback firebaseImageUploadCallback = (downloadUri, success) -> {
            if (success) {
                setModel(downloadUri);
            } else
                loadingDialogSave.dismiss();
        };
        saveProfile.setOnClickListener(view -> {
            loadingDialogSave.show();
            if (validateEditTexts(editTextList)) {
                if (chipGroupProfileTiming.getChildCount() >= 2) {
                    uploadImage(firebaseImageUploadCallback);
                    loadingDialogSave.dismiss();
                } else {
                    Toast.makeText(this, "Timing is Empty.Please Click on the \"Pen\" icon to add" +
                                    " time.",
                            Toast.LENGTH_SHORT).show();
                    loadingDialogSave.dismiss();
                }
            } else
                loadingDialogSave.dismiss();

        });


        //callback interface when profile data is received
        ProfileDataReceived profileDataReceived = (object, success) -> {
            Doctor doc = (Doctor) object;
            doctor = doc;
            if (success && doc.getName() != null && !doc.getName().isEmpty()) {
                setData((Doctor) object);
            }
            //not adding else case since this can be the first time user is setting up account

        };
        retrieveData(profileDataReceived);


    }

    //dob date picker
    private void datePicker(EditText dob) {
        int mYear, mMonth, mDay;
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> dob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), mYear, mMonth, mDay);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }


    //--------------doctor profileExtra--------------------------------//
    private void showDialog(TimeDialogOnSave timeDialogOnSave) {
        Dialog dialog = new Dialog(DoctorProfile.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_doctor_timings);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        Button addTime = dialog.findViewById(R.id.buttonAddNewTime);
        ChipGroup chipGroup = dialog.findViewById(R.id.chpGrpTime);
        ChipGroup chipGroupDays = dialog.findViewById(R.id.chipGroup);
        addTime.setOnClickListener(view -> addNewTime(chipGroup));

        Button save = dialog.findViewById(R.id.buttonSaveTime);
        save.setOnClickListener(view -> {
            List<String> days = getChipsText(chipGroupDays);
            List<String> times = getChipsText(chipGroup);
            if (chipGroupDays.getCheckedChipIds().size() > 0
                    && chipGroup.getCheckedChipIds().size() > 0) {
                timeDialogOnSave.onSave(days, times);
                dialog.dismiss();
            } else if (chipGroupDays.getCheckedChipIds().size() == 0) {
                Toast.makeText(this, "Select at least one working day", Toast.LENGTH_SHORT).show();
            } else if (chipGroup.getCheckedChipIds().size() == 0) {
                Toast.makeText(this, "Add at least one visiting or appointment time",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }


    //get text data from chips
    private List<String> getChipsText(ChipGroup chipGroup) {
        Log.d(TAG, "getChipsText: childcount: " + chipGroup.getChildCount());
        List<Integer> chipIds = chipGroup.getCheckedChipIds();
        List<String> result = new ArrayList<>();
        Log.d(TAG, "getChipsText: size " + chipIds.size());
        for (Integer id : chipIds) {
            Chip chip = chipGroup.findViewById(id);
            result.add(chip.getText().toString());
        }
        return result;
    }


    private void addNewTime(ChipGroup chipGroup) {
        time = "";
        AtomicInteger i = new AtomicInteger();
        TimePickerCallback callback = result ->
        {
            i.getAndIncrement();
            time = time + result;
            Log.d(TAG, "addNewTime:result " + result);
            if (i.get() % 2 == 0) {
                addNewChip(time, chipGroup);
            }
            time += " - ";
        };

        timePicker(callback, "End Time");
        timePicker(callback, "Start Time");
        Log.d(TAG, "addNewTime: " + (time));
        //addNewChip(time, chipGroup);
    }

    private void addNewChip(String value, ChipGroup chipGroup) {
        Log.d(TAG, "addNewChip: " + value);
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.single_chip_layout, chipGroup,
                false);
        chip.setId(ViewCompat.generateViewId());
        chip.setText(value);

        chip.setOnCloseIconClickListener(view -> chipGroup.removeView(chip));
        chipGroup.addView(chip);
    }

    private void addNewChipWithoutSelectAction(String value, ChipGroup chipGroup) {
        Log.d(TAG, "addNewChip: " + value);
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.single_chip_layout, chipGroup,
                false);
        chip.setId(ViewCompat.generateViewId());
        chip.setText(value);
        chip.setCheckable(false);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(view -> {
            chipGroup.removeView(chip);
            removeValue(value);
        });

        chipGroup.addView(chip);
    }

    private void removeValue(String value) {
        List<String> days = doctor.getDays();
        List<String> time = doctor.getTime();
        days.remove(value);
        time.remove(value);
    }


    private void timePicker(TimePickerCallback callback, String title) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(DoctorProfile.this,
                (timePicker, selectedHour, selectedMinute) -> {
                    String am_pm = "";
                    Calendar datetime = Calendar.getInstance();
                    datetime.set(Calendar.HOUR_OF_DAY, selectedHour);
                    datetime.set(Calendar.MINUTE, selectedMinute);

                    if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                        am_pm = "AM";
                    else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                        am_pm = "PM";

                    /*String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" :
                            datetime.get(Calendar.HOUR) + "";
                    String time = strHrsToShow + ":" + datetime.get(Calendar.MINUTE) + " " +
                    am_pm;*/

                    int strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? 12 :
                            datetime.get(Calendar.HOUR) ;

                    String time = String.format(Locale.US, "%02d:%02d %s", strHrsToShow,
                            datetime.get(Calendar.MINUTE), am_pm);
                    callback.setTime(time);
                    Log.d(TAG, "timePicker: " + selectedHour + ":" + selectedMinute);

                }, hour, minute, false);
        mTimePicker.setTitle(title);

        mTimePicker.show();
    }


    //----------------------Validation-----------------//

    private boolean validateEditTexts(List<EditText> editTexts) {
        for (EditText editText : editTexts) {
            if (TextUtils.isEmpty(editText.getText())) {
                editText.setError("Field Can't be empty");
                return false;
            } else
                editText.setError(null);
        }
        return true;
    }


    private void setModel(Uri downloadUri) {
        String nameStr, emailStr, dobStr, phoneStr, latStr, locStr;
        nameStr = name.getText().toString();
        emailStr = email.getText().toString();
        dobStr = dob.getText().toString();
        phoneStr = phone.getText().toString();
        locStr = locationEdittext.getText().toString();


        doctor.setName(nameStr.isEmpty() ? doctor.getName() : nameStr);
        doctor.setEmail(emailStr.isEmpty() ? doctor.getEmail() : emailStr);
        doctor.setDob(dobStr.isEmpty() ? doctor.getDob() : dobStr);
        //doctor.setSpeciality(speciality.getText().toString());
        doctor.setSpeciality(speciality.getSelectedItem().toString());
        doctor.setGender(gender.getSelectedItem().toString());
        doctor.setPhoneNumber(phoneStr.isEmpty() ? doctor.getPhoneNumber() : phoneStr);
        doctor.setLatLong(latLong != null ? latLong : doctor.getLatLong());
        doctor.setLocation(locStr.isEmpty() ? doctor.getLocation() : locStr);
        doctor.setImgUrl(downloadUri.toString());
        doctor.setUid(auth.getUid());

        sendData();
    }


    //--------------------Firebase----------------------------//

    //uploads Img
    private void uploadImage(FirebaseImageUploadCallback firebaseImageUploadCallback) {
        StorageReference profileRef = storageReference.child("ProfileImg/" + auth.getUid());

        Bitmap bitmap = ((BitmapDrawable) profile_pic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot,
                Task<Uri>>) task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return profileRef.getDownloadUrl();
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                firebaseImageUploadCallback.imageUploaded(downloadUri, true);
            } else {
                Toast.makeText(this, "Failed to Upload Image", Toast.LENGTH_SHORT).show();
                firebaseImageUploadCallback.imageUploaded(null, false);
            }
        });


    }


    //upload data  to firebase
    private void sendData() {
        if (doctor == null) {
            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection(DOCTOR).document(auth.getUid()).set(doctor, SetOptions.merge()).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DoctorMainScreen.class));
                finish();
            }
            else
            {
                Toast.makeText(this, "Error Occurred while saving profile", Toast.LENGTH_SHORT).show();
            }
            loadingDialog.dismiss();
        });
    }

    //retrieve Data
    private void retrieveData(ProfileDataReceived profileDataReceived) {
        DocumentReference docRef = db.collection(DOCTOR).document(auth.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Doctor doc = documentSnapshot.toObject(Doctor.class);
            profileDataReceived.dataReceived(doc, doc != null);

        });
    }

    //set data to profile
    private void setData(Doctor doc) {
        name.setText(doc.getName());
        email.setText(doc.getEmail());
        phone.setText(doc.getPhoneNumber());
        speciality.setSelection(getIndex(speciality, doc.getSpeciality()));
        gender.setSelection(getIndex(gender, doc.getGender()));
        dob.setText(doc.getDob());
        locationEdittext.setText(doc.getLocation());

        for (String value : doc.getDays()) {
            addNewChipWithoutSelectAction(value, chipGroupProfileTiming);
        }
        for (String value : doc.getTime()) {
            addNewChipWithoutSelectAction(value, chipGroupProfileTiming);
        }

        Glide.with(this).load(doc.getImgUrl())
                .placeholder(R.drawable.ic_baseline_person_24dp)
                .into(profile_pic);

    }


    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;

            }
        }

        return 0;
    }


    //----------------------Location---------------------------//
    // TODO: 01-04-2021 convert to class

    private void myPermission() {

        if (hasPermission(this, myPermissions)) {
            getLatLong();
        } else if (shouldShowRequestPermissionRationale(myPermissions[0])) {
            Toast.makeText(this, "Send user to setting screen", Toast.LENGTH_SHORT).show();
            showAlert(getString(R.string.permission_title), getString(R.string.permission_denied)
                    , 0);
        } else {
            showAlert(getString(R.string.permission_title),
                    getString(R.string.permission_denied_permanently), 1);

        }
    }

    @SuppressLint("MissingPermission")
    private void getLatLong() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            //400==400 meter
            if (location != null && location.hasAccuracy() && location.getAccuracy() <= 400) {
                try {
                    getAddressFromLatLong(location.getLatitude(), location.getLongitude());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                createLocationRequest();
                Log.d(TAG, "onSuccess: FusedLocation is Null or No Accuracy or less than " +
                        "400 meter Accuracy");
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        loadingDialog.show();
        Log.d(TAG,
                "getCurrentLocation: Method" + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Log.d(TAG, "getCurrentLocation: Method After");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    Log.d(TAG, "onLocationResult: Fused" + location.getLatitude());
                    try {
                        getAddressFromLatLong(location.getLatitude(), location.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            }

        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: createLocationRequest");
            getCurrentLocation();

        });
        task.addOnFailureListener(this, e -> {
            Log.d(TAG, "onFailure: createLocationRequest");
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(DoctorProfile.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

    }

    private void getAddressFromLatLong(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        latLong = latitude + "," + longitude;
        locationEdittext.setText(addresses.get(0).getAddressLine(0));

    }

    private boolean hasPermission(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showAlert(String Title, String Message, final int decider) {
        final AlertDialog alert;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Title);
        builder.setMessage(Message);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            if (decider == 0) {
                requestPermissions(myPermissions, RequestCode);
            } else {

                Toast.makeText(DoctorProfile.this, "Send to setting", Toast.LENGTH_SHORT).show();
                startSetting();
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alert = builder.create();
        alert.show();
    }

    private void startSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLatLong();
            } else if (shouldShowRequestPermissionRationale(permissions[0])) {

                Log.d(TAG, "onRequestPermissionsResult: Rational");
            } else {
                // TODO:  Request is cancelled (Handle it ?)
            }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                getLatLong();

                Log.d(TAG, "onActivityResult: getCurrentLocation");
            } else {
                Log.d(TAG, "onActivityResult: cancelled by user");
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                //filePath = data.getData();
                //Compressor compressor=new Compressor(this).compressToFile()
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                            data.getData());
                    profile_pic.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Some error Occurred", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "onActivityResult: requestCode!=RequestCheck Setting");
        }
    }
}