package com.android.doctorAppointment.myInterface;

import com.android.doctorAppointment.model.User;

public interface UserListener {

    void initiateVideoMeeting(User user,String token);
    void initiateAudioMeeting(User user,String token);
}
