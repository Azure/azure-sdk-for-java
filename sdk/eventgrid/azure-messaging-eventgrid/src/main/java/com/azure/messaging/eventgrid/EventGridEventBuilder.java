package com.azure.messaging.eventgrid;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A builder to construct {@link EventGridEvent}s. Subject, event type, and data version
 * are all required to set for an event, others have defaults or are not always required based
 * on the event.
 * @see EventGridEvent
 */
public class EventGridEventBuilder {

    private final ClientLogger logger = new ClientLogger(EventGridEventBuilder.class);

    private String id;

    private String topic;

    private String subject;

    private Object data;

    private String eventType;

    private OffsetDateTime eventTime;

    private String dataVersion;

    /**
     * Construct an instance of the builder. No values are set.
     */
    public EventGridEventBuilder() {
    }

    /**
     * Create an EventGridEvent based on the set values.
     * @return an EventGridEvent constructed from the set values.
     */
    public EventGridEvent build() {
        if (subject == null) {
            throw logger.logExceptionAsError(new NullPointerException("subject cannot be null"));
        } else if (eventType == null) {
            throw logger.logExceptionAsError(new NullPointerException("event type cannot be null"));
        } else if (dataVersion == null) {
            throw logger.logExceptionAsError(new NullPointerException("data version cannot be null"));
        }

        String buildId = id != null ? id : UUID.randomUUID().toString();

        OffsetDateTime buildTime = eventTime != null ? eventTime : OffsetDateTime.now();

        com.azure.messaging.eventgrid.implementation.models.EventGridEvent result =
            new com.azure.messaging.eventgrid.implementation.models.EventGridEvent();

        result
            .setId(buildId)
            .setTopic(topic)
            .setSubject(subject)
            .setEventType(eventType)
            .setEventTime(buildTime)
            .setDataVersion(dataVersion);

        if (data == null) {
            // need to serialize as an empty value because the service requires it, however it could be null
            // if the event type needs no additional data.
            result.setData(new Object());
        } else {
            result.setData(data);
        }

        return new EventGridEvent(result);
    }

    /**
     * Set a unique id associated with this event. A default random id will be created for each event if this is
     * not set.
     * @param id the id to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder id(String id) {
        if (id.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("id cannot be empty"));
        }
        this.id = id;
        return this;
    }

    /**
     * Set the topic for this event if it is being sent to a domain.
     * @param topic the topic to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder topic(String topic) {
        if (topic.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("topic cannot be empty"));
        }
        this.topic = topic;
        return this;
    }

    /**
     * Set the subject for this event.
     * @param subject the subject to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder subject(String subject) {
        if (CoreUtils.isNullOrEmpty(subject)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("subject cannot be null or empty"));
        }
        this.subject = subject;
        return this;
    }

    /**
     * Set the data associated with this event.
     * @param data the data to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder data(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Set the type of event this is, e.g. "Contoso.Items.ItemReceived".
     * @param eventType the type to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder eventType(String eventType) {
        if (CoreUtils.isNullOrEmpty(eventType)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("event type cannot be null or empty"));
        }
        this.eventType = eventType;
        return this;
    }

    /**
     * Set the time associated with the occurrence of this event. If no time is set one will be provided
     * at the time of event building.
     * @param eventTime the time to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder eventTime(OffsetDateTime eventTime) {
        if (eventTime == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("time cannot be set as null"));
        }
        this.eventTime = eventTime;
        return this;
    }

    /**
     * Set the version of the specification that the data follows. This, along with the event type,
     * should correspond with a single set of fields for the data.
     * @param dataVersion the data version to set.
     *
     * @return the builder itself.
     */
    public EventGridEventBuilder dataVersion(String dataVersion) {
        if (CoreUtils.isNullOrEmpty(dataVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("data version cannot be null or empty"));
        }
        this.dataVersion = dataVersion;
        return this;
    }

}
