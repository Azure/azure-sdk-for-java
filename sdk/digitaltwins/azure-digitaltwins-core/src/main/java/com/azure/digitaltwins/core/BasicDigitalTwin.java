// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.implementation.serializer.SerializationHelpers;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An optional, helper class for deserializing a digital twin. Only properties with non-null values are included.
 * <p>
 * Note that this class uses {@link JsonSerializable} from {@code azure-json}. Because of this, this type will work with
 * any implementation of {@code azure-json} but support for generic {@link Object objects} is limited to what
 * {@link JsonWriter} supports in {@link JsonWriter#writeUntyped(Object)}. In order to support custom objects, a custom
 * serializer must be used.
 */
@Fluent
public final class BasicDigitalTwin implements JsonSerializable<BasicDigitalTwin> {

    private String id;
    private String etag;
    private OffsetDateTime lastUpdatedOn;
    private BasicDigitalTwinMetadata metadata;

    private final Map<String, Object> contents = new HashMap<>();

    /**
     * Construct a basic digital twin.
     *
     * @param digitalTwinId The ID of the digital twin. The Id is unique within the service and case-sensitive.
     */
    public BasicDigitalTwin(String digitalTwinId) {
        this.id = digitalTwinId;
    }

    // Empty constructor for json deserialization purposes
    private BasicDigitalTwin() {
    }

    /**
     * Gets the unique ID of the digital twin in a digital twins instance. This field is present on every digital twin.
     *
     * @return The unique ID of the digital twin in a digital twins instance. This field is present on every digital
     * twin.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets a string representing a weak ETag for the entity that this request performs an operation against, as per
     * RFC7232.
     *
     * @return A string representing a weak ETag for the entity that this request performs an operation against, as per
     * RFC7232.
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets a string representing a weak ETag for the entity that this request performs an operation against, as per
     * RFC7232.
     *
     * @param etag A string representing a weak ETag for the entity that this request performs an operation against, as
     * per RFC7232.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the date and time when the twin was last updated.
     *
     * @return The date and time the twin was last updated.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    /**
     * Gets the information about the model a digital twin conforms to. This field is present on every digital twin.
     *
     * @return The information about the model a digital twin conforms to. This field is present on every digital twin.
     */
    public BasicDigitalTwinMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the information about the model a digital twin conforms to. This field is present on every digital twin.
     *
     * @param metadata The information about the model a digital twin conforms to. This field is present on every
     * digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setMetadata(BasicDigitalTwinMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the additional custom contents of the digital twin. This field will contain any contents of the digital
     * twin that are not already defined by the other strong types of this class.
     *
     * @return The additional contents of the digital twin. This field will contain any contents of the digital twin
     * that are not already defined by the other strong types of this class.
     */
    public Map<String, Object> getContents() {
        return contents;
    }

    /**
     * Adds a custom property to the digital twin contents. This field will contain any contents of the digital twin
     * that are not already defined by the other strong types of this class.
     *
     * @param key The key of the additional property to be added to the digital twin.
     * @param value The value of the additional property to be added to the digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin addToContents(String key, Object value) {
        this.contents.put(key, value);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID, id)
            .writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG, etag)
            .writeJsonField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA, metadata);

        for (Map.Entry<String, Object> additionalProperty : contents.entrySet()) {
            if (additionalProperty.getValue() instanceof String) {
                SerializationHelpers.serializeStringHelper(jsonWriter, additionalProperty.getKey(),
                    (String) additionalProperty.getValue());
            } else {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of BasicDigitalTwin from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BasicDigitalTwin if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BasicDigitalTwin.
     */
    public static BasicDigitalTwin fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BasicDigitalTwin twin = new BasicDigitalTwin();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID.equals(fieldName)) {
                    twin.id = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG.equals(fieldName)) {
                    twin.etag = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA.equals(fieldName)) {
                    if (reader.currentToken() == JsonToken.START_OBJECT) {
                        handleMetadata(reader.readMap(JsonReader::readUntyped),
                            lastUpdatedOn -> twin.lastUpdatedOn = lastUpdatedOn, twin::setMetadata);
                    }
                } else {
                    twin.addToContents(fieldName, reader.readUntyped());
                }
            }

            return twin;
        });
    }

    private static void handleMetadata(Map<String, Object> metadata, Consumer<OffsetDateTime> lastUpdatedOnConsumer,
        Consumer<BasicDigitalTwinMetadata> metadataConsumer) throws IOException {
        String lastUpdatedOnString  = (String) metadata.remove(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME);
        lastUpdatedOnConsumer.accept((lastUpdatedOnString == null) ? null : OffsetDateTime.parse(lastUpdatedOnString));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeMap(metadata, JsonWriter::writeUntyped).flush();
        }

        try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
            metadataConsumer.accept(BasicDigitalTwinMetadata.fromJson(jsonReader));
        }
    }
}
