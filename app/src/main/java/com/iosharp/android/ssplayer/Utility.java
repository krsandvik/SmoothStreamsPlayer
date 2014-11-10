package com.iosharp.android.ssplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class Utility {

    private boolean isDst() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    private Date adjustForDst(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    static Long convertDateToLong(String dateString) {
        SimpleDateFormat dateFormat;
        // For the start/end datetime
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
            // If we adjust justDate for DST, we could be an hour behind and the date is not correct.
//            if (isDst()) {
//                return adjustForDst(convertedDate);
//            }
            return convertedDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
