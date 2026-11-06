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
 * A filter for a feature flag resource, with string-valued parameters.
 */
@Fluent
public final class FeatureFilter implements JsonSerializable<FeatureFilter> {
    private final String name;
    private Map<String, String> parameters;

    /**
     * Creates a feature filter.
     * @param name The filter name.
     */
    public FeatureFilter(String name) {
        this.name = name;
    }

    /**
     * Gets the filter name.
     * @return The filter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the string-valued filter parameters.
     * @return The parameters, or null if not specified.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the string-valued filter parameters.
     * @param parameters The parameters.
     * @return The updated filter.
     */
    public FeatureFilter setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("name", name)
            .writeMapField("parameters", parameters, (writer, value) -> writer.writeString(value))
            .writeEndObject();
    }

    /**
     * Reads a feature filter.
     * @param jsonReader The JSON reader.
     * @return The filter, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static FeatureFilter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String name = null;
            Map<String, String> parameters = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("parameters".equals(fieldName)) {
                    parameters = reader.readMap(JsonReader::getString);
                } else {
                    reader.skipChildren();
                }
            }
            return new FeatureFilter(name).setParameters(parameters);
        });
    }
}
