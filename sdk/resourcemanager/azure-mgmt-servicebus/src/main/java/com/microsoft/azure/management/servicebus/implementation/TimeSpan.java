/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import org.joda.time.Period;
import rx.functions.Func0;

/**
 * Represents a time interval.
 */
public class TimeSpan {
    private static long ticksPerMillisecond = 10000;
    private static long ticksPerSecond = ticksPerMillisecond * 1000;
    private static long ticksPerMinute = ticksPerSecond * 60;
    private static long ticksPerHour = ticksPerMinute * 60;
    private static long ticksPerDay = ticksPerHour * 24;
    private static double millisecondsPerTick = 1.0 / ticksPerMillisecond;
    private static double secondsPerTick = 1.0 / ticksPerSecond;
    private static double minutesPerTick = 1.0 / ticksPerMinute;

    private int days;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;

    /**
     * Creates TimeSpan.
     */
    public TimeSpan() {
    }

    /**
     * Specifies the number of days.
     *
     * @param days number of days
     * @return TimeSpan
     */
    public TimeSpan withDays(final int days) {
        this.days = days;
        return this;
    }

    /**
     * Specifies the number of hours.
     *
     * @param hours number of hours
     * @return TimeSpan
     */
    public TimeSpan withHours(final int hours) {
        this.hours = hours;
        return this;
    }

    /**
     * Specifies the number of minutes.
     *
     * @param minutes number of minutes
     * @return TimeSpan
     */
    public TimeSpan withMinutes(final int minutes) {
        this.minutes = minutes;
        return this;
    }

    /**
     * Specifies the number of seconds.
     *
     * @param seconds number of seconds
     * @return TimeSpan
     */
    public TimeSpan withSeconds(final int seconds) {
        this.seconds = seconds;
        return this;
    }

    /**
     * Specifies the number of milliseconds.
     *
     * @param milliseconds number of milliseconds
     * @return TimeSpan
     */
    public TimeSpan withMilliseconds(final int milliseconds) {
        this.milliseconds = milliseconds;
        return this;
    }

    /**
     * @return days value
     */
    public int days() {
        return this.days;
    }

    /**
     * @return hours value
     */
    public int hours() {
        return this.hours;
    }

    /**
     * @return minutes value
     */
    public int minutes() {
        return this.minutes;
    }

    /**
     * @return seconds value
     */
    public int seconds() {
        return this.seconds;
    }

    /**
     * @return mill-seconds value
     */
    public int milliseconds() {
        return this.milliseconds;
    }

    /**
     * @return total number of milliseconds represented by this instance
     */
    public double totalMilliseconds() {
        return totalTicks() * millisecondsPerTick;
    }

    /**
     * @return total number of seconds represented by this instance
     */
    public double totalSeconds() {
        return totalTicks() * secondsPerTick;
    }

    /**
     * @return total number of minutes represented by this instance
     */
    public double totalMinutes() {
        return totalTicks() * minutesPerTick;
    }

    /**
     * Gets TimeSpan from given period.
     *
     * @param period duration in period format
     * @return TimeSpan
     */
    public static TimeSpan fromPeriod(Period period) {
        // Normalize (e.g. move weeks to hour part)
        //
        Period p = new Period(period.toStandardDuration().getMillis());
        return TimeSpan.parse((new TimeSpan()
                .withDays(p.getDays())
                .withHours(p.getHours())
                .withMinutes(p.getMinutes())
                .withSeconds(p.getSeconds())
                .withMilliseconds(p.getMillis())).toString());
    }

    /**
     * Parses the TimeSpan in string format.
     *
     * Valid formats for TimeSpan are:
     * [-]d\0
     * [-]d.hh:mm\0
     * [-]hh:mm\0
     * [-]dd.hh:mm:ss\0
     * [-]hh:mm:ss\0
     * [-]dd.hh:mm:ss:ffffffff\0
     * [-]hh:mm:ss:ffffffff\0
     *
     * @param input the string representation of TimeSpan
     * @return TimeSpan
     */
    public static TimeSpan parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        final String str = input.trim();
        TimeSpan timeSpan = new TimeSpan();
        TokenParser parser = new TokenParser(str, str.charAt(0) == '-' ? 1 : 0);
        Token token = parser.nextToken();
        // Empty string not allowed
        //
        token.throwIfEmpty();
        int sign = str.charAt(0) == '-' ? -1 : 1;
        if (token.isTerminalCharNull()) {
            // 'dd\0'
            //
            timeSpan.withDays(toInt(token.getRawValue()) * sign);
            return timeSpan;
        }
        if (token.isTerminalMatched('.')) {
            // 'dd.hh'
            //
            timeSpan.withDays(toInt(token.getRawValue()) * sign);
            token = parser.nextToken();
            // If '.' follows 'dd' then there must be 'hh' token
            //
            token.throwIfEmpty();
            timeSpan.withHours(toInt(token.getRawValue()) * sign);
        } else {
            // 'hh'
            //
            timeSpan.withDays(0);
            timeSpan.withHours(toInt(token.getRawValue()) * sign);
        }
        if (timeSpan.hours() > 23) {
            parser.throwOutOfRange();
        }
        // there must be ':' followed by 'dd.hh' or 'hh'
        //
        token.throwIfTerminalCharNotMatch(':');
        token = parser.nextToken();
        // there must be 'mm' token
        //
        token.throwIfEmpty();
        timeSpan.withMinutes(toInt(token.getRawValue()) * sign);
        if (timeSpan.minutes() > 59) {
            parser.throwOutOfRange();
        }
        if (token.isTerminalCharNull()) {
            // 'dd.hh:mm\0' or 'hh:mm\0'
            //
            return timeSpan;
        }
        token.throwIfTerminalCharNotMatch(':');
        token = parser.nextToken();
        // There must be 'ss' token
        //
        token.throwIfEmpty();
        timeSpan.withSeconds(toInt(token.getRawValue()) * sign);
        if (timeSpan.seconds() > 59) {
            parser.throwOutOfRange();
        }
        if (token.isTerminalCharNull()) {
            // 'dd.hh:mm:ss\0' or 'hh:mm:ss\0'
            //
            return timeSpan;
        }
        token.throwIfTerminalCharNotMatch('.');
        token = parser.nextToken();
        // There must be 'fraction' token
        //
        token.throwIfEmpty();
        String milliStr = "." + token.getRawValue();
        if (milliStr.length() > 8) {
            parser.throwOutOfRange();
        }
        int milliSeconds = (int) (Double.parseDouble(milliStr) * 1000);
        timeSpan.withMilliseconds(milliSeconds * sign);
        // There should not be more tokens
        //
        if (!token.isTerminalCharNull()) {
            TokenParser.throwParseError();
        }
        return timeSpan;
    }

    /**
     * @return TimeSpan in [-][d.]hh:mm:ss[.fffffff] format
     */
    public String toString() {
        long totalTicks = totalTicks();
        int days = (int) Math.abs(totalTicks / ticksPerDay);
        StringBuilder stringBuilder = new StringBuilder();
        // Sign part
        //
        if (totalTicks < 0) {
            stringBuilder.append("-");
        }
        // Days part
        //
        if (days != 0) {
            stringBuilder.append(String.format("%d.", days));
        }
        long remainingTicks = Math.abs(totalTicks % ticksPerDay);
        int hours = (int) (remainingTicks / ticksPerHour % 24);
        int minutes = (int) (remainingTicks / ticksPerMinute % 60);
        int seconds = (int) (remainingTicks / ticksPerSecond % 60);
        // Hour, Minute, Second part
        //
        stringBuilder.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        int fraction = (int) (remainingTicks % ticksPerSecond);
        // Fraction part
        //
        if (fraction != 0) {
            stringBuilder.append(String.format(".%07d", fraction));
        }
        return stringBuilder.toString();
    }

    /**
     * @return total number of ticks represented by this instance
     */
    private long totalTicks() {
        long totalMilliSeconds = ((long) days * 3600 * 24 + (long) hours * 3600 + (long) minutes * 60 + seconds) * 1000
                + milliseconds;
        return totalMilliSeconds * ticksPerMillisecond;
    }

    private static int toInt(String intStr) {
        return Integer.parseInt(intStr);
    }
}

/**
 * Parses the TimeSpan in string format.
 */
class TokenParser {
    private final String str;
    private final int startIndex;
    private Func0<Token> nextTokenProvider;

    TokenParser(final String str, final int startIndex) {
        this.str = str;
        this.startIndex = startIndex;
        this.nextTokenProvider = nextTokenProvider();
    }

    /**
     * Gets the next token from the string represented in TimeSpan format.
     * Each token is a number followed by single char. For the last token-character
     * will be null. This method return an empty token if there is no more token left
     * in the string.
     *
     * @return next token
     */
    Token nextToken() {
        return nextTokenProvider.call();
    }

    static void throwParseError() {
        throw new IllegalArgumentException("String was not recognized as a valid TimeSpan");
    }

    static void throwOutOfRange() {
        throw new IllegalArgumentException("The TimeSpan could not be parsed because at least one of the numeric components is out of range or contains too many digits");
    }

    private Func0<Token> nextTokenProvider() {
        return new Func0<Token>() {
            int currentIndex = startIndex;
            int length = str.length();

            @Override
            public Token call() {
                if (currentIndex >= length) {
                    return new Token(null, null);
                }
                StringBuilder builder = new StringBuilder();
                while (currentIndex < length
                        && Character.isDigit(str.charAt(currentIndex))) {
                    builder.append(str.charAt(currentIndex));
                    currentIndex++;
                }
                String val = builder.toString();
                if (val == null) {
                    throwParseError();
                }
                try {
                    Integer.parseInt(val);
                } catch (Exception ex) {
                    throwOutOfRange();
                }
                if (currentIndex < length) {
                    return new Token(val, str.charAt(currentIndex++));
                }
                return new Token(val, null);
            }
        };
    }
}

/**
 * Represents a token (part of) a TimeSpan represented in string format.
 */
class Token {
    private String rawValue;
    private Character terminalChar;

    Token(String rawValue, Character terminalChar) {
        this.rawValue = rawValue;
        this.terminalChar = terminalChar;
    }

    String getRawValue() {
        return this.rawValue;
    }

    boolean isTerminalMatched(Character charToMatch) {
        if (terminalChar == null && charToMatch == null) {
            return true;
        }
        return terminalChar != null && charToMatch != null && terminalChar.equals(charToMatch);
    }

    boolean isTerminalCharNull() {
        return isTerminalMatched(null);
    }

    void throwIfTerminalCharNotMatch(Character matchChar) {
        if (!isTerminalMatched(matchChar)) {
            throw new IllegalArgumentException("String was not recognized as a valid TimeSpan");
        }
    }

    void throwIfEmpty() {
        if (isEmpty()) {
            throw new IllegalArgumentException("String was not recognized as a valid TimeSpan");
        }
    }

    boolean isEmpty() {
        return this.rawValue == null && this.terminalChar == null;
    }
}
