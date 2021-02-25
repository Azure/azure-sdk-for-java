// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The EventGridEvent model. This represents events in the EventGrid schema to be used with the EventGrid service.
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

    private static final ClientLogger logger = new ClientLogger(EventGridEvent.class);

    /**
     * Create a new instance of the EventGridEvent, with the given required fields.
     * @param subject     the subject of the event.
     * @param eventType   the type of the event, e.g. "Contoso.Items.ItemReceived".
     * @param data        the data associated with this event.
     * @param dataVersion the version of the data sent along with the event.
     *
     * @throws IllegalArgumentException if subject, eventType or data is {@code null} or empty.
     */
    public EventGridEvent(String subject, String eventType, Object data, String dataVersion) {
        if (CoreUtils.isNullOrEmpty(subject)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'subject' cannot be null or empty."));
        } else if (CoreUtils.isNullOrEmpty(eventType)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventType' cannot be null or empty."));
        } else if (CoreUtils.isNullOrEmpty(dataVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'dataVersion' cannot be null or empty."));
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
     * Deserialize the {@link EventGridEvent} from a JSON string.
     * @param eventGridJsonString the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link EventGridEvent EventGridEvents}.
     * @throws IllegalArgumentException if eventGridJsonString isn't a JSON string for a eventgrid event
     * or an array of it.
     * @throws NullPointerException if eventGridJsonString is {@code null}.
     */
    public static List<EventGridEvent> fromString(String eventGridJsonString) {
        return EventGridDeserializer.deserializeEventGridEvents(eventGridJsonString);
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
            throw logger.logExceptionAsError(new IllegalArgumentException("'id' cannot be null or empty."));
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
     * Get the data associated with this event as a {@link BinaryData}, which has API to deserialize the data into
     * a String, an Object, or a byte[].
     * @return A {@link BinaryData} that wraps the this event's data payload.
     */
    public BinaryData getData() {
        return EventGridDeserializer.getData(event.getData());
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
