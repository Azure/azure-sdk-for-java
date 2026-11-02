// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;

/**
 * The telemetry configuration for a feature flag.
 */
@Fluent
public final class FeatureFlagTelemetryConfiguration implements JsonSerializable<FeatureFlagTelemetryConfiguration> {
    private final boolean enabled;
    private Map<String, String> metadata;

    /**
     * Creates a telemetry configuration.
     * @param enabled Whether telemetry is enabled.
     */
    public FeatureFlagTelemetryConfiguration(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets whether telemetry is enabled.
     * @return Whether telemetry is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the telemetry metadata.
     * @return The metadata, or null if not specified.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the telemetry metadata.
     * @param metadata The metadata.
     * @return The updated telemetry configuration.
     */
    public FeatureFlagTelemetryConfiguration setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBooleanField("enabled", enabled)
            .writeMapField("metadata", metadata, (writer, value) -> writer.writeString(value))
            .writeEndObject();
    }

    /**
     * Reads a telemetry configuration.
     * @param jsonReader The JSON reader.
     * @return The configuration, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static FeatureFlagTelemetryConfiguration fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            boolean enabled = false;
            Map<String, String> metadata = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("enabled".equals(fieldName)) {
                    enabled = reader.getBoolean();
                } else if ("metadata".equals(fieldName)) {
                    metadata = reader.readMap(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }
            return new FeatureFlagTelemetryConfiguration(enabled).setMetadata(metadata);
        });
    }
}
