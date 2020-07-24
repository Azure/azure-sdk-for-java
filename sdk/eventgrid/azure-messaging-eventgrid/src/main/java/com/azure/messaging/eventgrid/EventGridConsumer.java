// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.experimental.serializer.JsonArray;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.JsonSerializerProviders;
import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.CoreUtils;
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
public final class EventGridConsumer {

    private final Map<String, Class<?>> typeMappings;

    private JsonSerializer deserializer;

    /**
     * Creates a new instance of the consumer with a default deserializer and system event mappings.
     */
    public EventGridConsumer() {
        this(null);
    }

    /**
     * Creates an instance of the consumer using a provided custom {@link JsonSerializer}. Contains all system
     * event mappings.
     * @param deserializer the serializer/deserializer to use. If none is provided a default will be loaded
     *                     from the classpath.
     */
    public EventGridConsumer(JsonSerializer deserializer) {
        this.typeMappings = new HashMap<>(SystemEventMappings.getSystemEventMappings());
        if (deserializer == null) {
            this.deserializer = JsonSerializerProviders.createInstance();
        } else {
            this.deserializer = deserializer;
        }
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
     * Add a custom deserializer to interpret the json as a rich event object. If this value is not set, the default
     * is loaded from the classpath.
     * @param deserializer the deserializer to use.
     *
     * @return the builder itself.
     */
    public EventGridConsumer deserializer(JsonSerializer deserializer) {
        if (deserializer == null) {
            // reset to default
            this.deserializer = JsonSerializerProviders.createInstance();
        } else {
            this.deserializer = deserializer;
        }
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
            deserializer.toTree(event.getData())
                .flatMap(jsonNode -> deserializer.deserializeTree(jsonNode, typeMappings.get(eventType)))
                .subscribe(event::setData);
        }
        return new EventGridEvent(event);
    }

    /**
     * Deserialize the given JSON using the available mappings and return the rich CloudEvent, with the
     * data field as a rich object, if possible. Binary data will be directly serialized
     * @param json the information to be deserialize as a JSON string.
     *
     * @return the deserialized events in a List.
     */
    public List<CloudEvent> deserializeCloudEvents(String json) {
        return deserializeCloudEvents(json, null);
    }

    /**
     * Deserialize the given JSON using the set deserializer and return the rich CloudEvent, with the data field as a
     * rich object, if possible. This overload is to be used with data serialized not in "application/json" format,
     * for example as an xml string or with the use of the {@link CloudEvent#setData(Object, ObjectSerializer, String)}
     * overload. There should be a custom event mapping for the expected event type field which
     * corresponds to the deserialized type expected in the data field.
     * @param json                   the JSON string containing the event that was recieved from the EventGrid service.
     * @param customDataDeserializer The deserializer to decode the data field, or null to use the same JSON
     *                               deserializer behavior as the rest of the event.
     *
     * @return the deserialized cloud events in a list, with rich object data.
     */
    public List<CloudEvent> deserializeCloudEvents(String json, ObjectSerializer customDataDeserializer) {
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
            .map(cloudEvent -> richDataAndConvert(cloudEvent, customDataDeserializer))
            .collectList()
            .block();
    }

    private CloudEvent richDataAndConvert(com.azure.messaging.eventgrid.implementation.models.CloudEvent
                                              event, ObjectSerializer objectSerializer) {
        String eventType = SystemEventMappings.canonicalizeEventType(event.getType());
        if (typeMappings.containsKey(eventType)) {
            if (objectSerializer != null) {
                // data was custom serialized, expect a json string as data
                objectSerializer.deserialize(new ByteArrayInputStream(((String) event.getData())
                        .getBytes(StandardCharsets.UTF_8)),
                    typeMappings.get(eventType))
                    .subscribe(event::setData);
            }
            deserializer.toTree(event.getData())
                .flatMap(jsonNode -> deserializer.deserializeTree(jsonNode, typeMappings.get(eventType)))
                .subscribe(event::setData);
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
