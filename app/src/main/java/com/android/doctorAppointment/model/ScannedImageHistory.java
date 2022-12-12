package com.android.doctorAppointment.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;


@Entity(tableName = "history_table")
public class ScannedImageHistory {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    private String imageUri;
    private String name;
    private Float confidence;
    @ColumnInfo(name = "timeStamp")
    private long timeStamp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public @NotNull String toString() {
        return "ScannedImageHistory{" +
                "uid=" + uid +
                ", imageUri='" + imageUri + '\'' +
                ", name='" + name + '\'' +
                ", confidence=" + confidence +
                ", timeStamp=" + timeStamp +
                '}';
    }
}


