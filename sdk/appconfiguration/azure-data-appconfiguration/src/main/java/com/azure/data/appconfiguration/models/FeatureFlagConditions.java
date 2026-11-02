// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The conditions used to evaluate a feature flag.
 */
@Fluent
public final class FeatureFlagConditions implements JsonSerializable<FeatureFlagConditions> {
    private RequirementType requirementType;
    private List<FeatureFilter> filters;

    /**
     * Creates feature flag conditions.
     */
    public FeatureFlagConditions() {
    }

    /**
     * Gets how the filters are combined.
     * @return The requirement type.
     */
    public RequirementType getRequirementType() {
        return requirementType;
    }

    /**
     * Sets how the filters are combined.
     * @param requirementType The requirement type.
     * @return The updated conditions.
     */
    public FeatureFlagConditions setRequirementType(RequirementType requirementType) {
        this.requirementType = requirementType;
        return this;
    }

    /**
     * Gets the filters.
     * @return The filters, or null if not specified.
     */
    public List<FeatureFilter> getFilters() {
        return filters;
    }

    /**
     * Sets the filters.
     * @param filters The filters.
     * @return The updated conditions.
     */
    public FeatureFlagConditions setFilters(List<FeatureFilter> filters) {
        this.filters = filters;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("requirement_type", requirementType == null ? null : requirementType.toString())
            .writeArrayField("filters", filters, (writer, value) -> writer.writeJson(value))
            .writeEndObject();
    }

    /**
     * Reads feature flag conditions.
     * @param jsonReader The JSON reader.
     * @return The conditions, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static FeatureFlagConditions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FeatureFlagConditions result = new FeatureFlagConditions();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("requirement_type".equals(fieldName)) {
                    result.requirementType = RequirementType.fromString(reader.getString());
                } else if ("filters".equals(fieldName)) {
                    result.filters = reader.readArray(FeatureFilter::fromJson);
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
