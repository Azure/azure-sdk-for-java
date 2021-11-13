package com.azure.core.util.logging;

import com.azure.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

// heavily inspired by http://www.slf4j.org/apidocs/org/slf4j/spi/LoggingEventBuilder.html
public class LogEntryBuilder {

    private final ClientLogger logger;
    private final LogLevel level;

    // instead of virtual methods and builder inheritance
    private final boolean isEnabled;

    private List<ContextKeyValuePair> context;

    private static final LogEntryBuilder NOOP = new LogEntryBuilder(null, null, false);

    static LogEntryBuilder create(ClientLogger logger, LogLevel level, boolean isEnabled) {
        if (isEnabled) {
            return new LogEntryBuilder(logger, level, true);
        }

        return NOOP;
    }

    private LogEntryBuilder(ClientLogger logger, LogLevel level, boolean isEnabled) {
        this.logger = logger;
        this.level = level;
        this.isEnabled = isEnabled;
        this.context = null;
    }

    public LogEntryBuilder addKeyValue(String key, String value) {
        if (this.isEnabled) {
            addKeyValueInternal(key, value);
        }

        return this;
    }

    public LogEntryBuilder addKeyValue(String key, boolean value) {
        if (this.isEnabled) {
            addKeyValueInternal(key, value);
        }
        return this;
    }

    public LogEntryBuilder addKeyValue(String key, long value) {
        if (this.isEnabled) {
            addKeyValueInternal(key, value);
        }
        return this;
    }

    // presumably primitive values are always calculated and never need deferred value calculation
    public LogEntryBuilder addKeyValue(String key, Supplier<String> valueSupplier) {
        if (this.isEnabled) {
            if (this.context == null) {
                this.context = new ArrayList<>();
            }

            this.context.add(new ContextKeyValuePair(key, null, valueSupplier));
        }
        return this;
    }

    public void log(String message) {
        if (this.isEnabled) {
            logger.performLogging(level, getMessageWithContext(message));
        }
    }

    public void log(Supplier<String> messageSupplier) {
        if (this.isEnabled) {
            String message = messageSupplier != null ? messageSupplier.get() : null;
            logger.performLogging(level, getMessageWithContext(message));
        }
    }

    public void log(String format, Object... args) {
        if (this.isEnabled) {
            logger.performLogging(level, getMessageWithContext(format), args);
        }
    }

    public void log(Throwable throwable) {
        Objects.requireNonNull(throwable, "'throwable' cannot be null.");

        if (this.isEnabled) {
            logger.performThrowableLogging(level, getMessageWithContext(throwable.getMessage()), throwable);
        }
    }

    public String getMessageWithContext(String message) {
        if (this.context == null || this.context.isEmpty()) {
            return message;
        }

        StringBuilder sb;

        if (CoreUtils.isNullOrEmpty(message)) {
            sb = new StringBuilder(16);
        } else {
            sb = new StringBuilder(message.length() + 18)
                .append(message)
                .append(", ");
        }

        sb.append("az.sdk.context={");

        int i = 0;
        for (; i < context.size() - 1; i ++) {
            context.get(i)
                .writeKeyAndValue(sb)
                .append(",");
        }

        context.get(i)
            .writeKeyAndValue(sb)
            .append("}");

        return sb.toString();
    }

    private void addKeyValueInternal(String key, Object value) {
        if (this.context == null) {
            this.context = new ArrayList<>();
        }

        this.context.add(new ContextKeyValuePair(key, value, null));
    }

    static private class ContextKeyValuePair {
        private final String key;
        private final Object value;
        private final Supplier<String> valueSupplier;

        public ContextKeyValuePair(String key, Object value, Supplier<String> valueSupplier) {
            this.key = key;
            this.value = value;
            this.valueSupplier = valueSupplier;
        }

        public StringBuilder writeKeyAndValue(StringBuilder formatter) {
            formatter.append("\"")
                .append(key)
                .append("\"")
                .append(":");

            String valueStr = null;
            if (value != null) {
                // LogEntryBuilder ensures only strings and primitives could be here
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
