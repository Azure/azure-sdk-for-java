// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A feature flag resource identified by its name and label.
 */
@Fluent
public final class FeatureFlag implements JsonSerializable<FeatureFlag> {
    private String name;
    private boolean enabled;
    private String label;
    private String description;
    private FeatureFlagConditions conditions;
    private List<FeatureFlagVariantDefinition> variants;
    private FeatureFlagAllocation allocation;
    private FeatureFlagTelemetryConfiguration telemetry;
    private Map<String, String> tags;
    private OffsetDateTime lastModified;
    private String etag;

    /**
     * Creates a feature flag.
     *
     * @param name The name of the feature flag.
     * @param enabled Whether the feature flag is enabled.
     * @throws NullPointerException If {@code name} is null.
     */
    public FeatureFlag(String name, boolean enabled) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null.");
        this.enabled = enabled;
    }

    // Selected-field responses may omit the name and enabled state.
    private FeatureFlag() {
    }

    /**
     * Gets the name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name The name.
     * @return The updated feature flag.
     * @throws NullPointerException If {@code name} is null.
     */
    public FeatureFlag setName(String name) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null.");
        return this;
    }

    /**
     * Gets whether the feature flag is enabled.
     * @return Whether the feature flag is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the feature flag is enabled.
     * @param enabled Whether the feature flag is enabled.
     * @return The updated feature flag.
     */
    public FeatureFlag setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the label.
     * @return The label, or null for an unlabeled feature flag.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * @param label The label, or null for an unlabeled feature flag.
     * @return The updated feature flag.
     */
    public FeatureFlag setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the description.
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description The description.
     * @return The updated feature flag.
     */
    public FeatureFlag setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the evaluation conditions.
     * @return The conditions.
     */
    public FeatureFlagConditions getConditions() {
        return conditions;
    }

    /**
     * Sets the evaluation conditions.
     * @param conditions The conditions.
     * @return The updated feature flag.
     */
    public FeatureFlag setConditions(FeatureFlagConditions conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * Gets the variants.
     * @return The variants, or null if not specified.
     */
    public List<FeatureFlagVariantDefinition> getVariants() {
        return variants;
    }

    /**
     * Sets the variants.
     * @param variants The variants.
     * @return The updated feature flag.
     */
    public FeatureFlag setVariants(List<FeatureFlagVariantDefinition> variants) {
        this.variants = variants;
        return this;
    }

    /**
     * Gets the variant allocation.
     * @return The allocation.
     */
    public FeatureFlagAllocation getAllocation() {
        return allocation;
    }

    /**
     * Sets the variant allocation.
     * @param allocation The allocation.
     * @return The updated feature flag.
     */
    public FeatureFlag setAllocation(FeatureFlagAllocation allocation) {
        this.allocation = allocation;
        return this;
    }

    /**
     * Gets the telemetry configuration.
     * @return The telemetry configuration.
     */
    public FeatureFlagTelemetryConfiguration getTelemetry() {
        return telemetry;
    }

    /**
     * Sets the telemetry configuration.
     * @param telemetry The telemetry configuration.
     * @return The updated feature flag.
     */
    public FeatureFlag setTelemetry(FeatureFlagTelemetryConfiguration telemetry) {
        this.telemetry = telemetry;
        return this;
    }

    /**
     * Gets the tags.
     * @return The tags, or null if not specified.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Sets the tags.
     * @param tags The tags.
     * @return The updated feature flag.
     */
    public FeatureFlag setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the time the feature flag was last modified.
     * @return The last modification time.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the entity tag representing the resource state.
     * @return The entity tag.
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets the entity tag representing the resource state.
     * @param etag The entity tag.
     * @return The updated feature flag.
     */
    public FeatureFlag setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("name", name)
            .writeBooleanField("enabled", enabled)
            .writeStringField("label", label)
            .writeStringField("description", description)
            .writeJsonField("conditions", conditions)
            .writeArrayField("variants", variants, (writer, value) -> writer.writeJson(value))
            .writeJsonField("allocation", allocation)
            .writeJsonField("telemetry", telemetry)
            .writeMapField("tags", tags, (writer, value) -> writer.writeString(value))
            .writeStringField("last_modified", lastModified == null ? null : lastModified.toString())
            .writeStringField("etag", etag)
            .writeEndObject();
    }

    /**
     * Reads a feature flag, including selected-field responses with omitted properties.
     * @param jsonReader The JSON reader.
     * @return The feature flag, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static FeatureFlag fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FeatureFlag result = new FeatureFlag();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    result.name = reader.getString();
                } else if ("enabled".equals(fieldName)) {
                    result.enabled = reader.getBoolean();
                } else if ("label".equals(fieldName)) {
                    result.label = reader.getString();
                } else if ("description".equals(fieldName)) {
                    result.description = reader.getString();
                } else if ("conditions".equals(fieldName)) {
                    result.conditions = FeatureFlagConditions.fromJson(reader);
                } else if ("variants".equals(fieldName)) {
                    result.variants = reader.readArray(FeatureFlagVariantDefinition::fromJson);
                } else if ("allocation".equals(fieldName)) {
                    result.allocation = FeatureFlagAllocation.fromJson(reader);
                } else if ("telemetry".equals(fieldName)) {
                    result.telemetry = FeatureFlagTelemetryConfiguration.fromJson(reader);
                } else if ("tags".equals(fieldName)) {
                    result.tags = reader.readMap(JsonReader::getString);
                } else if ("last_modified".equals(fieldName)) {
                    result.lastModified
                        = reader.getNullable(value -> CoreUtils.parseBestOffsetDateTime(value.getString()));
                } else if ("etag".equals(fieldName)) {
                    result.etag = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
