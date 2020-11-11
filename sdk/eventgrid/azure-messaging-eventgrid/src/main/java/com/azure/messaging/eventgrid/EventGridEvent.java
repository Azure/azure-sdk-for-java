// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The EventGridEvent model. This represents events in the EventGrid schema to be used with the EventGrid service.
 * @see EventGridPublisherAsyncClient
 * @see EventGridPublisherClient
 **/
@Fluent
public final class EventGridEvent {

    private final com.azure.messaging.eventgrid.implementation.models.EventGridEvent event;

    private static final ClientLogger logger = new ClientLogger(EventGridEvent.class);

    private boolean parsed = false;

    private static final JsonSerializer deserializer = new JacksonJsonSerializerBuilder()
        .serializer(new JacksonAdapter().serializer() // this is a workaround to get the FlatteningDeserializer
            .registerModule(new JavaTimeModule())) // probably also change this to DateTimeDeserializer when/if it
        .build();                                  // becomes public in core

    /**
     * Create a new instance of the EventGridEvent, with the given required fields.
     * @param subject     the subject of the event.
     * @param eventType   the type of the event, e.g. "Contoso.Items.ItemReceived".
     * @param data        the data associated with this event.
     * @param dataVersion the version of the data sent along with the event.
     */
    public EventGridEvent(String subject, String eventType, Object data, String dataVersion) {
        if (CoreUtils.isNullOrEmpty(subject)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("subject cannot be null or empty"));
        } else if (CoreUtils.isNullOrEmpty(eventType)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("event type cannot be null or empty"));
        } else if (CoreUtils.isNullOrEmpty(dataVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("data version cannot be null or empty"));
        }

        this.event = new com.azure.messaging.eventgrid.implementation.models.EventGridEvent()
            .setEventTime(OffsetDateTime.now())
            .setId(UUID.randomUUID().toString())
            .setSubject(subject)
            .setEventType(eventType)
            .setData(data)
            .setDataVersion(dataVersion);
    }

    /**
     * Parse the EventGrid Event from a JSON string. This can be used to interpret the event at the event destination
     * from raw JSON into rich event(s).
     * @param json the JSON payload containing one or more events.
     *
     * @return all of the events in the payload parsed as CloudEvents.
     */
    public static List<EventGridEvent> parse(String json) {
        return Flux.fromArray(deserializer
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.EventGridEvent[].class))
            )
            .map(event -> {
                if (event.getData() == null) {
                    return new EventGridEvent(event);
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                deserializer.serialize(stream, event.getData());
                return new EventGridEvent(event.setData(stream.toByteArray())); // use BinaryData instead?
            })
            .collectList()
            .block();
    }


    /**
     * Get the unique id associated with this event.
     * @return the id.
     */
    public String getId() {
        return this.event.getId();
    }

    /**
     * Set the unique id of the event. Note that a random id has already been set by default.
     * @param id the unique id to set.
     *
     * @return the event itself.
     */
    public EventGridEvent setId(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("id cannot be null or empty"));
        }
        this.event.setId(id);
        return this;
    }

    /**
     * Get the topic associated with this event if it is associated with a domain.
     * @return the topic, or null if the topic is not set (i.e. the event came from or is going to a domain).
     */
    public String getTopic() {
        return this.event.getTopic();
    }

    /**
     * Set the topic associated with this event. Used to route events from domain endpoints.
     * @param topic the topic to set.
     *
     * @return the event itself.
     */
    public EventGridEvent setTopic(String topic) {
        this.event.setTopic(topic);
        return this;
    }

    /**
     * Get the subject associated with this event.
     * @return the subject.
     */
    public String getSubject() {
        return this.event.getSubject();
    }


    /**
     * Get the data associated with this event. For use in a parsed event only.
     * @return If the event was parsed from a Json, this method will return the rich
     * system event data if it is a system event, and a {@code byte[]} otherwise, such as in the case of custom event
     * data.
     * @throws IllegalStateException If the event was not created through {@link EventGridEvent#parse(String)}.
     */
    public Object getData() {
        if (!parsed) {
            // data was set instead of parsed, throw error
            throw logger.logExceptionAsError(new IllegalStateException(
                "This method should only be called on events created through the parse method"));
        }
        String eventType = SystemEventMappings.canonicalizeEventType(event.getEventType());
        if (SystemEventMappings.getSystemEventMappings().containsKey(eventType)) {
            // system event
            return deserializer.deserialize(new ByteArrayInputStream((byte[]) this.event.getData()),
                TypeReference.createInstance(SystemEventMappings.getSystemEventMappings().get(eventType)));
        }
        return event.getData();
    }

    /**
     * Get the deserialized data property from the parsed event. The behavior is undefined if this method is called
     * on an event that was not created through the parse method.
     * @param clazz the class of the type to deserialize the data into.
     * @param <T>   the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using a default deserializer.
     * @throws IllegalStateException If the event was not created through {@link EventGridEvent#parse(String)}.
     */
    public <T> T getData(Class<T> clazz) {
        return getDataAsync(clazz, deserializer).block();
    }

    /**
     * Get the deserialized data property from the parsed event.
     * @param clazz the class of the type to deserialize the data into.
     * @param <T>   the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using a default deserializer, delivered asynchronously through
     * a {@link Mono}.
     * @throws IllegalStateException If the event was not created through {@link EventGridEvent#parse(String)}.
     */
    public <T> Mono<T> getDataAsync(Class<T> clazz) {
        return getDataAsync(clazz, deserializer);
    }

    /**
     * Get the deserialized data property from the parsed event.
     * @param clazz            the class of the type to deserialize the data into.
     * @param dataDeserializer the deserializer to use.
     * @param <T>              the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using the given deserializer.
     * @throws IllegalStateException If the event was not created through {@link EventGridEvent#parse(String)}.
     */
    public <T> T getData(Class<T> clazz, JsonSerializer dataDeserializer) {
        return getDataAsync(clazz, dataDeserializer).block();
    }

    /**
     * Get the deserialized data property from the parsed event.
     * @param clazz            the class of the type to deserialize the data into.
     * @param dataDeserializer the deserializer to use.
     * @param <T>              the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using the given deserializer, delivered asynchronously through
     * a {@link Mono}.
     * @throws IllegalStateException If the event was not created through {@link EventGridEvent#parse(String)}.
     */
    public <T> Mono<T> getDataAsync(Class<T> clazz, JsonSerializer dataDeserializer) {
        if (!parsed) {
            // data was set instead of parsed, throw exception because we don't know how the data relates to clazz
            return FluxUtil.monoError(logger, new IllegalStateException(
                "This method should only be called on events created through the parse method"));
        }

        return dataDeserializer.deserializeAsync(new ByteArrayInputStream((byte[]) this.event.getData()),
            TypeReference.createInstance(clazz));
    }

    /**
     * Get the type of this event.
     * @return the event type.
     */
    public String getEventType() {
        return this.event.getEventType();
    }

    /**
     * Get the time associated with the occurrence of this event.
     * @return the event time.
     */
    public OffsetDateTime getEventTime() {
        return this.event.getEventTime();
    }

    /**
     * Set the time associated with the event. Note that a default time has already been set when the event was
     * constructed.
     * @param time the time to set.
     *
     * @return the event itself.
     */
    public EventGridEvent setEventTime(OffsetDateTime time) {
        this.event.setEventTime(time);
        return this;
    }

    /**
     * Get the version of the data in the event. This can be used to specify versioning of event data schemas over time.
     * @return the version of the event data.
     */
    public String getDataVersion() {
        return this.event.getDataVersion();
    }

    /**
     * Get the metadata version of this event. Note that metadata version is a read-only property set by the service.
     * @return the metadata version of this event.
     */
    public String getMetadataVersion() {
        return this.event.getMetadataVersion();
    }

    private EventGridEvent(com.azure.messaging.eventgrid.implementation.models.EventGridEvent impl) {
        this.event = impl;
        parsed = true;
    }

    com.azure.messaging.eventgrid.implementation.models.EventGridEvent toImpl() {
        return this.event;
    }

}
