package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    /**
     * Converts a path to an OffsetDateTime.
     * <p>For example,
     * <p>Segment path : idx/segments/1601/01/01/0000/meta.json
     * <p>OffsetDateTime : year - 1601, month - 01, day - 01, hour - 00, minute - 00
     * <p>OR
     * <p>Year path : idx/segments/1601/
     * <p>OffsetDateTime : year - 1601, month - 00, day - 00, hour - 00, minute - 00
     *
     * @param path The path to convert.
     * @return The time associated with the path.
     */
    public static OffsetDateTime convertPathToTime(String path, ClientLogger logger) {
        if (path == null) {
            return null;
        }
        String[] splitPath = path.split("/");
        boolean yearPath = splitPath.length == 3;
        if (splitPath.length != 3 && splitPath.length != 7) {
            throw logger.logExceptionAsError(new RuntimeException("Error converting segment path to OffsetDateTime"));
        }
        return OffsetDateTime.of(
            Integer.parseInt(splitPath[2]), /* year */
            yearPath ? 1 : Integer.parseInt(splitPath[3]), /* month */
            yearPath ? 1 : Integer.parseInt(splitPath[4]), /* day */
            yearPath ? 0 : Integer.parseInt(splitPath[5]) / 100, /* hour */
            yearPath ? 0 : Integer.parseInt(splitPath[5]) % 100, /* minute */
            0, /* second */
            0, /* nano second */
            ZoneOffset.UTC /* zone offset */
        );
    }

    /**
     * Rounds a time up to the nearest hour.
     *
     * @param time The time to round.
     * @return The time rounded up to the nearest hour.
     */
    public static OffsetDateTime roundUpToNearestHour(OffsetDateTime time) {
        if (time.equals(OffsetDateTime.MIN) || time.equals(OffsetDateTime.MAX)) {
            return time;
        }
        return time.truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

    /**
     * Rounds a time down to the nearest hour.
     *
     * @param time The time to round.
     * @return The time rounded down to the nearest hour.
     */
    public static OffsetDateTime roundDownToNearestHour(OffsetDateTime time) {
        if (time.equals(OffsetDateTime.MIN) || time.equals(OffsetDateTime.MAX)) {
            return time;
        }
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * Rounds a time down to the nearest year.
     *
     * @param time The time to round.
     * @return The time rounded down to the nearest year.
     */
    public static OffsetDateTime roundDownToNearestYear(OffsetDateTime time) {
        return OffsetDateTime.of(time.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }
}
