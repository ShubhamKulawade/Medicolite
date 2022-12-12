package com.android.doctorAppointment.model;

public class DoctorCategory {
    private String imageLoc;
    private String value;
    private int imageLocInt;

    public DoctorCategory(String imageLoc, String value) {
        this.imageLoc = imageLoc;
        this.value = value;
    }

    public DoctorCategory(int imageLocInt, String value) {
        this.value = value;
        this.imageLocInt = imageLocInt;
    }

    public String getImageLoc() {
        return imageLoc;
    }

    public void setImageLoc(String imageLoc) {
        this.imageLoc = imageLoc;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getImageLocInt() {
        return imageLocInt;
    }

    public void setImageLocInt(int imageLocInt) {
        this.imageLocInt = imageLocInt;
    }

    public String getTitle()
    {
        return value.split("\\n")[0];
    }

    @Override
    public String toString() {
        return "DoctorCategory{" +
                "imageLoc='" + imageLoc + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
