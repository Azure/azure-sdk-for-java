// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * A variant of a feature flag.
 */
@Fluent
public final class FeatureFlagVariantDefinition implements JsonSerializable<FeatureFlagVariantDefinition> {
    private final String name;
    private String value;
    private String contentType;
    private StatusOverride statusOverride;

    /**
     * Creates a feature flag variant.
     * @param name The variant name.
     */
    public FeatureFlagVariantDefinition(String name) {
        this.name = name;
    }

    /**
     * Gets the variant name.
     * @return The variant name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the variant value.
     * @return The variant value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the variant value.
     * @param value The variant value.
     * @return The updated variant.
     */
    public FeatureFlagVariantDefinition setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the content type of the variant value.
     * @return The content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the variant value.
     * @param contentType The content type.
     * @return The updated variant.
     */
    public FeatureFlagVariantDefinition setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the feature flag status override.
     * @return The status override.
     */
    public StatusOverride getStatusOverride() {
        return statusOverride;
    }

    /**
     * Sets the feature flag status override.
     * @param statusOverride The status override.
     * @return The updated variant.
     */
    public FeatureFlagVariantDefinition setStatusOverride(StatusOverride statusOverride) {
        this.statusOverride = statusOverride;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("name", name)
            .writeStringField("value", value)
            .writeStringField("content_type", contentType)
            .writeStringField("status_override", statusOverride == null ? null : statusOverride.toString())
            .writeEndObject();
    }

    /**
     * Reads a feature flag variant.
     * @param jsonReader The JSON reader.
     * @return The variant, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static FeatureFlagVariantDefinition fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String name = null;
            String value = null;
            String contentType = null;
            StatusOverride statusOverride = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("value".equals(fieldName)) {
                    value = reader.getString();
                } else if ("content_type".equals(fieldName)) {
                    contentType = reader.getString();
                } else if ("status_override".equals(fieldName)) {
                    statusOverride = StatusOverride.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return new FeatureFlagVariantDefinition(name).setValue(value)
                .setContentType(contentType)
                .setStatusOverride(statusOverride);
        });
    }
}
