// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the EventGrid event conforming to the <a href="https://docs.microsoft.com/azure/event-grid/event-schema">
 *     EventGrid event schema</a>.
 *
 * <p>Depending on your scenario, you can either use the constructor
 * {@link #EventGridEvent(String, String, BinaryData, String)} to create an EventGridEvent,
 * or use the factory method {@link #fromString(String)} to deserialize EventGridEvent instances
 * from a Json String representation of EventGrid events.</p>
 *
 * <p>If you have the data payload of an EventGridEvent and want to send it out, use the constructor
 * {@link #EventGridEvent(String, String, BinaryData, String)} to create it. Then use
 * {@link EventGridPublisherAsyncClient} or {@link EventGridPublisherClient} to send it the EventGrid service.</p>
 *
 * <p><strong>Create EventGridEvent Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridEvent#constructor}
 *
 * <p>On the contrary, if you receive events from any event handlers and therefore have the Json string representation
 * of one or more of EventGridEvents, use {@link #fromString(String)} to deserialize them from the Json string.</p>
 *
 * <p><strong>Deserialize EventGridEvent Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridEvent.fromString}
 *
 * @see EventGridPublisherAsyncClient to send EventGridEvents asynchronously.
 * @see EventGridPublisherClient to send EventGridEvents sychronously.
 **/
@Fluent
public final class EventGridEvent {

    private final com.azure.messaging.eventgrid.implementation.models.EventGridEvent event;

    private static final ClientLogger LOGGER = new ClientLogger(EventGridEvent.class);
    private static final JsonSerializer SERIALIZER;
    static {
        JsonSerializer tmp;
        try {
            tmp = JsonSerializerProviders.createInstance();
        } catch (IllegalStateException e) {
            tmp = new JacksonSerializer();
        }
        SERIALIZER = tmp;
    }
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
     *
     * <p><strong>Create EventGridEvent Samples</strong></p>
     * {@codesnippet com.azure.messaging.eventgrid.EventGridEvent#constructor}
     *
     * @param subject the subject of the event.
     * @param eventType the type of the event, e.g. "Contoso.Items.ItemReceived".
     * @param data the data associated with this event. The content of this {@link BinaryData} must be a Json value.
     * @param dataVersion the version of the data sent along with the event.
     *
     * @throws NullPointerException if subject, eventType, data, or dataVersion is {@code null}.
     * @throws IllegalArgumentException if the content of data isn't a Json value.
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
     * Deserialize {@link EventGridEvent} JSON string representation that has one EventGridEvent object or
     * an array of CloudEvent objects into a list of EventGridEvents.
     *
     * <p><strong>Deserialize EventGridEvent Samples</strong></p>
     * {@codesnippet com.azure.messaging.eventgrid.EventGridEvent.fromString}
     *
     * @param eventGridJsonString the JSON string containing one or more EventGridEvent objects.
     *
     * @return A list of {@link EventGridEvent EventGridEvents} deserialized from eventGridJsonString.
     * @throws IllegalArgumentException if eventGridJsonString isn't a JSON string for a eventgrid event
     * or an array of it.
     * @throws NullPointerException if eventGridJsonString is {@code null}.
     * @throws IllegalArgumentException if the {eventGridJsonString isn't a Json string or can't be deserialized
     * into valid EventGridEvent instances.
     */
    public static List<EventGridEvent> fromString(String eventGridJsonString) {
        try {
            List<com.azure.messaging.eventgrid.implementation.models.EventGridEvent> internalEvents =
                SERIALIZER.deserialize(new ByteArrayInputStream(eventGridJsonString.getBytes(StandardCharsets.UTF_8)),
                DESERIALIZER_TYPE_REFERENCE);

            List<EventGridEvent> events = new ArrayList<EventGridEvent>();
            for (int i = 0; i < internalEvents.size(); i++) {
                com.azure.messaging.eventgrid.implementation.models.EventGridEvent internalEvent =
                    internalEvents.get(i);
                if (internalEvent.getSubject() == null || internalEvent.getEventType() == null
                    || internalEvent.getData() == null || internalEvent.getDataVersion() == null) {
                    List<String> nullAttributes = new ArrayList<String>();
                    if (internalEvent.getSubject() == null) {
                        nullAttributes.add("'subject'");
                    }
                    if (internalEvent.getEventType() == null) {
                        nullAttributes.add("'eventType'");
                    }
                    if (internalEvent.getData() == null) {
                        nullAttributes.add("'data'");
                    }
                    if (internalEvent.getDataVersion() == null) {
                        nullAttributes.add("'dataVersion'");
                    }
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "'subject', 'eventType', 'data' and 'dataVersion' are mandatory attributes for an "
                            + "EventGridEvent. This Json string doesn't have "
                            + String.join(",", nullAttributes)
                            + " for the object at index " + i
                            + ". Please make sure the input Json string has the required attributes"));
                }
                events.add(new EventGridEvent(internalEvent));
            }
            return events;
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
     * @throws NullPointerException if id is null.
     * @throws IllegalArgumentException if id is an empty String.
     */
    public EventGridEvent setId(String id) {
        Objects.requireNonNull(id, "'id' cannot be null.");
        if (id.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'id' cannot be empty."));
        }
        this.event.setId(id);
        return this;
    }

    /**
     * Get the topic associated with this event if it is associated with a domain.
     * @return the topic, or null if the topic is not set.
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
     * any objects by using {@link BinaryData#toObject(TypeReference)}.
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

    private static class JacksonSerializer implements JsonSerializer {
        private final SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();

        @Override
        public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
            try {
                return jacksonAdapter.deserialize(stream, typeReference.getJavaType(), SerializerEncoding.JSON);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
            }
        }

        @Override
        public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
            return Mono.defer(() -> Mono.fromCallable(() -> deserialize(stream, typeReference)));
        }

        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                jacksonAdapter.serialize(value, SerializerEncoding.JSON, stream);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
            }
        }

        @Override
        public Mono<Void> serializeAsync(OutputStream stream, Object value) {
            return Mono.fromRunnable(() -> serialize(stream, value));
        }
    }

}
