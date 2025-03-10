// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.deviceregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Defines the event properties.
 */
@Fluent
public final class Event extends EventBase {
    /*
     * An indication of how the event should be mapped to OpenTelemetry.
     */
    private EventObservabilityMode observabilityMode;

    /**
     * Creates an instance of Event class.
     */
    public Event() {
    }

    /**
     * Get the observabilityMode property: An indication of how the event should be mapped to OpenTelemetry.
     * 
     * @return the observabilityMode value.
     */
    public EventObservabilityMode observabilityMode() {
        return this.observabilityMode;
    }

    /**
     * Set the observabilityMode property: An indication of how the event should be mapped to OpenTelemetry.
     * 
     * @param observabilityMode the observabilityMode value to set.
     * @return the Event object itself.
     */
    public Event withObservabilityMode(EventObservabilityMode observabilityMode) {
        this.observabilityMode = observabilityMode;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event withName(String name) {
        super.withName(name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event withEventNotifier(String eventNotifier) {
        super.withEventNotifier(eventNotifier);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event withEventConfiguration(String eventConfiguration) {
        super.withEventConfiguration(eventConfiguration);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event withTopic(Topic topic) {
        super.withTopic(topic);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (name() == null) {
            throw LOGGER.atError().log(new IllegalArgumentException("Missing required property name in model Event"));
        }
        if (eventNotifier() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property eventNotifier in model Event"));
        }
        if (topic() != null) {
            topic().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(Event.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", name());
        jsonWriter.writeStringField("eventNotifier", eventNotifier());
        jsonWriter.writeStringField("eventConfiguration", eventConfiguration());
        jsonWriter.writeJsonField("topic", topic());
        jsonWriter.writeStringField("observabilityMode",
            this.observabilityMode == null ? null : this.observabilityMode.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of Event from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of Event if the JsonReader was pointing to an instance of it, or null if it was pointing to
     * JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the Event.
     */
    public static Event fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Event deserializedEvent = new Event();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    deserializedEvent.withName(reader.getString());
                } else if ("eventNotifier".equals(fieldName)) {
                    deserializedEvent.withEventNotifier(reader.getString());
                } else if ("eventConfiguration".equals(fieldName)) {
                    deserializedEvent.withEventConfiguration(reader.getString());
                } else if ("topic".equals(fieldName)) {
                    deserializedEvent.withTopic(Topic.fromJson(reader));
                } else if ("observabilityMode".equals(fieldName)) {
                    deserializedEvent.observabilityMode = EventObservabilityMode.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedEvent;
        });
    }
}
