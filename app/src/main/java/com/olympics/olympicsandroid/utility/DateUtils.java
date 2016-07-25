package com.olympics.olympicsandroid.utility;

import android.text.TextUtils;

import com.olympics.olympicsandroid.OlympicsApplication;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by tkmagz4 on 7/10/16.
 */
public class DateUtils {

    public static final String DATE_TIME_WITH_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
    public static final String DATE_TIME_WITHOUT_TIMEZONE_FORMAT = "MMM-dd'    Time - 'HH:mm";
    public static final String DATE_TIME_WITHOUT_DATE_TIMEZONE_FORMAT = "HH:mm";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // Aug-03-2016
    public static final long OLYMPIC_EVENT_START_DATE = 1470182400000L;

    // Aug-21-2016

    public static final long OLYMPIC_EVENT_END_DATE = 1471737600000L;

    public static final long NUM_OF_MILISECONDS_IN_DAY = 86400000;

    public static String getDateTimeInMillis(String dateStr) {

        if (TextUtils.isEmpty(dateStr)) {
            return null;
        } else {
            Date date = null;
            try {
                date = new SimpleDateFormat(DATE_TIME_WITH_TIMEZONE_FORMAT).parse(dateStr);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                return String.valueOf(calendar.getTime().getTime());

            } catch (ParseException e) {
                return "0";
            }
        }
    }

    public static boolean isCurrentDateInOlympics()
    {
        if(System.currentTimeMillis() < OlympicsApplication.getAppInstance().getCacheStartDate())
        {
            return false;
        }
        return true;
    }

    public static String setUpUnitDate(String timeInMiliSeconds) {

        try {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.setTimeInMillis(Long.parseLong(timeInMiliSeconds));
            return formatter.format(localCalendar.getTime());
        } catch (Exception ex) {
            return "";
        }

    }

    public static String getUnitDateWithTime(String timeInMiliSeconds) {

        try {

            SimpleDateFormat f = new SimpleDateFormat(DATE_TIME_WITH_TIMEZONE_FORMAT);
            Date d = f.parse(timeInMiliSeconds);
            long milliseconds = d.getTime();


            DateFormat formatter = new SimpleDateFormat(DATE_TIME_WITHOUT_TIMEZONE_FORMAT, Locale.getDefault());
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.setTimeInMillis(milliseconds);
            return formatter.format(localCalendar.getTime());
        } catch (Exception ex) {
            return "";
        }

    }


    public static String getUnitTime(String timeInMiliSeconds) {

        try {

            SimpleDateFormat f = new SimpleDateFormat(DATE_TIME_WITH_TIMEZONE_FORMAT);
            Date d = f.parse(timeInMiliSeconds);
            long milliseconds = d.getTime();


            DateFormat formatter = new SimpleDateFormat(DATE_TIME_WITHOUT_DATE_TIMEZONE_FORMAT, Locale.getDefault());
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.setTimeInMillis(milliseconds);
            return formatter.format(localCalendar.getTime());
        } catch (Exception ex) {
            return "";
        }

    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}

