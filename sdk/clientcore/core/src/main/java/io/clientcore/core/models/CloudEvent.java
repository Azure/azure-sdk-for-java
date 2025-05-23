// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.utils.CoreUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the CloudEvent conforming to the 1.0 schema defined by the
 * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">Cloud Native Computing Foundation</a>.
 *
 * <p>
 * CloudEvents is a specification for describing event data in common formats to provide interoperability across
 * services, platforms and systems.
 * </p>
 * <p>
 * Depending on your scenario, you can either use the constructor
 * {@link #CloudEvent(String, String, BinaryData, CloudEventDataFormat, String)} to create a CloudEvent, or use the
 * factory method {@link #fromString(String)} to deserialize CloudEvent instances from a Json String representation of
 * CloudEvents.
 * </p>
 *
 * <p>
 * If you have the data payload of a CloudEvent and want to send it out, use the constructor
 * {@link #CloudEvent(String, String, BinaryData, CloudEventDataFormat, String)} to create it. Then you can serialize
 * the CloudEvent into its Json String representation and send it.
 * </p>
 *
 * <p>
 * <strong>Create CloudEvent Samples</strong>
 * </p>
 * <!-- src_embed io.clientcore.core.models.CloudEvent#constructor -->
 * <pre>
 * &#47;&#47; Use BinaryData.fromBytes&#40;&#41; to create data in format CloudEventDataFormat.BYTES
 * byte[] exampleBytes = &quot;Hello World&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
 * CloudEvent cloudEvent = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromBytes&#40;exampleBytes&#41;, CloudEventDataFormat.BYTES, &quot;application&#47;octet-stream&quot;&#41;;
 *
 * &#47;&#47; Use BinaryData.fromObject&#40;&#41; to create CloudEvent data in format CloudEventDataFormat.JSON
 * &#47;&#47; From a model class
 * User user = new User&#40;&quot;Stephen&quot;, &quot;James&quot;&#41;;
 * CloudEvent cloudEventDataObject = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;user&#41;, CloudEventDataFormat.JSON, &quot;application&#47;json&quot;&#41;;
 *
 * &#47;&#47; From a String
 * CloudEvent cloudEventDataStr = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;&quot;Hello World&quot;&#41;, CloudEventDataFormat.JSON, &quot;text&#47;plain&quot;&#41;;
 *
 * &#47;&#47; From an Integer
 * CloudEvent cloudEventDataInt = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;1&#41;, CloudEventDataFormat.JSON, &quot;int&quot;&#41;;
 *
 * &#47;&#47; From a Boolean
 * CloudEvent cloudEventDataBool = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;true&#41;, CloudEventDataFormat.JSON, &quot;bool&quot;&#41;;
 *
 * &#47;&#47; From null
 * CloudEvent cloudEventDataNull = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;null&#41;, CloudEventDataFormat.JSON, &quot;null&quot;&#41;;
 *
 * &#47;&#47; Use BinaryData.fromString&#40;&#41; if you have a Json String for the CloudEvent data.
 * String jsonStringForData = &quot;&#92;&quot;Hello World&#92;&quot;&quot;;  &#47;&#47; A json String.
 * CloudEvent cloudEventDataJsonStr = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromString&#40;jsonStringForData&#41;, CloudEventDataFormat.JSON, &quot;text&#47;plain&quot;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.models.CloudEvent#constructor -->
 *
 * <p>
 * On the contrary, if you receive CloudEvents and have the Json string representation of one or more of
 * CloudEvents, use {@link #fromString(String)} to deserialize them from the Json string.
 * </p>
 *
 * <p>
 * <strong>Deserialize CloudEvent Samples</strong>
 * </p>
 * <!-- src_embed io.clientcore.core.models.CloudEvent.fromString -->
 * <pre>
 * List&lt;CloudEvent&gt; cloudEventList = CloudEvent.fromString&#40;cloudEventJsonString&#41;;
 * CloudEvent cloudEvent = cloudEventList.get&#40;0&#41;;
 * BinaryData cloudEventData = cloudEvent.getData&#40;&#41;;
 *
 * byte[] bytesValue = cloudEventData.toBytes&#40;&#41;;  &#47;&#47; If data payload is in bytes &#40;data_base64 is not null&#41;.
 * User objectValue = cloudEventData.toObject&#40;User.class&#41;;  &#47;&#47; If data payload is a User object.
 * int intValue = cloudEventData.toObject&#40;Integer.class&#41;;  &#47;&#47; If data payload is an int.
 * boolean boolValue = cloudEventData.toObject&#40;Boolean.class&#41;;  &#47;&#47; If data payload is boolean.
 * String stringValue = cloudEventData.toObject&#40;String.class&#41;;  &#47;&#47; If data payload is String.
 * String jsonStringValue = cloudEventData.toString&#40;&#41;;  &#47;&#47; The data payload represented in Json String.
 * </pre>
 * <!-- end io.clientcore.core.models.CloudEvent.fromString -->
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class CloudEvent implements JsonSerializable<CloudEvent> {
    private static final String SPEC_VERSION = "1.0";

    private static final ClientLogger LOGGER = new ClientLogger(CloudEvent.class);
    private static final Set<String> RESERVED_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList("specversion", "id",
        "source", "type", "datacontenttype", "dataschema", "subject", "time", "data", "data_base64"));

    private static final String ILLEGAL_ATTRIBUTE_NAME_MESSAGE = "Extension attribute 'name' must have only lower-case "
        + "alphanumeric characters and not be one of the CloudEvent reserved attribute names: "
        + String.join(", ", RESERVED_ATTRIBUTE_NAMES);

    /*
     * An identifier for the event. The combination of id and source must be
     * unique for each distinct event.
     */
    private String id;

    /*
     * Identifies the context in which an event happened. The combination of id
     * and source must be unique for each distinct event.
     */
    private String source;

    /*
     * Event data specific to the event type. This is internal only for data serialization.
     */
    private String data;

    /*
     * Event data specific to the event type, encoded as a base64 string. This is internal only for
     * data_base64 serialization.
     */
    private String dataBase64;

    /*
     * Type of event related to the originating occurrence.
     */
    private String type;

    /*
     * The time (in UTC) the event was generated, in RFC3339 format.
     */
    private OffsetDateTime time;

    /*
     * The version of the CloudEvents specification which the event uses.
     */
    private String specVersion;

    /*
     * Identifies the schema that data adheres to.
     */
    private String dataSchema;

    /*
     * Content type of data value.
     */
    private String dataContentType;

    /*
     * This describes the subject of the event in the context of the event
     * producer (identified by source).
     */
    private String subject;

    private Map<String, Object> extensionAttributes;

    /*
     * Cache serialized data for getData()
     */
    private BinaryData binaryData;

    /**
     * Create an instance of {@link CloudEvent}.
     *
     * <p>{@code source}, {@code type}, {@code id}, and {@code specversion} are required attributes according to the
     * <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md">CNCF CloudEvent spec</a>.
     * You must set the {@code source} and {@code type} when using this constructor. For convenience, {@code id} and
     * {@code specversion} are automatically assigned. You can change the {@code id} by using {@link #setId(String)}
     * after you create a CloudEvent. But you can not change {@code specversion} because this class is specifically for
     * CloudEvent 1.0 schema.</p>
     *
     * <p>For the CloudEvent data payload, this constructor accepts {@code data} of {@link BinaryData} as the
     * CloudEvent payload. The {@code data} can be created from objects of type String, bytes, boolean, null, array or
     * other types. A CloudEvent will be serialized to its Json String representation to be sent out. Use param
     * {@code format} to indicate whether the {@code data} will be serialized as bytes, or Json. When
     * {@link CloudEventDataFormat#BYTES} is used, the data payload will be serialized to base64 bytes and stored in
     * attribute <em>data_base64</em> of the CloudEvent's Json representation. When {@link CloudEventDataFormat#JSON} is
     * used, the data payload will be serialized as Json data and stored in attribute <em>data</em> of the CloudEvent's
     * Json representation.</p>
     *
     * <p><strong>Create CloudEvent Samples</strong></p>
     * <!-- src_embed io.clientcore.core.models.CloudEvent#constructor -->
     * <pre>
     * &#47;&#47; Use BinaryData.fromBytes&#40;&#41; to create data in format CloudEventDataFormat.BYTES
     * byte[] exampleBytes = &quot;Hello World&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;;
     * CloudEvent cloudEvent = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromBytes&#40;exampleBytes&#41;, CloudEventDataFormat.BYTES, &quot;application&#47;octet-stream&quot;&#41;;
     *
     * &#47;&#47; Use BinaryData.fromObject&#40;&#41; to create CloudEvent data in format CloudEventDataFormat.JSON
     * &#47;&#47; From a model class
     * User user = new User&#40;&quot;Stephen&quot;, &quot;James&quot;&#41;;
     * CloudEvent cloudEventDataObject = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromObject&#40;user&#41;, CloudEventDataFormat.JSON, &quot;application&#47;json&quot;&#41;;
     *
     * &#47;&#47; From a String
     * CloudEvent cloudEventDataStr = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromObject&#40;&quot;Hello World&quot;&#41;, CloudEventDataFormat.JSON, &quot;text&#47;plain&quot;&#41;;
     *
     * &#47;&#47; From an Integer
     * CloudEvent cloudEventDataInt = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromObject&#40;1&#41;, CloudEventDataFormat.JSON, &quot;int&quot;&#41;;
     *
     * &#47;&#47; From a Boolean
     * CloudEvent cloudEventDataBool = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromObject&#40;true&#41;, CloudEventDataFormat.JSON, &quot;bool&quot;&#41;;
     *
     * &#47;&#47; From null
     * CloudEvent cloudEventDataNull = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromObject&#40;null&#41;, CloudEventDataFormat.JSON, &quot;null&quot;&#41;;
     *
     * &#47;&#47; Use BinaryData.fromString&#40;&#41; if you have a Json String for the CloudEvent data.
     * String jsonStringForData = &quot;&#92;&quot;Hello World&#92;&quot;&quot;;  &#47;&#47; A json String.
     * CloudEvent cloudEventDataJsonStr = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
     *     BinaryData.fromString&#40;jsonStringForData&#41;, CloudEventDataFormat.JSON, &quot;text&#47;plain&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.models.CloudEvent#constructor -->
     *
     * @param source Identifies the context in which an event happened. The combination of id and source must be unique
     * for each distinct event.
     * @param type Type of event related to the originating occurrence.
     * @param data A {@link BinaryData} that wraps the original data, which can be a String, byte[], or model class.
     * @param format Set to {@link CloudEventDataFormat#BYTES} to serialize the data to base64 format, or
     * {@link CloudEventDataFormat#JSON} to serialize the data to JSON value.
     * @param dataContentType The content type of the data. It has no impact on how the data is serialized but tells the
     * event subscriber how to use the data. Typically, the value is of MIME types such as "application/json",
     * "text/plain", "text/xml", "avro/binary", etc. It can be null.
     * @throws NullPointerException If source or type is null or format is null while data isn't null.
     * @throws IllegalArgumentException if format is {@link CloudEventDataFormat#JSON} but the data isn't in a correct
     * JSON format.
     */
    public CloudEvent(String source, String type, BinaryData data, CloudEventDataFormat format,
        String dataContentType) {
        this.source = Objects.requireNonNull(source, "'source' cannot be null.");
        this.type = Objects.requireNonNull(type, "'type' cannot be null.");

        if (data != null) {
            Objects.requireNonNull(format, "'format' cannot be null when 'data' isn't null.");
            if (CloudEventDataFormat.BYTES == format) {
                this.dataBase64 = Base64.getEncoder().encodeToString(data.toBytes());
            } else {
                try (JsonReader jsonReader = JsonReader.fromBytes(data.toBytes())) {
                    JsonToken token = jsonReader.nextToken();
                    if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                        this.data = jsonReader.readChildren();
                    } else if (token == JsonToken.STRING) {
                        this.data = "\"" + jsonReader.getString() + "\"";
                    } else {
                        this.data = jsonReader.getString();
                    }
                } catch (IOException e) {
                    throw LOGGER.throwableAtError()
                        .log("'data' isn't in valid Json format", e, IllegalArgumentException::new);
                }
            }
        }

        this.dataContentType = dataContentType;
        this.id = CoreUtils.randomUuid().toString();
        this.specVersion = CloudEvent.SPEC_VERSION;
        this.binaryData = data;
        this.time = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private CloudEvent() {
        // for deserialization
    }

    /**
     * Deserialize {@link CloudEvent} JSON string representation that has one CloudEvent object or an array of
     * CloudEvent objects into a list of CloudEvents, and validate whether any CloudEvents have null {@code id},
     * {@code source}, or {@code type}. If you want to skip this validation, use {@link #fromString(String, boolean)}.
     *
     * <p><strong>Deserialize CloudEvent Samples</strong></p>
     * <!-- src_embed io.clientcore.core.models.CloudEvent.fromString -->
     * <pre>
     * List&lt;CloudEvent&gt; cloudEventList = CloudEvent.fromString&#40;cloudEventJsonString&#41;;
     * CloudEvent cloudEvent = cloudEventList.get&#40;0&#41;;
     * BinaryData cloudEventData = cloudEvent.getData&#40;&#41;;
     *
     * byte[] bytesValue = cloudEventData.toBytes&#40;&#41;;  &#47;&#47; If data payload is in bytes &#40;data_base64 is not null&#41;.
     * User objectValue = cloudEventData.toObject&#40;User.class&#41;;  &#47;&#47; If data payload is a User object.
     * int intValue = cloudEventData.toObject&#40;Integer.class&#41;;  &#47;&#47; If data payload is an int.
     * boolean boolValue = cloudEventData.toObject&#40;Boolean.class&#41;;  &#47;&#47; If data payload is boolean.
     * String stringValue = cloudEventData.toObject&#40;String.class&#41;;  &#47;&#47; If data payload is String.
     * String jsonStringValue = cloudEventData.toString&#40;&#41;;  &#47;&#47; The data payload represented in Json String.
     * </pre>
     * <!-- end io.clientcore.core.models.CloudEvent.fromString -->
     *
     * @param cloudEventsJson the JSON payload containing one or more events.
     * @return all the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws NullPointerException if cloudEventsJson is null.
     * @throws IllegalArgumentException if the input parameter isn't a correct JSON string for a CloudEvent or an array
     * of CloudEvents, or any deserialized CloudEvents have null {@code id}, {@code source}, or {@code type}.
     */
    public static List<CloudEvent> fromString(String cloudEventsJson) {
        return fromString(cloudEventsJson, false);
    }

    /**
     * Deserialize {@link CloudEvent CloudEvents} JSON string representation that has one CloudEvent object or an array
     * of CloudEvent objects into a list of CloudEvents.
     *
     * @param cloudEventsJson the JSON payload containing one or more events.
     * @param skipValidation set to true if you'd like to skip the validation for the deserialized CloudEvents. A valid
     * CloudEvent should have 'id', 'source' and 'type' not null.
     * @return all the events in the payload deserialized as {@link CloudEvent CloudEvents}.
     * @throws NullPointerException if cloudEventsJson is null.
     * @throws IllegalArgumentException if the input parameter isn't a JSON string for a CloudEvent or an array of
     * CloudEvents, or skipValidation is false and any CloudEvents have null id', 'source', or 'type'.
     */
    public static List<CloudEvent> fromString(String cloudEventsJson, boolean skipValidation) {
        Objects.requireNonNull(cloudEventsJson, "'cloudEventsJson' cannot be null");

        List<CloudEvent> cloudEvents;
        try (JsonReader jsonReader = JsonReader.fromString(cloudEventsJson)) {
            JsonToken arrayOrObjectCheckToken = jsonReader.nextToken();
            if (arrayOrObjectCheckToken == JsonToken.START_OBJECT) {
                cloudEvents = new ArrayList<>(1);
                cloudEvents.add(jsonReader.readObject(CloudEvent::fromJson));
            } else if (arrayOrObjectCheckToken == JsonToken.START_ARRAY) {
                cloudEvents = jsonReader.readArray(CloudEvent::fromJson);
            } else if (arrayOrObjectCheckToken == JsonToken.NULL) {
                return null;
            } else {
                throw LOGGER.throwableAtError()
                    .log("JSON string started at an invalid state "
                        + "for reading a single instance or an array of CloudEvents. Starting token was: "
                        + arrayOrObjectCheckToken, IllegalArgumentException::new);
            }
        } catch (IOException ex) {
            throw LOGGER.throwableAtError().log(ex, CoreException::from);
        }

        if (skipValidation || cloudEvents == null) {
            return cloudEvents;
        }

        for (int i = 0; i < cloudEvents.size(); i++) {
            CloudEvent event = cloudEvents.get(i);
            if (event.getId() == null || event.getSource() == null || event.getType() == null) {
                List<String> nullAttributes = new ArrayList<>();
                if (event.getId() == null) {
                    nullAttributes.add("'id'");
                }
                if (event.getSource() == null) {
                    nullAttributes.add("'source'");
                }
                if (event.getType() == null) {
                    nullAttributes.add("'type'");
                }

                throw LOGGER.throwableAtError()
                    .log(
                        "'id', 'source' and 'type' are mandatory "
                            + "attributes for a CloudEvent according to the spec. This JSON string doesn't have "
                            + String.join(", ", nullAttributes) + " for the object at index " + i + ". Please make "
                            + "sure the input Json string has the required attributes or use "
                            + "CloudEvent.fromString(cloudEventsJson, true) to skip the null check.",
                        IllegalArgumentException::new);
            }
        }

        return cloudEvents;
    }

    /**
     * Get the id of the cloud event.
     *
     * @return the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set a custom id. Note that a random id is already set by default.
     *
     * @param id the id to set.
     * @return the cloud event itself.
     * @throws NullPointerException if id is null.
     * @throws IllegalArgumentException if id is empty.
     */
    public CloudEvent setId(String id) {
        Objects.requireNonNull(id, "'id' cannot be null");
        if (id.isEmpty()) {
            throw LOGGER.throwableAtError().log("'id' cannot be empty", IllegalArgumentException::new);
        }
        this.id = id;
        return this;
    }

    /**
     * Get the source of the event.
     *
     * @return the source.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Get the data associated with this event as a {@link BinaryData}, which has API to deserialize the data into a
     * String, an Object, or a byte[].
     *
     * @return A {@link BinaryData} that wraps the event's data payload.
     */
    public BinaryData getData() {
        if (this.binaryData == null) {
            if (this.data != null) {
                this.binaryData = BinaryData.fromString(this.data);
            } else if (this.dataBase64 != null) {
                this.binaryData = BinaryData.fromBytes(Base64.getDecoder().decode(this.dataBase64));
            }
        }
        return this.binaryData;
    }

    /**
     * Get the type of event, e.g. "Contoso.Items.ItemReceived".
     *
     * @return the type of the event.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the time associated with the occurrence of the event.
     *
     * @return the event time, or null if the time is not set.
     */
    public OffsetDateTime getTime() {
        return this.time;
    }

    /**
     * Set the time associated with the occurrence of the event.
     * <p>
     * At creation, the time is set to the current UTC time. It can be unset by setting it to null.
     *
     * @param time the time to set.
     * @return the cloud event itself.
     */
    public CloudEvent setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    /**
     * Get the content MIME type that the data is in.
     *
     * @return the content type the data is in, or null it is not set.
     */
    public String getDataContentType() {
        return this.dataContentType;
    }

    /**
     * Get the schema that the data adheres to.
     *
     * @return a URI of the data schema, or null if it is not set.
     */
    public String getDataSchema() {
        return this.dataSchema;
    }

    /**
     * Set the schema that the data adheres to.
     *
     * @param dataSchema a String identifying the schema of the data. The <a
     * href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md#dataschema"> CNCF CloudEvent spec dataschema</a> is
     * defined as a URI. For compatibility with legacy system, this class accepts any String. But for interoperability,
     * you should use a URI format string.
     * @return the cloud event itself.
     */
    public CloudEvent setDataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
        return this;
    }

    /**
     * Get the subject associated with this event.
     *
     * @return the subject, or null if it is not set.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Set the subject of the event.
     *
     * @param subject the subject to set.
     * @return the cloud event itself.
     */
    public CloudEvent setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get a map of the additional user-defined attributes associated with this event.
     *
     * @return an unmodifiable map of the extension attributes.
     */
    public Map<String, Object> getExtensionAttributes() {
        return this.extensionAttributes == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(this.extensionAttributes);
    }

    /**
     * Add/Overwrite a single extension attribute to the cloud event.
     *
     * @param name the name of the attribute. It must contain only lower-case alphanumeric characters and not be any
     * CloudEvent reserved attribute names.
     * @param value the value to associate with the name.
     * @return the cloud event itself.
     * @throws NullPointerException if name or value is null.
     * @throws IllegalArgumentException if name format isn't correct.
     */
    public CloudEvent addExtensionAttribute(String name, Object value) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");
        if (!validateAttributeName(name)) {
            throw LOGGER.throwableAtError().log(ILLEGAL_ATTRIBUTE_NAME_MESSAGE, IllegalArgumentException::new);
        }
        if (this.extensionAttributes == null) {
            this.extensionAttributes = new HashMap<>();
        }
        this.extensionAttributes.put(name, value);
        return this;
    }

    /**
     * Get the spec version. Users don't need to access it because it's always 1.0. Make it package level to test
     * deserialization.
     *
     * @return The spec version.
     */
    String getSpecVersion() {
        return this.specVersion;
    }

    /**
     * Set the spec version. Users don't need to access it because it's always 1.0. Make it package level to test
     * serialization.
     *
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject().writeStringField("id", id).writeStringField("source", source);

        if (dataBase64 != null) {
            jsonWriter.writeStringField("data_base64", dataBase64);
        } else if (data != null) {
            jsonWriter.writeRawField("data", data);
        } else {
            jsonWriter.writeNullField("data");
        }

        jsonWriter.writeStringField("type", type);

        if (time != null) {
            jsonWriter.writeStringField("time", time.toString());
        }

        jsonWriter.writeStringField("specversion", specVersion)
            .writeStringField("dataschema", dataSchema)
            .writeStringField("datacontenttype", dataContentType)
            .writeStringField("subject", subject);

        if (!CoreUtils.isNullOrEmpty(extensionAttributes)) {
            for (Map.Entry<String, Object> extensionAttribute : extensionAttributes.entrySet()) {
                jsonWriter.writeUntypedField(String.valueOf(extensionAttribute.getKey()),
                    extensionAttribute.getValue());
            }
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link CloudEvent}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link CloudEvent} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If a {@link CloudEvent} fails to be read from the {@code jsonReader}.
     */
    public static CloudEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CloudEvent cloudEvent = new CloudEvent();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                JsonToken token = reader.nextToken();

                if ("id".equals(fieldName)) {
                    cloudEvent.id = reader.getString();
                } else if ("source".equals(fieldName)) {
                    cloudEvent.source = reader.getString();
                } else if ("data".equals(fieldName)) {
                    if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                        cloudEvent.data = jsonReader.readChildren();
                    } else if (token == JsonToken.STRING) {
                        cloudEvent.data = "\"" + jsonReader.getRawText() + "\"";
                    } else {
                        cloudEvent.data = jsonReader.getString();
                    }
                } else if ("data_base64".equals(fieldName)) {
                    cloudEvent.dataBase64 = reader.getString();
                } else if ("type".equals(fieldName)) {
                    cloudEvent.type = reader.getString();
                } else if ("time".equals(fieldName)) {
                    cloudEvent.time = reader.getNullable(r -> OffsetDateTime.parse(r.getString()));
                } else if ("specversion".equals(fieldName)) {
                    cloudEvent.specVersion = reader.getString();
                } else if ("dataschema".equals(fieldName)) {
                    cloudEvent.dataSchema = reader.getString();
                } else if ("datacontenttype".equals(fieldName)) {
                    cloudEvent.dataContentType = reader.getString();
                } else if ("subject".equals(fieldName)) {
                    cloudEvent.subject = reader.getString();
                } else {
                    if (cloudEvent.extensionAttributes == null) {
                        cloudEvent.extensionAttributes = new LinkedHashMap<>();
                    }

                    cloudEvent.extensionAttributes.put(fieldName, reader.readUntyped());
                }
            }

            return cloudEvent;
        });
    }
}
