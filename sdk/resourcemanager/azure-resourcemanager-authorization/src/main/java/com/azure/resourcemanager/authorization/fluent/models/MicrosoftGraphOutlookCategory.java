// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * outlookCategory.
 */
@Fluent
public final class MicrosoftGraphOutlookCategory extends MicrosoftGraphEntity {
    /*
     * categoryColor
     */
    private MicrosoftGraphCategoryColor color;

    /*
     * A unique name that identifies a category in the user's mailbox. After a category is created, the name cannot be
     * changed. Read-only.
     */
    private String displayName;

    /*
     * outlookCategory
     */
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance of MicrosoftGraphOutlookCategory class.
     */
    public MicrosoftGraphOutlookCategory() {
    }

    /**
     * Get the color property: categoryColor.
     * 
     * @return the color value.
     */
    public MicrosoftGraphCategoryColor color() {
        return this.color;
    }

    /**
     * Set the color property: categoryColor.
     * 
     * @param color the color value to set.
     * @return the MicrosoftGraphOutlookCategory object itself.
     */
    public MicrosoftGraphOutlookCategory withColor(MicrosoftGraphCategoryColor color) {
        this.color = color;
        return this;
    }

    /**
     * Get the displayName property: A unique name that identifies a category in the user's mailbox. After a category is
     * created, the name cannot be changed. Read-only.
     * 
     * @return the displayName value.
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: A unique name that identifies a category in the user's mailbox. After a category is
     * created, the name cannot be changed. Read-only.
     * 
     * @param displayName the displayName value to set.
     * @return the MicrosoftGraphOutlookCategory object itself.
     */
    public MicrosoftGraphOutlookCategory withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the additionalProperties property: outlookCategory.
     * 
     * @return the additionalProperties value.
     */
    public Map<String, Object> additionalProperties() {
        return this.additionalProperties;
    }

    /**
     * Set the additionalProperties property: outlookCategory.
     * 
     * @param additionalProperties the additionalProperties value to set.
     * @return the MicrosoftGraphOutlookCategory object itself.
     */
    public MicrosoftGraphOutlookCategory withAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MicrosoftGraphOutlookCategory withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id());
        jsonWriter.writeStringField("color", this.color == null ? null : this.color.toString());
        jsonWriter.writeStringField("displayName", this.displayName);
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MicrosoftGraphOutlookCategory from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MicrosoftGraphOutlookCategory if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MicrosoftGraphOutlookCategory.
     */
    public static MicrosoftGraphOutlookCategory fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MicrosoftGraphOutlookCategory deserializedMicrosoftGraphOutlookCategory
                = new MicrosoftGraphOutlookCategory();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedMicrosoftGraphOutlookCategory.withId(reader.getString());
                } else if ("color".equals(fieldName)) {
                    deserializedMicrosoftGraphOutlookCategory.color
                        = MicrosoftGraphCategoryColor.fromString(reader.getString());
                } else if ("displayName".equals(fieldName)) {
                    deserializedMicrosoftGraphOutlookCategory.displayName = reader.getString();
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedMicrosoftGraphOutlookCategory.additionalProperties = additionalProperties;

            return deserializedMicrosoftGraphOutlookCategory;
        });
    }
}
