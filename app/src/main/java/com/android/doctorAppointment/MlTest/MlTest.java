package com.android.doctorAppointment.MlTest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.android.doctorAppointment.DAO.HistoryDao;
import com.android.doctorAppointment.ML.ClassifierActivity1;
import com.android.doctorAppointment.ML.tflite.Classifier1;
import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.ScannedImageHistory;
import com.android.doctorAppointment.sqlDb.HistoryDatabase;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("LogNotTimber")
public class MlTest extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 2;
    private static final int REQUEST_CODE_PERMISSIONS2 = 3;
    private static final String TAG = "MyMlTest";
    private static final int PERMISSION_ALL = 100;
    private Classifier1 classifier;
    private Bitmap bitmap;
    private final int cameraRequestCode = 0;
    private final int galleryRequestCode = 1;

    private final Classifier1.Device device = Classifier1.Device.CPU;
    private ImageView imageView;
    private HistoryDao historyDao;
    private String currentPhotoPath;
    private final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            /*Manifest.permission.WRITE_EXTERNAL_STORAGE,*/
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml_test);
        try {
            int numOfThreads = -1;
            classifier = Classifier1.create(this, device, numOfThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initDb();
        init();

    }

    @SuppressLint("SwitchIntDef")
    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }


    private void init() {
        //TextView textView = findViewById(R.id.textViewTest);

        TextView textViewDiseaseName = findViewById(R.id.textViewDiseaseName);
        TextView textViewDiseasePercentage = findViewById(R.id.textViewDiseasePercentage);

        imageView = findViewById(R.id.imageViewTest);
        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        currentPhotoPath = "default";
        findViewById(R.id.textViewTestchange).setOnClickListener(view -> startActivity(new Intent(this, ClassifierActivity1.class)));


        findViewById(R.id.buttonTest).setOnClickListener(v -> {
            List<Classifier1.Recognition> recognitionList = classifier.recognizeImage(bitmap, 90 - getScreenOrientation());
            Classifier1.Recognition recognition = recognitionList.get(0);
            if (recognition == null) {
                return;
            }
            textViewDiseaseName.setText(recognition.getTitle().substring(2));
            textViewDiseasePercentage.setText(  String.format("%.2f %%", recognition.getConfidence()*100));
            //textView.setText(String.format("Name: %s\n Confidence: %s", recognition.getTitle(), recognition.getConfidence()));
            addNewHistory(recognition);

        });


        findViewById(R.id.buttonTestCamera).setOnClickListener(view -> {
            if (hasPermission(PERMISSIONS)) {
                dispatchTakePictureIntent();
                //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), cameraRequestCode);
            } else {
                permission();
            }
        });

        findViewById(R.id.buttontestGallery).setOnClickListener(view -> {
            if (hasPermission(PERMISSIONS)) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, galleryRequestCode);
            } else {
                permission();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile: " + currentPhotoPath);
        return image;

    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, cameraRequestCode);
            }
        }
    }


    private void initDb() {
        HistoryDatabase historyDatabase = HistoryDatabase.getDatabase(this);
        historyDao = historyDatabase.historyDao();
    }

    private void addNewHistory(Classifier1.Recognition recognition) {
        ScannedImageHistory scannedImageHistory = new ScannedImageHistory();
        scannedImageHistory.setName(recognition.getTitle());
        scannedImageHistory.setConfidence(recognition.getConfidence());
        scannedImageHistory.setImageUri(currentPhotoPath);
        scannedImageHistory.setTimeStamp(System.currentTimeMillis());

        HistoryDatabase.databaseWriteExecutor.execute(() -> historyDao.insert(scannedImageHistory));

        Toast.makeText(this, "Added in Local History", Toast.LENGTH_SHORT).show();
    }

    private void permission() {
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS2);
        }*/

        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

    }

    private boolean hasPermission(String[] permissions) {

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Camera Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                //finish();
            }
        } else if (requestCode == REQUEST_CODE_PERMISSIONS2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Gallery Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Gallery Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                //finish();
            }
        } else if (requestCode == PERMISSION_ALL) {

            ArrayList<String> deniedPermission = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    deniedPermission.add(permissions[i]);
                }
            }
            if (deniedPermission.size() > 0)
                Toast.makeText(this, deniedPermission.size() + " permissions denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == cameraRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {


                Glide.with(this).load(currentPhotoPath).into(imageView);
                //this.bitmap = (Bitmap) data.getExtras().get("data");
                //imageView.setImageBitmap(bitmap);

            }
        } else if (requestCode == galleryRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                String filePath = data.getData().getPath().substring(5);
                currentPhotoPath = filePath;
                Log.d(TAG, "onActivityResult: " + filePath);
                //Compressor compressor=new Compressor(this).compressToFile()
                try {
                    this.bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                            data.getData());
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}