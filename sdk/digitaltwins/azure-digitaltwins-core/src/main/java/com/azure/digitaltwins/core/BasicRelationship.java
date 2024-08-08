// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.implementation.serializer.SerializationHelpers;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Although relationships have a user-defined schema, these properties should exist on every instance. This is useful to
 * use as a base class to ensure your custom relationships have the necessary properties.
 * <p>
 * Note that this class uses {@link JsonSerializable} from {@code azure-json}. Because of this, this type will work with
 * any implementation of {@code azure-json} but support for generic {@link Object objects} is limited to what
 * {@link JsonWriter} supports in {@link JsonWriter#writeUntyped(Object)}. In order to support custom objects, a custom
 * serializer must be used.
 */
@Fluent
public final class BasicRelationship implements JsonSerializable<BasicRelationship> {
    private String id;
    private String sourceId;
    private String targetId;
    private String name;
    private String etag;

    private final Map<String, Object> properties = new HashMap<>();

    /**
     * Construct a basic digital twin relationship.
     *
     * @param relationshipId The unique Id of this relationship.
     * @param sourceDigitalTwinId The digital twin that this relationship comes from.
     * @param targetDigitalTwinId The digital twin that this relationship points to.
     * @param relationshipName The user defined name of this relationship, for instance "Contains" or "isAdjacentTo"
     */
    public BasicRelationship(String relationshipId, String sourceDigitalTwinId, String targetDigitalTwinId,
        String relationshipName) {
        this.id = relationshipId;
        this.sourceId = sourceDigitalTwinId;
        this.targetId = targetDigitalTwinId;
        this.name = relationshipName;
    }

    // Empty constructor for json deserialization purposes
    private BasicRelationship() {
    }

    /**
     * Gets the unique ID of the relationship. This field is present on every relationship.
     *
     * @return The unique ID of the relationship. This field is present on every relationship.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the unique ID of the source digital twin. This field is present on every relationship.
     *
     * @return The unique ID of the source digital twin. This field is present on every relationship.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Gets the unique ID of the target digital twin. This field is present on every relationship.
     *
     * @return The unique ID of the target digital twin. This field is present on every relationship.
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Gets the name of the relationship, which defines the type of link (e.g. Contains). This field is present on every
     * relationship.
     *
     * @return The name of the relationship, which defines the type of link (e.g. Contains). This field is present on
     * every relationship.
     */
    public String getName() {
        return name;
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
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the additional custom properties defined in the model. This field will contain any properties of the
     * relationship that are not already defined by the other strong types of this class.
     *
     * @return The additional custom properties defined in the model. This field will contain any properties of the
     * relationship that are not already defined by the other strong types of this class.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Adds a custom property to this model. This field will contain any properties of the relationship that are not
     * already defined by the other strong types of this class.
     *
     * @param key The key of the additional property to be added to the relationship.
     * @param value The value of the additional property to be added to the relationship.
     * @return The BasicRelationship object itself.
     */
    public BasicRelationship addProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_ID, id)
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_SOURCE_ID, sourceId)
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_TARGET_ID, targetId)
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_NAME, name)
            .writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG, etag);

        for (Map.Entry<String, Object> additionalProperty : properties.entrySet()) {
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
     * Reads an instance of BasicRelationship from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BasicRelationship if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BasicRelationship.
     */
    public static BasicRelationship fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BasicRelationship basicRelationship = new BasicRelationship();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_ID.equals(fieldName)) {
                    basicRelationship.id = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_SOURCE_ID.equals(fieldName)) {
                    basicRelationship.sourceId = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_TARGET_ID.equals(fieldName)) {
                    basicRelationship.targetId = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_NAME.equals(fieldName)) {
                    basicRelationship.name = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG.equals(fieldName)) {
                    basicRelationship.etag = reader.getString();
                } else {
                    basicRelationship.addProperty(fieldName, reader.readUntyped());
                }
            }

            return basicRelationship;
        });
    }
}
