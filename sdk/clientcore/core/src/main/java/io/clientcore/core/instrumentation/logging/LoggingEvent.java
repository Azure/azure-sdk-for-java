// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.instrumentation.logging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.instrumentation.DefaultLogger;
import io.clientcore.core.implementation.instrumentation.Slf4jLoggerShim;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.clientcore.core.annotations.MetadataProperties.FLUENT;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.CAUSE_MESSAGE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.CAUSE_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.EVENT_NAME_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.EXCEPTION_MESSAGE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.EXCEPTION_STACKTRACE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.EXCEPTION_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SPAN_ID_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.TRACE_ID_KEY;

/**
 * This class provides fluent API to write logs using {@link ClientLogger} and
 * enrich them with additional context.
 *
 * <p><strong>Code samples</strong></p>
 *
 * <p>Logging event with context.</p>
 *
 * <!-- src_embed io.clientcore.core.instrumentation.logging.loggingeventbuilder -->
 * <pre>
 * logger.atInfo&#40;&#41;
 *     .addKeyValue&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
 *     .addKeyValue&#40;&quot;key2&quot;, true&#41;
 *     .addKeyValue&#40;&quot;key3&quot;, this::getName&#41;
 *     .log&#40;&quot;A structured log message.&quot;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.instrumentation.logging.loggingeventbuilder -->
 */
@Metadata(properties = FLUENT)
public final class LoggingEvent {
    private static final LoggingEvent NOOP = new LoggingEvent(null, null, null, false);

    private final Slf4jLoggerShim logger;
    private final LogLevel level;
    private final Map<String, Object> globalPairs;
    private final boolean isEnabled;
    private Map<String, Object> keyValuePairs;
    private InstrumentationContext context = null;
    private Throwable throwable = null;

    /**
     * Creates {@code LoggingEvent} for provided level and  {@link ClientLogger}.
     * If level is disabled, returns no-op instance.
     */
    static LoggingEvent create(Slf4jLoggerShim logger, LogLevel level, Map<String, Object> globalContext) {
        if (logger.canLogAtLevel(level)) {
            return new LoggingEvent(logger, level, globalContext, true);
        }

        return NOOP;
    }

    LoggingEvent(Slf4jLoggerShim logger, LogLevel level, Map<String, Object> globalContext, boolean isEnabled) {
        this.logger = logger;
        this.level = level;
        this.isEnabled = isEnabled;
        this.globalPairs = globalContext;
    }

    /**
     * Returns true if this logging event will be logged.
     *
     * @return true if this logging event will be logged.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Adds key with String value to the context of current log being created.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Adding string value to logging event context.</p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atInfo -->
     * <pre>
     * logger.atInfo&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .addKeyValue&#40;&quot;hello&quot;, name&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atInfo -->
     *
     * @param key String key.
     * @param value String value.
     * @return The updated {@code LoggingEvent} object.
     */
    public LoggingEvent addKeyValue(String key, String value) {
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
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#object -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     &#47;&#47; equivalent to addKeyValue&#40;&quot;key&quot;, &#40;&#41; -&gt; new LoggableObject&#40;&quot;string representation&quot;&#41;.toString&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, new LoggableObject&#40;&quot;string representation&quot;&#41;&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#object -->
     *
     * @param key String key.
     * @param value Object value.
     * @return The updated {@code LoggingEvent} object.
     */
    public LoggingEvent addKeyValue(String key, Object value) {
        if (this.isEnabled) {
            addKeyValueInternal(key, value);
        }

        return this;
    }

    /**
     * Adds a key with a boolean value to the context of the current log being created.
     *
     * @param key Key to associate the provided {@code value} with.
     * @param value The boolean value.
     * @return The updated {@link LoggingEvent} object.
     */
    public LoggingEvent addKeyValue(String key, boolean value) {
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
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @param key Key to associate the provided {@code value} with.
     * @param value The long value.
     * @return The updated {@link LoggingEvent} object.
     */
    public LoggingEvent addKeyValue(String key, long value) {
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
     * @return The updated {@link LoggingEvent} object.
     */
    public LoggingEvent addKeyValue(String key, Supplier<String> valueSupplier) {
        if (this.isEnabled && valueSupplier != null) {
            this.addKeyValue(key, valueSupplier.get());
        }
        return this;
    }

    /**
     * Sets operation context on the log event being created.
     * It's used to correlate logs between each other and with other telemetry items.
     *
     * @param context operation context.
     * @return The updated {@link LoggingEvent} object.
     */
    public LoggingEvent setInstrumentationContext(InstrumentationContext context) {
        this.context = context;
        return this;
    }

    /**
     * Sets the throwable for the current log event.
     *
     * @param throwable The throwable to be logged.
     * @return The updated {@link LoggingEvent}  object.
     */
    public LoggingEvent setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    /**
     * Sets the event name for the current log event. The event name is used to query all logs
     * that describe the same event. It must not contain any dynamic parts.
     *
     * @param eventName The name of the event.
     * @return The updated {@link LoggingEvent}  object.
     */
    public LoggingEvent setEventName(String eventName) {
        addKeyValueInternal(EVENT_NAME_KEY, eventName);
        return this;
    }

    /**
     * Logs event annotated with context.
     * Logs event with context.
     */
    public void log() {
        log((String) null);
    }

    /**
     * Logs message annotated with context.
     *
     * @param message log message.
     */
    public void log(String message) {
        performLogging(message);
    }

    /**
     * Logs with a message supplier annotated with context.
     * <p>
     * If the message supplier is null, the message will be null.
     * <p>
     * If the {@link LoggingEvent} is not {@link #isEnabled() enabled} this will be a no-op and the supplier won't be
     * called.
     * <p>
     * This method is preferred if the message is expensive to create and may not be logged.
     *
     * @param message log message.
     */
    public void log(Supplier<String> message) {
        if (this.isEnabled) {
            performLogging(message.get());
        }
    }

    private void performLogging(String message) {
        if (logger != null && logger.canLogAtLevel(level)) {
            logger.performLogging(level, getMessageWithContext(message),
                logger.canLogAtLevel(LogLevel.VERBOSE) ? throwable : null);
        }
    }

    private String getMessageWithContext(String message) {
        if (this.context != null && this.context.isValid()) {
            // TODO (limolkova) we can set context from implicit current span
            // we should also support OTel as a logging provider and avoid adding redundant
            // traceId and spanId to the logs

            addKeyValue(TRACE_ID_KEY, context.getTraceId());
            addKeyValue(SPAN_ID_KEY, context.getSpanId());
        }

        if (throwable != null) {
            recordExceptionDetails(throwable, throwable.getMessage());
        }

        int pairsCount
            = (keyValuePairs == null ? 0 : keyValuePairs.size()) + (globalPairs == null ? 0 : globalPairs.size());

        int messageLength = message == null ? 0 : message.length();
        int speculatedSize = 20 + pairsCount * 20 + messageLength;
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream(speculatedSize);
            JsonWriter jsonWriter = JsonWriter.toStream(outputStream)) {
            jsonWriter.writeStartObject();

            if (message != null) {
                jsonWriter.writeStringField("message", message);
            }

            writeContext(jsonWriter);

            jsonWriter.writeEndObject().flush();

            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    void recordExceptionDetails(Throwable throwable, String message) {
        addKeyValueInternal(EXCEPTION_TYPE_KEY, throwable.getClass().getCanonicalName());
        if (message != null) {
            addKeyValueInternal(EXCEPTION_MESSAGE_KEY, message);
        }

        if (logger != null && logger.canLogAtLevel(LogLevel.VERBOSE)) {
            StringBuilder stackTrace = new StringBuilder();
            if (throwable.getStackTrace().length > 0) {
                DefaultLogger.appendThrowable(stackTrace, throwable);
            } else {
                stackTrace.append("stacktrace disabled");
            }
            addKeyValue(EXCEPTION_STACKTRACE_KEY, stackTrace.toString());
        }
    }

    String getExceptionMessageWithContext(String shortMessage, Throwable cause) {
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause != null) {
            addKeyValue(CAUSE_TYPE_KEY, cause.getClass().getCanonicalName());
            addKeyValue(CAUSE_MESSAGE_KEY, cause.getMessage());
        }

        int pairsCount
            = (keyValuePairs == null ? 0 : keyValuePairs.size()) + (globalPairs == null ? 0 : globalPairs.size());

        if (pairsCount == 0) {
            return shortMessage;
        }

        int speculatedSize = 20 + pairsCount * 20;
        try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream(speculatedSize);
            JsonWriter jsonWriter = JsonWriter.toStream(outputStream)) {
            jsonWriter.writeStartObject();

            writeContext(jsonWriter);

            jsonWriter.writeEndObject().flush();

            String context = outputStream.toString(StandardCharsets.UTF_8);
            return shortMessage == null ? context : shortMessage + "; " + context;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void writeContext(JsonWriter jsonWriter) throws IOException {
        if (globalPairs != null) {
            for (Map.Entry<String, Object> kvp : globalPairs.entrySet()) {
                jsonWriter.writeUntypedField(kvp.getKey(), kvp.getValue());
            }
        }

        if (keyValuePairs != null) {
            for (Map.Entry<String, Object> kvp : keyValuePairs.entrySet()) {
                jsonWriter.writeUntypedField(kvp.getKey(), kvp.getValue());
            }
        }
    }

    private void addKeyValueInternal(String key, Object value) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = new HashMap<>();
        }

        this.keyValuePairs.put(key, value);
    }
}
