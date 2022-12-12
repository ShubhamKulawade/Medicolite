package com.android.doctorAppointment.utility;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MyUtility {

    /*Converts Wind Degree to the Compass Direction*/
    //Since there is an angle change at every 22.5 degrees, the direction should swap hands after 11.25 degrees.
    public static String degToCompass(float deg)
    {
        int val = (int) Math.floor((deg / 22.5) + 0.5);
        String[] arr = new String[] {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        return arr[ (val % 16)];
    }


    public static String timeStampToDate(long timeStamp)
    {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("d MMM yyyy");
        return new LocalDate(timeStamp).toString(dateFormatter);
    }

    public static String timeStampToTime(long timeStamp)
    {
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh:mm:ss a");
        return new LocalTime(timeStamp).toString(timeFormatter);
    }

}
