// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util;

import com.generic.core.annotation.Metadata;
import com.generic.core.implementation.util.CoreUtils;
import com.generic.core.util.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.generic.core.annotation.TypeConditions.FLUENT;

/**
 * This is a fluent logger helper class that wraps a pluggable {@link Logger}.
 *
 * <p>This logger logs format-able messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for the stack trace for the throwable is
 * logged.</p>
 *
 * <p>A minimum logging level threshold is determined by the
 * {@link Configuration#PROPERTY_LOG_LEVEL AZURE_LOG_LEVEL} environment configuration. By default logging is
 * <b>disabled</b>.</p>
 *
 * <p>The logger is capable of producing json-formatted messages enriched with key value pairs.
 * Context can be provided in the constructor and populated on every message or added per each log record.</p>
 *
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
     *
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
     * Objects are serialized with {@code toString()} method.
     */
    public ClientLogger(Class<?> clazz, Map<String, Object> context) {
        this(clazz.getName(), context);
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory} with
     * context that will be populated on all log records produced with this logger.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context.</p>
     *
     * <!-- src_embed com.generic.core.util.logging.clientlogger#globalcontext -->
     * <pre>
     * Map&lt;String, Object&gt; context = new HashMap&lt;&gt;&#40;&#41;;
     * context.put&#40;&quot;connectionId&quot;, &quot;95a47cf&quot;&#41;;
     *
     * ClientLogger loggerWithContext = new ClientLogger&#40;ClientLoggerJavaDocCodeSnippets.class, context&#41;;
     * loggerWithContext.atInfo&#40;&#41;.log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger#globalcontext -->
     *
     * @param className Class name creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     * Objects are serialized with {@code toString()} method.
     *
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.log -->
     * <pre>
     * logger.log&#40;ClientLogger.LogLevel.VERBOSE,
     *     &#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.log -->
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.log#throwable -->
     * <pre>
     * Throwable illegalArgumentException = new IllegalArgumentException&#40;&quot;An invalid argument was encountered.&quot;&#41;;
     * logger.log&#40;ClientLogger.LogLevel.VERBOSE,
     *     &#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;,
     *     illegalArgumentException&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.log#throwable -->
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
     * This API covers the cases where a checked exception type needs to be thrown and logged.
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
     * This API covers the cases where a checked exception type needs to be thrown and logged.
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @return instance of {@link LoggingEventBuilder}  or no-op if error logging is disabled.
     */
    public LoggingEventBuilder atError() {
        return LoggingEventBuilder.create(logger, LogLevel.ERROR, globalContextSerialized,
            canLogAtLevel(LogLevel.ERROR));
    }

    /**
     * Creates {@link LoggingEventBuilder} for {@code warning} log level that can be
     * used to enrich log with additional context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at warning level.</p>
     *
     * <!-- src_embed com.generic.core.util.logging.clientlogger.atWarning -->
     * <pre>
     * logger.atWarning&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name, exception&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.atWarning -->
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.atInfo -->
     * <pre>
     * logger.atInfo&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.atInfo -->
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.atLevel -->
     * <pre>
     * ClientLogger.LogLevel level = response.getStatusCode&#40;&#41; == 200
     *     ? ClientLogger.LogLevel.INFORMATIONAL : ClientLogger.LogLevel.WARNING;
     * logger.atLevel&#40;level&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;message&quot;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.atLevel -->
     *
     * @param level log level.
     *
     * @return instance of {@link LoggingEventBuilder} or no-op if logging at provided level is disabled.
     */
    public LoggingEventBuilder atLevel(LogLevel level) {
        return LoggingEventBuilder.create(logger, level, globalContextSerialized,
            canLogAtLevel(level));
    }

    /**
     * This class provides fluent API to write logs using {@link ClientLogger} and enrich them with additional context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging event with context.</p>
     *
     * <!-- src_embed com.generic.core.util.logging.loggingeventbuilder -->
     * <pre>
     * logger.atInfo&#40;&#41;
     *     .addKeyValue&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .addKeyValue&#40;&quot;key2&quot;, true&#41;
     *     .addKeyValue&#40;&quot;key3&quot;, &#40;&#41; -&gt; getName&#40;&#41;&#41;
     *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.loggingeventbuilder -->
     */
    @Metadata(conditions = FLUENT)
    public static final class LoggingEventBuilder {
        private static final LoggingEventBuilder NOOP = new LoggingEventBuilder(null, null, null, false);
        private static final String SDK_LOG_MESSAGE_KEY = "sdk.message";

        private final Logger logger;
        private final LogLevel level;
        private List<ContextKeyValuePair> context;
        private final String globalContextCached;
        private final boolean hasGlobalContext;
        // Flag for no-op instance instead of inheritance
        private final boolean isEnabled;

        /**
         * Creates {@link LoggingEventBuilder} for provided level and  {@link ClientLogger}.
         * If level is disabled, returns no-op instance.
         */
        static LoggingEventBuilder create(Logger logger, LogLevel level, String globalContextSerialized,
                                          boolean canLogAtLevel) {
            if (canLogAtLevel) {
                return new LoggingEventBuilder(logger, level, globalContextSerialized, true);
            }

            return NOOP;
        }

        private LoggingEventBuilder(Logger logger, LogLevel level, String globalContextSerialized, boolean isEnabled) {
            this.logger = logger;
            this.level = level;
            this.isEnabled = isEnabled;
            this.context = Collections.emptyList();
            this.globalContextCached = globalContextSerialized == null ? "" : globalContextSerialized;
            this.hasGlobalContext = !this.globalContextCached.isEmpty();
        }

        /**
         * Adds a key with a {@link String} value pair to the context of current log being created.
         *
         * <p><strong>Code samples</strong></p>
         *
         * <p>Adding a String value to the logging event context.</p>
         *
         * <!-- src_embed com.generic.core.util.logging.clientlogger.atInfo -->
         * <pre>
         * logger.atInfo&#40;&#41;
         *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
         *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
         * </pre>
         * <!-- end com.generic.core.util.logging.clientlogger.atInfo -->
         *
         * @param key Key to associate the provided {@code value} with.
         * @param value The {@link String} value.
         * @return The updated {@link LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, String value) {
            if (this.isEnabled) {
                addKeyValueInternal(key, value);
            }

            return this;
        }

        /**
         * Adds a key with an {@link Object} value to the context of current log being created. If logging is enabled at
         * the given level, and {@code value} is not {@code null}, it uses {@link Object#toString()} to serialize the
         * provided {@code value}.
         *
         * <p><strong>Code samples</strong></p>
         *
         * <p>Adding a String value to the logging event context.</p>
         *
         * <!-- src_embed com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#object -->
         * <pre>
         * logger.atVerbose&#40;&#41;
         *     &#47;&#47; equivalent to addKeyValue&#40;&quot;key&quot;, &#40;&#41; -&gt; new LoggableObject&#40;&quot;string representation&quot;&#41;.toString&#40;&#41;
         *     .addKeyValue&#40;&quot;key&quot;, new LoggableObject&#40;&quot;string representation&quot;&#41;&#41;
         *     .log&#40;&quot;Param 1: &#123;&#125;, Param 2: &#123;&#125;, Param 3: &#123;&#125;&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;;
         * </pre>
         * <!-- end com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#object -->
         *
         * @param key Key to associate the provided {@code value} with.
         * @param value The {@link Object} value.
         * @return The updated {@link LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, Object value) {
            if (this.isEnabled) {
                addKeyValueInternal(key, value == null ? null : value.toString());
            }

            return this;
        }

        /**
         * Adds a key with a boolean value to the context of the current log being created.
         *
         * @param key Key to associate the provided {@code value} with.
         * @param value The boolean value.
         * @return The updated {@link LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, boolean value) {
            if (this.isEnabled) {
                addKeyValueInternal(key, value);
            }

            return this;
        }

        /**
         * Adds a key with a long value to the context of current log event being created.
         *
         * <p><strong>Code samples</strong></p>
         *
         * <p>Adding a long value to the logging event context.</p>
         *
         * <!-- src_embed com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
         * <pre>
         * logger.atVerbose&#40;&#41;
         *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
         *     .log&#40;&#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
         * </pre>
         * <!-- end com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
         *
         * @param key Key to associate the provided {@code value} with.
         * @param value The long value.
         * @return The updated {@link LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, long value) {
            if (this.isEnabled) {
                addKeyValueInternal(key, value);
            }

            return this;
        }

        /**
         * Adds a key with a {@link String} value {@link Supplier} to the context of the current log event being
         * created.
         *
         * @param key Key to associate the provided {@code value} with.
         * @param valueSupplier The {@link String} value {@link Supplier} function.
         * @return The updated {@link LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, Supplier<String> valueSupplier) {
            if (this.isEnabled) {
                if (this.context.isEmpty()) {
                    this.context = new ArrayList<>();
                }

                this.context.add(new ContextKeyValuePair(key, valueSupplier));
            }

            return this;
        }

        /**
         * Logs a message annotated with context.
         *
         * @param message The message to log.
         */
        public void log(String message) {
            if (this.isEnabled) {
                performLogging(level, message);
            }
        }

        /**
         * Logs a message annotated with context.
         *
         * @param messageSupplier {@link String} message {@link Supplier}.
         */
        public void log(Supplier<String> messageSupplier) {
            if (this.isEnabled) {
                String message = messageSupplier != null ? messageSupplier.get() : null;
                performLogging(level, message);
            }
        }

        /**
         * Logs a message annotated with context.
         *
         * @param messageSupplier {@link String} message {@link Supplier}.
         * @param throwable {@link Throwable} for the message.
         */
        public void log(Supplier<String> messageSupplier, Throwable throwable) {
            if (this.isEnabled) {
                String message = messageSupplier != null ? messageSupplier.get() : null;
                performLogging(level, message, throwable);
            }
        }

        /**
         * Logs a formattable message that uses {@code {}} as the placeholder at the {@code warning} log level.
         *
         * @param format The formattable message to log.
         * @param args Arguments for the message. If an exception is being logged, the last argument should be the
         * {@link Throwable}.
         */
        public void log(String format, Object... args) {
            if (this.isEnabled) {
                performLogging(level, format, args);
            }
        }

        /**
         * Logs the {@link Throwable} and returns it to be thrown.
         *
         * @param throwable {@link Throwable} to be logged and returned.
         * @return The passed {@link Throwable}.
         * @throws NullPointerException If {@code throwable} is {@code null}.
         */
        public Throwable log(Throwable throwable) {
            Objects.requireNonNull(throwable, "'throwable' cannot be null.");

            if (this.isEnabled) {
                performLogging(level, null, throwable);
            }

            return throwable;
        }

        /**
         * Logs the {@link RuntimeException} and returns it to be thrown. This API covers the cases where a checked
         * exception type needs to be thrown and logged.
         *
         * @param runtimeException {@link RuntimeException} to be logged and returned.
         * @return The passed {@link RuntimeException}.
         * @throws NullPointerException If {@code runtimeException} is {@code null}.
         */
        public RuntimeException log(RuntimeException runtimeException) {
            Objects.requireNonNull(runtimeException, "'runtimeException' cannot be null.");

            if (this.isEnabled) {
                performLogging(level, null, runtimeException);
            }

            return runtimeException;
        }

        private String getMessageWithContext(String message, Throwable throwable) {
            if (message == null) {
                message = "";
            }

            StringBuilder sb = new StringBuilder(20 + context.size() * 20 + message.length()
                + globalContextCached.length());

            sb.append("{\"")
                // message must be first for log parsing tooling to work, key also works as a
                // marker for SDK logs, so we'll write it even if there is no message
                .append(SDK_LOG_MESSAGE_KEY)
                .append("\":\"");
            //JSON_STRING_ENCODER.quoteAsString(message, sb);
            sb.append("\"");

            if (throwable != null) {
                sb.append(",\"exception\":");

                String exceptionMessage = throwable.getMessage();

                if (exceptionMessage != null) {
                    sb.append("\"");
                    //JSON_STRING_ENCODER.quoteAsString(exceptionMessage, sb);
                    sb.append("\"");
                } else {
                    sb.append("null");
                }
            }

            if (hasGlobalContext) {
                sb.append(",").append(globalContextCached);
            }

            for (ContextKeyValuePair contextKeyValuePair : context) {
                contextKeyValuePair.write(sb.append(","));
            }

            sb.append("}");

            return sb.toString();
        }

        private void addKeyValueInternal(String key, Object value) {
            if (this.context.isEmpty()) {
                this.context = new ArrayList<>();
            }

            this.context.add(new ContextKeyValuePair(key, value));
        }

        /**
         * Performs the logging.
         *
         * @param format Formattable message.
         * @param args Arguments for the message, if an exception is being logged the last argument should be the
         * {@link Throwable}.
         */
        private void performLogging(LogLevel logLevel, String format, Object... args) {
            Throwable throwable = null;

            if (doesArgsHaveThrowable(args)) {
                Object throwableObj = args[args.length - 1];

                // This is true from before but is needed to appease SpotBugs.
                if (throwableObj instanceof Throwable) {
                    throwable = (Throwable) throwableObj;
                }

                /*
                 * Environment is logging at a level higher than verbose, strip out the throwable as it would log its
                 * stack trace which is only expected when logging at a verbose level.
                 */
                if (!logger.isDebugEnabled()) {
                    args = removeThrowable(args);
                }
            }

            FormattingTuple tuple = MessageFormatter.arrayFormat(format, args);
            String message = getMessageWithContext(tuple.getMessage(), throwable);

            switch (logLevel) {
                case VERBOSE:
                    logger.debug(message, tuple.getThrowable());
                    break;
                case INFORMATIONAL:
                    logger.info(message, tuple.getThrowable());
                    break;
                case WARNING:
                    logger.warn(message, tuple.getThrowable());
                    break;
                case ERROR:
                    logger.error(message, tuple.getThrowable());
                    break;
                default:
                    // Don't do anything, this state shouldn't be possible.
                    break;
            }
        }

        /**
         * Serializes passed map to string containing valid JSON fragment: e.g. "k1":"v1","k2":"v2", properly escaped
         * and without a trailing comma.
         *
         * <p>For complex object serialization, it calls {@code toString()} guarded with null check.</p>
         *
         * @param context Context to serialize.
         * @return Serialized JSON fragment or an empty string.
         */
        static String writeJsonFragment(Map<String, Object> context) {
            if (CoreUtils.isNullOrEmpty(context)) {
                return "";
            }

            StringBuilder formatter = new StringBuilder(context.size() * 20);
            for (Map.Entry<String, Object> pair : context.entrySet()) {
                writeKeyAndValue(pair.getKey(), pair.getValue(), formatter).append(",");
            }

            // Remove trailing comma just in case
            return formatter.deleteCharAt(formatter.length() - 1).toString();
        }

        private static StringBuilder writeKeyAndValue(String key, Object value, StringBuilder formatter) {
            formatter.append("\"");
            //JSON_STRING_ENCODER.quoteAsString(key, formatter);
            formatter.append("\":");

            if (value == null) {
                return formatter.append("null");
            }

            if (isPrimitive(value)) {
                //JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
                return formatter;
            }

            formatter.append("\"");
            //JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
            return formatter.append("\"");
        }

        /**
         * Returns true if the value is an instance of a primitive type and false otherwise.
         */
        private static boolean isPrimitive(Object value) {
            // Most of the time values are strings
            if (value instanceof String) {
                return false;
            }

            return value instanceof Boolean
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Byte
                || value instanceof Double
                || value instanceof Float;
        }

        private static final class ContextKeyValuePair {
            private final String key;
            private final Object value;
            private final Supplier<String> valueSupplier;

            ContextKeyValuePair(String key, Object value) {
                this.key = key;
                this.value = value;
                this.valueSupplier = null;
            }

            ContextKeyValuePair(String key, Supplier<String> valueSupplier) {
                this.key = key;
                this.value = null;
                this.valueSupplier = valueSupplier;
            }

            /**
             * Writes a "key":"value" JSON string to the provided StringBuilder.
             */
            public StringBuilder write(StringBuilder formatter) {
                if (valueSupplier == null) {
                    return writeKeyAndValue(key, value, formatter);
                }

                return writeKeyAndValue(key, valueSupplier.get(), formatter);
            }
        }

        /**
         * Determines if the arguments contain a throwable that should be logged, SLF4J logs a throwable if it is the
         * last element in the argument list.
         *
         * @param args The arguments passed to format the log message.
         * @return {@code true} if the last element is a {@link Throwable}, {@code false} otherwise.
         */
        private static boolean doesArgsHaveThrowable(Object... args) {
            if (args.length == 0) {
                return false;
            }

            return args[args.length - 1] instanceof Throwable;
        }

        /**
         * Removes the last element from the arguments as it is a {@link Throwable}.
         *
         * @param args The arguments passed to format the log message.
         * @return The arguments with the last element removed.
         */
        private static Object[] removeThrowable(Object... args) {
            return Arrays.copyOf(args, args.length - 1);
        }
    }

    /**
     * Enum which represent logging levels used in Core SDKs.
     */
    public enum LogLevel {
        /**
         * Indicates that the log level is at the verbose level.
         */
        VERBOSE(1, "1", "verbose", "debug"),

        /**
         * Indicates that the log level is at the information level.
         */
        INFORMATIONAL(2, "2", "info", "information", "informational"),

        /**
         * Indicates that the log level is at the warning level.
         */
        WARNING(3, "3", "warn", "warning"),

        /**
         * Indicates that the log level is at the error level.
         */
        ERROR(4, "4", "err", "error"),

        /**
         * Indicates that no log level is set.
         */
        NOT_SET(5, "5");

        private final int numericValue;
        private final String[] allowedLogLevelVariables;
        private static final HashMap<String, LogLevel> LOG_LEVEL_STRING_MAPPER = new HashMap<>();

        static {
            for (LogLevel logLevel : LogLevel.values()) {
                for (String val : logLevel.allowedLogLevelVariables) {
                    LOG_LEVEL_STRING_MAPPER.put(val, logLevel);
                }
            }
        }

        LogLevel(int numericValue, String... allowedLogLevelVariables) {
            this.numericValue = numericValue;
            this.allowedLogLevelVariables = allowedLogLevelVariables;
        }

        /**
         * Converts the log level into a numeric representation used for comparisons.
         *
         * @return The numeric representation of the log level.
         */
        public int getLogLevel() {
            return numericValue;
        }

        /**
         * Converts the passed log level string to the corresponding {@link LogLevel}.
         *
         * <p>The valid strings for {@link LogLevel} are:</p>
         *
         * <ul>
         *     <li>VERBOSE: "verbose", "debug"</li>
         *     <li>INFO: "info", "information", "informational"</li>
         *     <li>WARNING: "warn", "warning"</li>
         *     <li>ERROR: "err", "error"</li>
         * </ul>
         *
         * <p>Returns NOT_SET if {@code null} is passed in.</p>
         *
         * @param logLevelVal The log level value which needs to be converted.
         * @return The {@link LogLevel} enum if a valid string was provided.
         * @throws IllegalArgumentException If the log level value is invalid.
         */
        public static LogLevel fromString(String logLevelVal) {
            if (logLevelVal == null) {
                return LogLevel.NOT_SET;
            }

            String caseInsensitiveLogLevel = logLevelVal.toLowerCase(Locale.ROOT);

            if (!LOG_LEVEL_STRING_MAPPER.containsKey(caseInsensitiveLogLevel)) {
                throw new IllegalArgumentException("We currently do not support the log level you set. LogLevel: "
                    + logLevelVal);
            }

            return LOG_LEVEL_STRING_MAPPER.get(caseInsensitiveLogLevel);
        }
    }
}
