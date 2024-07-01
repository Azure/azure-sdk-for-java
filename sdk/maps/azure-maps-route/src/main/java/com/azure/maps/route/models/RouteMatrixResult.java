// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import java.io.IOException;
import java.util.List;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.maps.route.implementation.helpers.RouteMatrixResultPropertiesHelper;
import com.azure.maps.route.implementation.models.RouteMatrixResultPrivate;

/** Route Matrix result. */
public class RouteMatrixResult implements JsonSerializable<RouteMatrixResult> {
    private String formatVersion;
    private List<List<RouteMatrix>> matrix;
    private RouteMatrixSummary summary;
    private String matrixId;

    static {
        RouteMatrixResultPropertiesHelper.setAccessor(RouteMatrixResult::setFromRouteMatrixResultPrivate);
    }

    /**
     * Creates a new instance of {@link RouteMatrixResult}.
     */
    public RouteMatrixResult() {
    }

    /**
     * Get the formatVersion property: Format Version property.
     *
     * @return the formatVersion value.
     */
    public String getFormatVersion() {
        return this.formatVersion;
    }

    /**
     * Get the matrix property: Results as a 2 dimensional array of route summaries.
     *
     * @return the matrix value.
     */
    public List<List<RouteMatrix>> getMatrix() {
        return this.matrix;
    }

    /**
     * Get the summary property: Summary object.
     *
     * @return the summary value.
     */
    public RouteMatrixSummary getSummary() {
        return this.summary;
    }

    /**
     * Get the matrix id of this request.
     *
     * @return the matrix id
     */
    public String getMatrixId() {
        return matrixId;
    }

    /**
     * Sets the matrix id for this request.
     * @param matrixId the matrix id
     */
    public void setMatrixId(String matrixId) {
        this.matrixId = matrixId;
    }

    // private setter
    private void setFromRouteMatrixResultPrivate(RouteMatrixResultPrivate privateResult) {
        this.formatVersion = privateResult.getFormatVersion();
        this.matrix = privateResult.getMatrix();
        this.summary = privateResult.getSummary();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RouteMatrixResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RouteMatrixResult if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RouteMatrixResult.
     */
    public static RouteMatrixResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RouteMatrixResult deserializedRouteMatrixResult = new RouteMatrixResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("formatVersion".equals(fieldName)) {
                    deserializedRouteMatrixResult.formatVersion = reader.getString();
                } else if ("matrix".equals(fieldName)) {
                    List<List<RouteMatrix>> matrix
                        = reader.readArray(reader1 -> reader1.readArray(reader2 -> RouteMatrix.fromJson(reader2)));
                    deserializedRouteMatrixResult.matrix = matrix;
                } else if ("summary".equals(fieldName)) {
                    deserializedRouteMatrixResult.summary = RouteMatrixSummary.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRouteMatrixResult;
        });
    }
}
