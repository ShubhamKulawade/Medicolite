package com.android.doctorAppointment.utility;

import com.android.doctorAppointment.model.User;

public class MyCurrentUser {
    private static User user;

    public static void setUser(User user) {
        MyCurrentUser.user = user;
    }

    public static User getUser() {
        return user;
    }
}