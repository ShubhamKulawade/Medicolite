package com.android.doctorAppointment.Invitations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.model.User;
import com.android.doctorAppointment.network.ApiService;
import com.android.doctorAppointment.network.ApiClient;
import com.android.doctorAppointment.utility.MyJitsi;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import static com.android.doctorAppointment.utility.MyConstants.CURRENT_USER;
import static com.android.doctorAppointment.utility.MyConstants.EMAIL;
import static com.android.doctorAppointment.utility.MyConstants.IMG_URL;
import static com.android.doctorAppointment.utility.MyConstants.MEETING_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.MSG_DATA;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_ACCEPTED;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_CANCEL;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_REJECTED;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_RESPONSE;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITER_TOKEN;
import static com.android.doctorAppointment.utility.MyConstants.MSG_MEETING_ROOM;
import static com.android.doctorAppointment.utility.MyConstants.MSG_REGISTRATION_IDS;
import static com.android.doctorAppointment.utility.MyConstants.MSG_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.NAME;
import static com.android.doctorAppointment.utility.MyConstants.USER;
import static com.android.doctorAppointment.utility.MyConstants.getRemoteMsgHeaders;

@SuppressLint("LogNotTimber")
public class OutgoingInvitation extends AppCompatActivity {

    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        init();
    }

    private void init() {
        CircleImageView circleImageView = findViewById(R.id.imageViewProfileOutgoing);
        TextView name = findViewById(R.id.textViewReceiverName);
        TextView email = findViewById(R.id.textViewReceiverEmail);
        FloatingActionButton cancel = findViewById(R.id.fabCancel);
        ImageView meetingTypeImage = findViewById(R.id.imageViewMeetingTypeOutgoint);
        meetingType = getIntent().getStringExtra(MEETING_TYPE);
        User user = (User) getIntent().getSerializableExtra(USER);
        User currentUser = (User) getIntent().getSerializableExtra(CURRENT_USER);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            inviterToken =
                    task.getResult();
            if (meetingType != null && user != null) {

                initMeeting(meetingType, currentUser, user.getToken());
            }
        });


        if (meetingType != null)
            if (meetingType.equals("audio"))
                meetingTypeImage.setImageResource(R.drawable.ic_baseline_local_phone_24);


        if (user != null && !user.getName().trim().isEmpty()) {
            name.setText(user.getName());
            email.setText(user.getEmail());
            Glide.with(this).load(user.getImgUrl()).into(circleImageView);
        }

        System.out.println(user.toString());

        cancel.setOnClickListener(view -> {
            if (user != null) {
                Log.d("MyOutGoing", "init: " + user.getToken());
                cancelInvitation(user.getToken());
            }
        });


    }


    //Data of currentUser
    private void initMeeting(String meetingType, User user, String receiverToken) {
        try {
            JSONArray token = new JSONArray();
            token.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(MSG_TYPE, MSG_INVITATION);
            data.put(MEETING_TYPE, meetingType);
            data.put(NAME, user.getName());
            data.put(EMAIL, user.getEmail());
            data.put(IMG_URL, user.getImgUrl());
            data.put(MSG_INVITER_TOKEN, inviterToken);
            meetingRoom = UUID.randomUUID().toString();
            data.put(MSG_MEETING_ROOM, meetingRoom);

            body.put(MSG_DATA, data);
            body.put(MSG_REGISTRATION_IDS, token);

            sendRemoteMessage(body.toString(), MSG_INVITATION);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void sendRemoteMessage(String body, String type) {
        ApiClient.getRetrofit().create(ApiService.class).sendRemoteMessage(
                getRemoteMsgHeaders(), body
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(MSG_INVITATION)) {
                        Toast.makeText(OutgoingInvitation.this, "Invitation Sent",
                                Toast.LENGTH_SHORT).show();
                    } else if (type.equals(MSG_INVITATION_RESPONSE)) {
                        Toast.makeText(OutgoingInvitation.this, "Invitation Cancelled",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OutgoingInvitation.this, response.message(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(OutgoingInvitation.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();

            }
        });
    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray token = new JSONArray();
            token.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(MSG_TYPE, MSG_INVITATION_RESPONSE);
            data.put(MSG_INVITATION_RESPONSE, MSG_INVITATION_CANCEL);


            body.put(MSG_DATA, data);
            body.put(MSG_REGISTRATION_IDS, token);

            sendRemoteMessage(body.toString(), MSG_INVITATION_RESPONSE);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(MSG_INVITATION_ACCEPTED)) {
                    if(meetingType.equals("video"))
                    MyJitsi.initJitsi(OutgoingInvitation.this, false, meetingRoom);
                    else if(meetingType.equals("audio"))
                        MyJitsi.initJitsi(OutgoingInvitation.this, true, meetingRoom);



                } else if (type.equals(MSG_INVITATION_REJECTED)) {
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(invitationResponseReceiver,
                new IntentFilter(MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(invitationResponseReceiver);
    }
}