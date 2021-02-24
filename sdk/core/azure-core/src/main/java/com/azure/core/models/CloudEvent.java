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

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The CloudEvent model. This represents a cloud event as specified by the
 * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">Cloud Native Computing Foundation</a>
 */
@Fluent
public final class CloudEvent {
    private static final String SPEC_VERSION = "1.0";
    private static final JsonSerializer DESERIALIZER = JsonSerializerProviders.createInstance();
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
        "data"
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
     * Event data specific to the event type.
     */
    @JsonProperty(value = "data")
    private Object data;

    /*
     * Event data specific to the event type, encoded as a base64 string.
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
     *               for each distinct event.
     * @param type Type of event related to the originating occurrence.
     * @param data A {@link BinaryData} that wraps the original data, which can be a String, byte[], or model class.
     * @param format Set to {@link CloudEventDataFormat#BYTES} to serialize the data to base64 format, or
     *               {@link CloudEventDataFormat#JSON} to serialize the data to JSON.
     * @param dataContentType The content type of the data. It has no impact on how the data is serialized but tells
     *                        the event subscriber how to use the data. Typically the value is of MIME types such as
     *                        "application/json", "text/plain", "text/xml", "application/+avro", etc. It can be null.
     * @throws NullPointerException if source, type, data, or format is null.
     * @throws IllegalStateException if source isn't in a URI-formatted string.
     */
    public CloudEvent(String source, String type, BinaryData data, CloudEventDataFormat format, String dataContentType) {
        try {
            URI.create(source);
        } catch (NullPointerException npe) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'source' must not be null."));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'dataSchema' isn't a correct URI format",
                illegalArgumentException));
        }
        if (Objects.isNull(type)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'type' must not be null."));
        }
        if (Objects.isNull(data)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'data' must not be null."));
        }
        if (Objects.isNull(format)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'format' must not be null."));
        }
        this.source = source;
        this.type = type;
        if (CloudEventDataFormat.BYTES == format) {
            this.dataBase64 = Base64.getEncoder().encodeToString(data.toBytes());
        } else {
            this.data = data.toString();
        }
        this.dataContentType = dataContentType;
        this.id = UUID.randomUUID().toString();
        this.specVersion = CloudEvent.SPEC_VERSION;
    }

    /**
     * Deserialize the {@link CloudEvent} from a JSON string.
     * @param cloudEventsJson the JSON payload containing one or more events.
     *
     * @return all of the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws IllegalArgumentException if the input parameter isn't a JSON string for a cloud event or an array of it.
     */
    public static List<CloudEvent> fromString(String cloudEventsJson) {
        try {
            List<CloudEvent> events = Arrays.asList(DESERIALIZER.deserialize(
                new ByteArrayInputStream(cloudEventsJson.getBytes(StandardCharsets.UTF_8)),
                TypeReference.createInstance(CloudEvent[].class)));
            for (CloudEvent event : events) {
                if (event.getSource() == null || event.getType() == null) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "'source' and 'type' are mandatory attributes for a CloudEvent. "
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
        if (Objects.isNull(id)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("id cannot be null"));
        }
        if (id.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("id cannot be empty"));
        }
        this.id = id;
        return this;
    }

    /**
     * Get the URI source of the event.
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
                if (this.data instanceof String) {
                    this.binaryData = BinaryData.fromString((String) this.data);
                } else if (this.data instanceof byte[]) {
                    this.binaryData = BinaryData.fromBytes((byte[]) this.data);
                } else {
                    this.binaryData = BinaryData.fromObject(this.data);
                }
            } else if (this.dataBase64 != null) {
                this.binaryData = BinaryData.fromString(this.dataBase64);
            }
        }
        return this.binaryData;
    }

    /**
     * Set the data content type with this event.
     * @param dataContentType the data content type to set.
     * @return the cloud event itself.
     */
    public CloudEvent setDataContentType(String dataContentType) {
        this.dataContentType = dataContentType;
        return this;
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
     * @param dataSchema a URI identifying the schema of the data.
     *
     * @return the cloud event itself.
     */
    public CloudEvent setDataSchema(String dataSchema) {
        if (!Objects.isNull(dataSchema)) {
            try {
                URI.create(dataSchema);
            } catch (IllegalArgumentException illegalArgumentException) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'dataSchema' isn't a correct URI format",
                    illegalArgumentException));
            }
        }
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
     * @return the extension attributes as an unmodifiable map.
     */
    @JsonAnyGetter
    public Map<String, Object> getExtensionAttributes() {
        return this.extensionAttributes;
    }

    /**
     * Add/Overwrite a single extension attribute to the cloud event. The property name will be transformed
     * to lowercase and must not share a name with any reserved cloud event properties.
     * @param name the name of the attribute.
     * @param value the value to associate with the name.
     *
     * @return the cloud event itself.
     */
    @JsonAnySetter
    public CloudEvent addExtensionAttribute(String name, Object value) {
        if (!validateAttributeName(name)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'name' must have at most 20 alphanumeric characters and not be one of the CloudEvent attribute names"));
        }
        if (this.extensionAttributes == null) {
            this.extensionAttributes = new HashMap<>();
        }
        this.extensionAttributes.put(name.toLowerCase(Locale.ENGLISH), value);
        return this;
    }

    private static boolean validateAttributeName(String name) {
        if (name.length() > 20) {
            return false;
        }
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
