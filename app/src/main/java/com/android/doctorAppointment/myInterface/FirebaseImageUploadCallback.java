package com.android.doctorAppointment.myInterface;

import android.net.Uri;

public interface FirebaseImageUploadCallback {
    void imageUploaded(Uri downloadUri,boolean success);
}
