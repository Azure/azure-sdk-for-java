// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The CloudEvent model. This represents a cloud event as specified by the
 * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">Cloud Native Computing Foundation</a>
 *
 * When you send a CloudEvent to an Event Grid Topic, the topic must be configured to receive the CloudEvent schema.
 *
 * For new customers, CloudEvent is generally preferred over {@link EventGridEvent} because the
 *
 * <a href="https://docs.microsoft.com/azure/event-grid/cloud-event-schema">CloudEvent schema</a> is supported across
 * organizations while the <a href="https://docs.microsoft.com/azure/event-grid/event-schema">EventGridEvent schema</a> is not.
 *
 * @see EventGridPublisherAsyncClient to send cloud events asynchronously.
 * @see EventGridPublisherClient to send cloud events.
 **/
@Fluent
public final class CloudEvent {

    private static final String SPEC_VERSION = "1.0";

    private final com.azure.messaging.eventgrid.implementation.models.CloudEvent cloudEvent;

    private static final ClientLogger logger = new ClientLogger(CloudEvent.class);

    /**
     * Create an instance of CloudEvent. The source and type are required fields to publish.
     * @param source a URI identifying the origin of the event. It can't be null or empty.
     * @param type   the type of event, e.g. "Contoso.Items.ItemReceived". It can't be null or empty.
     * @param data the payload of this event. Set to null if your event doesn't have the data payload.
     *             It will be serialized as a String if it's a String, or application/json if it's not a String.
     * @throws NullPointerException if source or type is {@code null}.
     */
    public CloudEvent(String source, String type, Object data) {
        this(source, type);
        this.setData(data);
    }

    /**
     * Create an instance of CloudEvent. The source and type are required fields to publish.
     * @param source a URI identifying the origin of the event.
     * @param type   the type of event, e.g. "Contoso.Items.ItemReceived".
     * @param data the payload in bytes of this event. It will be serialized to Base64 format.
     * @param dataContentType the type of the data.
     * @throws NullPointerException if source or type is {@code null}.
     */
    public CloudEvent(String source, String type, byte[] data, String dataContentType) {
        this(source, type);
        this.setDataBase64(data, dataContentType);
    }

    private CloudEvent(String source, String type) {
        if (CoreUtils.isNullOrEmpty(source)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'source' cannot be null or empty."));
        }
        if (CoreUtils.isNullOrEmpty(type)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'type' cannot be null or empty."));
        }

        this.cloudEvent = new com.azure.messaging.eventgrid.implementation.models.CloudEvent()
            .setId(UUID.randomUUID().toString())
            .setSource(source)
            .setType(type)
            .setSpecversion(SPEC_VERSION);
    }
    /**
     * Deserialize the {@link CloudEvent} from a JSON string.
     * @param cloudEventJsonString the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws IllegalArgumentException if cloudEventJsonString isn't a JSON string for a cloud event or an array of it.
     * @throws NullPointerException if cloudEventJsonString is {@code null}.
     */
    public static List<CloudEvent> fromString(String cloudEventJsonString) {
        return EventGridDeserializer.deserializeCloudEvents(cloudEventJsonString);
    }

    /**
     * Get the id of the cloud event.
     * @return the id.
     */
    public String getId() {
        return this.cloudEvent.getId();
    }

    /**
     * Set a custom id. Note that a random id is already set by default.
     * @param id the id to set.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setId(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        this.cloudEvent.setId(id);
        return this;
    }

    /**
     * Get the URI source of the event.
     * @return the source.
     */
    public String getSource() {
        return this.cloudEvent.getSource();
    }

    /**
     * Get the data associated with this event as a {@link BinaryData}, which has API to deserialize the data into
     * a String, an Object, or a byte[].
     * @return A {@link BinaryData} that wraps the this event's data payload.
     */
    public BinaryData getData() {
        if (cloudEvent.getDataBase64() != null) {
            return BinaryData.fromBytes(cloudEvent.getDataBase64());
        }
        return EventGridDeserializer.getData(cloudEvent.getData());
    }

    /**
     * Set the data associated with this event.
     * @param data the data to set.
     *
     * @return the cloud event itself.
     */
    CloudEvent setData(Object data) {
        this.cloudEvent.setData(data);
        return this;
    }

    /**
     * Set the Base64 data associated with this event.
     * @param data the data to set.
     * @param dataContentType the data content type of the CloudEvent.
     *
     * @return the cloud event itself.
     */
    private CloudEvent setDataBase64(byte[] data, String dataContentType) {
        if (data != null) {
            byte[] encoded = Base64.getEncoder().encode(data);
            this.cloudEvent.setDataBase64(encoded);
            this.cloudEvent.setDatacontenttype(dataContentType);
        }
        return this;
    }

    /**
     * Set the data content type with this event.
     * @param dataContentType the data content type to set.
     * @return the cloud event itself.
     */
    public CloudEvent setDataContentType(String dataContentType) {
        this.cloudEvent.setDatacontenttype(dataContentType);
        return this;
    }

    /**
     * Get the type of event, e.g. "Contoso.Items.ItemReceived".
     * @return the type of the event.
     */
    public String getType() {
        return this.cloudEvent.getType();
    }

    /**
     * Get the time associated with the occurrence of the event.
     * @return the event time, or null if the time is not set.
     */
    public OffsetDateTime getTime() {
        return this.cloudEvent.getTime();
    }

    /**
     * Set the time associated with the occurrence of the event.
     * @param time the time to set.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setTime(OffsetDateTime time) {
        this.cloudEvent.setTime(time);
        return this;
    }

    /**
     * Get the content MIME type that the data is in. A null value indicates that the data is either nonexistent or in the
     * "application/json" type. Note that "application/json" is still a possible value for this field.
     * @return the content type the data is in, or null if the data is nonexistent or in "application/json" format.
     */
    public String getDataContentType() {
        return this.cloudEvent.getDatacontenttype();
    }

    /**
     * Get the schema that the data adheres to.
     * @return a URI of the data schema, or null if it is not set.
     */
    public String getDataSchema() {
        return this.cloudEvent.getDataschema();
    }

    /**
     * Set the schema that the data adheres to.
     * @param dataSchema a URI identifying the schema of the data.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setDataSchema(String dataSchema) {
        this.cloudEvent.setDataschema(dataSchema);
        return this;
    }

    /**
     * Get the subject associated with this event.
     * @return the subject, or null if the subject was not set.
     */
    public String getSubject() {
        return this.cloudEvent.getSubject();
    }

    /**
     * Set the subject of the event.
     * @param subject the subject to set.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setSubject(String subject) {
        this.cloudEvent.setSubject(subject);
        return this;
    }

    /**
     * Get a map of the additional user-defined attributes associated with this event.
     * @return the extension attributes as an unmodifiable map.
     */
    public Map<String, Object> getExtensionAttributes() {
        if (this.cloudEvent.getAdditionalProperties() == null) {
            return null;
        }
        return Collections.unmodifiableMap(this.cloudEvent.getAdditionalProperties());
    }

    /**
     * Add/Overwrite a single extension attribute to the cloud event. The property name will be transformed
     * to lowercase and must not share a name with any reserved cloud event properties.
     * @param name  the name of the attribute.
     * @param value the value to associate with the name.
     *
     * @return the cloud event itself.
     */
    public CloudEvent addExtensionAttribute(String name, Object value) {
        if (this.cloudEvent.getAdditionalProperties() == null) {
            this.cloudEvent.setAdditionalProperties(new HashMap<>());
        }
        this.cloudEvent.getAdditionalProperties().put(name.toLowerCase(Locale.ENGLISH), value);
        return this;
    }

    CloudEvent(com.azure.messaging.eventgrid.implementation.models.CloudEvent impl) {
        this.cloudEvent = impl;
    }

    com.azure.messaging.eventgrid.implementation.models.CloudEvent toImpl() {
        return this.cloudEvent;
    }
}
