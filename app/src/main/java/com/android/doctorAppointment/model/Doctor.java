package com.android.doctorAppointment.model;

import java.util.List;

public class Doctor extends User {
    /*private String name;
    private String email;
    private String dob;
    private String gender;*/
    private String speciality;
    private List<String> time;
    private List<String> days;
    /*private String phoneNumber;
    private String location;
    private String latLong;
    private String imgUrl;*/
    private double distanceFromUser;
    //private String uid;

    public Doctor() {
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    /* public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getSpeciality() {
            return speciality;
        }

        public void setSpeciality(String speciality) {
            this.speciality = speciality;
        }

        public List<String> getTime() {
            return time;
        }

        public List<String> getDays() {
            return days;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLatLong() {
            return latLong;
        }

        public void setLatLong(String latLong) {
            this.latLong = latLong;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public void setDays(List<String> days) {
            this.days = days;
        }
    */
    public double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", speciality='" + speciality + '\'' +
                ", time=" + time +
                ", days=" + days +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", location='" + location + '\'' +
                ", latLong='" + latLong + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", distanceFromUser=" + distanceFromUser +
                ", uid='" + uid + '\'' +
                '}';
    }
}
