// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.DigitalTwinsAsyncClient;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

// This class exists so that the public APIs don't directly consume a generated type and so that we can avoid exposing a
// validate() method that the generated type comes with when client side validation is enabled.

/**
 * Defines an incoming relationship on a digital twin. Unlike outgoing relationships, incoming relationships have no
 * user-defined properties when retrieved using {@link DigitalTwinsClient#listIncomingRelationships(String, Context)} or
 * {@link DigitalTwinsAsyncClient#listIncomingRelationships(String)}. Because of this, there is no need for user-defined
 * types for deserialization. This class will capture the full service response when listing incoming relationships.
 */
@Fluent
public final class IncomingRelationship implements JsonSerializable<IncomingRelationship> {
    /*
     * A user-provided string representing the id of this relationship, unique in the context of the source digital
     * twin, i.e. sourceId + relationshipId is unique in the context of the service.
     */
    private final String relationshipId;

    /*
     * The id of the source digital twin.
     */
    private final String sourceId;

    /*
     * The name of the relationship.
     */
    private final String relationshipName;

    /*
     * Link to the relationship, to be used for deletion.
     */
    private final String relationshipLink;

    /**
     * Construct a new IncomingRelationship instance. This class should only be constructed internally since the service
     * never takes this as an input.
     *
     * @param relationshipId The ID of this incoming relationship.
     * @param sourceDigitalTwinId The ID of the digital twin that this relationship comes from.
     * @param relationshipName The name of the relationship, for instance "Contains" or "IsAdjacentTo".
     * @param relationshipLink The link to the relationship, to be used for deletion.
     */
    public IncomingRelationship(String relationshipId, String sourceDigitalTwinId, String relationshipName,
        String relationshipLink) {
        this.relationshipId = relationshipId;
        this.sourceId = sourceDigitalTwinId;
        this.relationshipName = relationshipName;
        this.relationshipLink = relationshipLink;
    }

    /**
     * Get the relationshipId property: A user-provided string representing the id of this relationship, unique in the
     * context of the source digital twin, i.e. sourceId + relationshipId is unique in the context of the service.
     *
     * @return the relationshipId value.
     */
    public String getRelationshipId() {
        return this.relationshipId;
    }

    /**
     * Get the sourceId property: The id of the source digital twin.
     *
     * @return the sourceId value.
     */
    public String getSourceId() {
        return this.sourceId;
    }

    /**
     * Get the relationshipName property: The name of the relationship.
     *
     * @return the relationshipName value.
     */
    public String getRelationshipName() {
        return this.relationshipName;
    }

    /**
     * Get the relationshipLink property: Link to the relationship, to be used for deletion.
     *
     * @return the relationshipLink value.
     */
    public String getRelationshipLink() {
        return this.relationshipLink;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_ID, relationshipId)
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_SOURCE_ID, sourceId)
            .writeStringField(DigitalTwinsJsonPropertyNames.RELATIONSHIP_NAME, relationshipName)
            .writeStringField("$relationshipLink", relationshipLink)
            .writeEndObject();
    }

    /**
     * Reads an instance of IncomingRelationship from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of IncomingRelationship if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the IncomingRelationship.
     */
    public static IncomingRelationship fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String relationshipId = null;
            String sourceId = null;
            String relationshipName = null;
            String relationshipLink = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_ID.equals(fieldName)) {
                    relationshipId = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_SOURCE_ID.equals(fieldName)) {
                    sourceId = reader.getString();
                } else if (DigitalTwinsJsonPropertyNames.RELATIONSHIP_NAME.equals(fieldName)) {
                    relationshipName = reader.getString();
                } else if ("$relationshipLink".equals(fieldName)) {
                    relationshipLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new IncomingRelationship(relationshipId, sourceId, relationshipName, relationshipLink);
        });
    }
}
