package com.android.doctorAppointment.model;

import org.jetbrains.annotations.NotNull;

public class Prescription {
    private String doctorUid;
    private String userUid;
    private String documentUidDoctor;
    private String documentUidUser;
    private long creationTimeStamp;
    private String prescription;
    private String doctorName;
    private String userName;

    public Prescription() {
    }

    public String getDoctorUid() {
        return doctorUid;
    }

    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getDocumentUidDoctor() {
        return documentUidDoctor;
    }

    public void setDocumentUidDoctor(String documentUidDoctor) {
        this.documentUidDoctor = documentUidDoctor;
    }

    public String getDocumentUidUser() {
        return documentUidUser;
    }

    public void setDocumentUidUser(String documentUidUser) {
        this.documentUidUser = documentUidUser;
    }

    public long getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(long creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public @NotNull String toString() {
        return "Prescription{" +
                "doctorUid='" + doctorUid + '\'' +
                ", userUid='" + userUid + '\'' +
                ", documentUidDoctor='" + documentUidDoctor + '\'' +
                ", documentUidUser='" + documentUidUser + '\'' +
                ", creationTimeStamp=" + creationTimeStamp +
                ", prescription='" + prescription + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
