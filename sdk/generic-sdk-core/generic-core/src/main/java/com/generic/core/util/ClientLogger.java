// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util;

import com.generic.core.annotation.Metadata;
import com.generic.core.implementation.util.CoreUtils;
import com.generic.core.implementation.util.DefaultLogger;
import com.generic.core.util.configuration.Configuration;
import com.generic.json.implementation.jackson.core.io.JsonStringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
 * @see Configuration
 */
public class ClientLogger {
    private final Logger logger;
    private final String globalContextSerialized;
    private static final char CR = '\r';
    private static final char LF = '\n';

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
     * * <!-- src_embed com.generic.core.util.logging.clientlogger#globalcontext -->
     * <pre>
     * Map&lt;String, Object&gt; context = new HashMap&lt;&gt;&#40;&#41;;
     * context.put&#40;&quot;connectionId&quot;, &quot;95a47cf&quot;&#41;;
     *
     * ClientLogger loggerWithContext = new ClientLogger&#40;ClientLoggerJavaDocCodeSnippets.class, context&#41;;
     * loggerWithContext.info&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger#globalcontext -->
     *
     * @param className Class name creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     * Objects are serialized with {@code toString()} method.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(String className, Map<String, Object> context) {
        Logger initLogger = LoggerFactory.getLogger(className);
        logger = initLogger instanceof NOPLogger ? new DefaultLogger(className) : initLogger;
        globalContextSerialized = LoggingEventBuilder.writeJsonFragment(context);
    }

    ClientLogger(Logger logger, Map<String, Object> context) {
        this.logger = logger;
        this.globalContextSerialized = LoggingEventBuilder.writeJsonFragment(context);
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
            LoggingEventBuilder.create(logger, LogLevel.WARNING, globalContextSerialized, true)
                .log(throwable.getMessage(), throwable);
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
        if (logger.isErrorEnabled()) {
            LoggingEventBuilder.create(logger, LogLevel.ERROR, globalContextSerialized, true)
                .log(throwable.getMessage(), throwable);
        }
        return throwable;
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
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
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
     * <!-- src_embed com.generic.core.util.logging.clientlogger.atWarning -->
     * <pre>
     * logger.atWarning&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;A structured log message with exception.&quot;, exception&#41;;
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
     *     .addKeyValue&#40;&quot;hello&quot;, name&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
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
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
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
     * @return instance of {@link LoggingEventBuilder} or no-op if logging at provided level is disabled.
     */
    public LoggingEventBuilder atLevel(LogLevel level) {
        return LoggingEventBuilder.create(logger, level, globalContextSerialized,
            canLogAtLevel(level));
    }

    /**
     * This class provides fluent API to write logs using {@link ClientLogger} and
     * enrich them with additional context.
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
     *     .addKeyValue&#40;&quot;key3&quot;, this::getName&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.logging.loggingeventbuilder -->
     */
    @Metadata(conditions = FLUENT)
    public static final class LoggingEventBuilder {
        private static final JsonStringEncoder JSON_STRING_ENCODER = JsonStringEncoder.getInstance();
        private static final LoggingEventBuilder NOOP = new LoggingEventBuilder(null, null, null, false);
        private static final String SDK_LOG_MESSAGE_KEY = "{\"message\":\"";

        private final Logger logger;
        private final LogLevel level;
        private List<ContextKeyValuePair> context;
        private final String globalContextCached;
        private final boolean hasGlobalContext;
        // Flag for no-op instance instead of inheritance
        private final boolean isEnabled;

        /**
         * Creates {@code LoggingEventBuilder} for provided level and  {@link ClientLogger}.
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
            this.globalContextCached = globalContextSerialized == null ? "" : globalContextSerialized;
            this.hasGlobalContext = !this.globalContextCached.isEmpty();
        }

        /**
         * Adds key with String value pair to the context of current log being created.
         *
         * <p><strong>Code samples</strong></p>
         *
         * <p>Adding string value to logging event context.</p>
         *
         * <!-- src_embed com.generic.core.util.logging.clientlogger.atInfo -->
         * <pre>
         * logger.atInfo&#40;&#41;
         *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
         *     .addKeyValue&#40;&quot;hello&quot;, name&#41;
         *     .log&#40;&quot;A structured log message.&quot;&#41;;
         * </pre>
         * <!-- end com.generic.core.util.logging.clientlogger.atInfo -->
         *
         * @param key String key.
         * @param value String value.
         * @return The updated {@code LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, String value) {
            if (this.isEnabled) {
                addKeyValueInternal(key, value);
            }

            return this;
        }

        /**
         * Adds key with Object value to the context of current log being created.
         * If logging is enabled at given level, and object is not null, uses {@code value.toString()} to
         * serialize object.
         *
         * <p><strong>Code samples</strong></p>
         *
         * <p>Adding string value to logging event context.</p>
         *
         * <!-- src_embed com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#object -->
         * <pre>
         * logger.atVerbose&#40;&#41;
         *     &#47;&#47; equivalent to addKeyValue&#40;&quot;key&quot;, &#40;&#41; -&gt; new LoggableObject&#40;&quot;string representation&quot;&#41;.toString&#40;&#41;
         *     .addKeyValue&#40;&quot;key&quot;, new LoggableObject&#40;&quot;string representation&quot;&#41;&#41;
         *     .log&#40;&quot;A structured log message.&quot;&#41;;
         * </pre>
         * <!-- end com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#object -->
         *
         * @param key String key.
         * @param value Object value.
         * @return The updated {@code LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, Object value) {
            if (this.isEnabled) {
                // Previously this eagerly called toString() on the value, but that can be expensive and unnecessary.
                // This is now deferred until the value is being logged, which was calling toString() anyway.
                addKeyValueInternal(key, value);
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
         * Adds key with long value to the context of current log event being created.
         *
         * <p><strong>Code samples</strong></p>
         *
         * <p>Adding a long value to the logging event context.</p>
         *
         * <!-- src_embed com.generic.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
         * <pre>
         * logger.atVerbose&#40;&#41;
         *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
         *     .log&#40;&quot;A structured log message.&quot;&#41;;
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
         * Adds key with String value supplier to the context of current log event being created.
         *
         * @param key String key.
         * @param valueSupplier String value supplier function.
         * @return The updated {@code LoggingEventBuilder} object.
         */
        public LoggingEventBuilder addKeyValue(String key, Supplier<String> valueSupplier) {
            if (this.isEnabled) {
                if (this.context == null) {
                    this.context = new ArrayList<>();
                }

                this.context.add(new ContextKeyValuePair(key, valueSupplier));
            }
            return this;
        }

        /**
         * Logs message annotated with context.
         *
         * @param message log message.
         */
        public void log(String message) {
            if (this.isEnabled) {
                message = removeNewLinesFromLogMessage(message);
                if (isEmptyMessage(message)) {
                    return;
                }

                performLogging(level, getMessageWithContext(message), (Throwable) null);
            }
        }

        /**
         * Logs message annotated with context.
         *
         * @param message log message.
         * @param throwable {@link Throwable} for the message.
         * @param <T> Type of the Throwable being logged.
         *
         * @return The passed {@link Throwable}.
         */
        public <T extends Throwable> T log(String message, T throwable) {
            if (this.isEnabled) {
                message = removeNewLinesFromLogMessage(message);
                if (throwable != null) {
                    addKeyValueInternal("exception.message", throwable.getMessage());
                    if (logger instanceof DefaultLogger && logger.isDebugEnabled()) {
                        addKeyValue("exception.stacktrace", getStackTrace(throwable));
                    }
                }
                String messageWithContext = getMessageWithContext(message);
                performLogging(level, messageWithContext, logger.isDebugEnabled() ? throwable : null);
            }
            return throwable;
        }

        private String getStackTrace(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return sw.toString().trim();
        }

        private boolean isEmptyMessage(String message) {
            return CoreUtils.isNullOrEmpty(message)
                && CoreUtils.isNullOrEmpty(context)
                && !hasGlobalContext;
        }

        private String getMessageWithContext(String message) {
            if (message == null) {
                message = "";
            }

            StringBuilder sb = new StringBuilder(20 + (context == null ? 0 : context.size()) * 20 + message.length()
                + globalContextCached.length());
            // message must be first for log parsing tooling to work, key also works as a
            // marker for SDK logs so we'll write it even if there is no message
            sb.append(SDK_LOG_MESSAGE_KEY);
            JSON_STRING_ENCODER.quoteAsString(message, sb);
            sb.append('"');

            if (hasGlobalContext) {
                sb.append(',').append(globalContextCached);
            }

            if (context != null) {
                for (ContextKeyValuePair contextKeyValuePair : context) {
                    contextKeyValuePair.write(sb.append(','));
                }
            }

            sb.append('}');
            return sb.toString();
        }

        private void addKeyValueInternal(String key, Object value) {
            if (this.context == null) {
                this.context = new ArrayList<>();
            }

            this.context.add(new ContextKeyValuePair(key, value));
        }

        private void performLogging(LogLevel logLevel, String message, Throwable throwable) {
            switch (logLevel) {
                case VERBOSE:
                    logger.debug(message, throwable);
                    break;
                case INFORMATIONAL:
                    logger.info(message, throwable);
                    break;
                case WARNING:
                    logger.warn(message, throwable);
                    break;
                case ERROR:
                    logger.error(message, throwable);
                    break;
                default:
                    // Don't do anything, this state shouldn't be possible.
                    break;
            }
        }

        /**
         * Serializes passed map to string containing valid JSON fragment:
         * e.g. "k1":"v1","k2":"v2", properly escaped and without trailing comma.
         * <p>
         * For complex object serialization, it calls {@code toString()} guarded with null check.
         *
         * @param context to serialize.
         *
         * @return Serialized JSON fragment or an empty string.
         */
        static String writeJsonFragment(Map<String, Object> context) {
            if (CoreUtils.isNullOrEmpty(context)) {
                return "";
            }

            StringBuilder formatter = new StringBuilder(context.size() * 20);

            // Keep track of whether we've written a value yet so we don't write a trailing comma.
            // The previous implementation would delete the trailing comma, but internally this causes StringBuilder to
            // copy the entirety of the string to a new buffer, which is very expensive.
            boolean firstValueWritten = false;
            for (Map.Entry<String, Object> pair : context.entrySet()) {
                if (firstValueWritten) {
                    formatter.append(',');
                } else {
                    firstValueWritten = true;
                }

                writeKeyAndValue(pair.getKey(), pair.getValue(), formatter);
            }

            return formatter.toString();
        }

        private static void writeKeyAndValue(String key, Object value, StringBuilder formatter) {
            formatter.append('"');
            JSON_STRING_ENCODER.quoteAsString(key, formatter);
            formatter.append("\":");

            if (value == null) {
                formatter.append("null");
            } else if (isUnquotedType(value)) {
                JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
            } else {
                formatter.append('"');
                JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
                formatter.append('"');
            }
        }

        /**
         *  Returns true if the value is an unquoted JSON type (boolean, number, null).
         */
        private static boolean isUnquotedType(Object value) {
            return value instanceof Boolean || value instanceof Number;
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
             * Writes "key":"value" json string to provided StringBuilder.
             */
            public void write(StringBuilder formatter) {
                if (valueSupplier == null) {
                    writeKeyAndValue(key, value, formatter);
                } else {
                    writeKeyAndValue(key, valueSupplier.get(), formatter);
                }
            }
        }
    }

    /**
     * Enum which represent logging levels used in Azure SDKs.
     */
    public enum LogLevel {
        /**
         * Indicates that no log level is set.
         */
        NOTSET(0, "0", "notSet"),

        /**
         * Indicates that log level is at verbose level.
         */
        VERBOSE(1, "1", "verbose", "debug"),

        /**
         * Indicates that log level is at information level.
         */
        INFORMATIONAL(2, "2", "info", "information", "informational"),

        /**
         * Indicates that log level is at warning level.
         */
        WARNING(3, "3", "warn", "warning"),

        /**
         * Indicates that log level is at error level.
         */
        ERROR(4, "4", "err", "error");
        private final int numericValue;
        private final String[] allowedLogLevelVariables;
        private static final HashMap<String, LogLevel> LOG_LEVEL_STRING_MAPPER = new HashMap<>();
        private final String caseSensitive;

        static {
            for (LogLevel logLevel: LogLevel.values()) {
                for (String val: logLevel.allowedLogLevelVariables) {
                    LOG_LEVEL_STRING_MAPPER.put(val, logLevel);
                }
            }
        }

        LogLevel(int numericValue, String... allowedLogLevelVariables) {
            this.numericValue = numericValue;
            this.allowedLogLevelVariables = allowedLogLevelVariables;
            this.caseSensitive = allowedLogLevelVariables[0];
        }

        /**
         * Converts the log level into a numeric representation used for comparisons.
         *
         * @return The numeric representation of the log level.
         */
        private int getLevelCode() {
            return numericValue;
        }

        /**
         * Compares the passed log level with the configured log level and returns true if the passed log level is greater
         * @param level The log level to compare.
         * @param configuredLevel The configured log level.
         * @return True if the passed log level is greater or equal to the configured log level, false otherwise.
         */
        public static boolean isGreaterOrEqual(LogLevel level, LogLevel configuredLevel) {
            return level.getLevelCode() >= configuredLevel.getLevelCode();
        }

        /**
         * Converts the passed log level string to the corresponding {@link LogLevel}.
         *
         * @param logLevelVal The log level value which needs to convert
         * @return The LogLevel Enum if pass in the valid string.
         * The valid strings for {@link LogLevel} are:
         * <ul>
         * <li>VERBOSE: "verbose", "debug"</li>
         * <li>INFO: "info", "information", "informational"</li>
         * <li>WARNING: "warn", "warning"</li>
         * <li>ERROR: "err", "error"</li>
         * </ul>
         * Returns NOT_SET if null is passed in.
         * @throws IllegalArgumentException if the log level value is invalid.
         */
        public static LogLevel fromString(String logLevelVal) {
            if (logLevelVal == null) {
                return LogLevel.NOTSET;
            }
            String caseInsensitiveLogLevel = logLevelVal.toLowerCase(Locale.ROOT);
            if (!LOG_LEVEL_STRING_MAPPER.containsKey(caseInsensitiveLogLevel)) {
                throw new IllegalArgumentException("We currently do not support the log level you set. LogLevel: "
                    + logLevelVal);
            }
            return LOG_LEVEL_STRING_MAPPER.get(caseInsensitiveLogLevel);
        }

        /**
         * Converts the log level to a string representation.
         *
         * @return The string representation of the log level.
         */
        public String toString() {
            return caseSensitive;
        }
    }

    /**
     * Removes CR, LF or CRLF pattern in the {@code logMessage}.
     *
     * @param logMessage The log message to sanitize.
     * @return The updated logMessage.
     */
    private static String removeNewLinesFromLogMessage(String logMessage) {
        if (CoreUtils.isNullOrEmpty(logMessage)) {
            return logMessage;
        }

        StringBuilder sb = null;
        int prevStart = 0;

        for (int i = 0; i < logMessage.length(); i++) {
            if (logMessage.charAt(i) == CR || logMessage.charAt(i) == LF) {
                if (sb == null) {
                    sb = new StringBuilder(logMessage.length());
                }

                if (prevStart != i) {
                    sb.append(logMessage, prevStart, i);
                }
                prevStart = i + 1;
            }
        }

        if (sb == null) {
            return logMessage;
        }
        sb.append(logMessage, prevStart, logMessage.length());
        return sb.toString();
    }
}
