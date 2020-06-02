package com.example.automatedpillworks.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormats {
    public static String onlyDay(Long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        return dateFormat.format(date);
    }

    public static String dayWithTime(Long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY hh:mm aa");
        return dateFormat.format(date);
    }
}
