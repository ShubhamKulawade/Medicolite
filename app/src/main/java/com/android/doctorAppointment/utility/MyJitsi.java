package com.android.doctorAppointment.utility;

import android.app.Activity;
import android.widget.Toast;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class MyJitsi {


    //If error change to context from Activity or check IncomingInvitation#sendInvitation()
    //

    public static void initJitsi(Activity context, boolean audioOnly, String roomId)
    {
        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(roomId)
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .setAudioOnly(audioOnly)
                    .setWelcomePageEnabled(false)
                    .build();
            JitsiMeetActivity.launch(context,options);
            context.finish();

        } catch (MalformedURLException e) {
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
            context.finish();
        }
    }


}
