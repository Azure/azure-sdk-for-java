package com.azure.messaging.eventgrid.events;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A builder to construct {@link CloudEvent}s. The source and type are required values to set.
 * All other fields have defaults or are optional based on the purpose of the event.
 * @see CloudEvent
 */
public class CloudEventBuilder {

    private static final String SPEC_VERSION = "1.0";

    private final ClientLogger logger = new ClientLogger(CloudEventBuilder.class);

    private String id;

    private String source;

    // flag to know whether to use binary (base64) or object data if both are not null;
    private boolean BINARY = false;

    private Object data;

    private ObjectSerializer serializer;

    private String dataBase64;

    private String dataContentType;

    private String type;

    private OffsetDateTime time;

    private String subject;

    private String dataSchema;

    private Map<String, Object> additionalProperties = new HashMap<>();


    /**
     * Constructs an instance of the builder to start creating events. No values are set.
     */
    public CloudEventBuilder() {
    }

    /**
     * Create an instance of a CloudEvent from the given values.
     * @return the created CloudEvent instance.
     */
    public CloudEvent build() {
        if (source == null) {
            throw logger.logExceptionAsError(new NullPointerException("source cannot be null"));
        } else if (type == null) {
            throw logger.logExceptionAsError(new NullPointerException("type cannot be null"));
        }

        String buildID = id != null ? id : UUID.randomUUID().toString();

        com.azure.messaging.eventgrid.implementation.models.CloudEvent result =
            new com.azure.messaging.eventgrid.implementation.models.CloudEvent();
        result
            .setSpecversion("1.0")
            .setId(buildID)
            .setSource(source)
            .setDatacontenttype(dataContentType)
            .setTime(time)
            .setType(type)
            .setDataschema(dataSchema)
            .setSubject(subject);

        if (BINARY) {
            result.setDataBase64(dataBase64);
        } else if (serializer == null) {
            result.setData(data);
        } else {
            result.setData(serializer.serialize(new ByteArrayOutputStream(), data).map(Object::toString).block());
        }

        if (!additionalProperties.isEmpty()) {
            result.setAdditionalProperties(new HashMap<>(additionalProperties));
        }

        return new CloudEvent(result);
    }

    /**
     * Set an id to use when building cloud events. The combination of id and source must be unique for each cloud event
     * sent. If an id is not provided, then a default random one will be given during the building of each event.
     * @param id the id to set.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder id(String id) {
        if (id.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("id cannot be empty"));
        }
        this.id = id;
        return this;
    }

    /**
     * Set the source to use when building cloud events. The combination of id and source must be unique for
     * each cloud event sent.
     * @param source the event source to set.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder source(String source) {
        if (CoreUtils.isNullOrEmpty(source)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("source cannot be null or empty"));
        }
        return this;
    }

    /**
     * Set the data to use when building cloud events. The type should be associated with the "type" field. Data
     * passed through this method will be serialized in JSON format, and should have
     * {@link com.fasterxml.jackson.annotation.JsonProperty} annotations on all fields to serialize.
     * @param data the data to associate with this event and serialize in JSON format.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder data(Object data) {
        return data(data, null);
    }

    /**
     * Set the data to use when building cloud events, as above, and explicitly state the content type of the data.
     * data passed this way will still be serialized in JSON format.
     * @param data            the data to associate with this event and serialize in JSON format.
     * @param dataContentType the content type of the data field, see {@link CloudEvent#getDataContentType()}.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder data(Object data, String dataContentType) {
        this.data = data;
        this.dataContentType = dataContentType;
        // in case events were created before that serialized data differently
        this.serializer = null;
        this.BINARY = false;
        return this;
    }

    /**
     * Set the data to use when building cloud events, and set a custom serializer to serialize the data. Also set the
     * content type of the data, which is the type that the serializer will produce.
     * @param data            the data to associate with this event.
     * @param serializer      a serializer to encode the contents of the data.
     * @param dataContentType the content type of the data field, see {@link CloudEvent#getDataContentType()}.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder data(Object data, ObjectSerializer serializer, String dataContentType) {
        this.data = data;
        this.dataContentType = dataContentType;
        this.serializer = serializer;
        this.BINARY = false;
        return this;
    }

    /**
     * Set the data as binary data, which will be encoded into a base 64 string when sent/received. Also set the content
     * type of the data, as it is no longer the default JSON encoding.
     * @param data            the binary data to associate with this event.
     * @param dataContentType the content type of the binary data.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder data(byte[] data, String dataContentType) {
        this.dataBase64 = new String(Base64.getEncoder().encode(data));
        this.dataContentType = dataContentType;
        this.BINARY = true;
        return this;
    }

    /**
     * Set the type of the event, such as "Contoso.Items.ItemReceived". This should indicate what type of event is
     * occurring.
     * @param type the type of event.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder type(String type) {
        if (CoreUtils.isNullOrEmpty(type)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("type cannot be null or empty"));
        }
        this.type = type;
        return this;
    }

    /**
     * Set the time associated with the event's occurrence.
     * @param time the time associated with the event.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder time(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    /**
     * Set the subject associated with the event.
     * @param subject the subject of the event to set.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Set the URI that identifies the schema that the data adheres to.
     * @param dataSchema the schema URI to set.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder dataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
        return this;
    }

    /**
     * Put an additional user-defined property to the event, possibly overriding a previous value. Property names are
     * not case sensitive and must be composed of letters and numbers.
     * @param name  the name of the property.
     * @param value the value of the property.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder putProperty(String name, Object value) {
        this.additionalProperties.put(name.toLowerCase(), value);
        return this;
    }

    /**
     * Remove a user-defined property from the event. If the property exists, then it and its value will be removed.
     * Nothing will happen if the property does not exist.
     * @param name the name of the property to remove. Property names are not case sensitive.
     *
     * @return the builder itself.
     */
    public CloudEventBuilder removeProperty(String name) {
        this.additionalProperties.remove(name);
        return this;
    }
}
