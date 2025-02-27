// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.security.fluent.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.management.ProxyResource;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.security.models.AlertSyncSettings;
import com.azure.resourcemanager.security.models.DataExportSettings;
import com.azure.resourcemanager.security.models.SettingKind;
import java.io.IOException;

/**
 * The kind of the security setting.
 */
@Immutable
public class SettingInner extends ProxyResource {
    /*
     * the kind of the settings string
     */
    private SettingKind kind = SettingKind.fromString("Setting");

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
     * Creates an instance of SettingInner class.
     */
    public SettingInner() {
    }

    /**
     * Get the kind property: the kind of the settings string.
     * 
     * @return the kind value.
     */
    public SettingKind kind() {
        return this.kind;
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
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SettingInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SettingInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SettingInner.
     */
    public static SettingInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                readerToUse.nextToken(); // Prepare for reading
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("kind".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("DataExportSettings".equals(discriminatorValue)) {
                    return DataExportSettings.fromJson(readerToUse.reset());
                } else if ("AlertSyncSettings".equals(discriminatorValue)) {
                    return AlertSyncSettings.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    static SettingInner fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SettingInner deserializedSettingInner = new SettingInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedSettingInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedSettingInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedSettingInner.type = reader.getString();
                } else if ("kind".equals(fieldName)) {
                    deserializedSettingInner.kind = SettingKind.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSettingInner;
        });
    }
}
