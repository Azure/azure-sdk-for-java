// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Strictly used in update requests.
 */
@Fluent
public final class PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties
    implements JsonSerializable<PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties> {
    /*
     * Additional attributes of the entity.
     */
    private PartialBatchDeployment properties;

    /*
     * Resource tags.
     */
    private Map<String, String> tags;

    /**
     * Creates an instance of PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties class.
     */
    public PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties() {
    }

    /**
     * Get the properties property: Additional attributes of the entity.
     * 
     * @return the properties value.
     */
    public PartialBatchDeployment properties() {
        return this.properties;
    }

    /**
     * Set the properties property: Additional attributes of the entity.
     * 
     * @param properties the properties value to set.
     * @return the PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties object itself.
     */
    public PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties
        withProperties(PartialBatchDeployment properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the tags property: Resource tags.
     * 
     * @return the tags value.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags property: Resource tags.
     * 
     * @param tags the tags value to set.
     * @return the PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties object itself.
     */
    public PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (properties() != null) {
            properties().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("properties", this.properties);
        jsonWriter.writeMapField("tags", this.tags, (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties if the JsonReader was
     * pointing to an instance of it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the
     * PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties.
     */
    public static PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties fromJson(JsonReader jsonReader)
        throws IOException {
        return jsonReader.readObject(reader -> {
            PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties deserializedPartialBatchDeploymentPartialMinimalTrackedResourceWithProperties
                = new PartialBatchDeploymentPartialMinimalTrackedResourceWithProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("properties".equals(fieldName)) {
                    deserializedPartialBatchDeploymentPartialMinimalTrackedResourceWithProperties.properties
                        = PartialBatchDeployment.fromJson(reader);
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedPartialBatchDeploymentPartialMinimalTrackedResourceWithProperties.tags = tags;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPartialBatchDeploymentPartialMinimalTrackedResourceWithProperties;
        });
    }
}
