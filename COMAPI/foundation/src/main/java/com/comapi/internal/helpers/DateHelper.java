package com.comapi.internal.helpers;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper methods to obtain and format date and time.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DateHelper {

    /**
     * Gets the current UTC date and time in 'yyyy-MM-dd'T'HH:mm:ss.SSS'Z'' format.
     *
     * @return Current UTC date and time.
     */
    public static String getCurrentUTC() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * Gets the UTC time in milliseconds from 'yyyy-MM-dd'T'HH:mm:ss.SSS'Z'' formatted date string.
     *
     * @param dateStr 'yyyy-MM-dd'T'HH:mm:ss.SSSz' formatted date string.
     * @return UTC time in milliseconds.
     */
    public static long getUTCMilliseconds(@NonNull final String dateStr) {

        if (!TextUtils.isEmpty(dateStr)) {

            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (date != null) {
                return date.getTime();
            }
        }

        return -1;
    }

    /**
     * Gets the UTC date and time in 'yyyy-MM-dd'T'HH:mm:ss.SSS'Z'' format.
     *
     * @return UTC date and time.
     */
    public static String getUTC(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(time);
    }
}