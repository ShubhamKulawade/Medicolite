package com.android.doctorAppointment.utility;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.android.doctorAppointment.R;

import androidx.annotation.NonNull;

public class LoadingDialog extends Dialog {
    Context context;
    String message;
    public LoadingDialog(@NonNull Context context,String message) {
        super(context);
        this.context=context;
        this.message=message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflateView = inflater.inflate(R.layout.loading_dialog, (ViewGroup) findViewById(R.id.loading_cont));
        TextView t=inflateView.findViewById(R.id.textViewLoading);
        t.setText(message);
        setContentView(inflateView);
    }
}
