package com.android.doctorAppointment.MlTest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.DAO.HistoryDao;
import com.android.doctorAppointment.R;
import com.android.doctorAppointment.adapter.HistoryAdapter;
import com.android.doctorAppointment.model.ScannedImageHistory;
import com.android.doctorAppointment.myInterface.MyRecyclerClickListener;
import com.android.doctorAppointment.sqlDb.HistoryDatabase;
import com.android.doctorAppointment.utility.LoadingDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Scanned Image History
public class History extends AppCompatActivity {
    private static final String TAG = "MyHistory";
    private ArrayList<ScannedImageHistory> historyArrayList;
    private HistoryAdapter historyAdapter;
    private  LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyArrayList = new ArrayList<>();
        init();
        getData();
    }

    private void init() {

        loadingDialog = new LoadingDialog(this,"Retrieving Data...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        historyAdapter = new HistoryAdapter(this, historyArrayList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(historyAdapter);

    }

    //retrieve data from sqlite
    private void getData() {
        TextView textViewEmptyMessage = findViewById(R.id.textViewHistoryEmpty);
        @SuppressLint("LogNotTimber") MySqlCallback mySqlCallback = histories -> {
            historyArrayList.clear();
            historyArrayList.addAll(histories);
            if (historyArrayList.size() > 0) {
                textViewEmptyMessage.setVisibility(View.INVISIBLE);
            } else {
                textViewEmptyMessage.setVisibility(View.VISIBLE);
            }
            loadingDialog.dismiss();
            historyAdapter.notifyDataSetChanged();

        };
        HistoryDatabase historyDatabase = HistoryDatabase.getDatabase(this);
        HistoryDao historyDao = historyDatabase.historyDao();
        HistoryDatabase.databaseWriteExecutor.execute(() -> mySqlCallback.dataReceived(historyDao.getAll()));


    }

    private interface MySqlCallback {
        void dataReceived(List<ScannedImageHistory> histories);
    }

}