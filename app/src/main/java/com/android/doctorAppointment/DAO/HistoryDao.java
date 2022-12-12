package com.android.doctorAppointment.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.doctorAppointment.model.ScannedImageHistory;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insert(ScannedImageHistory scannedImageHistory);

    @Query("Delete from history_table")
    void deleteAll();

    @Query("SELECT * FROM history_table ORDER BY timeStamp ASC")
    List<ScannedImageHistory> getAll();
}
