// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.experimental.serializer.JsonArray;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

/**
 * A class used to decode events and their data from EventGrid at the endpoints. Able to decode all system event data
 * types, and provides support for custom event data deserialization mapping.
 * @see EventGridEvent
 * @see CloudEvent
 */
@Fluent
public final class EventGridAsyncConsumer {

    private final Map<String, Class<?>> typeMappings;


    private final JsonSerializer deserializer;

    private final JsonSerializer dataDeserializer;

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
     * Creates a new instance of the consumer with a default deserializer and system event mappings.
     */
    EventGridAsyncConsumer(Map<String, Class<?>> typeMappings, JsonSerializer dataDeserializer) {
        this.typeMappings = typeMappings;
        this.deserializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().registerModule(
                new SimpleModule().addDeserializer(OffsetDateTime.class,
                    new OffsetDateTimeUtcDefaultDeserializer()))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
            .build();
        this.dataDeserializer = dataDeserializer == null ? this.deserializer : dataDeserializer;
    }


    /**
     * Deserialize the given JSON using the available mappings and return the rich EventGridEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize in JSON format.
     *
     * @return The deserialized events in an {@link Flux}.
     */
    public Flux<EventGridEvent> deserializeEventGridEvents(String json) {
        return deserializer.toTree(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            .flatMapMany(jsonNode -> {
                if (jsonNode.isArray()) {
                    return deserializer.deserializeTree(jsonNode,
                        com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class)
                        .flatMapMany(Flux::fromArray);
                } else {
                    return deserializer.deserializeTree(jsonNode,
                        com.azure.messaging.eventgrid.implementation.models.EventGridEvent.class)
                        .as(Flux::from);

                }
            })
            .flatMap(this::richDataAndConvert);
    }


    private Mono<EventGridEvent> richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.EventGridEvent event) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getEventType());
        if (typeMappings.containsKey(eventType)) {
            return deserializer.toTree(event.getData())
                .flatMap(jsonNode -> dataDeserializer.deserializeTree(jsonNode, typeMappings.get(eventType)))
                .map(data -> new EventGridEvent(event).setData(data));
        }
        return Mono.just(new EventGridEvent(event));
    }

    /**
     * Deserialize the given JSON and return the rich CloudEvent, with the data field as a
     * rich object(s), if possible.
     * @param json the JSON string containing the event that was received from the EventGrid service.
     *
     * @return the deserialized cloud events in an {@link Flux}, with rich object data.
     */
    public Flux<CloudEvent> deserializeCloudEvents(String json) {
        return deserializer.toTree(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            .flatMapMany(jsonNode -> {
                if (jsonNode.isArray()) {
                    return deserializer.deserializeTree(jsonNode,
                        com.azure.messaging.eventgrid.implementation.models.CloudEvent[].class)
                        .flatMapMany(Flux::fromArray);
                } else {
                    return deserializer.deserializeTree(jsonNode,
                        com.azure.messaging.eventgrid.implementation.models.CloudEvent.class)
                        .as(Flux::from);
                }
            })
            .flatMap(this::richDataAndConvert);
    }

    private Mono<CloudEvent> richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.CloudEvent event) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getType());
        if (typeMappings.containsKey(eventType) && event.getData() != null) {
            return deserializer.toTree(event.getData())
                .flatMap(jsonNode -> dataDeserializer.deserializeTree(jsonNode, typeMappings.get(eventType)))
                .map(data -> new CloudEvent(event).setData(data));
        }
        return Mono.just(new CloudEvent(event));
    }

    /**
     * Deserialize the given JSON into a given custom event schema and return the rich event type, using
     * available mappings to fill in rich object data
     * @param json     The information to deserialize as a JSON string
     * @param clazz    The class type of the custom event
     * @param <TEvent> The custom event object. Must be deserializable by the set deserializer.
     *
     * @return The deserialized events in an {@link Flux}
     */
    public <TEvent> Flux<TEvent> deserializeCustomEvents(String json, Class<TEvent> clazz) {
        return deserializer.toTree(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            .flatMapMany(jsonNode -> {
                if (jsonNode.isArray()) {
                    JsonArray jsonArr = (JsonArray) jsonNode;
                    return Flux.fromStream(jsonArr.elements())
                        .flatMap(node -> deserializer.deserializeTree(node, clazz));
                } else {
                    return deserializer.deserializeTree(jsonNode, clazz)
                        .as(Flux::from);
                }
            });
    }
}
