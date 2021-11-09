package com.azure.core.util.logging;

import org.slf4j.MDC;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// heavily inspired by http://www.slf4j.org/apidocs/org/slf4j/spi/LoggingEventBuilder.html
public class LogEntryBuilder {
    private final ClientLogger logger;
    private final LogLevel level;
    private int size=0;

    // instead of virtual methods and builder inheritance
    private final boolean isEnabled;
    private List<AbstractMap.SimpleEntry<String, String>> context;
    private Throwable throwable;

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
        this.throwable = null;
    }

    public LogEntryBuilder setCause(Throwable cause) {
        if (this.isEnabled) {
            this.throwable = cause;
        }
        return this;
    }

    public LogEntryBuilder addKeyValue(String key, String value) {
        if (this.isEnabled) {
            if (this.context == null) {
                this.context = new ArrayList<>();
            }

            this.context.add(new AbstractMap.SimpleEntry<>(key, value));
            size += key.length() + value.length();
        }
        return this;
    }

    // todo: overloads
    public void log(String message) {
        if (this.isEnabled) {

            String messageAndContext;
            if (this.context == null || this.context.isEmpty()) {
                messageAndContext = message;
            } else {
                // can potentially calculate capacity
                StringBuilder sb = new StringBuilder(message.length() + size + context.size() * 6)
                    .append(message)
                    .append(", az.sdk.context={\"")
                    .append(context.get(0).getKey())
                    .append("\":\"")
                    // todo support for primitive types
                    .append(context.get(0).getValue())
                    .append("\"");

                for (int i = 1; i < context.size(); i ++) {
                    AbstractMap.SimpleEntry<String ,String> entry = context.get(i);
                    sb.append(",\"")
                        .append(entry.getKey())
                        .append("\":\"")
                        // todo support for primitive types
                        .append(entry.getValue())
                        .append("\"");
                }

                sb.append("}");

                messageAndContext = sb.toString();
            }

            logger.performDeferredLogging(level, messageAndContext, throwable);
        }
    }
}
