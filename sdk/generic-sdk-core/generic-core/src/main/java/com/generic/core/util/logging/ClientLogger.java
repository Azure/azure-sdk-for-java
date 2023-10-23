// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.logging;

import com.generic.core.util.CoreUtils;
import com.generic.core.util.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This is a fluent logger helper class that wraps a pluggable {@link Logger}.
 *
 * <p>This logger logs format-able messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for the stack trace for the throwable is logged.</p>
 *
 * <p>A minimum logging level threshold is determined by the
 * {@link Configuration#PROPERTY_LOG_LEVEL AZURE_LOG_LEVEL} environment configuration. By default logging is
 * <b>disabled</b>.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * </ol>
 *
 * <p>The logger is capable of producing json-formatted messages enriched with key value pairs.
 * Context can be provided in the constructor and populated on every message or added per each log record.</p>
 * @see Configuration
 */
public class ClientLogger {
    private final Logger logger;
    private final String globalContextSerialized;
    private final boolean hasGlobalContext;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     *
     * @param clazz Class creating the logger.
     */
    public ClientLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory}.
     *
     * @param className Class name creating the logger.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(String className) {
        this(className, Collections.emptyMap());
    }

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     *
     * @param clazz Class creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     *                Objects are serialized with {@code toString()} method.
     */
    public ClientLogger(Class<?> clazz, Map<String, Object> context) {
        this(clazz.getName(), context);
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory} with
     * context that will be populated on all log records produced with this logger.
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger#globalcontext -->
     * <!-- end com.azure.core.util.logging.clientlogger#globalcontext -->
     *
     * @param className Class name creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     *                Objects are serialized with {@code toString()} method.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(String className, Map<String, Object> context) {
        Logger initLogger = LoggerFactory.getLogger(className);
        logger = initLogger;
        globalContextSerialized = LoggingEventBuilder.writeJsonFragment(context);
        hasGlobalContext = !CoreUtils.isNullOrEmpty(globalContextSerialized);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at the given {@code logLevel}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with a specific log level</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.log -->
     * <!-- end com.azure.core.util.logging.clientlogger.log -->
     *
     * @param logLevel Logging level for the log message.
     * @param message The format-able message to log.
     */
    public void log(LogLevel logLevel, Supplier<String> message) {
        log(logLevel, message, null);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code verbose} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with a specific log level and exception</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.log#throwable -->
     * <!-- end com.azure.core.util.logging.clientlogger.log#throwable -->
     *
     * @param logLevel Logging level for the log message.
     * @param message The format-able message to log.
     * @param throwable Throwable for the message. {@link Throwable}.
     */
    public void log(LogLevel logLevel, Supplier<String> message, Throwable throwable) {
        if (message != null && canLogAtLevel(logLevel)) {
            performDeferredLogging(logLevel, message, throwable);
        }
    }

    /**
     * Logs the {@link Throwable} at the warning level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     *
     * @param throwable Throwable to be logged and returned.
     * @param <T> Type of the Throwable being logged.
     * @return The passed {@link Throwable}.
     * @throws NullPointerException If {@code throwable} is {@code null}.
     */
    public <T extends Throwable> T logThrowableAsWarning(T throwable) {
        Objects.requireNonNull(throwable, "'throwable' cannot be null.");
        if (logger.isWarnEnabled()) {
            performLogging(LogLevel.WARNING, true, throwable.getMessage(), throwable);
        }

        return throwable;
    }

    /**
     * Logs the {@link Throwable} at the error level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     *
     * @param throwable Throwable to be logged and returned.
     * @param <T> Type of the Throwable being logged.
     * @return The passed {@link Throwable}.
     * @throws NullPointerException If {@code throwable} is {@code null}.
     */
    public <T extends Throwable> T logThrowableAsError(T throwable) {
        Objects.requireNonNull(throwable, "'throwable' cannot be null.");
        if (!logger.isErrorEnabled()) {
            return throwable;
        }

        performLogging(LogLevel.ERROR, true, throwable.getMessage(), throwable);
        return throwable;
    }

    /*
     * Performs the logging. Call only if logging at this level is enabled.
     *
     * @param format format-able message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(LogLevel logLevel, boolean isExceptionLogging, String format, Object... args) {
        if (hasGlobalContext) {
            LoggingEventBuilder.create(logger, logLevel, globalContextSerialized, true)
                .log(format, args);
            return;
        }

        // If the logging level is less granular than verbose remove the potential throwable from the args.
        // String throwableMessage = "";
        // if (LoggingUtils.doesArgsHaveThrowable(args)) {
        //     // If we are logging an exception the format string is already the exception message, don't append it.
        //     if (!isExceptionLogging) {
        //         Object throwable = args[args.length - 1];
        //
        //         // This is true from before but is needed to appease SpotBugs.
        //         if (throwable instanceof Throwable) {
        //             throwableMessage = ((Throwable) throwable).getMessage();
        //         }
        //     }
        //
        //     /*
        //      * Environment is logging at a level higher than verbose, strip out the throwable as it would log its
        //      * stack trace which is only expected when logging at a verbose level.
        //      */
        //     if (!logger.isDebugEnabled()) {
        //         args = LoggingUtils.removeThrowable(args);
        //     }
        // }
        //
        // format = LoggingUtils.removeNewLinesFromLogMessage(format);
        //
        // switch (logLevel) {
        //     case VERBOSE:
        //         logger.debug(format, args);
        //         break;
        //     case INFORMATIONAL:
        //         logger.info(format, args);
        //         break;
        //     case WARNING:
        //         if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
        //             format += System.lineSeparator() + throwableMessage;
        //         }
        //         logger.warn(format, args);
        //         break;
        //     case ERROR:
        //         if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
        //             format += System.lineSeparator() + throwableMessage;
        //         }
        //         logger.error(format, args);
        //         break;
        //     default:
        //         // Don't do anything, this state shouldn't be possible.
        //         break;
        // }
    }

    /*
     * Performs deferred logging. Call only if logging at this level is enabled.
     *
     * @param logLevel sets the logging level
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performDeferredLogging(LogLevel logLevel, Supplier<String> messageSupplier, Throwable throwable) {

        if (hasGlobalContext) {
            // LoggingEventBuilder writes log messages as json and performs all necessary escaping, i.e. no
            // sanitization needed
            LoggingEventBuilder.create(logger, logLevel, globalContextSerialized, true)
                .log(messageSupplier, throwable);
            return;
        }

        // String message = LoggingUtils.removeNewLinesFromLogMessage(messageSupplier.get());
        // String throwableMessage = (throwable != null) ? throwable.getMessage() : "";
        //
        // switch (logLevel) {
        //     case VERBOSE:
        //         if (throwable != null) {
        //             logger.debug(message, throwable);
        //         } else {
        //             logger.debug(message);
        //         }
        //         break;
        //     case INFORMATIONAL:
        //         logger.info(message);
        //         break;
        //     case WARNING:
        //         if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
        //             message += System.lineSeparator() + throwableMessage;
        //         }
        //         logger.warn(message);
        //         break;
        //     case ERROR:
        //         if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
        //             message += System.lineSeparator() + throwableMessage;
        //         }
        //         logger.error(message);
        //         break;
        //     default:
        //         // Don't do anything, this state shouldn't be possible.
        //         break;
        // }
    }

    /*
     * @param args The arguments passed to evaluate suppliers in args.
     * @return Return the argument with evaluated supplier
     */
    Object[] evaluateSupplierArgument(Object[] args) {
        if (isSupplierLogging(args)) {
            args[0] = ((Supplier<?>) args[0]).get();
        }
        return args;
    }

    /*
     * @param args The arguments passed to determine supplier evaluation
     * @return Determines if it is supplier logging
     */
    boolean isSupplierLogging(Object[] args) {
        return (args.length == 1 && args[0] instanceof Supplier)
            || (args.length == 2 && args[0] instanceof Supplier && (args[1] instanceof Throwable || args[1] == null));
    }

    /**
     * Determines if the app or environment logger support logging at the given log level.
     *
     * @param logLevel Logging level for the log message.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    public boolean canLogAtLevel(LogLevel logLevel) {
        if (logLevel == null) {
            return false;
        }
        switch (logLevel) {
            case VERBOSE:
                return logger.isDebugEnabled();
            case INFORMATIONAL:
                return logger.isInfoEnabled();
            case WARNING:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            default:
                return false;
        }
    }

    /**
     * Creates {@link LoggingEventBuilder} for {@code error} log level that can be
     * used to enrich log with additional context.
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at error level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <!-- end com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @return instance of {@link LoggingEventBuilder}  or no-op if error logging is disabled.
     */
    public LoggingEventBuilder atError() {
        return LoggingEventBuilder.create(logger, LogLevel.ERROR, globalContextSerialized, canLogAtLevel(LogLevel.ERROR));
    }

    /**
     * Creates {@link LoggingEventBuilder} for {@code warning} log level that can be
     * used to enrich log with additional context.

     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at warning level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atWarning -->
     * <!-- end com.azure.core.util.logging.clientlogger.atWarning -->
     *
     * @return instance of {@link LoggingEventBuilder} or no-op if warn logging is disabled.
     */
    public LoggingEventBuilder atWarning() {
        return LoggingEventBuilder.create(logger, LogLevel.WARNING, globalContextSerialized,
            canLogAtLevel(LogLevel.WARNING));
    }

    /**
     * Creates {@link LoggingEventBuilder} for {@code info} log level that can be
     * used to enrich log with additional context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at info level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atInfo -->
     * <!-- end com.azure.core.util.logging.clientlogger.atInfo -->
     *
     * @return instance of {@link LoggingEventBuilder} or no-op if info logging is disabled.
     */
    public LoggingEventBuilder atInfo() {
        return LoggingEventBuilder.create(logger, LogLevel.INFORMATIONAL, globalContextSerialized,
            canLogAtLevel(LogLevel.INFORMATIONAL));
    }

    /**
     * Creates {@link LoggingEventBuilder} for {@code verbose} log level that can be
     * used to enrich log with additional context.
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at verbose level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <!-- end com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @return instance of {@link LoggingEventBuilder} or no-op if verbose logging is disabled.
     */
    public LoggingEventBuilder atVerbose() {
        return LoggingEventBuilder.create(logger, LogLevel.VERBOSE, globalContextSerialized,
            canLogAtLevel(LogLevel.VERBOSE));
    }

    /**
     * Creates {@link LoggingEventBuilder} for log level that can be
     * used to enrich log with additional context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at provided level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atLevel -->
     * <!-- end com.azure.core.util.logging.clientlogger.atLevel -->
     *
     * @param level log level.
     * @return instance of {@link LoggingEventBuilder} or no-op if logging at provided level is disabled.
     */
    public LoggingEventBuilder atLevel(LogLevel level) {
        return LoggingEventBuilder.create(logger, level, globalContextSerialized,
            canLogAtLevel(level));
    }
}
