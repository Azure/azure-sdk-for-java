// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

import com.azure.core.implementation.StringBuilderWriter;
import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

/**
 * This class is an internal implementation of slf4j logger.
 */
public final class DefaultLogger implements Logger {

    // The template for the log message:
    // YYYY-MM-DD HH:MM:ss.SSS [thread] [level] classpath - message
    // E.g: 2020-01-09 12:35:14.232 [main] [WARN] com.azure.core.DefaultLogger - This is my log message.
    private static final String WHITESPACE = " ";
    private static final String HYPHEN = " - ";
    private static final String OPEN_BRACKET = " [";
    private static final String CLOSE_BRACKET = "]";
    public static final String WARN = "WARN";
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String ERROR = "ERROR";
    public static final String TRACE = "TRACE";

    private final String classPath;
    private final boolean isTraceEnabled;
    private final boolean isDebugEnabled;
    private final boolean isInfoEnabled;
    private final boolean isWarnEnabled;
    private final boolean isErrorEnabled;
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
        this(getClassPathFromClassName(className), System.out, fromEnvironment());
    }

    /**
     * Construct DefaultLogger for the given class name.
     *
     * @param className Class name creating the logger. Will use class canonical name if exists, otherwise use the class
     * name passes in.
     * @param logLocation The location to log the messages.
     * @param logLevel The log level supported by the logger.
     */
    public DefaultLogger(String className, PrintStream logLocation, LogLevel logLevel) {
        this.classPath = getClassPathFromClassName(className);
        int configuredLogLevel = logLevel.getLogLevel();

        isTraceEnabled = LogLevel.VERBOSE.getLogLevel() > configuredLogLevel;
        isDebugEnabled = LogLevel.VERBOSE.getLogLevel() >= configuredLogLevel;
        isInfoEnabled = LogLevel.INFORMATIONAL.getLogLevel() >= configuredLogLevel;
        isWarnEnabled = LogLevel.WARNING.getLogLevel() >= configuredLogLevel;
        isErrorEnabled = LogLevel.ERROR.getLogLevel() >= configuredLogLevel;
        this.logLocation = logLocation;
    }

    private static String getClassPathFromClassName(String className) {
        try {
            return Class.forName(className).getCanonicalName();
        } catch (ClassNotFoundException | InvalidPathException e) {
            // Swallow ClassNotFoundException as the passed class name may not correlate to an actual class.
            // Swallow InvalidPathException as the className may contain characters that aren't legal file characters.
            return className;
        }
    }

    private static LogLevel fromEnvironment() {
        // LogLevel is so basic, we can't use configuration to read it (since Configuration needs to log too)
        String level = EnvironmentConfiguration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL);
        return LogLevel.fromString(level);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return classPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg) {
        logMessageWithFormat(TRACE, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object arg1) {
        logMessageWithFormat(TRACE, format, arg1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        logMessageWithFormat(TRACE, format, arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object... arguments) {
        logMessageWithFormat(TRACE, format, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg, final Throwable t) {
        log(TRACE, msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String s) {
        trace(s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        trace(s, o);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        trace(s, o, o1);
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        trace(s, objects);
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        trace(s, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public void debug(final String msg) {
        logMessageWithFormat(DEBUG, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object arg) {
        logMessageWithFormat(DEBUG, format, arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        logMessageWithFormat(DEBUG, format, arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object... args) {
        logMessageWithFormat(DEBUG, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg, final Throwable t) {
        log(DEBUG, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String s) {
        debug(s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        debug(s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        debug(s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        debug(s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        debug(s, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg) {
        logMessageWithFormat(INFO, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String format, Object arg) {
        logMessageWithFormat(INFO, format, arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        logMessageWithFormat(INFO, format, arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String format, Object... args) {
        logMessageWithFormat(INFO, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg, final Throwable t) {
        log(INFO, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String s) {
        info(s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        info(s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        info(s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        info(s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        info(s, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return isWarnEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg) {
        logMessageWithFormat(WARN, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String format, Object arg) {
        logMessageWithFormat(WARN, format, arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        logMessageWithFormat(WARN, format, arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String format, Object... args) {
        logMessageWithFormat(WARN, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg, final Throwable t) {
        log(WARN, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String s) {
        warn(s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        warn(s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        warn(s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        warn(s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        warn(s, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return isErrorEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String format, Object arg) {
        logMessageWithFormat(ERROR, format, arg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg) {
        logMessageWithFormat(ERROR, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        logMessageWithFormat(ERROR, format, arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String format, Object... args) {
        logMessageWithFormat(ERROR, format, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg, final Throwable t) {
        log(ERROR, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String s) {
        error(s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        error(s, o);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        error(s, o, o1);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        error(s, objects);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        error(s, throwable);
    }

    /**
     * Format and write the message according to the {@code MESSAGE_TEMPLATE}.
     *
     * @param levelName The level to log.
     * @param format The log message format.
     * @param arguments a list of arbitrary arguments taken in by format.
     */
    private void logMessageWithFormat(String levelName, String format, Object... arguments) {
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        log(levelName, tp.getMessage(), tp.getThrowable());
    }

    /**
     * Format and write the message according to the {@code MESSAGE_TEMPLATE}.
     *
     * @param levelName log level
     * @param message The message itself
     * @param t The exception whose stack trace should be logged
     */
    private void log(String levelName, String message, Throwable t) {
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
            .append(levelName)
            .append(CLOSE_BRACKET)
            .append(WHITESPACE)
            .append(classPath)
            .append(HYPHEN)
            .append(message)
            .append(System.lineSeparator());

        writeWithThrowable(stringBuilder, t);
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

    /**
     * Write the log message with throwable stack trace if any.
     *
     * @param stringBuilder Take the log messages.
     * @param t The exception whose stack trace should be logged
     */
    void writeWithThrowable(StringBuilder stringBuilder, Throwable t) {
        if (t != null) {
            StringBuilderWriter sw = new StringBuilderWriter(stringBuilder);
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
            }
        }
        logLocation.print(stringBuilder.toString());
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
}
