package com.android.doctorAppointment.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.mainScreen.UserMainScreen;
import com.android.doctorAppointment.model.User;
import com.android.doctorAppointment.myInterface.FirebaseImageUploadCallback;
import com.android.doctorAppointment.myInterface.ProfileDataReceived;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.doctorAppointment.utility.ErrorConstantString.EMAIL_ERROR;
import static com.android.doctorAppointment.utility.ErrorConstantString.NAME_ERROR;
import static com.android.doctorAppointment.utility.ErrorConstantString.PHONE_ERROR;

import static com.android.doctorAppointment.utility.MyConstants.USER;

public class UserProfile extends AppCompatActivity {

    private  final String[] myPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int RequestCode = 101;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private static final int PICK_IMAGE_REQUEST = 73;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LoadingDialog loadingDialog;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String TAG = "UserProfile";
    private EditText locationEdittext, name, email, dob, phone;
    private CircleImageView profile_pic;
    private String latLong;
    private User user;
    private Spinner gender;
    private LoadingDialog loadingDialogSave;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        loadingDialog = new LoadingDialog(this, "Retrieving Location...");
        loadingDialogSave = new LoadingDialog(this, "Uploading Data...");
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        init();
    }


    private void init() {
        List<EditText> editTextList = new ArrayList<>();
        user = new User();
        name = findViewById(R.id.editTextName);
        email = findViewById(R.id.editTextEmail);
        dob = findViewById(R.id.editTextDOB);
        phone = findViewById(R.id.editTextPhone);
        profile_pic = findViewById(R.id.profile_image_user);
        locationEdittext = findViewById(R.id.editTextLocation);
        gender = findViewById(R.id.spinnerGender);
        Button saveProfile = findViewById(R.id.buttonProfileSave);

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

        FirebaseImageUploadCallback firebaseImageUploadCallback = (downloadUri, success) -> {
            if (success) {
                setModel(downloadUri);
            } else
                loadingDialogSave.dismiss();
        };
        saveProfile.setOnClickListener(view -> {
            loadingDialogSave.show();
            if (validateEditTexts(editTextList)) {
                uploadImage(firebaseImageUploadCallback);
            }
            else
                loadingDialogSave.dismiss();
        });


        //callback interface when profile data is received
        ProfileDataReceived profileDataReceived = (object, success) -> {
            User user= (User) object;
            this.user=user;
            if (success&&user.getName()!=null&&!user.getName().isEmpty()) {
                setData((User) object);
            }
            //not adding else case since this can be the first time user is setting up account

        };
        retrieveData(profileDataReceived);
    }

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
        String nameStr,emailStr,dobStr,phoneStr,latStr,locStr;
        nameStr=name.getText().toString();
        emailStr=email.getText().toString();
        dobStr=dob.getText().toString();
        phoneStr=phone.getText().toString();
        locStr=locationEdittext.getText().toString();




        user.setName(nameStr.isEmpty()?user.getName():nameStr);
        user.setEmail(emailStr.isEmpty()?user.getEmail():emailStr);
        user.setDob(dobStr.isEmpty()?user.getDob():dobStr);
        user.setGender(gender.getSelectedItem().toString());
        user.setPhoneNumber(phoneStr.isEmpty()?user.getPhoneNumber():phoneStr);
        user.setLatLong(latLong!=null?latLong:user.getLatLong());
        user.setLocation(locStr.isEmpty()?user.getLocation():locStr);
        user.setImgUrl(downloadUri.toString());
        user.setUid(auth.getUid());



       /* user.setName(name.getText().toString());
        user.setEmail(email.getText().toString());
        user.setDob(dob.getText().toString());
        user.setPhoneNumber(phone.getText().toString());
        user.setGender(gender.getSelectedItem().toString());
        user.setPhoneNumber(phone.getText().toString());
        user.setLatLong(latLong!=null?latLong:user.getLatLong());

        user.setLocation(locationEdittext.getText().toString());
        user.setImgUrl(downloadUri.toString());
*/
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
        if (user == null) {
            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "sendData: ");
        db.collection(USER).document(auth.getUid()).set(user, SetOptions.merge()).addOnCompleteListener(task -> {
            Log.d(TAG, "sendData: DataUploaded: "+user.toString());
            if (task.isSuccessful())
            {
                Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, UserMainScreen.class));
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
        DocumentReference docRef = db.collection(USER).document(auth.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            profileDataReceived.dataReceived(user, user != null);

        });
    }

    //set data to profile
    private void setData(User user) {
        name.setText(user.getName());
        email.setText(user.getEmail());
        phone.setText(user.getPhoneNumber());
        gender.setSelection(getIndex(gender, user.getGender()));
        dob.setText(user.getDob());
        locationEdittext.setText(user.getLocation());


        Glide.with(this).load(user.getImgUrl())
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
                    resolvable.startResolutionForResult(UserProfile.this,
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
        loadingDialog.dismiss();

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

                Toast.makeText(UserProfile.this, "Send to setting", Toast.LENGTH_SHORT).show();
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
            }
        }

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

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
                    Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

                    profile_pic.setImageBitmap(decoded);
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