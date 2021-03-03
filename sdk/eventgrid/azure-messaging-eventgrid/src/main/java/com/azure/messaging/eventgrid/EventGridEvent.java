// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.models.CloudEvent;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents events in the EventGrid schema to be used with the EventGrid service.
 *
 * When you send a EventGridEvent to an Event Grid Topic, the topic must be configured to receive the EventGridEvent schema.
 *
 * For new customers, {@link CloudEvent} is generally preferred over EventGridEvent because the
 * <a href="https://docs.microsoft.com/azure/event-grid/cloud-event-schema">CloudEvent schema</a> is supported across
 * organizations while the <a href="https://docs.microsoft.com/azure/event-grid/event-schema">EventGridEvent schema</a> is not.
 *
 * @see EventGridPublisherAsyncClient
 * @see EventGridPublisherClient
 **/
@Fluent
public final class EventGridEvent {

    private final com.azure.messaging.eventgrid.implementation.models.EventGridEvent event;

    private static final ClientLogger LOGGER = new ClientLogger(EventGridEvent.class);
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance();
    private static final TypeReference<List<com.azure.messaging.eventgrid.implementation.models.EventGridEvent>>
        DESERIALIZER_TYPE_REFERENCE =
        new TypeReference<List<com.azure.messaging.eventgrid.implementation.models.EventGridEvent>>() {
        };
    // May get SERIALIZER's object mapper in the future.
    private static final ObjectMapper BINARY_DATA_OBJECT_MAPPER = new ObjectMapper();

    /*
     * Cache serialized data for getData()
     */
    private BinaryData binaryData;

    /**
     * Create a new instance of the EventGridEvent, with the given required fields.
     * @param subject the subject of the event.
     * @param eventType the type of the event, e.g. "Contoso.Items.ItemReceived".
     * @param data the data associated with this event.
     * @param dataVersion the version of the data sent along with the event.
     *
     * @throws IllegalArgumentException if subject, eventType or data is {@code null} or empty.
     */
    public EventGridEvent(String subject, String eventType, BinaryData data, String dataVersion) {
        Objects.requireNonNull(subject, "'subject' cannot be null.");
        Objects.requireNonNull(eventType, "'eventType' cannot be null.");
        Objects.requireNonNull(data, "'data' cannot be null");
        Objects.requireNonNull(dataVersion, "'dataVersion' cannot be null");

        this.event = new com.azure.messaging.eventgrid.implementation.models.EventGridEvent()
            .setEventTime(OffsetDateTime.now())
            .setId(UUID.randomUUID().toString())
            .setSubject(subject)
            .setEventType(eventType)
            .setDataVersion(dataVersion);
        this.binaryData = data;
        try {
            this.event.setData(BINARY_DATA_OBJECT_MAPPER.readTree(data.toBytes()));
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'data' isn't in valid Json format",
                e));
        }
    }

    /**
     * Deserialize the {@link EventGridEvent} from a JSON string.
     * @param eventGridJsonString the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link EventGridEvent EventGridEvents}.
     * @throws IllegalArgumentException if eventGridJsonString isn't a JSON string for a eventgrid event
     * or an array of it.
     * @throws NullPointerException if eventGridJsonString is {@code null}.
     */
    public static List<EventGridEvent> fromString(String eventGridJsonString) {
        try {
            return SERIALIZER.deserialize(
                new ByteArrayInputStream(eventGridJsonString.getBytes(StandardCharsets.UTF_8)),
                DESERIALIZER_TYPE_REFERENCE)
                .stream()
                .map(internalEvent -> {
                    if (internalEvent.getSubject() == null || internalEvent.getEventType() == null
                        || internalEvent.getData() == null) {
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                            "'subject', 'type', and 'data' are mandatory attributes for an EventGridEvent. "
                            + "Check if the input param is a JSON string for an EventGridEvent or an array of it."));
                    }
                    return new EventGridEvent(internalEvent);
                })
                .collect(Collectors.toList());
        } catch (UncheckedIOException uncheckedIOException) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The input parameter isn't a JSON string.",
                uncheckedIOException.getCause()));
        }
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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'id' cannot be null or empty."));
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
     * Get the data associated with this event as a {@link BinaryData}, which has API to deserialize the data to
     * any objects by using {@link BinaryData#toObject(TypeReference)}
     * @return A {@link BinaryData} that wraps the this event's data payload.
     */
    public BinaryData getData() {
        if (this.binaryData == null) {
            Object data = this.event.getData();
            if (data != null) {
                this.binaryData = BinaryData.fromObject(data, SERIALIZER);
            }
        }
        return this.binaryData;
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

    EventGridEvent(com.azure.messaging.eventgrid.implementation.models.EventGridEvent impl) {
        this.event = impl;
    }

    com.azure.messaging.eventgrid.implementation.models.EventGridEvent toImpl() {
        return this.event;
    }

}
