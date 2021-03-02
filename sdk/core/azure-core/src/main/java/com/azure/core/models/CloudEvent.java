// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.serializer.JacksonSerializer;
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
import java.util.Arrays;
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
 * Represents the CloudEvent model defined by the
 * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">Cloud Native Computing Foundation</a>
 */
@Fluent
public final class CloudEvent {
    private static final String SPEC_VERSION = "1.0";
    private static final JsonSerializer SERIALIZER;
    // May get SERIALIZER's object mapper in the future.
    private static final ObjectMapper BINARY_DATA_OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, Object> EMPTY_ATTRIBUTES_MAP = Collections.unmodifiableMap(
        new HashMap<String, Object>());

    static {
        JsonSerializer tmp;
        try {
            tmp = JsonSerializerProviders.createInstance();
        } catch (IllegalStateException e) {
            tmp = new JacksonSerializer();
        }
        SERIALIZER = tmp;
    }

    private static final TypeReference<List<CloudEvent>> DESERIALIZER_TYPE_REFERENCE =
        new TypeReference<List<CloudEvent>>() {
        };
    private static final ClientLogger LOGGER = new ClientLogger(CloudEvent.class);
    private static final Set<String> RESERVED_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList(
        "specversion",
        "id",
        "source",
        "type",
        "datacontenttype",
        "dataschema",
        "subject",
        "time",
        "data",
        "data_base64"
    ));

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
     *
     * @param source Identifies the context in which an event happened. The combination of id and source must be unique
     *               for each distinct event. It should be in format URI-reference according to <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md#source-1">
     *                   CNCF CloudEvent spec source</a>
     *               even though this class accepts any String for compatibility with legacy systems.
     * @param type Type of event related to the originating occurrence.
     * @param data A {@link BinaryData} that wraps the original data, which can be a String, byte[], or model class.
     * @param format Set to {@link CloudEventDataFormat#BYTES} to serialize the data to base64 format, or
     *               {@link CloudEventDataFormat#JSON} to serialize the data to JSON.
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
     * Deserialize a list of {@link CloudEvent CloudEvents} from a JSON string and validate whether any CloudEvents have
     * null id', 'source', or 'type'. If you want to skip this validation, use {@link #fromString(String, boolean)}.
     * @param cloudEventsJson the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws NullPointerException if cloudEventsJson is null.
     * @throws IllegalArgumentException if the input parameter isn't a correct JSON string for a cloud event
     * or an array of it, or any deserialized CloudEvents have null 'id', 'source', or 'type'.
     */
    public static List<CloudEvent> fromString(String cloudEventsJson) {
        return fromString(cloudEventsJson, false);
    }

    /**
     * Deserialize a list of {@link CloudEvent CloudEvents} from a JSON string.
     * @param cloudEventsJson the JSON payload containing one or more events.
     * @param skipValidation set to true if you'd like to skip the validation for the deserialized CloudEvents. A valid
     *                       CloudEvent should have 'id', 'source' and 'type' not null.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws NullPointerException if cloudEventsJson is null.
     * @throws IllegalArgumentException if the input parameter isn't a JSON string for a cloud event or an array of it,
     * or skipValidation is false and any CloudEvents have null id', 'source', or 'type'.
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
            for (CloudEvent event : events) {
                if (event.getId() == null || event.getSource() == null || event.getType() == null) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "'id', 'source' and 'type' are mandatory attributes for a CloudEvent. "
                            + "Check if the input param is a JSON string for a CloudEvent or an array of it."));
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
     * Get the content MIME type that the data is in. A null value indicates that the data is either nonexistent or in the
     * "application/json" type. Note that "application/json" is still a possible value for this field.
     * @return the content type the data is in, or null if the data is nonexistent or in "application/json" format.
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
     *                   accepts any String. But for interoperability, you should use a URI string.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setDataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
        return this;
    }

    /**
     * Get the subject associated with this event.
     * @return the subject, or null if the subject was not set.
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
