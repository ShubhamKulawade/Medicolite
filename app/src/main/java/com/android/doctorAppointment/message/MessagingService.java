package com.android.doctorAppointment.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.doctorAppointment.Invitations.IncomingInvitation;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.android.doctorAppointment.utility.MyConstants.EMAIL;
import static com.android.doctorAppointment.utility.MyConstants.MEETING_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_RESPONSE;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITER_TOKEN;
import static com.android.doctorAppointment.utility.MyConstants.MSG_MEETING_ROOM;
import static com.android.doctorAppointment.utility.MyConstants.MSG_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.NAME;

@SuppressLint("LogNotTimber")
public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService FCM";

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG,"onNewToken: " + s);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String type=remoteMessage.getData().get(MSG_TYPE);
        if(type!=null)
        {
            if(type.equals(MSG_INVITATION)){
                Intent intent=new Intent(getApplicationContext(), IncomingInvitation.class);
                intent.putExtra(MEETING_TYPE,remoteMessage.getData().get(MEETING_TYPE));
                intent.putExtra(NAME,remoteMessage.getData().get(NAME));
                intent.putExtra(EMAIL,remoteMessage.getData().get(EMAIL));
                intent.putExtra(MSG_INVITER_TOKEN,remoteMessage.getData().get(MSG_INVITER_TOKEN));
                intent.putExtra(MSG_INVITER_TOKEN,remoteMessage.getData().get(MSG_INVITER_TOKEN));
                intent.putExtra(MSG_MEETING_ROOM,remoteMessage.getData().get(MSG_MEETING_ROOM));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else if(type.equals(MSG_INVITATION_RESPONSE)){
                Intent intent=new Intent(MSG_INVITATION_RESPONSE);
                intent.putExtra(MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(MSG_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }





    }

}
