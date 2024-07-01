// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.maps.route.implementation.helpers.RouteDirectionsBatchResultPropertiesHelper;
import com.azure.maps.route.implementation.models.RouteDirectionsBatchResultPrivate;

import java.io.IOException;
import java.util.List;

/**
 * Route Directions Batch Result
 *
 */
public class RouteDirectionsBatchResult implements JsonSerializable<RouteDirectionsBatchResult> {
    private List<RouteDirectionsBatchItem> batchItems;
    private String batchId;

    static {
        RouteDirectionsBatchResultPropertiesHelper.setAccessor(
            RouteDirectionsBatchResult::setFromRouteDirectionsBatchResultPrivate);
    }

    /**
     * Creates a new instance of {@link RouteDirectionsBatchResult}.
     */
    public RouteDirectionsBatchResult() {
    }

    /**
     * Get the batchItems property: Array containing the batch results.
     *
     * @return the batchItems value.
     */
    public List<RouteDirectionsBatchItem> getBatchItems() {
        return this.batchItems;
    }

    /**
     * Get the batch id of this request.
     *
     * @return the batch id
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the matrix id for this request.
     * @param batchId the bach id for this request.
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    // private setter
    private void setFromRouteDirectionsBatchResultPrivate(RouteDirectionsBatchResultPrivate privateResult) {
        this.batchItems = privateResult.getBatchItems();
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
     * Reads an instance of RouteDirectionsBatchResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RouteDirectionsBatchResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RouteDirectionsBatchResult.
     */
    public static RouteDirectionsBatchResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RouteDirectionsBatchResult deserializedRouteDirectionsBatchResult
                = new RouteDirectionsBatchResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("batchItems".equals(fieldName)) {
                    List<RouteDirectionsBatchItem> batchItems
                        = reader.readArray(reader1 -> RouteDirectionsBatchItem.fromJson(reader1));
                    deserializedRouteDirectionsBatchResult.batchItems = batchItems;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRouteDirectionsBatchResult;
        });
    }
}
