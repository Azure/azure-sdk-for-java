// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.concurrent.Callable;

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

    private long days;
    private long hours;
    private long minutes;
    private long seconds;
    private long milliseconds;

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
    public TimeSpan withDays(final long days) {
        this.days = days;
        return this;
    }

    /**
     * Specifies the number of hours.
     *
     * @param hours number of hours
     * @return TimeSpan
     */
    public TimeSpan withHours(final long hours) {
        this.hours = hours;
        return this;
    }

    /**
     * Specifies the number of minutes.
     *
     * @param minutes number of minutes
     * @return TimeSpan
     */
    public TimeSpan withMinutes(final long minutes) {
        this.minutes = minutes;
        return this;
    }

    /**
     * Specifies the number of seconds.
     *
     * @param seconds number of seconds
     * @return TimeSpan
     */
    public TimeSpan withSeconds(final long seconds) {
        this.seconds = seconds;
        return this;
    }

    /**
     * Specifies the number of milliseconds.
     *
     * @param milliseconds number of milliseconds
     * @return TimeSpan
     */
    public TimeSpan withMilliseconds(final long milliseconds) {
        this.milliseconds = milliseconds;
        return this;
    }

    /**
     * @return days value
     */
    public long days() {
        return this.days;
    }

    /**
     * @return hours value
     */
    public long hours() {
        return this.hours;
    }

    /**
     * @return minutes value
     */
    public long minutes() {
        return this.minutes;
    }

    /**
     * @return seconds value
     */
    public long seconds() {
        return this.seconds;
    }

    /**
     * @return milliseconds value
     */
    public long milliseconds() {
        return this.milliseconds;
    }

    /**
     * @return total number of milliseconds represented by this instance
     */
    public double totalMilliseconds() {
        return totalTicks() * millisecondsPerTick;
    }

    /**
     * @return the duration represented by this instance
     */
    public Duration toDuration() {
        Double millis = Double.valueOf(totalMilliseconds());
        return Duration.ofMillis(millis.longValue());
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
     * Gets TimeSpan from given duration.
     *
     * @param duration duration
     * @return TimeSpan
     */
    public static TimeSpan fromDuration(Duration duration) {
        long totalTicks = duration.toMillis() * ticksPerMillisecond;
        long days = Math.abs(totalTicks / ticksPerDay);
        totalTicks = Math.abs(totalTicks % ticksPerDay);
        long hours = totalTicks / ticksPerHour % 24;
        long minutes = totalTicks / ticksPerMinute % 60;
        long seconds = totalTicks / ticksPerSecond % 60;
        long milliseconds = duration.toMillis() - (days * 3600 * 24 + hours * 3600 + minutes * 60 + seconds) * 1000;

        return new TimeSpan().withDays(days)
            .withHours(hours)
            .withMinutes(minutes)
            .withSeconds(seconds)
            .withMilliseconds(milliseconds);
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
            throw new ClientLogger(TimeSpan.class)
                .logExceptionAsError(new IllegalArgumentException("input cannot be null"));
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
            TokenParser.throwOutOfRange();
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
            TokenParser.throwOutOfRange();
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
            TokenParser.throwOutOfRange();
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
            TokenParser.throwOutOfRange();
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
        long days = Math.abs(totalTicks / ticksPerDay);
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
        long hours = remainingTicks / ticksPerHour % 24;
        long minutes = remainingTicks / ticksPerMinute % 60;
        long seconds = remainingTicks / ticksPerSecond % 60;
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
        long totalMilliSeconds = (days * 3600 * 24 + hours * 3600 + minutes * 60 + seconds) * 1000
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
    private Callable<Token> nextTokenProvider;
    private final ClientLogger logger = new ClientLogger(TokenParser.class);

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
        try {
            return nextTokenProvider.call();
        } catch (Exception e) {
            e.printStackTrace();
            throw logger.logExceptionAsError(new RuntimeException("TimeSpan::nextToken() failed."));
        }
    }

    static void throwParseError() {
        throw new ClientLogger(TokenParser.class)
            .logExceptionAsError(new IllegalArgumentException("String was not recognized as a valid TimeSpan"));
    }

    static void throwOutOfRange() {
        throw new ClientLogger(TokenParser.class)
            .logExceptionAsError(
                new IllegalArgumentException(
                    "The TimeSpan could not be parsed because at least one of the numeric "
                        + "components is out of range or contains too many digits"));
    }

    private Callable<Token> nextTokenProvider() {
        return new Callable<Token>() {
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
                if (val.isEmpty()) {
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

    private final ClientLogger logger = new ClientLogger(Token.class);

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
            throw logger.logExceptionAsError(
                new IllegalArgumentException("String was not recognized as a valid TimeSpan"));
        }
    }

    void throwIfEmpty() {
        if (isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("String was not recognized as a valid TimeSpan"));
        }
    }

    boolean isEmpty() {
        return this.rawValue == null && this.terminalChar == null;
    }
}
