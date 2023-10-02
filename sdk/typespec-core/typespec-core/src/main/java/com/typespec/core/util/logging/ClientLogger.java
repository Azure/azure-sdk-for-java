// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.logging;

import com.typespec.core.implementation.logging.DefaultLogger;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.CoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.typespec.core.implementation.logging.LoggingUtils.doesArgsHaveThrowable;
import static com.typespec.core.implementation.logging.LoggingUtils.removeNewLinesFromLogMessage;
import static com.typespec.core.implementation.logging.LoggingUtils.removeThrowable;

/**
 * This is a fluent logger helper class that wraps a pluggable {@link Logger}.
 *
 * <p>This logger logs format-able messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for {@link ClientLogger#verbose(String,
 * Object...) verbose}, the stack trace for the throwable is logged.</p>
 *
 * <p>A minimum logging level threshold is determined by the
 * {@link Configuration#PROPERTY_AZURE_LOG_LEVEL AZURE_LOG_LEVEL} environment configuration. By default logging is
 * <b>disabled</b>.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * <li>{@link ClientLogger#error(String, Object...) Error}</li>
 * <li>{@link ClientLogger#warning(String, Object...) Warning}</li>
 * <li>{@link ClientLogger#info(String, Object...) Info}</li>
 * <li>{@link ClientLogger#verbose(String, Object...) Verbose}</li>
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
     * <pre>
     * Map&lt;String, Object&gt; context = new HashMap&lt;&gt;&#40;&#41;;
     * context.put&#40;&quot;connectionId&quot;, &quot;95a47cf&quot;&#41;;
     *
     * ClientLogger loggerWithContext = new ClientLogger&#40;ClientLoggerJavaDocCodeSnippets.class, context&#41;;
     * loggerWithContext.info&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger#globalcontext -->
     *
     * @param className Class name creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     *                Objects are serialized with {@code toString()} method.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(String className, Map<String, Object> context) {
        Logger initLogger = LoggerFactory.getLogger(className);
        logger = initLogger instanceof NOPLogger ? new DefaultLogger(className) : initLogger;
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
     * <pre>
     * logger.log&#40;LogLevel.VERBOSE,
     *     &#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
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
     * <pre>
     * Throwable illegalArgumentException = new IllegalArgumentException&#40;&quot;An invalid argument was encountered.&quot;&#41;;
     * logger.log&#40;LogLevel.VERBOSE,
     *     &#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;,
     *     illegalArgumentException&#41;;
     * </pre>
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
     * Logs a message at {@code verbose} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at verbose log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.verbose -->
     * <pre>
     * logger.verbose&#40;&quot;A log message&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.verbose -->
     *
     * @param message The message to log.
     */
    public void verbose(String message) {
        if (logger.isDebugEnabled()) {
            if (hasGlobalContext) {
                atVerbose().log(message);
            } else {
                logger.debug(removeNewLinesFromLogMessage(message));
            }
        }
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code verbose} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at verbose log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.verbose#string-object -->
     * <pre>
     * logger.verbose&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.verbose#string-object -->
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the {@link
     * Throwable}.
     */
    public void verbose(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            performLogging(LogLevel.VERBOSE, false, format, args);
        }
    }

    /**
     * Logs a message at {@code info} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at verbose log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.info -->
     * <pre>
     * logger.info&#40;&quot;A log message&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.info -->
     *
     * @param message The message to log.
     */
    public void info(String message) {
        if (logger.isInfoEnabled()) {
            if (hasGlobalContext) {
                atInfo().log(message);
            } else {
                logger.info(removeNewLinesFromLogMessage(message));
            }
        }
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code informational} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at informational log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.info#string-object -->
     * <pre>
     * logger.info&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.info#string-object -->
     *
     * @param format The format-able message to log
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the {@link
     * Throwable}.
     */
    public void info(String format, Object... args) {
        if (logger.isInfoEnabled()) {
            performLogging(LogLevel.INFORMATIONAL, false, format, args);
        }
    }

    /**
     * Logs a message at {@code warning} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at warning log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.warning -->
     * <pre>
     * Throwable detailedException = new IllegalArgumentException&#40;&quot;A exception with a detailed message&quot;&#41;;
     * logger.warning&#40;detailedException.getMessage&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.warning -->
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        if (logger.isWarnEnabled()) {
            if (hasGlobalContext) {
                atWarning().log(message);
            } else {
                logger.warn(removeNewLinesFromLogMessage(message));
            }
        }
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code warning} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at warning log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.warning#string-object -->
     * <pre>
     * Throwable exception = new IllegalArgumentException&#40;&quot;An invalid argument was encountered.&quot;&#41;;
     * logger.warning&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name, exception&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.warning#string-object -->
     *
     * @param format The format-able message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the {@link
     * Throwable}.
     */
    public void warning(String format, Object... args) {
        if (logger.isWarnEnabled()) {
            performLogging(LogLevel.WARNING, false, format, args);
        }
    }

    /**
     * Logs a message at {@code error} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at error log level.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.error -->
     * <pre>
     * try &#123;
     *     upload&#40;resource&#41;;
     * &#125; catch &#40;IOException ex&#41; &#123;
     *     logger.error&#40;ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.error -->
     *
     * @param message The message to log.
     */
    public void error(String message) {
        if (logger.isErrorEnabled()) {
            if (hasGlobalContext) {
                atError().log(message);
            } else {
                logger.error(removeNewLinesFromLogMessage(message));
            }
        }
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code error} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging an error with stack trace.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.error#string-object -->
     * <pre>
     * try &#123;
     *     upload&#40;resource&#41;;
     * &#125; catch &#40;IOException ex&#41; &#123;
     *     logger.error&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name, ex&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.error#string-object -->
     *
     * @param format The format-able message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the {@link
     * Throwable}.
     */
    public void error(String format, Object... args) {
        if (logger.isErrorEnabled()) {
            performLogging(LogLevel.ERROR, false, format, args);
        }
    }

    /**
     * Logs the {@link RuntimeException} at the warning level and returns it to be thrown.
     * <p>
     * This API covers the cases where a runtime exception type needs to be thrown and logged. If a {@link Throwable} is
     * being logged use {@link #logThrowableAsWarning(Throwable)} instead.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@link RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsWarning(RuntimeException runtimeException) {
        Objects.requireNonNull(runtimeException, "'runtimeException' cannot be null.");

        return logThrowableAsWarning(runtimeException);
    }

    /**
     * Logs the {@link Throwable} at the warning level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     * RuntimeException} is being logged use {@link #logExceptionAsWarning(RuntimeException)} instead.
     *
     * @param throwable Throwable to be logged and returned.
     * @param <T> Type of the Throwable being logged.
     * @return The passed {@link Throwable}.
     * @throws NullPointerException If {@code throwable} is {@code null}.
     * @deprecated Use {@link #logThrowableAsWarning(Throwable)} instead.
     */
    @Deprecated
    public <T extends Throwable> T logThowableAsWarning(T throwable) {
        Objects.requireNonNull(throwable, "'throwable' cannot be null.");
        if (!logger.isWarnEnabled()) {
            return throwable;
        }

        performLogging(LogLevel.WARNING, true, throwable.getMessage(), throwable);
        return throwable;
    }

    /**
     * Logs the {@link Throwable} at the warning level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     * RuntimeException} is being logged use {@link #logExceptionAsWarning(RuntimeException)} instead.
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
     * Logs the {@link RuntimeException} at the error level and returns it to be thrown.
     * <p>
     * This API covers the cases where a runtime exception type needs to be thrown and logged. If a {@link Throwable} is
     * being logged use {@link #logThrowableAsError(Throwable)} instead.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@code RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsError(RuntimeException runtimeException) {
        Objects.requireNonNull(runtimeException, "'runtimeException' cannot be null.");

        return logThrowableAsError(runtimeException);
    }

    /**
     * Logs the {@link Throwable} at the error level and returns it to be thrown.
     * <p>
     * This API covers the cases where a checked exception type needs to be thrown and logged. If a {@link
     * RuntimeException} is being logged use {@link #logExceptionAsError(RuntimeException)} instead.
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
        String throwableMessage = "";
        if (doesArgsHaveThrowable(args)) {
            // If we are logging an exception the format string is already the exception message, don't append it.
            if (!isExceptionLogging) {
                Object throwable = args[args.length - 1];

                // This is true from before but is needed to appease SpotBugs.
                if (throwable instanceof Throwable) {
                    throwableMessage = ((Throwable) throwable).getMessage();
                }
            }

            /*
             * Environment is logging at a level higher than verbose, strip out the throwable as it would log its
             * stack trace which is only expected when logging at a verbose level.
             */
            if (!logger.isDebugEnabled()) {
                args = removeThrowable(args);
            }
        }

        format = removeNewLinesFromLogMessage(format);

        switch (logLevel) {
            case VERBOSE:
                logger.debug(format, args);
                break;
            case INFORMATIONAL:
                logger.info(format, args);
                break;
            case WARNING:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    format += System.lineSeparator() + throwableMessage;
                }
                logger.warn(format, args);
                break;
            case ERROR:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    format += System.lineSeparator() + throwableMessage;
                }
                logger.error(format, args);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }
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

        String message = removeNewLinesFromLogMessage(messageSupplier.get());
        String throwableMessage = (throwable != null) ? throwable.getMessage() : "";

        switch (logLevel) {
            case VERBOSE:
                if (throwable != null) {
                    logger.debug(message, throwable);
                } else {
                    logger.debug(message);
                }
                break;
            case INFORMATIONAL:
                logger.info(message);
                break;
            case WARNING:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    message += System.lineSeparator() + throwableMessage;
                }
                logger.warn(message);
                break;
            case ERROR:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    message += System.lineSeparator() + throwableMessage;
                }
                logger.error(message);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }
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
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
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
     * <pre>
     * logger.atWarning&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name, exception&#41;;
     * </pre>
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
     * <pre>
     * logger.atInfo&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
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
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
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
     * <pre>
     * LogLevel level = response.getStatusCode&#40;&#41; == 200 ? LogLevel.INFORMATIONAL : LogLevel.WARNING;
     * logger.atLevel&#40;level&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;message&quot;&#41;;
     * </pre>
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
