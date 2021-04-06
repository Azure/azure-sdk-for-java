// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 *
 * Represents the CloudEvent conforming to the 1.0 schema defined by the
 * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">Cloud Native Computing Foundation</a>.
 * <q>
 * CloudEvents is a specification for describing event data in common formats to provide interoperability across
 * services, platforms and systems.
 * </q>
 *
 * <p>Some Azure services, for instance, EventGrid, are compatible with this specification. You can use this class to
 * communicate with these Azure services.</p>
 * <p>Depending on your scenario, you can either use the constructor
 * {@link #CloudEvent(String, String, BinaryData, CloudEventDataFormat, String)} to
 * create a CloudEvent, or use the factory method {@link #fromString(String)} to deserialize CloudEvent instances
 * from a Json String representation of CloudEvents.</p>
 *
 * <p>If you have the data payload of a CloudEvent and want to send it out, use the constructor
 * {@link #CloudEvent(String, String, BinaryData, CloudEventDataFormat, String)} to create it. Then you can
 * serialize the CloudEvent into its Json String representation and send it.</p>
 *
 * <p><strong>Create CloudEvent Samples</strong></p>
 * {@codesnippet com.azure.core.model.CloudEvent#constructor}
 *
 * <p>On the contrary, if you receive CloudEvents and have the Json string representation of one or more of CloudEvents,
 * use {@link #fromString(String)} to deserialize them from the Json string.</p>
 *
 * <p><strong>Deserialize CloudEvent Samples</strong></p>
 * {@codesnippet com.azure.core.model.CloudEvent.fromString}
 */
@Fluent
public final class CloudEvent {
    private static final String SPEC_VERSION = "1.0";

    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    // May get SERIALIZER's object mapper in the future.
    private static final ObjectMapper BINARY_DATA_OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, Object> EMPTY_ATTRIBUTES_MAP = Collections.emptyMap();

    private static final TypeReference<List<CloudEvent>> DESERIALIZER_TYPE_REFERENCE =
        new TypeReference<List<CloudEvent>>() {
        };
    private static final ClientLogger LOGGER = new ClientLogger(CloudEvent.class);
    private static final Set<String> RESERVED_ATTRIBUTE_NAMES = new HashSet<String>() {{
            add("specversion");
            add("id");
            add("source");
            add("type");
            add("datacontenttype");
            add("dataschema");
            add("subject");
            add("time");
            add("data");
            add("data_base64");
        }};

    /*
     * An identifier for the event. The combination of id and source must be
     * unique for each distinct event.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * Identifies the context in which an event happened. The combination of id
     * and source must be unique for each distinct event.
     */
    @JsonProperty(value = "source", required = true)
    private String source;

    /*
     * Event data specific to the event type. This is internal only for data serialization.
     */
    @JsonProperty(value = "data")
    private JsonNode data;

    /*
     * Event data specific to the event type, encoded as a base64 string. This is internal only for
     * data_base64 serialization.
     */
    @JsonProperty(value = "data_base64")
    private String dataBase64;

    /*
     * Type of event related to the originating occurrence.
     */
    @JsonProperty(value = "type", required = true)
    private String type;

    /*
     * The time (in UTC) the event was generated, in RFC3339 format.
     */
    @JsonProperty(value = "time")
    private OffsetDateTime time;

    /*
     * The version of the CloudEvents specification which the event uses.
     */
    @JsonProperty(value = "specversion", required = true)
    private String specVersion;

    /*
     * Identifies the schema that data adheres to.
     */
    @JsonProperty(value = "dataschema")
    private String dataSchema;

    /*
     * Content type of data value.
     */
    @JsonProperty(value = "datacontenttype")
    private String dataContentType;

    /*
     * This describes the subject of the event in the context of the event
     * producer (identified by source).
     */
    @JsonProperty(value = "subject")
    private String subject;

    @JsonIgnore
    private Map<String, Object> extensionAttributes;

    /*
     * Cache serialized data for getData()
     */
    @JsonIgnore
    private BinaryData binaryData;



    /**
     * Create an instance of {@link CloudEvent}.
     * <p>{@code source}, {@code type}, {@code id}, and {@code specversion} are required attributes according to the
     * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">CNCF CloudEvent spec</a>.
     * You must set the {@code source} and {@code type} when using this constructor.
     * For convenience, {@code id} and {@code specversion} are automatically assigned. You can change the {@code id}
     * by using {@link #setId(String)} after you create a CloudEvent. But you can not change {@code specversion}
     * because this class is specifically for CloudEvent 1.0 schema.</p>
     *
     * <p>For the CloudEvent data payload, this constructor accepts {@code data} of {@link BinaryData} as the CloudEvent payload.
     * The {@code data} can be created from objects of type String, bytes, boolean, null, array or other types.
     * A CloudEvent will be serialized to its Json String representation
     * to be sent out. Use param {@code format} to indicate whether the {@code data} will be serialized as
     * bytes, or Json. When {@link CloudEventDataFormat#BYTES} is used, the data payload will be serialized to base64
     * bytes and stored in attribute <em>data_base64</em> of the CloudEvent's Json representation. When
     * {@link CloudEventDataFormat#JSON} is used, the data payload will be serialized as Json data and stored in
     * attribute <em>data</em> of the CloudEvent's Json representation.</p>
     *
     * <p><strong>Create CloudEvent Samples</strong></p>
     * {@codesnippet com.azure.core.model.CloudEvent#constructor}
     *
     * @param source Identifies the context in which an event happened. The combination of id and source must be unique
     *               for each distinct event.
     * @param type Type of event related to the originating occurrence.
     * @param data A {@link BinaryData} that wraps the original data, which can be a String, byte[], or model class.
     * @param format Set to {@link CloudEventDataFormat#BYTES} to serialize the data to base64 format, or
     *               {@link CloudEventDataFormat#JSON} to serialize the data to JSON value.
     * @param dataContentType The content type of the data. It has no impact on how the data is serialized but tells
     *                        the event subscriber how to use the data. Typically the value is of MIME types such as
     *                        "application/json", "text/plain", "text/xml", "avro/binary", etc. It can be null.
     * @throws NullPointerException if source, type is null, or format is null while data isn't null.
     * @throws IllegalArgumentException if format is {@link CloudEventDataFormat#JSON} but the data isn't in a correct
     * JSON format.
     */
    public CloudEvent(String source, String type, BinaryData data, CloudEventDataFormat format, String dataContentType) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        Objects.requireNonNull(type, "'type' cannot be null.");
        this.source = source;
        this.type = type;
        if (data != null) {
            Objects.requireNonNull(format, "'format' cannot be null when 'data' isn't null.");
            if (CloudEventDataFormat.BYTES == format) {
                this.dataBase64 = Base64.getEncoder().encodeToString(data.toBytes());
            } else {
                try {
                    this.data = BINARY_DATA_OBJECT_MAPPER.readTree(data.toBytes());
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException("'data' isn't in valid Json format",
                        e));
                }
            }
        }
        this.dataContentType = dataContentType;
        this.id = UUID.randomUUID().toString();
        this.specVersion = CloudEvent.SPEC_VERSION;
        this.binaryData = data;
    }

    private CloudEvent() {
        // for deserialization
    }

    /**
     * Deserialize {@link CloudEvent} JSON string representation that has one CloudEvent object or
     * an array of CloudEvent objects into a list of CloudEvents, and validate whether any CloudEvents have
     * null {@code id}, {@code source}, or {@code type}. If you want to skip this validation, use {@link #fromString(String, boolean)}.
     *
     * <p><strong>Deserialize CloudEvent Samples</strong></p>
     * {@codesnippet com.azure.core.model.CloudEvent.fromString}
     *
     * @param cloudEventsJson the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws NullPointerException if cloudEventsJson is null.
     * @throws IllegalArgumentException if the input parameter isn't a correct JSON string for a CloudEvent
     * or an array of CloudEvents, or any deserialized CloudEvents have null {@code id}, {@code source}, or {@code type}.
     */
    public static List<CloudEvent> fromString(String cloudEventsJson) {
        return fromString(cloudEventsJson, false);
    }

    /**
     * Deserialize {@link CloudEvent CloudEvents} JSON string representation that has one CloudEvent object or
     * an array of CloudEvent objects into a list of CloudEvents.
     *
     * @param cloudEventsJson the JSON payload containing one or more events.
     * @param skipValidation set to true if you'd like to skip the validation for the deserialized CloudEvents. A valid
     *                       CloudEvent should have 'id', 'source' and 'type' not null.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws NullPointerException if cloudEventsJson is null.
     * @throws IllegalArgumentException if the input parameter isn't a JSON string for a CloudEvent or an array of
     * CloudEvents, or skipValidation is false and any CloudEvents have null id', 'source', or 'type'.
     */
    public static List<CloudEvent> fromString(String cloudEventsJson, boolean skipValidation) {
        Objects.requireNonNull(cloudEventsJson, "'cloudEventsJson' cannot be null");
        try {
            List<CloudEvent> events = SERIALIZER.deserialize(
                new ByteArrayInputStream(cloudEventsJson.getBytes(StandardCharsets.UTF_8)),
                DESERIALIZER_TYPE_REFERENCE);
            if (skipValidation) {
                return events;
            }
            for (int i = 0; i < events.size(); i++) {
                CloudEvent event = events.get(i);
                if (event.getId() == null || event.getSource() == null || event.getType() == null) {
                    List<String> nullAttributes = new ArrayList<String>();
                    if (event.getId() == null) {
                        nullAttributes.add("'id'");
                    }
                    if (event.getSource() == null) {
                        nullAttributes.add("'source'");
                    }
                    if (event.getType() == null) {
                        nullAttributes.add("'type'");
                    }
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "'id', 'source' and 'type' are mandatory attributes for a CloudEvent according to the spec."
                            + " This Json string doesn't have " + String.join(",", nullAttributes)
                            + " for the object at index " + i
                            + ". Please make sure the input Json string has the required attributes"
                            + " or use CloudEvent.fromString(cloudEventsJson, true) to skip the null check."));
                }
            }
            return events;
        } catch (UncheckedIOException uncheckedIOException) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The input parameter isn't a JSON string.",
                uncheckedIOException.getCause()));
        }
    }

    /**
     * Get the id of the cloud event.
     * @return the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set a custom id. Note that a random id is already set by default.
     * @param id the id to set.
     *
     * @return the cloud event itself.
     * @throws NullPointerException if id is null.
     * @throws IllegalArgumentException if id is empty.
     */
    public CloudEvent setId(String id) {
        Objects.requireNonNull(id, "'id' cannot be null");
        if (id.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'id' cannot be empty"));
        }
        this.id = id;
        return this;
    }

    /**
     * Get the source of the event.
     * @return the source.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Get the data associated with this event as a {@link BinaryData}, which has API to deserialize the data into
     * a String, an Object, or a byte[].
     *
     * @return A {@link BinaryData} that wraps the this event's data payload.
     */
    public BinaryData getData() {
        if (this.binaryData == null) {
            if (this.data != null) {
                this.binaryData = BinaryData.fromObject(this.data, SERIALIZER);
            } else if (this.dataBase64 != null) {
                this.binaryData = BinaryData.fromBytes(Base64.getDecoder().decode(this.dataBase64));
            }
        }
        return this.binaryData;
    }

    /**
     * Get the type of event, e.g. "Contoso.Items.ItemReceived".
     * @return the type of the event.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the time associated with the occurrence of the event.
     * @return the event time, or null if the time is not set.
     */
    public OffsetDateTime getTime() {
        return this.time;
    }

    /**
     * Set the time associated with the occurrence of the event.
     * @param time the time to set.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    /**
     * Get the content MIME type that the data is in.
     * @return the content type the data is in, or null it is not set.
     */
    public String getDataContentType() {
        return this.dataContentType;
    }

    /**
     * Get the schema that the data adheres to.
     * @return a URI of the data schema, or null if it is not set.
     */
    public String getDataSchema() {
        return this.dataSchema;
    }

    /**
     * Set the schema that the data adheres to.
     * @param dataSchema a String identifying the schema of the data. The <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md#dataschema">
     *                   CNCF CloudEvent spec dataschema</a> is defined as a URI. For compatibility with legacy system, this class
     *                   accepts any String. But for interoperability, you should use a URI format string.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setDataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
        return this;
    }

    /**
     * Get the subject associated with this event.
     * @return the subject, or null if it is not set.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Set the subject of the event.
     * @param subject the subject to set.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get a map of the additional user-defined attributes associated with this event.
     * @return an unmodifiable map of the extension attributes.
     */
    @JsonAnyGetter
    public Map<String, Object> getExtensionAttributes() {
        return this.extensionAttributes == null
            ? EMPTY_ATTRIBUTES_MAP : Collections.unmodifiableMap(this.extensionAttributes);
    }

    /**
     * Add/Overwrite a single extension attribute to the cloud event.
     * @param name the name of the attribute. It must contains only lower-case alphanumeric characters and not be be any
     *             CloudEvent reserved attribute names.
     * @param value the value to associate with the name.
     *
     * @return the cloud event itself.
     * @throws NullPointerException if name or value is null.
     * @throws IllegalArgumentException if name format isn't correct.
     */
    @JsonAnySetter
    public CloudEvent addExtensionAttribute(String name, Object value) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");
        if (!validateAttributeName(name)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Extension attribute 'name' must have only lower-case alphanumeric characters and not be one of the "
                    + "CloudEvent reserved attribute names: " + String.join(",", RESERVED_ATTRIBUTE_NAMES)));
        }
        if (this.extensionAttributes == null) {
            this.extensionAttributes = new HashMap<>();
        }
        this.extensionAttributes.put(name, value);
        return this;
    }

    /**
     * Get the spec version. Users don't need to access it because it's always 1.0.
     * Make it package level to test deserialization.
     * @return The spec version.
     */
    String getSpecVersion() {
        return this.specVersion;
    }

    /**
     * Set the spec version. Users don't need to access it because it's always 1.0.
     * Make it package level to test serialization.
     * @return the cloud event itself.
     */
    CloudEvent setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
        return this;
    }

    private static boolean validateAttributeName(String name) {
        if (RESERVED_ATTRIBUTE_NAMES.contains(name)) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return true;
    }
}
