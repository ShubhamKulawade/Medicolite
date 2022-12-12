package com.android.doctorAppointment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.ScannedImageHistory;
import com.android.doctorAppointment.utility.MyBitmapConverter;
import com.bumptech.glide.Glide;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = "MyHistoryAdapter";
    private final Context context;
    private final ArrayList<ScannedImageHistory> historyArrayList;
    //private final MyRecyclerClickListener recyclerClickListener;

   /* public HistoryAdapter(ArrayList<ScannedImageHistory> historyArrayList, MyRecyclerClickListener recyclerClickListener) {
        this.historyArrayList = historyArrayList;
        this.recyclerClickListener = recyclerClickListener;
    }*/

    public HistoryAdapter(Context context, ArrayList<ScannedImageHistory> historyArrayList) {
        this.context = context;
        this.historyArrayList = historyArrayList;
        Log.d(TAG, "HistoryAdapter: " + Arrays.toString(historyArrayList.toArray()));
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_history_disease_ml, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        ScannedImageHistory history = historyArrayList.get(position);
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh:mm:ss a");
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("d MMM yyyy");
        Log.d(TAG, "onBindViewHolder: " + history.toString());
        holder.name.setText(history.getName().substring(2));
        holder.confidence.setText(String.format("%.2f %%", history.getConfidence()*100));
        String localTime = String.valueOf(new Date(history.getTimeStamp()));
        Log.d(TAG, "onBindViewHolder: " + localTime);
        holder.dateTime.setText(String.format("%s , %s", (new LocalTime(history.getTimeStamp()).toString(timeFormatter)),
                new LocalDate(history.getTimeStamp()).toString(dateFormatter)));

        if (history.getImageUri().equals("default")) {
            Glide.with(context).load(R.drawable.p).into(holder.image);
        } else {
            Glide.with(context).load(history.getImageUri()).error(R.drawable.ic_baseline_error_outline_24).into(holder.image);
        }

    }

    @Override
    public int getItemCount() {
        return historyArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, confidence, dateTime;
        protected ImageView image;
        //protected ImageButton buttonMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewHistoryDiseaseName);
            confidence = itemView.findViewById(R.id.textViewHistoryDiseaseConfidence);
            dateTime = itemView.findViewById(R.id.textViewHistoryDateTime);
            image = itemView.findViewById(R.id.imageViewHistoryImage);
            //buttonMore = itemView.findViewById(R.id.imageButtonHistoryViewMore);
        }
    }
}
