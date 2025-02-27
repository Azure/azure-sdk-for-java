// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.customerinsights.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.ProxyResource;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The WidgetTypeResourceFormat.
 */
@Fluent
public final class WidgetTypeResourceFormatInner extends ProxyResource {
    /*
     * Definition of WidgetType.
     */
    private WidgetType innerProperties;

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
     * Creates an instance of WidgetTypeResourceFormatInner class.
     */
    public WidgetTypeResourceFormatInner() {
    }

    /**
     * Get the innerProperties property: Definition of WidgetType.
     * 
     * @return the innerProperties value.
     */
    private WidgetType innerProperties() {
        return this.innerProperties;
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
     * Get the widgetTypeName property: Name of the widget type.
     * 
     * @return the widgetTypeName value.
     */
    public String widgetTypeName() {
        return this.innerProperties() == null ? null : this.innerProperties().widgetTypeName();
    }

    /**
     * Get the definition property: Definition for widget type.
     * 
     * @return the definition value.
     */
    public String definition() {
        return this.innerProperties() == null ? null : this.innerProperties().definition();
    }

    /**
     * Set the definition property: Definition for widget type.
     * 
     * @param definition the definition value to set.
     * @return the WidgetTypeResourceFormatInner object itself.
     */
    public WidgetTypeResourceFormatInner withDefinition(String definition) {
        if (this.innerProperties() == null) {
            this.innerProperties = new WidgetType();
        }
        this.innerProperties().withDefinition(definition);
        return this;
    }

    /**
     * Get the description property: Description for widget type.
     * 
     * @return the description value.
     */
    public String description() {
        return this.innerProperties() == null ? null : this.innerProperties().description();
    }

    /**
     * Set the description property: Description for widget type.
     * 
     * @param description the description value to set.
     * @return the WidgetTypeResourceFormatInner object itself.
     */
    public WidgetTypeResourceFormatInner withDescription(String description) {
        if (this.innerProperties() == null) {
            this.innerProperties = new WidgetType();
        }
        this.innerProperties().withDescription(description);
        return this;
    }

    /**
     * Get the displayName property: Localized display name for the widget type.
     * 
     * @return the displayName value.
     */
    public Map<String, String> displayName() {
        return this.innerProperties() == null ? null : this.innerProperties().displayName();
    }

    /**
     * Set the displayName property: Localized display name for the widget type.
     * 
     * @param displayName the displayName value to set.
     * @return the WidgetTypeResourceFormatInner object itself.
     */
    public WidgetTypeResourceFormatInner withDisplayName(Map<String, String> displayName) {
        if (this.innerProperties() == null) {
            this.innerProperties = new WidgetType();
        }
        this.innerProperties().withDisplayName(displayName);
        return this;
    }

    /**
     * Get the imageUrl property: The image URL.
     * 
     * @return the imageUrl value.
     */
    public String imageUrl() {
        return this.innerProperties() == null ? null : this.innerProperties().imageUrl();
    }

    /**
     * Set the imageUrl property: The image URL.
     * 
     * @param imageUrl the imageUrl value to set.
     * @return the WidgetTypeResourceFormatInner object itself.
     */
    public WidgetTypeResourceFormatInner withImageUrl(String imageUrl) {
        if (this.innerProperties() == null) {
            this.innerProperties = new WidgetType();
        }
        this.innerProperties().withImageUrl(imageUrl);
        return this;
    }

    /**
     * Get the tenantId property: The hub name.
     * 
     * @return the tenantId value.
     */
    public String tenantId() {
        return this.innerProperties() == null ? null : this.innerProperties().tenantId();
    }

    /**
     * Get the widgetVersion property: The widget version.
     * 
     * @return the widgetVersion value.
     */
    public String widgetVersion() {
        return this.innerProperties() == null ? null : this.innerProperties().widgetVersion();
    }

    /**
     * Set the widgetVersion property: The widget version.
     * 
     * @param widgetVersion the widgetVersion value to set.
     * @return the WidgetTypeResourceFormatInner object itself.
     */
    public WidgetTypeResourceFormatInner withWidgetVersion(String widgetVersion) {
        if (this.innerProperties() == null) {
            this.innerProperties = new WidgetType();
        }
        this.innerProperties().withWidgetVersion(widgetVersion);
        return this;
    }

    /**
     * Get the changed property: Date time when widget type was last modified.
     * 
     * @return the changed value.
     */
    public OffsetDateTime changed() {
        return this.innerProperties() == null ? null : this.innerProperties().changed();
    }

    /**
     * Get the created property: Date time when widget type was created.
     * 
     * @return the created value.
     */
    public OffsetDateTime created() {
        return this.innerProperties() == null ? null : this.innerProperties().created();
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of WidgetTypeResourceFormatInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of WidgetTypeResourceFormatInner if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the WidgetTypeResourceFormatInner.
     */
    public static WidgetTypeResourceFormatInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            WidgetTypeResourceFormatInner deserializedWidgetTypeResourceFormatInner
                = new WidgetTypeResourceFormatInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedWidgetTypeResourceFormatInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedWidgetTypeResourceFormatInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedWidgetTypeResourceFormatInner.type = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedWidgetTypeResourceFormatInner.innerProperties = WidgetType.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedWidgetTypeResourceFormatInner;
        });
    }
}
