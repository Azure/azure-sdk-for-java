// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class provides fluent API to write logs using {@link ClientLogger} and
 * enrich them with additional context.
 *
 * TODO code snippets
 */
@Fluent
public final class LoggingEventBuilder {
    private static final LoggingEventBuilder NOOP = new LoggingEventBuilder(null, null, false);
    private final ClientLogger logger;
    private final LogLevel level;
    private List<ContextKeyValuePair> context;

    // use flag instead for no-op instance instead of inheritance
    private final boolean isEnabled;

    /**
     * Creates {@code LoggingEventBuilder} for provided level and  {@link ClientLogger}.
     * If level is disabled, returns no-op instance.
     */
    static LoggingEventBuilder create(ClientLogger logger, LogLevel level) {
        if (logger.canLogAtLevel(level)) {
            return new LoggingEventBuilder(logger, level, true);
        }

        return NOOP;
    }

    private LoggingEventBuilder(ClientLogger logger, LogLevel level, boolean isEnabled) {
        this.logger = logger;
        this.level = level;
        this.isEnabled = isEnabled;
        this.context = null;
    }

    /**
     * Adds key with String value pair to the context of current log being created.
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
            logger.performLogging(level, false, getMessageWithContext(message));
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
            logger.performLogging(level, false, getMessageWithContext(message));
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
            logger.performLogging(level, false, getMessageWithContext(format), args);
        }
    }

    /**
     * Logs the {@link Throwable} and returns it to be thrown.
     *
     * @param throwable Throwable to be logged and returned.
     * @return The passed {@link Throwable}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public Throwable log(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' cannot be null.");

        if (this.isEnabled) {
            logger.performLogging(level, true, getMessageWithContext(throwable.getMessage()), throwable);
        }

        return throwable;
    }

    private String getMessageWithContext(String message) {
        if (this.context == null || this.context.isEmpty()) {
            return message;
        }

        StringBuilder sb;

        if (CoreUtils.isNullOrEmpty(message)) {
            sb = new StringBuilder(20 + context.size() * 20);
        } else {
            sb = new StringBuilder(message.length() + 20 + context.size() * 20)
                .append(message)
                .append(", ");
        }

        sb.append("az.sdk.context={");

        for (int i = 0; i < context.size() - 1; i++) {
            context.get(i)
                .writeKeyAndValue(sb)
                .append(",");
        }

        // json does not allow trailing commas
        context.get(context.size() - 1)
            .writeKeyAndValue(sb)
            .append("}");

        return sb.toString();
    }

    private void addKeyValueInternal(String key, Object value) {
        if (this.context == null) {
            this.context = new ArrayList<>();
        }

        this.context.add(new ContextKeyValuePair(key, value));
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
         * Writes {"key":"value"} json string to provided StringBuilder.
         */
        public StringBuilder writeKeyAndValue(StringBuilder formatter) {
            formatter.append("\"")
                .append(key)
                .append("\"")
                .append(":");

            String valueStr = null;
            if (value != null) {
                // LoggingEventBuilder only supports primitives and strings
                if (!(value instanceof String)) {
                    return formatter.append(value);
                }

                valueStr = (String) value;
            } else if (valueSupplier != null) {
                valueStr = valueSupplier.get();
            }

            if (valueStr == null) {
                return formatter.append("null");
            }

            return formatter.append("\"")
                .append(valueStr)
                .append("\"");
        }
    }
}
