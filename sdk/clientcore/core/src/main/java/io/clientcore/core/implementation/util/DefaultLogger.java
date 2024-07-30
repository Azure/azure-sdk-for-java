// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.configuration.Configuration;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import static io.clientcore.core.util.ClientLogger.LogLevel;

/**
 * This class is an internal implementation of slf4j logger.
 */
public final class DefaultLogger {

    // The template for the log message:
    // YYYY-MM-DD HH:MM:ss.SSS [thread] [level] classpath - message
    // E.g: 2020-01-09 12:35:14.232 [main] [WARN] com.azure.core.DefaultLogger - This is my log message.
    private static final String WHITESPACE = " ";
    private static final String HYPHEN = " - ";
    private static final String OPEN_BRACKET = " [";
    private static final String CLOSE_BRACKET = "]";
    private static final String WARN = "WARN";
    private static final String DEBUG = "DEBUG";
    private static final String INFO = "INFO";
    private static final String ERROR = "ERROR";
    private static final String NOTSET = "NOTSET";

    private final String classPath;
    private final LogLevel level;
    private final PrintStream logLocation;

    /**
     * Construct DefaultLogger for the given class.
     *
     * @param clazz Class creating the logger.
     */
    public DefaultLogger(Class<?> clazz) {
        this(clazz.getCanonicalName(), System.out, fromEnvironment());
    }

    /**
     * Construct DefaultLogger for the given class name.
     *
     * @param className Class name creating the logger. Will use class canonical name if exists, otherwise use the class
     * name passes in.
     */
    public DefaultLogger(String className) {
        this(className, System.out, fromEnvironment());
    }

    /**
     * Construct DefaultLogger for the given class name.
     *
     * @param className Class name creating the logger. Will use class canonical name if exists, otherwise use the class
     * name passes in.
     * @param logLocation The location to log the messages.
     * @param logLevel The log level supported by the logger.
     */
    public DefaultLogger(String className, PrintStream logLocation, ClientLogger.LogLevel logLevel) {
        this.classPath = className;
        this.logLocation = logLocation;
        this.level = logLevel;
    }

    public boolean isEnabled(LogLevel level) {
        if (this.level == LogLevel.NOTSET) {
            return false;
        }

        return level.compareTo(this.level) >= 0;
    }

    /**
     * Format and write the message according to the default template.
     *
     * @param level log level
     * @param message The message itself
     */
    public void log(LogLevel level, String message, Throwable throwable) {
        if (!isEnabled(level)) {
            return;
        }

        String dateTime = getFormattedDate();
        String threadName = Thread.currentThread().getName();
        // Use a larger initial buffer for the StringBuilder as it defaults to 16 and non-empty information is expected
        // to be much larger than that. This will reduce the amount of resizing and copying needed to be done.
        StringBuilder stringBuilder = new StringBuilder(256);
        stringBuilder.append(dateTime)
            .append(OPEN_BRACKET)
            .append(threadName)
            .append(CLOSE_BRACKET)
            .append(OPEN_BRACKET)
            .append(getLevelName(level))
            .append(CLOSE_BRACKET)
            .append(WHITESPACE)
            .append(classPath)
            .append(HYPHEN)
            .append(message)
            .append(System.lineSeparator());

        appendThrowable(stringBuilder, throwable);
        logLocation.print(stringBuilder);
    }

    private static String getLevelName(LogLevel level) {
        switch (level) {
            case VERBOSE:
                return DEBUG;
            case INFORMATIONAL:
                return INFO;
            case WARNING:
                return WARN;
            case ERROR:
                return ERROR;
            default:
                // not possible
                return NOTSET;
        }
    }

    public static void appendThrowable(StringBuilder stringBuilder, Throwable t) {
        if (t != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
                stringBuilder.append(sw);
            }
        }
    }

    /**
     * Get the current time in Local time zone.
     *
     * @return The current time in {@code DATE_FORMAT}
     */
    private static String getFormattedDate() {
        LocalDateTime now = LocalDateTime.now();

        // yyyy-MM-dd HH:mm:ss.SSS
        // 23 characters that will be ASCII
        byte[] bytes = new byte[23];

        // yyyy-
        int year = now.getYear();
        int round = year / 1000;
        bytes[0] = (byte) ('0' + round);
        year = year - (1000 * round);
        round = year / 100;
        bytes[1] = (byte) ('0' + round);
        year = year - (100 * round);
        round = year / 10;
        bytes[2] = (byte) ('0' + round);
        bytes[3] = (byte) ('0' + (year - (10 * round)));
        bytes[4] = '-';

        // MM-
        zeroPad(now.getMonthValue(), bytes, 5);
        bytes[7] = '-';

        // dd
        zeroPad(now.getDayOfMonth(), bytes, 8);
        bytes[10] = ' ';

        // HH:
        zeroPad(now.getHour(), bytes, 11);
        bytes[13] = ':';

        // mm:
        zeroPad(now.getMinute(), bytes, 14);
        bytes[16] = ':';

        // ss.
        zeroPad(now.getSecond(), bytes, 17);
        bytes[19] = '.';

        // SSS
        int millis = now.get(ChronoField.MILLI_OF_SECOND);
        round = millis / 100;
        bytes[20] = (byte) ('0' + round);
        millis = millis - (100 * round);
        round = millis / 10;
        bytes[21] = (byte) ('0' + round);
        bytes[22] = (byte) ('0' + (millis - (10 * round)));

        // Use UTF-8 as it's more performant than ASCII in Java 8
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void zeroPad(int value, byte[] bytes, int index) {
        if (value < 10) {
            bytes[index++] = '0';
            bytes[index] = (byte) ('0' + value);
        } else {
            int high = value / 10;
            bytes[index++] = (byte) ('0' + high);
            bytes[index] = (byte) ('0' + (value - (10 * high)));
        }
    }

    private static LogLevel fromEnvironment() {
        // LogLevel is so basic, we can't use configuration to read it (since Configuration needs to log too)
        String level = EnvironmentConfiguration.getGlobalConfiguration().get(Configuration.PROPERTY_LOG_LEVEL);
        return LogLevel.fromString(level);
    }
}
