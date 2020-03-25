/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.lang.time;


import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.Validate;

import java.util.ArrayList;

public class DurationFormatUtils {
    /**
     * <p>DurationFormatUtils instances should NOT be constructed in standard programming.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public DurationFormatUtils() {
        super();
    }

    /**
     * <p>Formats the time gap as a string.</p>
     *
     * <p>The format used is ISO 8601-like: {@code HH:mm:ss.SSS}.</p>
     *
     * @param durationMillis  the duration to format
     * @return the formatted duration, not null
     * @throws IllegalArgumentException if durationMillis is negative
     */
    public static String formatDurationHMS(final long durationMillis) {
        return formatDuration(durationMillis, "HH:mm:ss.SSS", true);
    }

    /**
     * <p>Formats the time gap as a string, using the specified format.
     * Padding the left hand side of numbers with zeroes is optional.</p>
     *
     * <p>This method formats durations using the days and lower fields of the
     * format pattern. Months and larger are not used.</p>
     *
     * @param durationMillis  the duration to format
     * @param format  the way in which to format the duration, not null
     * @param padWithZeros  whether to pad the left hand side of numbers with 0's
     * @return the formatted duration, not null
     * @throws IllegalArgumentException if durationMillis is negative
     */
    private static String formatDuration(final long durationMillis, final String format, final boolean padWithZeros) {
        Validate.inclusiveBetween(0, Long.MAX_VALUE, durationMillis, "durationMillis must not be negative");

        final Token[] tokens = lexx(format);

        long days         = 0;
        long hours        = 0;
        long minutes      = 0;
        long seconds      = 0;
        long milliseconds = durationMillis;

        if (Token.containsTokenWithValue(tokens, d) ) {
            days = milliseconds / DateUtils.MILLIS_PER_DAY;
            milliseconds = milliseconds - (days * DateUtils.MILLIS_PER_DAY);
        }
        if (Token.containsTokenWithValue(tokens, H) ) {
            hours = milliseconds / DateUtils.MILLIS_PER_HOUR;
            milliseconds = milliseconds - (hours * DateUtils.MILLIS_PER_HOUR);
        }
        if (Token.containsTokenWithValue(tokens, m) ) {
            minutes = milliseconds / DateUtils.MILLIS_PER_MINUTE;
            milliseconds = milliseconds - (minutes * DateUtils.MILLIS_PER_MINUTE);
        }
        if (Token.containsTokenWithValue(tokens, s) ) {
            seconds = milliseconds / DateUtils.MILLIS_PER_SECOND;
            milliseconds = milliseconds - (seconds * DateUtils.MILLIS_PER_SECOND);
        }

        return format(tokens, 0, 0, days, hours, minutes, seconds, milliseconds, padWithZeros);
    }

    /**
     * <p>The internal method to do the formatting.</p>
     *
     * @param tokens  the tokens
     * @param years  the number of years
     * @param months  the number of months
     * @param days  the number of days
     * @param hours  the number of hours
     * @param minutes  the number of minutes
     * @param seconds  the number of seconds
     * @param milliseconds  the number of millis
     * @param padWithZeros  whether to pad
     * @return the formatted string
     */
    private static String format(final Token[] tokens, final long years, final long months, final long days, final long hours, final long minutes, final long seconds,
            final long milliseconds, final boolean padWithZeros) {
        final StringBuilder buffer = new StringBuilder();
        boolean lastOutputSeconds = false;
        for (final Token token : tokens) {
            final Object value = token.getValue();
            final int count = token.getCount();
            if (value instanceof StringBuilder) {
                buffer.append(value.toString());
            } else {
                if (value.equals(y)) {
                    buffer.append(paddedValue(years, padWithZeros, count));
                    lastOutputSeconds = false;
                } else if (value.equals(M)) {
                    buffer.append(paddedValue(months, padWithZeros, count));
                    lastOutputSeconds = false;
                } else if (value.equals(d)) {
                    buffer.append(paddedValue(days, padWithZeros, count));
                    lastOutputSeconds = false;
                } else if (value.equals(H)) {
                    buffer.append(paddedValue(hours, padWithZeros, count));
                    lastOutputSeconds = false;
                } else if (value.equals(m)) {
                    buffer.append(paddedValue(minutes, padWithZeros, count));
                    lastOutputSeconds = false;
                } else if (value.equals(s)) {
                    buffer.append(paddedValue(seconds, padWithZeros, count));
                    lastOutputSeconds = true;
                } else if (value.equals(S)) {
                    if (lastOutputSeconds) {
                        // ensure at least 3 digits are displayed even if padding is not selected
                        final int width = padWithZeros ? Math.max(3, count) : 3;
                        buffer.append(paddedValue(milliseconds, true, width));
                    } else {
                        buffer.append(paddedValue(milliseconds, padWithZeros, count));
                    }
                    lastOutputSeconds = false;
                }
            }
        }
        return buffer.toString();
    }

    /**
     * <p>Converts a {@code long} to a {@code String} with optional
     * zero padding.</p>
     *
     * @param value the value to convert
     * @param padWithZeros whether to pad with zeroes
     * @param count the size to pad to (ignored if {@code padWithZeros} is false)
     * @return the string result
     */
    private static String paddedValue(final long value, final boolean padWithZeros, final int count) {
        final String longString = Long.toString(value);
        return padWithZeros ? StringUtils.leftPad(longString, count, '0') : longString;
    }

    private static final Object y = "y";
    private static final Object M = "M";
    private static final Object d = "d";
    private static final Object H = "H";
    private static final Object m = "m";
    private static final Object s = "s";
    private static final Object S = "S";

    /**
     * Parses a classic date format string into Tokens
     *
     * @param format  the format to parse, not null
     * @return array of Token[]
     */
    private static Token[] lexx(final String format) {
        final ArrayList<Token> list = new ArrayList<>(format.length());

        boolean inLiteral = false;
        // Although the buffer is stored in a Token, the Tokens are only
        // used internally, so cannot be accessed by other threads
        StringBuilder buffer = null;
        Token previous = null;
        for (int i = 0; i < format.length(); i++) {
            final char ch = format.charAt(i);
            if (inLiteral && ch != '\'') {
                buffer.append(ch); // buffer can't be null if inLiteral is true
                continue;
            }
            Object value = null;
            switch (ch) {
            // TODO: Need to handle escaping of '
            case '\'':
                if (inLiteral) {
                    buffer = null;
                    inLiteral = false;
                } else {
                    buffer = new StringBuilder();
                    list.add(new Token(buffer));
                    inLiteral = true;
                }
                break;
            case 'y':
                value = y;
                break;
            case 'M':
                value = M;
                break;
            case 'd':
                value = d;
                break;
            case 'H':
                value = H;
                break;
            case 'm':
                value = m;
                break;
            case 's':
                value = s;
                break;
            case 'S':
                value = S;
                break;
            default:
                if (buffer == null) {
                    buffer = new StringBuilder();
                    list.add(new Token(buffer));
                }
                buffer.append(ch);
            }

            if (value != null) {
                if (previous != null && previous.getValue().equals(value)) {
                    previous.increment();
                } else {
                    final Token token = new Token(value);
                    list.add(token);
                    previous = token;
                }
                buffer = null;
            }
        }
        if (inLiteral) { // i.e. we have not found the end of the literal
            throw new IllegalArgumentException("Unmatched quote in format: " + format);
        }
        return list.toArray(new Token[list.size()]);
    }

    /**
     * Element that is parsed from the format pattern.
     */
    private static class Token {

        /**
         * Helper method to determine if a set of tokens contain a value
         *
         * @param tokens set to look in
         * @param value to look for
         * @return boolean <code>true</code> if contained
         */
        static boolean containsTokenWithValue(final Token[] tokens, final Object value) {
            for (final Token token : tokens) {
                if (token.getValue() == value) {
                    return true;
                }
            }
            return false;
        }

        private final Object value;
        private int count;

        /**
         * Wraps a token around a value. A value would be something like a 'Y'.
         *
         * @param value to wrap
         */
        Token(final Object value) {
            this.value = value;
            this.count = 1;
        }

        void increment() {
            count++;
        }

        int getCount() {
            return count;
        }

        Object getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object obj2) {
            if (obj2 instanceof Token) {
                final Token tok2 = (Token) obj2;
                if (this.value.getClass() != tok2.value.getClass()) {
                    return false;
                }
                if (this.count != tok2.count) {
                    return false;
                }
                if (this.value instanceof StringBuilder) {
                    return this.value.toString().equals(tok2.value.toString());
                } else if (this.value instanceof Number) {
                    return this.value.equals(tok2.value);
                } else {
                    return this.value == tok2.value;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.value.hashCode();
        }

        @Override
        public String toString() {
            return StringUtils.repeat(this.value.toString(), this.count);
        }
    }

    private static class DateUtils {
        /**
         * Number of milliseconds in a standard second.
         */
        public static final long MILLIS_PER_SECOND = 1000;
        /**
         * Number of milliseconds in a standard minute.
         */
        public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
        /**
         * Number of milliseconds in a standard hour.
         */
        public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
        /**
         * Number of milliseconds in a standard day.
         */
        public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    }
}
