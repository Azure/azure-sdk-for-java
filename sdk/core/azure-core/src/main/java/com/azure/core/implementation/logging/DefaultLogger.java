// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * This class is an internal implementation of slf4j logger.
 */
public final class DefaultLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = -144261058636441630L;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // The template forms the log message in a format:
    // YYYY-MM-DD HH:MM [thread] [level] classpath - message
    // E.g: 2020-01-09 12:35 [main] [WARNING] com.azure.core.DefaultLogger - This is my log message.
    private static final String MESSAGE_TEMPLATE = "%s [%s] [%s] %s - %s%n";
    private static final String WHITESPACE = " ";
    private static final String HYPHEN = "-";
    private static final String OPEN_BRACKET = "[";
    private static final String CLOSE_BRACKET = "]";
    public static final String WARN = "WARN";
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String ERROR = "ERROR";
    public static final String TRACE = "TRACE";
    private static final int QUEUE_SIZE = 1024;

    private String classPath;
    private int configuredLogLevel;
    private PrintStream printStream;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<String> messages = new ArrayBlockingQueue<>(QUEUE_SIZE);

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
     * @param className Class name creating the logger. Will use class canonical name if exists, otherwise use the
     * class name passes in.
     */
    public DefaultLogger(String className) {
        try {
            this.classPath = Class.forName(className).getCanonicalName();
        } catch (ClassNotFoundException e) {
            this.classPath = className;
        }
        this.configuredLogLevel =
            LogLevel.fromString(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL))
                .getLogLevel();
//        executor.submit(() -> {
//            while (true) {
//                System.out.print(messages.take());
//            }
//        });
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
        return this.configuredLogLevel < LogLevel.VERBOSE.getLogLevel();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return LogLevel.VERBOSE.getLogLevel() >= this.configuredLogLevel;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return LogLevel.INFORMATIONAL.getLogLevel() >= this.configuredLogLevel;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return LogLevel.WARNING.getLogLevel() >= this.configuredLogLevel;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return LogLevel.ERROR.getLogLevel() >= this.configuredLogLevel;
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append(dateTime)
            .append(WHITESPACE)
            .append(OPEN_BRACKET)
            .append(threadName)
            .append(CLOSE_BRACKET)
            .append(WHITESPACE)
            .append(OPEN_BRACKET)
            .append(levelName)
            .append(CLOSE_BRACKET)
            .append(WHITESPACE)
            .append(classPath)
            .append(WHITESPACE)
            .append(HYPHEN)
            .append(WHITESPACE)
            .append(message)
            .append(System.lineSeparator());

        writeWithThrowable(stringBuilder, t);
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
     * @param stringBuilder Take the log messages.
     * @param t The exception whose stack trace should be logged
     */
    void writeWithThrowable(StringBuilder stringBuilder, Throwable t) {
        if (t != null) {
            stringBuilder.append(System.lineSeparator());
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
                stringBuilder.append(sw.toString());
            }
        }

        System.out.print(stringBuilder.toString());
//        System.out.flush();
//        try {
//            messages.put(stringBuilder.toString());
//        } catch (InterruptedException ex) {
//            throw new IllegalStateException(ex);
//        }
    }
}
