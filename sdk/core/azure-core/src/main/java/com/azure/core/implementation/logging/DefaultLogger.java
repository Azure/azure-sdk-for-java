// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

import com.azure.core.util.logging.LogLevel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * This class is an internal implementation of slf4j logger.
 */
public final class DefaultLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = -144261058636441630L;

    private String classPath;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // The template forms the log message in a format:
    // YYYY-MM-DD HH:MM [thread] [level] classpath - message
    // E.g: 2020-01-09 12:35 [main] [WARNING] com.azure.core.DefaultLogger - This is my log message.
    private static final String MESSAGE_TEMPLATE = "%s [%s] [%s] %s - %s";

    /**
     * Construct DefaultLogger for the given class.
     *
     * @param clazz Class creating the logger.
     */
    public DefaultLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Construct DefaultLogger for the given class name.
     *
     * @param className Class name creating the logger.
     * @throws RuntimeException it is an error.
     */
    public DefaultLogger(String className) {
        try {
            this.classPath = Class.forName(className).getCanonicalName();
        } catch (ClassNotFoundException e) {
            this.classPath = className;
        }
    }

    /**
     * {@inheritDoc}
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    @Override
    public void debug(String format, Object... args) {
        logFromFormat(LogLevel.VERBOSE, format, args);
    }

    /**
     * {@inheritDoc}
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    @Override
    public void info(String format, Object... args) {
        logFromFormat(LogLevel.INFORMATIONAL, format, args);
    }

    /**
     * {@inheritDoc}
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    @Override
    public void warn(String format, Object... args) {
        logFromFormat(LogLevel.WARNING, format, args);
    }

    /**
     * {@inheritDoc}
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    @Override
    public void error(String format, Object... args) {
        logFromFormat(LogLevel.ERROR, format, args);
    }

    /**
     * Format and write the message according to the {@code MESSAGE_TEMPLATE}.
     *
     * @param level The level to log.
     * @param format The log message format.
     * @param arguments a list of arbitrary arguments taken in by format.
     */
    private void logFromFormat(LogLevel level, String format, Object... arguments) {
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * Format and write the message according to the {@code MESSAGE_TEMPLATE}.
     *
     * @param level log level
     * @param message The message itself
     * @param t The exception whose stack trace should be logged
     */
    private void log(LogLevel level, String message, Throwable t) {
        String dateTime = getFormattedDate();
        String threadName = Thread.currentThread().getName();
        String levelName = level.name();
        StringBuilder buf = new StringBuilder(32);
        buf.append(String.format(MESSAGE_TEMPLATE, dateTime, threadName, levelName, classPath, message));
        writeWithThrowable(buf, t);
    }

    /**
     * Get the current time in Local time zone.
     *
     * @return The current time in {@code DATE_FORMAT}
     */
    private String getFormattedDate() {
        LocalDateTime now = LocalDateTime.now();
        return DATE_FORMAT.format(now);
    }

    /**
     * Write the log message with throwable stack trace if any.
     *
     * @param buf Take the log messages.
     * @param t The exception whose stack trace should be logged
     */
    void writeWithThrowable(StringBuilder buf, Throwable t) {
        if (t != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }
        System.out.println(buf.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String format, final Object... arguments) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(final String msg, final Throwable t) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(final String msg, final Throwable t) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(final String msg, final Throwable t) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(final String msg, final Throwable t) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(final String msg, final Throwable t) {
        throw new UnsupportedOperationException();
    }
}
