package com.android.doctorAppointment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.Prescription;
import com.android.doctorAppointment.utility.MyUtility;

import java.util.ArrayList;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {

    private ArrayList<Prescription> prescriptionArrayList;

    public PrescriptionAdapter(ArrayList<Prescription> prescriptionArrayList) {
        this.prescriptionArrayList = prescriptionArrayList;
    }

    @NonNull
    @Override
    public PrescriptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionAdapter.ViewHolder holder, int position) {

        Prescription prescription=prescriptionArrayList.get(position);
        long timeStamp=prescription.getCreationTimeStamp();
        holder.textViewDoctorName.setText(prescription.getDoctorName());
        holder.textViewTime.setText(String.format("%s , %s", MyUtility.timeStampToTime(timeStamp)
                , MyUtility.timeStampToDate(timeStamp)));
        holder.textViewPrescriptionData.setText(prescription.getPrescription());


    }

    @Override
    public int getItemCount() {
        return prescriptionArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewDoctorName,textViewTime,textViewPrescriptionData;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewDoctorName=itemView.findViewById(R.id.textViewPrescriptionDoctorName);
            textViewTime=itemView.findViewById(R.id.textViewPrescriptionDateTime);
            textViewPrescriptionData=itemView.findViewById(R.id.textViewPrescriptionData);


        }
    }
}
