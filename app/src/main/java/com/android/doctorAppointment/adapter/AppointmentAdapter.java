package com.android.doctorAppointment.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.Appointment;
import com.android.doctorAppointment.myInterface.AppointmentCancelListener;
import com.android.doctorAppointment.myInterface.AppointmentCompletedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointmentList;
    private final Context context;
    private final AppointmentCancelListener appointmentCancelListener;
    private final static String TAG = "AppointmentAdapter";
    private final AppointmentCompletedListener appointmentCompletedListener;
    private final boolean isDoctor;

    public AppointmentAdapter(List<Appointment> appointmentList, Context context,
                              AppointmentCancelListener appointmentCancelListener,
                              AppointmentCompletedListener appointmentCompletedListener,
                              boolean isDoctor) {
        this.appointmentList = appointmentList;
        this.context = context;
        this.appointmentCancelListener = appointmentCancelListener;
        this.appointmentCompletedListener = appointmentCompletedListener;
        this.isDoctor = isDoctor;
    }

    public void setArraylist(ArrayList<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        if (isDoctor) {
            holder.completeAppointment.setVisibility(View.VISIBLE);
        }
        else{
            holder.completeAppointment.setVisibility(View.GONE);
        }

        if (appointment.getCancelReason() != null) {
            holder.cancelReason.setText(appointment.getCancelReason());
            holder.cancelAppointment.setVisibility(View.GONE);
            holder.completeAppointment.setVisibility(View.GONE);

        } else{
            holder.cancelReason.setVisibility(View.GONE);
            holder.cancelAppointment.setVisibility(View.VISIBLE);

        }


        if (appointment.getStatus().equals("Cancelled")) {
            holder.status.setTextColor(Color.RED);
        } else if (appointment.getStatus().equals("Completed")) {
            holder.cancelAppointment.setVisibility(View.GONE);
            holder.completeAppointment.setVisibility(View.GONE);
            holder.status.setTextColor(Color.parseColor("#006633"));
        } else {
            holder.status.setTextColor(Color.parseColor("#222222"));
        }




        holder.userName.setText(appointment.getUserName());
        holder.docName.setText(appointment.getDoctorName());
        holder.status.setText(appointment.getStatus());
        holder.dateTime.setText(String.format("%s %s", appointment.getDate(),
                appointment.getTime()));
        holder.cancelAppointment.setOnClickListener(view -> appointmentCancelListener.onclick(appointment));
        holder.completeAppointment.setOnClickListener(view -> appointmentCompletedListener.onComplete(appointment));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView docName, userName, status, cancelReason, dateTime;
        FloatingActionButton cancelAppointment, completeAppointment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            docName = itemView.findViewById(R.id.textViewListDoctorName);
            userName = itemView.findViewById(R.id.textViewListUserName);
            dateTime = itemView.findViewById(R.id.textViewAppointmentDateTime);
            status = itemView.findViewById(R.id.textViewAppointmentStatus);
            cancelReason = itemView.findViewById(R.id.textViewAppointmentCancelReason);
            cancelAppointment = itemView.findViewById(R.id.buttonCancelAppointment);
            completeAppointment = itemView.findViewById(R.id.buttonCompleteAppointment);

            if (!isDoctor) {
                completeAppointment.setVisibility(View.GONE);
            }

        }
    }
}
