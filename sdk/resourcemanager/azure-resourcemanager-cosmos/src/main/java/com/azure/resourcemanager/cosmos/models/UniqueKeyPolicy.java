// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The unique key policy configuration for specifying uniqueness constraints on documents in the collection in the Azure
 * Cosmos DB service.
 */
@Fluent
public final class UniqueKeyPolicy implements JsonSerializable<UniqueKeyPolicy> {
    /*
     * List of unique keys on that enforces uniqueness constraint on documents in the collection in the Azure Cosmos DB
     * service.
     */
    private List<UniqueKey> uniqueKeys;

    /**
     * Creates an instance of UniqueKeyPolicy class.
     */
    public UniqueKeyPolicy() {
    }

    /**
     * Get the uniqueKeys property: List of unique keys on that enforces uniqueness constraint on documents in the
     * collection in the Azure Cosmos DB service.
     * 
     * @return the uniqueKeys value.
     */
    public List<UniqueKey> uniqueKeys() {
        return this.uniqueKeys;
    }

    /**
     * Set the uniqueKeys property: List of unique keys on that enforces uniqueness constraint on documents in the
     * collection in the Azure Cosmos DB service.
     * 
     * @param uniqueKeys the uniqueKeys value to set.
     * @return the UniqueKeyPolicy object itself.
     */
    public UniqueKeyPolicy withUniqueKeys(List<UniqueKey> uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (uniqueKeys() != null) {
            uniqueKeys().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("uniqueKeys", this.uniqueKeys, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of UniqueKeyPolicy from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of UniqueKeyPolicy if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the UniqueKeyPolicy.
     */
    public static UniqueKeyPolicy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UniqueKeyPolicy deserializedUniqueKeyPolicy = new UniqueKeyPolicy();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("uniqueKeys".equals(fieldName)) {
                    List<UniqueKey> uniqueKeys = reader.readArray(reader1 -> UniqueKey.fromJson(reader1));
                    deserializedUniqueKeyPolicy.uniqueKeys = uniqueKeys;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedUniqueKeyPolicy;
        });
    }
}
