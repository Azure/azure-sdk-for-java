// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.network.fluent.models.LoadBalancerInner;
import java.io.IOException;
import java.util.List;

/**
 * Response for list ip configurations API service call.
 */
@Fluent
public final class NetworkInterfaceLoadBalancerListResult
    implements JsonSerializable<NetworkInterfaceLoadBalancerListResult> {
    /*
     * A list of load balancers.
     */
    private List<LoadBalancerInner> value;

    /*
     * The URL to get the next set of results.
     */
    private String nextLink;

    /**
     * Creates an instance of NetworkInterfaceLoadBalancerListResult class.
     */
    public NetworkInterfaceLoadBalancerListResult() {
    }

    /**
     * Get the value property: A list of load balancers.
     * 
     * @return the value value.
     */
    public List<LoadBalancerInner> value() {
        return this.value;
    }

    /**
     * Set the value property: A list of load balancers.
     * 
     * @param value the value value to set.
     * @return the NetworkInterfaceLoadBalancerListResult object itself.
     */
    public NetworkInterfaceLoadBalancerListResult withValue(List<LoadBalancerInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The URL to get the next set of results.
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
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }

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
     * Reads an instance of NetworkInterfaceLoadBalancerListResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of NetworkInterfaceLoadBalancerListResult if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the NetworkInterfaceLoadBalancerListResult.
     */
    public static NetworkInterfaceLoadBalancerListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NetworkInterfaceLoadBalancerListResult deserializedNetworkInterfaceLoadBalancerListResult
                = new NetworkInterfaceLoadBalancerListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<LoadBalancerInner> value = reader.readArray(reader1 -> LoadBalancerInner.fromJson(reader1));
                    deserializedNetworkInterfaceLoadBalancerListResult.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedNetworkInterfaceLoadBalancerListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNetworkInterfaceLoadBalancerListResult;
        });
    }
}
