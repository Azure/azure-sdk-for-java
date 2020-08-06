// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to construct instances of the immutable classes {@link EventGridConsumer} and
 * {@link EventGridAsyncConsumer}, which are used to receive and deserialize events sent from the EventGrid service.
 */
@Fluent
public class EventGridConsumerBuilder {

    private final Map<String, Class<?>> typeMappings;

    private JsonSerializer deserializer;

    /**
     * Create the Consumer Builder with system event mappings already loaded.
     */
    public EventGridConsumerBuilder() {
        typeMappings = new HashMap<>(SystemEventMappings.getSystemEventMappings());
    }

    /* I'm unsure if all system events correctly give the time zone offset when sending their time, but the offset
       should always be +0:00 or Z, aka UTC, so this will handle exceptions when there isn't an offset given by
       assuming UTC time when not specified. This is needed because Jackson cannot deserialize OffsetDateTime
       by itself, and so this is added as a module.

       If the system events all send offsetDateTime correctly with an offset, then instead of this you can add
       the OffsetDateTimeKeyDeserializer, or the entire JavaTime module.
     */
    private static class OffsetDateTimeUtcDefaultDeserializer extends JsonDeserializer<OffsetDateTime> {
        @Override
        public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
            TemporalAccessor time = DateTimeFormatter.ISO_DATE_TIME.parse(jsonParser.getValueAsString());
            try {
                return OffsetDateTime.from(time);
            } catch (DateTimeException e) {
                return OffsetDateTime.of(LocalDateTime.from(time), ZoneOffset.UTC);
            }
        }
    }

    /**
     * Build an instance of the async consumer. If no deserializer is provided, then a default Jackson one is provided.
     * @return the async consumer with the settings that were already set.
     */
    public EventGridAsyncConsumer buildAsyncConsumer() {
        JsonSerializer buildDeserializer = deserializer;
        if (buildDeserializer == null) {
            buildDeserializer = new JacksonJsonSerializerBuilder()
                .serializer(new ObjectMapper().registerModule(
                    new SimpleModule().addDeserializer(OffsetDateTime.class,
                        new OffsetDateTimeUtcDefaultDeserializer()))
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
                .build();
        }

        return new EventGridAsyncConsumer(typeMappings, buildDeserializer);
    }

    /**
     * Build an instance of the sync consumer. If no deserializer is provided, then a default Jackson one is provided.
     * @return the sync consumer with the settings that were already set.
     */
    public EventGridConsumer buildConsumer() {
        return new EventGridConsumer(buildAsyncConsumer());
    }

    /**
     * Add a mapping from the event data type string to the object class for the deserializer to deserialize.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved". In EventGridEvents,
     *                  this is the <code>eventType</code> field. In CloudEvents, this is the <code>type</code> field.
     *                  The event data type will be converted to lowercase.
     * @param dataClass the data type class of the object to be decoded. The deserializer must be able to deserialize
     *                  this type, meaning it should have {@link com.fasterxml.jackson.annotation.JsonProperty}
     *                  annotations if the default serializer is used.
     *
     * @return the builder itself.
     */
    public EventGridConsumerBuilder addDataMapping(String eventType, Class<?> dataClass) {
        if (CoreUtils.isNullOrEmpty(eventType)) {
            throw new IllegalArgumentException("event type cannot be null or empty");
        }
        typeMappings.put(SystemEventMappings.canonicalizeEventType(eventType), dataClass);
        return this;
    }

    /**
     * Set the custom serializer to use when deserializing events. This deserializer should be able to decode all events
     * expected to be in the payload, including the {@link OffsetDateTime} that is included in most events
     * @param deserializer the deserializer to use.
     *
     * @return the builder itself.
     */
    public EventGridConsumerBuilder deserializer(JsonSerializer deserializer) {
        this.deserializer = deserializer;
        return this;
    }
}
