package com.android.doctorAppointment.sqlDb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.doctorAppointment.DAO.HistoryDao;
import com.android.doctorAppointment.model.ScannedImageHistory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ScannedImageHistory.class}, version = 2, exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {

    public abstract HistoryDao historyDao();

    public static volatile HistoryDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static HistoryDatabase getDatabase(final Context context)
    {
        if(instance==null)
        {
            synchronized (HistoryDatabase.class){
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            HistoryDatabase.class, "history_database")
                            .build();
                }
            }
        }
        return instance;
    }

}
