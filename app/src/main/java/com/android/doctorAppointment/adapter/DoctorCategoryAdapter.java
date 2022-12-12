package com.android.doctorAppointment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.DoctorCategory;
import com.android.doctorAppointment.myInterface.MyRecyclerClickListener;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DoctorCategoryAdapter extends RecyclerView.Adapter<DoctorCategoryAdapter.ViewHolder> {

    private int selectedItem = 0;
    private int lastSelected = 0;
    private final ArrayList<DoctorCategory> arrayList;
    private final Context context;
    private final MyRecyclerClickListener clickListener;

    public DoctorCategoryAdapter(ArrayList<DoctorCategory> arrayList, Context context, MyRecyclerClickListener clickListener) {
        this.arrayList = arrayList;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public DoctorCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.card_doctor_category,parent,false);
        return new DoctorCategoryAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull @NotNull DoctorCategoryAdapter.ViewHolder holder, int position) {
        DoctorCategory doctorCategory=arrayList.get(position);
        Glide.with(context).load(doctorCategory.getImageLocInt()).into(holder.imageView);
        holder.textViewName.setText(doctorCategory.getValue());

        int backgroundColor = (position == selectedItem) ? R.color.colorSelected : R.color.textContentColor;
        holder.textViewName.setTextColor(ContextCompat.getColor(context,backgroundColor));
        holder.itemView.setOnClickListener(v -> {
            //Save the position of the last selected item
            lastSelected = selectedItem;
            //Save the position of the current selected item
            selectedItem = position;

            //This update the last item selected
            notifyItemChanged(lastSelected);

            //This update the item selected
            notifyItemChanged(selectedItem);
            clickListener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textViewName;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView3);
            textViewName = itemView.findViewById(R.id.textView27);
        }
    }
}
