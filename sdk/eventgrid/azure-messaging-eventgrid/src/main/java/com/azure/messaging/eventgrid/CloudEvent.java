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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The CloudEvent model. This represents a cloud event as specified by the CNCF, for sending event based data.
 * @see EventGridPublisherAsyncClient
 * @see EventGridPublisherClient
 **/
@Fluent
public final class CloudEvent {

    private static final String SPEC_VERSION = "1.0";

    private final com.azure.messaging.eventgrid.implementation.models.CloudEvent cloudEvent;

    private static final ClientLogger logger = new ClientLogger(CloudEvent.class);

    private boolean parsed = false;

    private static final JsonSerializer deserializer = new JacksonJsonSerializerBuilder()
        .serializer(new JacksonAdapter().serializer() // this is a workaround to get the FlatteningDeserializer
            .registerModule(new JavaTimeModule())) // probably also change this to DateTimeDeserializer when/if it
        .build();                                  // becomes public in core

    /**
     * Create an instance of a CloudEvent. The source and type are required fields to publish.
     * @param source a URI identifying the origin of the event.
     * @param type   the type of event, e.g. "Contoso.Items.ItemReceived".
     */
    public CloudEvent(String source, String type) {
        if (CoreUtils.isNullOrEmpty(source)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Source cannot be null or empty"));
        } else if (CoreUtils.isNullOrEmpty(type)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("type cannot be null or empty"));
        }

        this.cloudEvent = new com.azure.messaging.eventgrid.implementation.models.CloudEvent()
            .setId(UUID.randomUUID().toString())
            .setSource(source)
            .setType(type)
            .setSpecversion(SPEC_VERSION);
    }

    /**
     * Parse the Cloud Event from a JSON string. This can be used to interpret the event at the event destination
     * from raw JSON into rich event(s).
     * @param json the JSON payload containing one or more events.
     *
     * @return all of the events in the payload parsed as CloudEvents.
     */
    public static List<CloudEvent> parse(String json) {
        return Flux.fromArray(deserializer
            .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(com.azure.messaging.eventgrid.implementation.models.CloudEvent[].class))
        )
            .map(event1 -> {
                if (event1.getData() != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    deserializer.serialize(stream, event1.getData());
                    return new CloudEvent(event1).setData(stream.toByteArray()); // use BinaryData instead?
                } else { // both null, don't set data and keep null
                    return new CloudEvent(event1);
                }
            })
            .collectList()
            .block();
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
     * Get the data associated with this event. For use in a parsed event only.
     * @return If the event was parsed from a Json, this method will return the rich
     * system event data if it is a system event, and a {@code byte[]} otherwise, such as in the case of binary event
     * data, including data set through {@link CloudEvent#setData(byte[], String)}.
     * @throws IllegalStateException If the event was not created through {@link EventGridEvent#parse(String)}.
     */
    public Object getData() {
        if (!parsed) {
            // data was set instead of parsed, throw error
            throw logger.logExceptionAsError(new IllegalStateException(
                "This method should only be called on events created through the parse method"));
        }
        if (cloudEvent.getDataBase64() != null) { // this means normal data is null
            return cloudEvent.getDataBase64();
        }
        String eventType = SystemEventMappings.canonicalizeEventType(cloudEvent.getType());
        if (SystemEventMappings.getSystemEventMappings().containsKey(eventType)) {
            // system event
            return deserializer.deserialize(new ByteArrayInputStream((byte[]) this.cloudEvent.getData()),
                TypeReference.createInstance(SystemEventMappings.getSystemEventMappings().get(eventType)));
        }
        return cloudEvent.getData();
    }

    /**
     * Get the deserialized data property from the parsed event. Note that this is only intended to work on
     * events with {@code application/json} data and has unspecified results on other media types, such as binary data.
     * @param clazz the class of the type to deserialize the data into, using a default deserializer.
     * @param <T>   the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using a default deserializer.
     * @throws IllegalStateException If the event was not created through {@link CloudEvent#parse(String)}.
     */
    public <T> T getData(Class<T> clazz) {
        return getDataAsync(clazz, deserializer).block();
    }

    /**
     * Get the deserialized data property from the parsed event. Note that this is only intended to work on
     * events with {@code application/json} data and has unspecified results on other media types, such as binary data.
     * @param clazz the class of the type to deserialize the data into, using a default deserializer.
     * @param <T>   the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using a default deserializer, wrapped asynchronously in a
     * {@link Mono}.
     * @throws IllegalStateException If the event was not created through {@link CloudEvent#parse(String)}.
     */
    public <T> Mono<T> getDataAsync(Class<T> clazz) {
        return getDataAsync(clazz, deserializer);
    }

    /**
     * Deserialize and get the data property from the parsed event. Note that this is only intended to work on
     * events with {@code application/json} data and has unspecified results on other media types.
     * @param clazz            the class of the type to deserialize the data into.
     * @param dataDeserializer the deserializer to use.
     * @param <T>              the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using the given deserializer.
     * @throws IllegalStateException If the event was not created through {@link CloudEvent#parse(String)}.
     */
    public <T> T getData(Class<T> clazz, JsonSerializer dataDeserializer) {
        return getDataAsync(clazz, dataDeserializer).block();
    }

    /**
     * Deserialize and get the data property from the parsed event. Note that this is only intended to work on
     * events with {@code application/json} data and has unspecified results on other media types.
     * @param clazz            the class of the type to deserialize the data into.
     * @param dataDeserializer the deserializer to use.
     * @param <T>              the type to deserialize the data into.
     *
     * @return the data deserialized into the given type using the given deserializer, wrapped asynchronously in a
     * {@link Mono}.
     * @throws IllegalStateException If the event was not created through {@link CloudEvent#parse(String)}.
     */
    public <T> Mono<T> getDataAsync(Class<T> clazz, JsonSerializer dataDeserializer) {
        if (!parsed) {
            // data was set instead of parsed, throw exception because we don't know how the data relates to clazz
            return FluxUtil.monoError(logger, new IllegalStateException(
                "This method should only be called on events created through the parse method"));
        }

        return dataDeserializer.deserializeAsync(new ByteArrayInputStream((byte[]) this.cloudEvent.getData()),
            TypeReference.createInstance(clazz));
    }

    /**
     * Set the data associated with this event.
     * @param data the data to set. Should be serializable with the serializer set on the publisher client.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setData(Object data) {
        this.cloudEvent.setData(data);
        return this;
    }

    /**
     * Set the data associated with this event, along with a content type URI
     * @param data            the data to set. Should be serializable by the serializer set on the publisher client.
     * @param dataContentType a URI identifying the MIME type of the data.
     *                        For example, if the data was an XML string, the data content type could be
     *                        {@code "application/xml"}.
     * @return the cloud event itself.
     */
    public CloudEvent setData(Object data, String dataContentType) {
        this.cloudEvent.setData(data);
        this.cloudEvent.setDatacontenttype(dataContentType);
        return this;
    }

    /**
     * Set byte data associated with this event, as well as the content type of the byte data. The data content
     * type should be a string identifying the media type of the data.
     * @param data            the data to set.
     * @param dataContentType the string identifying the media type of the byte data, as a MIME type. A null value will
     *                        be interpreted as the {@code application/json} type.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setData(byte[] data, String dataContentType) {
        this.cloudEvent
            .setDataBase64(data)
            .setDatacontenttype(dataContentType);
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
     * Get the spec version that this cloud event adheres to. Note that only CloudEvents spec version 1.0 is supported.
     * @return the cloud event spec version.
     */
    public String getSpecVersion() {
        return this.cloudEvent.getSpecversion();
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
     * Add/Overwrite a single extension attribute to the cloud event envelope. The property name will be transformed
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

    private CloudEvent(com.azure.messaging.eventgrid.implementation.models.CloudEvent impl) {
        this.cloudEvent = impl;
        this.parsed = true;
    }

    com.azure.messaging.eventgrid.implementation.models.CloudEvent toImpl() {
        return this.cloudEvent;
    }
}
