// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devtestlabs.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Properties for evaluating a policy set.
 */
@Fluent
public final class EvaluatePoliciesProperties implements JsonSerializable<EvaluatePoliciesProperties> {
    /*
     * The fact name.
     */
    private String factName;

    /*
     * The fact data.
     */
    private String factData;

    /*
     * The value offset.
     */
    private String valueOffset;

    /*
     * The user for which policies will be evaluated
     */
    private String userObjectId;

    /**
     * Creates an instance of EvaluatePoliciesProperties class.
     */
    public EvaluatePoliciesProperties() {
    }

    /**
     * Get the factName property: The fact name.
     * 
     * @return the factName value.
     */
    public String factName() {
        return this.factName;
    }

    /**
     * Set the factName property: The fact name.
     * 
     * @param factName the factName value to set.
     * @return the EvaluatePoliciesProperties object itself.
     */
    public EvaluatePoliciesProperties withFactName(String factName) {
        this.factName = factName;
        return this;
    }

    /**
     * Get the factData property: The fact data.
     * 
     * @return the factData value.
     */
    public String factData() {
        return this.factData;
    }

    /**
     * Set the factData property: The fact data.
     * 
     * @param factData the factData value to set.
     * @return the EvaluatePoliciesProperties object itself.
     */
    public EvaluatePoliciesProperties withFactData(String factData) {
        this.factData = factData;
        return this;
    }

    /**
     * Get the valueOffset property: The value offset.
     * 
     * @return the valueOffset value.
     */
    public String valueOffset() {
        return this.valueOffset;
    }

    /**
     * Set the valueOffset property: The value offset.
     * 
     * @param valueOffset the valueOffset value to set.
     * @return the EvaluatePoliciesProperties object itself.
     */
    public EvaluatePoliciesProperties withValueOffset(String valueOffset) {
        this.valueOffset = valueOffset;
        return this;
    }

    /**
     * Get the userObjectId property: The user for which policies will be evaluated.
     * 
     * @return the userObjectId value.
     */
    public String userObjectId() {
        return this.userObjectId;
    }

    /**
     * Set the userObjectId property: The user for which policies will be evaluated.
     * 
     * @param userObjectId the userObjectId value to set.
     * @return the EvaluatePoliciesProperties object itself.
     */
    public EvaluatePoliciesProperties withUserObjectId(String userObjectId) {
        this.userObjectId = userObjectId;
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
        jsonWriter.writeStringField("factName", this.factName);
        jsonWriter.writeStringField("factData", this.factData);
        jsonWriter.writeStringField("valueOffset", this.valueOffset);
        jsonWriter.writeStringField("userObjectId", this.userObjectId);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of EvaluatePoliciesProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of EvaluatePoliciesProperties if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the EvaluatePoliciesProperties.
     */
    public static EvaluatePoliciesProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            EvaluatePoliciesProperties deserializedEvaluatePoliciesProperties = new EvaluatePoliciesProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("factName".equals(fieldName)) {
                    deserializedEvaluatePoliciesProperties.factName = reader.getString();
                } else if ("factData".equals(fieldName)) {
                    deserializedEvaluatePoliciesProperties.factData = reader.getString();
                } else if ("valueOffset".equals(fieldName)) {
                    deserializedEvaluatePoliciesProperties.valueOffset = reader.getString();
                } else if ("userObjectId".equals(fieldName)) {
                    deserializedEvaluatePoliciesProperties.userObjectId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedEvaluatePoliciesProperties;
        });
    }
}
