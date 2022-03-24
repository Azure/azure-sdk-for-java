/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Represents the SQL Server DATETIMEOFFSET data type.
 *
 * The DateTimeOffset class represents a java.sql.Timestamp, including fractional seconds, plus an integer representing
 * the number of minutes offset from GMT.
 */
public final class DateTimeOffset implements java.io.Serializable, java.lang.Comparable<DateTimeOffset> {
    private static final long serialVersionUID = 541973748553014280L;

    /**
     * Milliseconds.
     */
    private final long utcMillis;
    /**
     * Nanoseconds.
     */
    private final int nanos;
    /**
     * Minutes offset.
     */
    private final int minutesOffset;

    private static final int NANOS_MIN = 0;
    private static final int NANOS_MAX = 999999999;
    private static final int MINUTES_OFFSET_MIN = -14 * 60;
    private static final int MINUTES_OFFSET_MAX = 14 * 60;
    private static final int HUNDRED_NANOS_PER_SECOND = 10000000;

    /**
     * Constructs a DateTimeOffset.
     *
     * This method does not check that its arguments represent a timestamp value that falls within the range of values
     * acceptable to SQL Server for the DATETIMEOFFSET data type. That is, it is possible to create a DateTimeOffset
     * instance representing a value outside the range from 1 January 1AD 00:00:00 UTC to 31 December 9999 00:00:00 UTC.
     */
    private DateTimeOffset(java.sql.Timestamp timestamp, int minutesOffset) {
        // Combined time zone and DST offset must be between -14:00 and 14:00
        if (minutesOffset < MINUTES_OFFSET_MIN || minutesOffset > MINUTES_OFFSET_MAX)
            throw new IllegalArgumentException(MicrosoftDataEncryptionExceptionResource.getResource("R_IllegalOffset"));
        this.minutesOffset = minutesOffset;

        // Nanos must be between 0 and 999999999 inclusive
        int timestampNanos = timestamp.getNanos();
        if (timestampNanos < NANOS_MIN || timestampNanos > NANOS_MAX)
            throw new IllegalArgumentException(MicrosoftDataEncryptionExceptionResource.getResource("R_IllegalNanos"));

        // This class represents values to 100ns precision. If the java.sql.Timestamp argument
        // represents a value that is more precise, then nanos in excess of the 100ns precision
        // allowed by this class are rounded to the nearest multiple of 100ns.
        //
        // Values within 50 nanoseconds of the next second are rounded up to the next second.
        // Note: Values within 50 nanoseconds of the end of time wrap back to the beginning.
        int hundredNanos = (timestampNanos + 50) / 100;
        this.nanos = 100 * (hundredNanos % HUNDRED_NANOS_PER_SECOND);
        this.utcMillis = timestamp.getTime() - timestamp.getNanos() / 1000000
                + 1000 * (hundredNanos / HUNDRED_NANOS_PER_SECOND);

        // Postconditions
        assert this.minutesOffset >= MINUTES_OFFSET_MIN && this.minutesOffset <= MINUTES_OFFSET_MAX : "minutesOffset: "
                + this.minutesOffset;
        assert this.nanos >= NANOS_MIN && this.nanos <= NANOS_MAX : "nanos: " + this.nanos;
        assert 0 == this.nanos % 100 : "nanos: " + this.nanos;
        assert 0 == this.utcMillis % 1000L : "utcMillis: " + this.utcMillis;
    }

    /**
     * Converts a java.sql.Timestamp value with an integer offset to the equivalent DateTimeOffset value
     *
     * @param timestamp
     *        A java.sql.Timestamp value
     * @param minutesOffset
     *        An integer offset in minutes
     * @return The DateTimeOffset value of the input timestamp and minutesOffset
     */
    static DateTimeOffset valueOf(java.sql.Timestamp timestamp, int minutesOffset) {
        return new DateTimeOffset(timestamp, minutesOffset);
    }

    /**
     * Converts a java.sql.Timestamp value with a Calendar value to the equivalent DateTimeOffset value
     *
     * @param timestamp
     *        A java.sql.Timestamp value
     * @param calendar
     *        A java.util.Calendar value
     * @return The DateTimeOffset value of the input timestamp and calendar
     */
    static DateTimeOffset valueOf(java.sql.Timestamp timestamp, Calendar calendar) {
        // (Re)Set the calendar's time to the value in the timestamp so that get(ZONE_OFFSET) and get(DST_OFFSET) report
        // the correct values for the time indicated, taking into account DST transition times and any historical
        // changes
        // to the DST transition schedule.
        calendar.setTimeInMillis(timestamp.getTime());

        return new DateTimeOffset(timestamp,
                (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000));
    }

    /**
     * The formatted value.
     */
    private String formattedValue = null;

    /**
     * Formats a datetimeoffset as yyyy-mm-dd hh:mm:ss[.fffffffff] [+|-]hh:mm, where yyyy-mm-dd hh:mm:ss[.fffffffff]
     * indicates a timestamp that is offset from UTC by the number of minutes indicated by [+|-]hh:mm.
     *
     * @return a String object in yyyy-mm-dd hh:mm:ss[.fffffffff] [+|-]hh:mm format
     */
    @Override
    public String toString() {
        // Because formatting the value as a string is computationally expensive (involving creation of a Calendar and
        // a TimeZone, String formatters, etc.), cache the formatted value the first time it is needed. This can be done
        // simply with the single-check idiom because the DateTimeOffset class is effectively immutable.
        String result = formattedValue;
        if (null == result) {
            // Format the offset as +hh:mm or -hh:mm. Zero offset is formatted as +00:00.
            String formattedOffset = (minutesOffset < 0) ?

                                                         String.format(Locale.US, "-%1$02d:%2$02d", -minutesOffset / 60,
                                                                 -minutesOffset % 60)
                                                         :

                                                         String.format(Locale.US, "+%1$02d:%2$02d", minutesOffset / 60,
                                                                 minutesOffset % 60);

            // Like java.sql.Date.toString() and java.sql.Timestamp.toString(), DateTimeOffset.toString() produces
            // a value that is not locale-sensitive. The date part of the returned string is a Gregorian date, even
            // if the VM default locale would otherwise indicate that a Buddhist calendar should be used.
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT" + formattedOffset), Locale.US);

            // Initialize the calendar with the UTC milliseconds value represented by this DateTimeOffset object
            calendar.setTimeInMillis(utcMillis);

            // Assumption: nanos is in a valid range for printing as a 0-prefixed, 7-digit decimal number
            // The DateTimeOffset constructor ensures that this is the case.
            assert nanos >= NANOS_MIN && nanos <= NANOS_MAX;

            // Format the returned string value from the calendar's component fields and the UTC offset
            formattedValue = result = (0 == nanos) ?

                                                   String.format(Locale.US, "%1$tF %1$tT %2$s", calendar,
                                                           formattedOffset)
                                                   :

                                                   String.format(Locale.US, "%1$tF %1$tT.%2$s %3$s", calendar, // Example
                                                                                                               // (nanos
                                                                                                               // =
                                                                                                               // 123456000):
                                                           java.math.BigDecimal.valueOf(nanos, 9) // -> 0.123456000
                                                                   .stripTrailingZeros() // -> 0.123456
                                                                   .toPlainString() // -> "0.123456"
                                                                   .substring(2), // -> "123456"
                                                           formattedOffset);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        // Fast check for reference equality
        if (this == o)
            return true;

        // Check other object's type (and implicitly test for null)
        if (!(o instanceof DateTimeOffset))
            return false;

        DateTimeOffset other = (DateTimeOffset) o;
        return utcMillis == other.utcMillis && nanos == other.nanos && minutesOffset == other.minutesOffset;
    }

    @Override
    public int hashCode() {

        // Start by approximately folding the date and time components together.
        // Ignore any sub-second component of the utcMillis, which is always 0.
        // Milliseconds are kept in the nanos field.
        assert 0 == utcMillis % 1000L;
        long seconds = utcMillis / 1000L;

        int result = 571;
        result = 2011 * result + (int) seconds;
        result = 3217 * result + (int) (seconds / 60 * 60 * 24 * 365);

        // Fold in nanoseconds/microseconds/milliseconds
        result = 3919 * result + nanos / 100000;
        result = 4463 * result + nanos / 1000;
        result = 5227 * result + nanos;

        // Fold in the hour and minute portions of the time zone offset
        // Typically the minutes are 0, so the hours have more impact on the hash
        result = 6689 * result + minutesOffset;
        result = 7577 * result + minutesOffset / 60;

        // The low order bits of the result should at this point be very
        // sensitive to differences in any of the DateTimeOffset fields,
        // even for small bucket sizes.
        return result;
    }

    /**
     * Returns this DateTimeOffset object's timestamp value.
     * <p>
     * The returned value represents an instant in time as the number of milliseconds since January 1, 1970, 00:00:00
     * GMT.
     *
     * @return this DateTimeOffset object's timestamp component
     */
    public java.sql.Timestamp getTimestamp() {
        java.sql.Timestamp timestamp = new java.sql.Timestamp(utcMillis);
        timestamp.setNanos(nanos);
        return timestamp;
    }

    /**
     * Returns OffsetDateTime equivalent to this DateTimeOffset object.
     *
     * @return OffsetDateTime equivalent to this DateTimeOffset object.
     */
    public java.time.OffsetDateTime getOffsetDateTime() {
        java.time.ZoneOffset zoneOffset = java.time.ZoneOffset.ofTotalSeconds(60 * minutesOffset);
        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofEpochSecond(utcMillis / 1000, nanos,
                zoneOffset);
        return java.time.OffsetDateTime.of(localDateTime, zoneOffset);
    }

    /**
     * Returns this DateTimeOffset object's offset value.
     *
     * @return this DateTimeOffset object's minutes offset from GMT
     */
    public int getMinutesOffset() {
        return minutesOffset;
    }

    /**
     * Compares this DateTimeOffset object with another DateTimeOffset object to determine their relative order.
     * <p>
     * The ordering is based on the timestamp component only. The offset component is not compared. Two DateTimeOffset
     * objects are considered equivalent with respect to ordering as long as they represent the same moment in time,
     * regardless of the location of the event. This is how SQL Server orders DATETIMEOFFSET values.
     *
     * @return a negative integer, zero, or a positive integer as this DateTimeOffset is less than, equal to, or greater
     *         than the specified DateTimeOffset.
     */
    public int compareTo(DateTimeOffset other) {
        // Note that no explicit check for null==other is necessary. The contract for compareTo()
        // says that a NullPointerException is to be thrown if null is passed as an argument.

        // The fact that nanos are non-negative guarantees the subtraction at the end
        // cannot produce a signed value outside the range representable in an int.
        assert nanos >= 0;
        assert other.nanos >= 0;

        return (utcMillis > other.utcMillis) ? 1 : (utcMillis < other.utcMillis) ? -1 : nanos - other.nanos;
    }

    /**
     * Serialization class for datetimeoffset.
     *
     */
    private static class SerializationProxy implements java.io.Serializable {
        private final long utcMillis;
        private final int nanos;
        private final int minutesOffset;

        SerializationProxy(DateTimeOffset dateTimeOffset) {
            this.utcMillis = dateTimeOffset.utcMillis;
            this.nanos = dateTimeOffset.nanos;
            this.minutesOffset = dateTimeOffset.minutesOffset;
        }

        private static final long serialVersionUID = 664661379547314226L;

        private Object readResolve() {
            java.sql.Timestamp timestamp = new java.sql.Timestamp(utcMillis);
            timestamp.setNanos(nanos);
            return new DateTimeOffset(timestamp, minutesOffset);
        }
    }

    /**
     * Get a serialization proxy.
     *
     * @return Object SerializationProxy
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
}
