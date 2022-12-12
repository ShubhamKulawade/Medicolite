package com.android.doctorAppointment.utility;

import java.util.HashMap;

public class MyConstants {
    public final static String DOCTOR="doctor";
    public final static String USER="user";
    public final static String INTENT_EXTRA_UID="Uid";
    public final static String USER_TYPE="user_type";
    public final static String CURRENT_USER="current_user";
    public final static String APPOINTMENT="appointment";
    public final static String PRESCRIPTION="prescription";
    public final static String FCM_TOKEN="token";
    public final static String MEETING_TYPE="meetingType";
    public final static String MSG_MEETING_ROOM="meetingRoom";

    public final static String NAME="Name";
    public final static String EMAIL="Email";
    public final static String IMG_URL="ImageUrl";

    public final static String REMOTE_MSG_AUTH="Authorization";
    public final static String REMOTE_CONTENT_TYPE="Content-Type";

    public final static String MSG_TYPE="type";
    public final static String MSG_INVITATION="invitation";
    //public final static String MSG_MEETING_TYPE="meetingType";
    public final static String MSG_INVITER_TOKEN="inviterToken";
    public final static String MSG_DATA="data";
    public final static String MSG_REGISTRATION_IDS="registration_ids";
    public final static String MSG_INVITATION_RESPONSE="invitationResponse";
    public final static String MSG_INVITATION_ACCEPTED="accepted";
    public final static String MSG_INVITATION_REJECTED="rejected";
    public final static String MSG_INVITATION_CANCEL="cancel";


    public static HashMap<String,String> getRemoteMsgHeaders(){

        HashMap<String,String> header=new HashMap<>();
        header.put(
                REMOTE_MSG_AUTH,
                "key=AAAAbaOtr8A:APA91bGmrlTLmfLXD_nYF6Ll3GRooX3l3_7oFzA-CDyBuxT_rcFR5HLy8-Vvz7Y3wrWq5T88aapQpjEJ_zYaZnqnQz1K4J_G4Ftfjw-WeZezn71NEXxZTnunyiFLKrBOn-TAEMvqXJMP"
        );
        header.put(REMOTE_CONTENT_TYPE,"application/json");
        return header;
    }
}
