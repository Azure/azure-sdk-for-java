// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.desktopvirtualization.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * ApplicationGroup properties that can be patched.
 */
@Fluent
public final class ApplicationGroupPatchProperties implements JsonSerializable<ApplicationGroupPatchProperties> {
    /*
     * Description of ApplicationGroup.
     */
    private String description;

    /*
     * Friendly name of ApplicationGroup.
     */
    private String friendlyName;

    /*
     * Boolean representing whether the applicationGroup is show in the feed.
     */
    private Boolean showInFeed;

    /**
     * Creates an instance of ApplicationGroupPatchProperties class.
     */
    public ApplicationGroupPatchProperties() {
    }

    /**
     * Get the description property: Description of ApplicationGroup.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: Description of ApplicationGroup.
     * 
     * @param description the description value to set.
     * @return the ApplicationGroupPatchProperties object itself.
     */
    public ApplicationGroupPatchProperties withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the friendlyName property: Friendly name of ApplicationGroup.
     * 
     * @return the friendlyName value.
     */
    public String friendlyName() {
        return this.friendlyName;
    }

    /**
     * Set the friendlyName property: Friendly name of ApplicationGroup.
     * 
     * @param friendlyName the friendlyName value to set.
     * @return the ApplicationGroupPatchProperties object itself.
     */
    public ApplicationGroupPatchProperties withFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Get the showInFeed property: Boolean representing whether the applicationGroup is show in the feed.
     * 
     * @return the showInFeed value.
     */
    public Boolean showInFeed() {
        return this.showInFeed;
    }

    /**
     * Set the showInFeed property: Boolean representing whether the applicationGroup is show in the feed.
     * 
     * @param showInFeed the showInFeed value to set.
     * @return the ApplicationGroupPatchProperties object itself.
     */
    public ApplicationGroupPatchProperties withShowInFeed(Boolean showInFeed) {
        this.showInFeed = showInFeed;
        return this;
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
        jsonWriter.writeStringField("description", this.description);
        jsonWriter.writeStringField("friendlyName", this.friendlyName);
        jsonWriter.writeBooleanField("showInFeed", this.showInFeed);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ApplicationGroupPatchProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ApplicationGroupPatchProperties if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ApplicationGroupPatchProperties.
     */
    public static ApplicationGroupPatchProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ApplicationGroupPatchProperties deserializedApplicationGroupPatchProperties
                = new ApplicationGroupPatchProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("description".equals(fieldName)) {
                    deserializedApplicationGroupPatchProperties.description = reader.getString();
                } else if ("friendlyName".equals(fieldName)) {
                    deserializedApplicationGroupPatchProperties.friendlyName = reader.getString();
                } else if ("showInFeed".equals(fieldName)) {
                    deserializedApplicationGroupPatchProperties.showInFeed = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedApplicationGroupPatchProperties;
        });
    }
}
