package io.clientcore.core.implementation.util;

import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonWriter;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class LoggingEvent implements JsonSerializable<LoggingEvent> {
    private String message;
    private Throwable throwable;

    private Map<String, Object> keyValuePairs;
    private Map<String, Supplier<Object>> keyValuePairsSupplier;

    public LoggingEvent() {
    }

    public LoggingEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public LoggingEvent setThrowable(Throwable throwable, boolean isDebugEnabled) {
        this.throwable = throwable;
        if (throwable != null) {
            addKeyValue("exception.message", throwable.getMessage());
            if (isDebugEnabled) {
                addKeyValue("exception.stack_trace", throwable.getStackTrace()
            }
        }

        return this;
    }

    public LoggingEvent addKeyValue(String key, Object value) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = new java.util.HashMap<>();
        }
        this.keyValuePairs.put(key, value);
        return this;
    }

    public LoggingEvent addKeyValue(String key, Supplier<Object> value) {
        if (this.keyValuePairsSupplier == null) {
            this.keyValuePairsSupplier = new java.util.HashMap<>();
        }
        this.keyValuePairsSupplier.put(key, value);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("message", message);

            .writeBooleanField("boolean", aBoolean)
            .writeStringField("string", aString)
            .writeNumberField("decimal", aNullableDecimal)
            .writeEndObject();
    }
}
