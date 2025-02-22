// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Cognitive Services account commitment quota.
 */
@Fluent
public final class CommitmentQuota implements JsonSerializable<CommitmentQuota> {
    /*
     * Commitment quota quantity.
     */
    private Long quantity;

    /*
     * Commitment quota unit.
     */
    private String unit;

    /**
     * Creates an instance of CommitmentQuota class.
     */
    public CommitmentQuota() {
    }

    /**
     * Get the quantity property: Commitment quota quantity.
     * 
     * @return the quantity value.
     */
    public Long quantity() {
        return this.quantity;
    }

    /**
     * Set the quantity property: Commitment quota quantity.
     * 
     * @param quantity the quantity value to set.
     * @return the CommitmentQuota object itself.
     */
    public CommitmentQuota withQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Get the unit property: Commitment quota unit.
     * 
     * @return the unit value.
     */
    public String unit() {
        return this.unit;
    }

    /**
     * Set the unit property: Commitment quota unit.
     * 
     * @param unit the unit value to set.
     * @return the CommitmentQuota object itself.
     */
    public CommitmentQuota withUnit(String unit) {
        this.unit = unit;
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
        jsonWriter.writeNumberField("quantity", this.quantity);
        jsonWriter.writeStringField("unit", this.unit);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CommitmentQuota from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CommitmentQuota if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CommitmentQuota.
     */
    public static CommitmentQuota fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CommitmentQuota deserializedCommitmentQuota = new CommitmentQuota();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("quantity".equals(fieldName)) {
                    deserializedCommitmentQuota.quantity = reader.getNullable(JsonReader::getLong);
                } else if ("unit".equals(fieldName)) {
                    deserializedCommitmentQuota.unit = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCommitmentQuota;
        });
    }
}
