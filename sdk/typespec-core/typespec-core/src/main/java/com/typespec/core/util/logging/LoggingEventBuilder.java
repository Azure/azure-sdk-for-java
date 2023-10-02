// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.logging;

import com.typespec.core.annotation.Fluent;
import com.typespec.core.util.CoreUtils;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.typespec.core.implementation.logging.LoggingUtils.doesArgsHaveThrowable;
import static com.typespec.core.implementation.logging.LoggingUtils.removeThrowable;

/**
 * This class provides fluent API to write logs using {@link ClientLogger} and
 * enrich them with additional context.
 *
 * <p><strong>Code samples</strong></p>
 *
 * <p>Logging event with context.</p>
 *
 * <!-- src_embed com.azure.core.util.logging.loggingeventbuilder -->
 * <pre>
 * logger.atInfo&#40;&#41;
 *     .addKeyValue&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
 *     .addKeyValue&#40;&quot;key2&quot;, true&#41;
 *     .addKeyValue&#40;&quot;key3&quot;, &#40;&#41; -&gt; getName&#40;&#41;&#41;
 *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
 * </pre>
 * <!-- end com.azure.core.util.logging.loggingeventbuilder -->
 */
@Fluent
public final class LoggingEventBuilder {
    private static final JsonStringEncoder JSON_STRING_ENCODER = JsonStringEncoder.getInstance();
    private static final LoggingEventBuilder NOOP = new LoggingEventBuilder(null, null, null, false);
    private static final String AZURE_SDK_LOG_MESSAGE_KEY = "az.sdk.message";

    private final Logger logger;
    private final LogLevel level;
    private List<ContextKeyValuePair> context;
    private final String globalContextCached;
    private final boolean hasGlobalContext;

    // flag for no-op instance instead of inheritance
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
        this.context = Collections.emptyList();
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
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atInfo -->
     * <pre>
     * logger.atInfo&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.atInfo -->
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
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#object -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     &#47;&#47; equivalent to addKeyValue&#40;&quot;key&quot;, &#40;&#41; -&gt; new LoggableObject&#40;&quot;string representation&quot;&#41;.toString&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, new LoggableObject&#40;&quot;string representation&quot;&#41;&#41;
     *     .log&#40;&quot;Param 1: &#123;&#125;, Param 2: &#123;&#125;, Param 3: &#123;&#125;&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#object -->
     *
     * @param key String key.
     * @param value Object value.
     * @return The updated {@code LoggingEventBuilder} object.
     */
    public LoggingEventBuilder addKeyValue(String key, Object value) {
        if (this.isEnabled) {
            addKeyValueInternal(key, value == null ? null : value.toString());
        }

        return this;
    }

    /**
     * Adds key with boolean value to the context of current log being created.
     *
     * @param key String key.
     * @param value boolean value.
     * @return The updated {@code LoggingEventBuilder} object.
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
     * <p>Adding an integer value to logging event context.</p>
     *
     * <!-- src_embed com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&#40;&#41; -&gt; String.format&#40;&quot;Param 1: %s, Param 2: %s, Param 3: %s&quot;, &quot;param1&quot;, &quot;param2&quot;, &quot;param3&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @param key String key.
     * @param value long value.
     * @return The updated {@code LoggingEventBuilder} object.
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
            if (this.context.isEmpty()) {
                this.context = new ArrayList<>();
            }

            this.context.add(new ContextKeyValuePair(key, valueSupplier));
        }
        return this;
    }

    /**
     * Logs message annotated with context.
     *
     * @param message the message to log.
     */
    public void log(String message) {
        if (this.isEnabled) {
            performLogging(level, message);
        }
    }

    /**
     * Logs message annotated with context.
     *
     * @param messageSupplier string message supplier.
     */
    public void log(Supplier<String> messageSupplier) {
        if (this.isEnabled) {
            String message = messageSupplier != null ? messageSupplier.get() : null;
            performLogging(level, message);
        }
    }

    /**
     * Logs message annotated with context.
     *
     * @param messageSupplier string message supplier.
     * @param throwable {@link Throwable} for the message.
     */
    public void log(Supplier<String> messageSupplier, Throwable throwable) {
        if (this.isEnabled) {
            String message = messageSupplier != null ? messageSupplier.get() : null;
            performLogging(level, message, throwable);
        }
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code warning} log level.
     *
     * @param format The format-able message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the {@link
     * Throwable}.
     */
    public void log(String format, Object... args) {
        if (this.isEnabled) {
            performLogging(level, format, args);
        }
    }

    /**
     * Logs the {@link Throwable} and returns it to be thrown.
     *
     * @param throwable Throwable to be logged and returned.
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
     * Logs the {@link RuntimeException} and returns it to be thrown.
     * This API covers the cases where a checked exception type needs to be thrown and logged.
     *
     * @param runtimeException RuntimeException to be logged and returned.
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
            // marker for Azure SDK logs so we'll write it even if there is no message
            .append(AZURE_SDK_LOG_MESSAGE_KEY)
            .append("\":\"");
        JSON_STRING_ENCODER.quoteAsString(message, sb);
        sb.append("\"");

        if (throwable != null) {
            sb.append(",\"exception\":");

            String exceptionMessage = throwable.getMessage();
            if (exceptionMessage != null) {
                sb.append("\"");
                JSON_STRING_ENCODER.quoteAsString(exceptionMessage, sb);
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

    /*
     * Performs the logging.
     *
     * @param format format-able message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
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
        for (Map.Entry<String, Object> pair : context.entrySet()) {
            writeKeyAndValue(pair.getKey(), pair.getValue(), formatter).append(",");
        }

        // remove trailing comma just in case
        return formatter.deleteCharAt(formatter.length() - 1)
            .toString();
    }

    private static StringBuilder writeKeyAndValue(String key, Object value, StringBuilder formatter) {
        formatter.append("\"");
        JSON_STRING_ENCODER.quoteAsString(key, formatter);
        formatter.append("\":");

        if (value == null) {
            return formatter.append("null");
        }

        if (isPrimitive(value)) {
            JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
            return formatter;
        }

        formatter.append("\"");
        JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
        return formatter.append("\"");
    }

    /**
     *  Returns true if the value is an instance of a primitive type and false otherwise.
     */
    private static boolean isPrimitive(Object value) {
        // most of the time values are strings
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
         * Writes "key":"value" json string to provided StringBuilder.
         */
        public StringBuilder write(StringBuilder formatter) {
            if (valueSupplier == null) {
                return writeKeyAndValue(key, value, formatter);
            }

            return writeKeyAndValue(key, valueSupplier.get(), formatter);
        }
    }
}
