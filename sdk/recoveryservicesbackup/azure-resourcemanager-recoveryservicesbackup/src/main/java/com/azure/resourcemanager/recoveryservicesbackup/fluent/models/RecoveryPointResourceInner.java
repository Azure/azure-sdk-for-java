// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Resource;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryPoint;
import java.io.IOException;
import java.util.Map;

/**
 * Base class for backup copies. Workload-specific backup copies are derived from this class.
 */
@Fluent
public final class RecoveryPointResourceInner extends Resource {
    /*
     * RecoveryPointResource properties
     */
    private RecoveryPoint properties;

    /*
     * Optional ETag.
     */
    private String etag;

    /*
     * The type of the resource.
     */
    private String type;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of RecoveryPointResourceInner class.
     */
    public RecoveryPointResourceInner() {
    }

    /**
     * Get the properties property: RecoveryPointResource properties.
     * 
     * @return the properties value.
     */
    public RecoveryPoint properties() {
        return this.properties;
    }

    /**
     * Set the properties property: RecoveryPointResource properties.
     * 
     * @param properties the properties value to set.
     * @return the RecoveryPointResourceInner object itself.
     */
    public RecoveryPointResourceInner withProperties(RecoveryPoint properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the etag property: Optional ETag.
     * 
     * @return the etag value.
     */
    public String etag() {
        return this.etag;
    }

    /**
     * Set the etag property: Optional ETag.
     * 
     * @param etag the etag value to set.
     * @return the RecoveryPointResourceInner object itself.
     */
    public RecoveryPointResourceInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the name property: The name of the resource.
     * 
     * @return the name value.
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecoveryPointResourceInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecoveryPointResourceInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (properties() != null) {
            properties().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("location", location());
        jsonWriter.writeMapField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("properties", this.properties);
        jsonWriter.writeStringField("eTag", this.etag);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RecoveryPointResourceInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecoveryPointResourceInner if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the RecoveryPointResourceInner.
     */
    public static RecoveryPointResourceInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RecoveryPointResourceInner deserializedRecoveryPointResourceInner = new RecoveryPointResourceInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedRecoveryPointResourceInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedRecoveryPointResourceInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedRecoveryPointResourceInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedRecoveryPointResourceInner.withLocation(reader.getString());
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedRecoveryPointResourceInner.withTags(tags);
                } else if ("properties".equals(fieldName)) {
                    deserializedRecoveryPointResourceInner.properties = RecoveryPoint.fromJson(reader);
                } else if ("eTag".equals(fieldName)) {
                    deserializedRecoveryPointResourceInner.etag = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRecoveryPointResourceInner;
        });
    }
}
