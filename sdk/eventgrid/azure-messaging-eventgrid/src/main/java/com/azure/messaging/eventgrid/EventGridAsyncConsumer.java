// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
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

    /**
     * Creates a new instance of the consumer with a default deserializer and system event mappings.
     */
    EventGridAsyncConsumer(Map<String, Class<?>> typeMappings, JsonSerializer dataDeserializer) {
        this.typeMappings = typeMappings;
        this.deserializer = new JacksonJsonSerializerBuilder()
            .serializer(new JacksonAdapter().serializer() // this is a workaround to get the FlatteningDeserializer
                .registerModule(new JavaTimeModule())) // probably also change this to DateTimeDeserializer when possible
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
        return Flux.fromArray(deserializer
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class))
        )
            .flatMap(this::richDataAndConvert);
    }


    private Mono<EventGridEvent> richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.EventGridEvent event) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getEventType());
        if (typeMappings.containsKey(eventType)) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            deserializer.serialize(stream, event.getData());

            return dataDeserializer.deserializeAsync(new ByteArrayInputStream(stream.toByteArray()),
                TypeReference.createInstance(typeMappings.get(eventType)))
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
        return deserializer
            .deserializeAsync(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.CloudEvent[].class))
            .flatMapMany(Flux::fromArray)
            .flatMap(this::richDataAndConvert);
    }

    private Mono<CloudEvent> richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.CloudEvent event) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getType());
        if (typeMappings.containsKey(eventType)) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            deserializer.serialize(stream, event.getData());

            return dataDeserializer.deserializeAsync(new ByteArrayInputStream(stream.toByteArray()),
                TypeReference.createInstance(typeMappings.get(eventType)))
                .map(data -> new CloudEvent(event).setData(data));
        }
        return Mono.just(new CloudEvent(event));
    }

    /**
     * Deserialize the given JSON into a given custom event schema and return event type, deserializing all
     * known data using the deserializer set with {@link EventGridConsumerBuilder#dataDeserializer(JsonSerializer)}
     * @param json     The information to deserialize as a JSON string
     * @param clazz    The class type of the custom event
     * @param <TEvent> The custom event object. Must be deserializable by the set deserializer.
     *
     * @return The deserialized events in an {@link Flux}
     */
    public <TEvent> Flux<TEvent> deserializeCustomEvents(String json, Class<TEvent> clazz) {

        return dataDeserializer
            .deserializeAsync(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(Array.newInstance(clazz, 0).getClass()))
            .flatMapMany(arr -> Flux.fromArray((TEvent[]) arr));
    }
}
