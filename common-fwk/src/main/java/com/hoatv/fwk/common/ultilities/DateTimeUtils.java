package com.hoatv.fwk.common.ultilities;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    private DateTimeUtils() {

    }

    public static long getCurrentEpochTimeInSecond() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static long getCurrentEpochTimeInMillisecond() {
        return System.currentTimeMillis();
    }
}
