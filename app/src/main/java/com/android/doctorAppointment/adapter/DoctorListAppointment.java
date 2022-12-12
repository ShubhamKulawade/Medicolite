package com.android.doctorAppointment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.Doctor;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DoctorListAppointment extends RecyclerView.Adapter<DoctorListAppointment.ViewHolder> {
    private final Context context;
    private ArrayList<Doctor> doctorArrayList;
    View.OnClickListener mOnClickListener;

   /* @SuppressWarnings("unused")
    public DoctorListAppointment(Context context, ArrayList<Doctor> doctorArrayList,
                                 View.OnClickListener mOnClickListener) {
        this.context = context;
        this.doctorArrayList = doctorArrayList;
        this.mOnClickListener = mOnClickListener;
    }*/

    public DoctorListAppointment(Context context, ArrayList<Doctor> doctorArrayList) {
        this.context = context;
        this.doctorArrayList = doctorArrayList;

    }
    public void setDoctorArrayList(ArrayList<Doctor> doctorArrayList){
        this.doctorArrayList = doctorArrayList;
    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.list_doctor,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor=doctorArrayList.get(position);
        holder.name.setText(doctor.getName());
        holder.speciality.setText(doctor.getSpeciality());
        holder.distance.setText(String.format("%s Km", doctor.getDistanceFromUser()));
        Glide.with(context).load(doctor.getImgUrl()).placeholder(R.drawable.ic_baseline_person_24dp).into(holder.imageView);

    }




    @Override
    public int getItemCount() {
        return doctorArrayList.size();
    }

    public void setOnItemClickListener(View.OnClickListener mOnClickListener)
    {
        this.mOnClickListener=mOnClickListener;
    }

    public void setArrayList(ArrayList<Doctor> result) {
        this.doctorArrayList=result;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name,speciality,distance;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageViewDoctorImageList);
            name=itemView.findViewById(R.id.textViewDoctorName);
            speciality=itemView.findViewById(R.id.textViewSpeciality);
            distance=itemView.findViewById(R.id.textViewDistance);
            itemView.setTag(this);
            itemView.setOnClickListener(mOnClickListener);
        }
    }
}
