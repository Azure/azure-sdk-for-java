// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dataprotection.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * TaggingCriteria
 * 
 * Tagging criteria.
 */
@Fluent
public final class TaggingCriteria implements JsonSerializable<TaggingCriteria> {
    /*
     * Criteria which decides whether the tag can be applied to a triggered backup.
     */
    private List<BackupCriteria> criteria;

    /*
     * Specifies if tag is default.
     */
    private boolean isDefault;

    /*
     * Retention Tag priority.
     */
    private long taggingPriority;

    /*
     * Retention tag information
     */
    private RetentionTag tagInfo;

    /**
     * Creates an instance of TaggingCriteria class.
     */
    public TaggingCriteria() {
    }

    /**
     * Get the criteria property: Criteria which decides whether the tag can be applied to a triggered backup.
     * 
     * @return the criteria value.
     */
    public List<BackupCriteria> criteria() {
        return this.criteria;
    }

    /**
     * Set the criteria property: Criteria which decides whether the tag can be applied to a triggered backup.
     * 
     * @param criteria the criteria value to set.
     * @return the TaggingCriteria object itself.
     */
    public TaggingCriteria withCriteria(List<BackupCriteria> criteria) {
        this.criteria = criteria;
        return this;
    }

    /**
     * Get the isDefault property: Specifies if tag is default.
     * 
     * @return the isDefault value.
     */
    public boolean isDefault() {
        return this.isDefault;
    }

    /**
     * Set the isDefault property: Specifies if tag is default.
     * 
     * @param isDefault the isDefault value to set.
     * @return the TaggingCriteria object itself.
     */
    public TaggingCriteria withIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    /**
     * Get the taggingPriority property: Retention Tag priority.
     * 
     * @return the taggingPriority value.
     */
    public long taggingPriority() {
        return this.taggingPriority;
    }

    /**
     * Set the taggingPriority property: Retention Tag priority.
     * 
     * @param taggingPriority the taggingPriority value to set.
     * @return the TaggingCriteria object itself.
     */
    public TaggingCriteria withTaggingPriority(long taggingPriority) {
        this.taggingPriority = taggingPriority;
        return this;
    }

    /**
     * Get the tagInfo property: Retention tag information.
     * 
     * @return the tagInfo value.
     */
    public RetentionTag tagInfo() {
        return this.tagInfo;
    }

    /**
     * Set the tagInfo property: Retention tag information.
     * 
     * @param tagInfo the tagInfo value to set.
     * @return the TaggingCriteria object itself.
     */
    public TaggingCriteria withTagInfo(RetentionTag tagInfo) {
        this.tagInfo = tagInfo;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (criteria() != null) {
            criteria().forEach(e -> e.validate());
        }
        if (tagInfo() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property tagInfo in model TaggingCriteria"));
        } else {
            tagInfo().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(TaggingCriteria.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeBooleanField("isDefault", this.isDefault);
        jsonWriter.writeLongField("taggingPriority", this.taggingPriority);
        jsonWriter.writeJsonField("tagInfo", this.tagInfo);
        jsonWriter.writeArrayField("criteria", this.criteria, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TaggingCriteria from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of TaggingCriteria if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TaggingCriteria.
     */
    public static TaggingCriteria fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TaggingCriteria deserializedTaggingCriteria = new TaggingCriteria();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("isDefault".equals(fieldName)) {
                    deserializedTaggingCriteria.isDefault = reader.getBoolean();
                } else if ("taggingPriority".equals(fieldName)) {
                    deserializedTaggingCriteria.taggingPriority = reader.getLong();
                } else if ("tagInfo".equals(fieldName)) {
                    deserializedTaggingCriteria.tagInfo = RetentionTag.fromJson(reader);
                } else if ("criteria".equals(fieldName)) {
                    List<BackupCriteria> criteria = reader.readArray(reader1 -> BackupCriteria.fromJson(reader1));
                    deserializedTaggingCriteria.criteria = criteria;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedTaggingCriteria;
        });
    }
}
