package com.android.doctorAppointment.model;

public class Appointment {
    private String doctorUid;
    private String userUid;
    private String documentUidDoctor;
    private String documentUidUser;
    private String date;
    private String time;
    private String key;
    private String status;
    private String cancelReason;
    private String doctorName;
    private String userName;

    public Appointment() {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey() {
        this.key = this.date+"_"+this.time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    @Override
    public String toString() {
        return "Appointment{" +
                "doctorUid='" + doctorUid + '\'' +
                ", userUid='" + userUid + '\'' +
                ", documentUidDoctor='" + documentUidDoctor + '\'' +
                ", documentUidUser='" + documentUidUser + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", key='" + key + '\'' +
                ", status='" + status + '\'' +
                ", cancelReason='" + cancelReason + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
