package com.android.doctorAppointment.Invitations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.doctorAppointment.R;
import com.android.doctorAppointment.network.ApiService;
import com.android.doctorAppointment.network.ApiClient;
import com.android.doctorAppointment.utility.MyJitsi;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.android.doctorAppointment.utility.MyConstants.EMAIL;
import static com.android.doctorAppointment.utility.MyConstants.IMG_URL;
import static com.android.doctorAppointment.utility.MyConstants.MEETING_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.MSG_DATA;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_ACCEPTED;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_CANCEL;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_REJECTED;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITATION_RESPONSE;
import static com.android.doctorAppointment.utility.MyConstants.MSG_INVITER_TOKEN;
import static com.android.doctorAppointment.utility.MyConstants.MSG_MEETING_ROOM;
import static com.android.doctorAppointment.utility.MyConstants.MSG_REGISTRATION_IDS;
import static com.android.doctorAppointment.utility.MyConstants.MSG_TYPE;
import static com.android.doctorAppointment.utility.MyConstants.NAME;
import static com.android.doctorAppointment.utility.MyConstants.getRemoteMsgHeaders;

public class IncomingInvitation extends AppCompatActivity {

    private String meetingType = null;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        init();
    }

    private void init() {
        meetingType = getIntent().getStringExtra(MEETING_TYPE);
        String imageUrl = getIntent().getStringExtra(IMG_URL);
        String name = getIntent().getStringExtra(NAME);
        String email = getIntent().getStringExtra(EMAIL);
        String inviterToken = getIntent().getStringExtra(MSG_INVITER_TOKEN);

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        player = MediaPlayer.create(this, alert);
        player.setLooping(true);
        player.start();


        ImageView incomeProfile = findViewById(R.id.imageViewProfileIncoming);
        ImageView meetingTypeImage = findViewById(R.id.imageViewMeetingType);
        TextView callerName = findViewById(R.id.textViewCallerName);
        TextView callerEmail = findViewById(R.id.textViewCallerEmail);
        FloatingActionButton fabAccept = findViewById(R.id.fabAccept);
        FloatingActionButton fabReject = findViewById(R.id.fabReject);
        if (meetingType != null)
            if (meetingType.equals("audio")) {
                meetingTypeImage.setImageResource(R.drawable.ic_baseline_local_phone_24);
            }

        callerName.setText(name);
        callerEmail.setText(email);
        Glide.with(this).load(imageUrl).into(incomeProfile);

        fabAccept.setOnClickListener(view -> sendInvitationResponse(MSG_INVITATION_ACCEPTED,
                inviterToken));

        fabReject.setOnClickListener(view -> sendInvitationResponse(MSG_INVITATION_REJECTED,
                inviterToken));

    }


    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray token = new JSONArray();
            token.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(MSG_TYPE, MSG_INVITATION_RESPONSE);
            data.put(MSG_INVITATION_RESPONSE, type);


            body.put(MSG_DATA, data);
            body.put(MSG_REGISTRATION_IDS, token);

            sendRemoteMessage(body.toString(), type);

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
                    if (type.equals(MSG_INVITATION_ACCEPTED)) {
                        //me (see MyJitsi.java)
                        if (meetingType.equals("video"))
                            MyJitsi.initJitsi(IncomingInvitation.this, false,
                                    getIntent().getStringExtra(MSG_MEETING_ROOM));
                        else if (meetingType.equals("audio"))
                            MyJitsi.initJitsi(IncomingInvitation.this, true,
                                    getIntent().getStringExtra(MSG_MEETING_ROOM));
                    } else {
                        Toast.makeText(IncomingInvitation.this, "Invitation Rejected",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(IncomingInvitation.this, response.message(),
                            Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(IncomingInvitation.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();

            }
        });
        player.stop();
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(MSG_INVITATION_CANCEL)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
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