// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.batch.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Node placement configuration for batch pools.
 * 
 * Allocation configuration used by Batch Service to provision the nodes.
 */
@Fluent
public final class NodePlacementConfiguration implements JsonSerializable<NodePlacementConfiguration> {
    /*
     * Allocation policy used by Batch Service to provision the nodes. If not specified, Batch will use the regional
     * policy.
     */
    private NodePlacementPolicyType policy;

    /**
     * Creates an instance of NodePlacementConfiguration class.
     */
    public NodePlacementConfiguration() {
    }

    /**
     * Get the policy property: Allocation policy used by Batch Service to provision the nodes. If not specified, Batch
     * will use the regional policy.
     * 
     * @return the policy value.
     */
    public NodePlacementPolicyType policy() {
        return this.policy;
    }

    /**
     * Set the policy property: Allocation policy used by Batch Service to provision the nodes. If not specified, Batch
     * will use the regional policy.
     * 
     * @param policy the policy value to set.
     * @return the NodePlacementConfiguration object itself.
     */
    public NodePlacementConfiguration withPolicy(NodePlacementPolicyType policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("policy", this.policy == null ? null : this.policy.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of NodePlacementConfiguration from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of NodePlacementConfiguration if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the NodePlacementConfiguration.
     */
    public static NodePlacementConfiguration fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NodePlacementConfiguration deserializedNodePlacementConfiguration = new NodePlacementConfiguration();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("policy".equals(fieldName)) {
                    deserializedNodePlacementConfiguration.policy
                        = NodePlacementPolicyType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNodePlacementConfiguration;
        });
    }
}
