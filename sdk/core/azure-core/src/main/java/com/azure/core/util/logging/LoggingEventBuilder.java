// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.azure.core.implementation.logging.LoggingUtils.doesArgsHaveThrowable;
import static com.azure.core.implementation.logging.LoggingUtils.removeThrowable;

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
    private static final LoggingEventBuilder NOOP = new LoggingEventBuilder(null, null, false);
    private static final String AZURE_SDK_LOG_MESSAGE_KEY = "az.sdk.message";

    private final Logger logger;
    private final LogLevel level;
    private List<ContextKeyValuePair> context;

    // use flag instead for no-op instance instead of inheritance
    private final boolean isEnabled;

    /**
     * Creates {@code LoggingEventBuilder} for provided level and  {@link ClientLogger}.
     * If level is disabled, returns no-op instance.
     */
    static LoggingEventBuilder create(Logger logger, LogLevel level, boolean canLogAtLevel) {
        if (canLogAtLevel) {
            return new LoggingEventBuilder(logger, level, true);
        }

        return NOOP;
    }

    private LoggingEventBuilder(Logger logger, LogLevel level, boolean isEnabled) {
        this.logger = logger;
        this.level = level;
        this.isEnabled = isEnabled;
        this.context = null;
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

    private String getMessageWithContext(String message, Throwable throwable) {
        if (message == null) {
            message = "";
        }

        int contextSize = context == null ? 0 : context.size();

        StringBuilder sb = new StringBuilder(20 + contextSize * 20 + message.length());
        sb.append("{\"")
            // message must be first for log parsing tooling to work, key also works as a
            // marker for Azure SDK logs so we'll write it even if there is no message
            .append(AZURE_SDK_LOG_MESSAGE_KEY)
            .append("\":\"");
        JSON_STRING_ENCODER.quoteAsString(message, sb);
        sb.append("\"");

        if (throwable != null) {
            sb.append(",\"exception\":\"");
            JSON_STRING_ENCODER.quoteAsString(throwable.getMessage(), sb);
            sb.append("\"");
        }

        if (context != null) {
            for (int i = 0; i < context.size(); i++) {
                context.get(i)
                    .writeKeyAndValue(sb.append(","));
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private void addKeyValueInternal(String key, Object value) {
        if (this.context == null) {
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
    void performLogging(LogLevel logLevel, String format, Object... args) {

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
     * Key value pair with basic serialization capabilities.
     */
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
        public StringBuilder writeKeyAndValue(StringBuilder formatter) {
            formatter.append("\"");
            JSON_STRING_ENCODER.quoteAsString(key, formatter);
            formatter.append("\":");

            String valueStr = null;
            if (value != null) {
                // LoggingEventBuilder only supports primitives and strings
                if (!(value instanceof String)) {
                    JSON_STRING_ENCODER.quoteAsString(value.toString(), formatter);
                    return formatter;
                }

                valueStr = (String) value;
            } else if (valueSupplier != null) {
                valueStr = valueSupplier.get();
            }

            if (valueStr == null) {
                return formatter.append("null");
            }

            formatter.append("\"");
            JSON_STRING_ENCODER.quoteAsString(valueStr, formatter);

            return formatter.append("\"");
        }
    }
}
