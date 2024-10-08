// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.containers.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.hdinsight.containers.fluent.models.ClusterPoolUpgradeHistoryInner;
import java.io.IOException;
import java.util.List;

/**
 * Represents a list of cluster pool upgrade history.
 */
@Fluent
public final class ClusterPoolUpgradeHistoryListResult
    implements JsonSerializable<ClusterPoolUpgradeHistoryListResult> {
    /*
     * The list of cluster pool upgrade history.
     */
    private List<ClusterPoolUpgradeHistoryInner> value;

    /*
     * The link (url) to the next page of results.
     */
    private String nextLink;

    /**
     * Creates an instance of ClusterPoolUpgradeHistoryListResult class.
     */
    public ClusterPoolUpgradeHistoryListResult() {
    }

    /**
     * Get the value property: The list of cluster pool upgrade history.
     * 
     * @return the value value.
     */
    public List<ClusterPoolUpgradeHistoryInner> value() {
        return this.value;
    }

    /**
     * Set the value property: The list of cluster pool upgrade history.
     * 
     * @param value the value value to set.
     * @return the ClusterPoolUpgradeHistoryListResult object itself.
     */
    public ClusterPoolUpgradeHistoryListResult withValue(List<ClusterPoolUpgradeHistoryInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The link (url) to the next page of results.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property value in model ClusterPoolUpgradeHistoryListResult"));
        } else {
            value().forEach(e -> e.validate());
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ClusterPoolUpgradeHistoryListResult.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("value", this.value, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ClusterPoolUpgradeHistoryListResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ClusterPoolUpgradeHistoryListResult if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ClusterPoolUpgradeHistoryListResult.
     */
    public static ClusterPoolUpgradeHistoryListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ClusterPoolUpgradeHistoryListResult deserializedClusterPoolUpgradeHistoryListResult
                = new ClusterPoolUpgradeHistoryListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<ClusterPoolUpgradeHistoryInner> value
                        = reader.readArray(reader1 -> ClusterPoolUpgradeHistoryInner.fromJson(reader1));
                    deserializedClusterPoolUpgradeHistoryListResult.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedClusterPoolUpgradeHistoryListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedClusterPoolUpgradeHistoryListResult;
        });
    }
}
