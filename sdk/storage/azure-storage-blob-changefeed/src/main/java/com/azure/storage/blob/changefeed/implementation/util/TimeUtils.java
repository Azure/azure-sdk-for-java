// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.util;

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
    public static OffsetDateTime convertPathToTime(String path) {
        if (path == null) {
            return null;
        }
        String[] splitPath = path.split("/");

        return OffsetDateTime.of(
            Integer.parseInt(splitPath[2]), /* year */
            splitPath.length < 4 ? 1 : Integer.parseInt(splitPath[3]), /* month */
            splitPath.length < 5 ? 1 : Integer.parseInt(splitPath[4]), /* day */
            splitPath.length < 6 ? 0 : Integer.parseInt(splitPath[5]) / 100, /* hour */
            0, /* minute */
            0, /* second */
            0, /* nano second */
            ZoneOffset.UTC /* zone offset */
        );
    }

    /**
     * Validates that the year lies within the start and end times.
     * @param year The year.
     * @param start The start time.
     * @param end The end time.
     * @return Whether or not the year lies within the start and end times.
     */
    public static boolean validYear(String year, OffsetDateTime start, OffsetDateTime end) {
        if (year == null || start == null || end == null) {
            return false;
        }
        OffsetDateTime currentYear = convertPathToTime(year);
        OffsetDateTime startYear = roundDownToNearestYear(start);
        OffsetDateTime endYear = roundDownToNearestYear(end);
        return ((currentYear.isEqual(startYear) || currentYear.isAfter(startYear))
            && (currentYear.isEqual(endYear) || currentYear.isBefore(endYear)));
    }

    /**
     * Validates that the segment lies within the start and end times.
     * @param segment The segment.
     * @param start The start time.
     * @param end The end time.
     * @return Whether or not the segment lies within the start and end times.
     */
    public static boolean validSegment(String segment, OffsetDateTime start, OffsetDateTime end) {
        if (segment == null || start == null || end == null) {
            return false;
        }
        OffsetDateTime hour = convertPathToTime(segment);
        OffsetDateTime startHour = roundDownToNearestHour(start);
        OffsetDateTime endHour = roundUpToNearestHour(end);
        return ((hour.isEqual(startHour) || hour.isAfter(startHour))
            && hour.isBefore(endHour));
    }

    /**
     * Rounds a time up to the nearest hour.
     */
    public static OffsetDateTime roundUpToNearestHour(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        if (time.equals(OffsetDateTime.MIN) || time.equals(OffsetDateTime.MAX)) {
            return time;
        }
        /* Don't want to round up a time that is already a valid hour. */
        if (time.equals(time.truncatedTo(ChronoUnit.HOURS))) {
            return time;
        }
        return time.truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

    /**
     * Rounds a time down to the nearest hour.
     */
    public static OffsetDateTime roundDownToNearestHour(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        if (time.equals(OffsetDateTime.MIN) || time.equals(OffsetDateTime.MAX)) {
            return time;
        }
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * Rounds a time down to the nearest year.
     */
    private static OffsetDateTime roundDownToNearestYear(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        return OffsetDateTime.of(time.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }
}
