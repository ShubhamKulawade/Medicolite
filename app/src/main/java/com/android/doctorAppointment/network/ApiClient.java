package com.android.doctorAppointment.network;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    private static Retrofit retrofit=null;

    public static Retrofit getRetrofit() {
        if(retrofit==null){
            String baseUrl = "https://fcm.googleapis.com/fcm/";
            retrofit=new  Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
