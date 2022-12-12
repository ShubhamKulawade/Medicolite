package com.android.doctorAppointment.myInterface;

import com.android.doctorAppointment.model.Appointment;

public interface AppointmentCompletedListener {
    void onComplete(Appointment appointment);
}
