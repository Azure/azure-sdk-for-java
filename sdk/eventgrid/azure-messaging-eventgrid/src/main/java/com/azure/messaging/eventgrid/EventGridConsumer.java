// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.experimental.serializer.JsonArray;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to decode events and their data from EventGrid at the endpoints. Able to decode all system event data
 * types, and provides support for custom event data deserialization mapping.
 * @see EventGridEvent
 * @see CloudEvent
 */
@Fluent
public final class EventGridConsumer {

    private final Map<String, Class<?>> typeMappings;


    private final JsonSerializer deserializer;

    /**
     * Creates a new instance of the consumer with a default deserializer and system event mappings.
     */
    public EventGridConsumer() {
        this.typeMappings = new HashMap<>(SystemEventMappings.getSystemEventMappings());
        this.deserializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().registerModule(new JavaTimeModule()))
            .build();
    }

    /**
     * Add a mapping from the event data type string to the object class for the deserializer to deserialize.
     * @param eventType the event data type identifier string, e.g. "Contoso.Items.ItemRecieved". In EventGridEvents,
     *                  this is the <code>eventType</code> field. In CloudEvents, this is the <code>type</code> field.
     * @param dataType  the data type class of the object to be decoded. To
     * @param <T>       the type of the data to be decoded. This corresponds to the data field. The object must
     *                  have proper JsonProperty annotations to allow for deserialization.
     *
     * @return the builder itself.
     */
    public <T> EventGridConsumer putDataMapping(String eventType, Class<T> dataType) {
        if (CoreUtils.isNullOrEmpty(eventType) || dataType == null) {
            return this;
        }
        this.typeMappings.put(SystemEventMappings.canonicalizeEventType(eventType), dataType);
        return this;
    }


    /**
     * Deserialize the given JSON using the available mappings and return the rich EventGridEvent Object, with the
     * data field as a rich object, if possible.
     * @param json The information to be deserialize in JSON format.
     *
     * @return The deserialized events in a List.
     */
    public List<EventGridEvent> deserializeEventGridEvents(String json) {
        return deserializer.toTree(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            .flatMapMany(jsonNode -> {
                if (jsonNode.isArray()) {
                    return deserializer.deserializeTree(jsonNode,
                        com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class)
                        .flatMapMany(Flux::fromArray)
                        .map(this::richDataAndConvert);
                } else {
                    return deserializer.deserializeTree(jsonNode,
                        com.azure.messaging.eventgrid.implementation.models.EventGridEvent.class)
                        .as(Flux::from)
                        .map(this::richDataAndConvert);
                }
            })
            .collectList()
            .block();
    }


    private EventGridEvent richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.EventGridEvent
                                                  event) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getEventType());
        if (typeMappings.containsKey(eventType)) {
            event.setData(deserializer.toTree(event.getData())
                .flatMap(jsonNode -> deserializer.deserializeTree(jsonNode, typeMappings.get(eventType)))
                .block());
        }
        return new EventGridEvent(event);
    }

    /**
     * Deserialize the given JSON and return the rich CloudEvent, with the data field as a
     * rich object, if possible.
     * @param json the JSON string containing the event that was received from the EventGrid service.
     *
     * @return the deserialized cloud events in a list, with rich object data.
     */
    public List<CloudEvent> deserializeCloudEvents(String json) {
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
            .map(this::richDataAndConvert)
            .collectList()
            .block();
    }

    private CloudEvent richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.CloudEvent event) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getType());
        if (typeMappings.containsKey(eventType)) {
            event.setData(deserializer.toTree(event.getData())
                .flatMap(jsonNode -> deserializer.deserializeTree(jsonNode, typeMappings.get(eventType)))
                .block());
        }
        return new CloudEvent(event);
    }

    /**
     * Deserialize the given JSON into a given custom event schema and return the rich event type, using
     * available mappings to fill in rich object data
     * @param json           The information to deserialize as a JSON string
     * @param eventTypeClass The class type of the custom event
     * @param <TEvent>       The custom event object. Must have the proper Json annotations to allow for deserialization
     *
     * @return The deserialized events in a List
     */
    public <TEvent> List<TEvent> deserializeCustomEvents(String json, Class<TEvent> eventTypeClass) {
        return deserializer.toTree(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
            .flatMapMany(jsonNode -> {
                if (jsonNode.isArray()) {
                    JsonArray jsonArr = (JsonArray) jsonNode;
                    return Flux.fromStream(jsonArr.elements())
                        .flatMap(node -> deserializer.deserializeTree(node, eventTypeClass));
                } else {
                    return deserializer.deserializeTree(jsonNode, eventTypeClass)
                        .as(Flux::from);
                }
            })
            .collectList()
            .block();
    }
}
