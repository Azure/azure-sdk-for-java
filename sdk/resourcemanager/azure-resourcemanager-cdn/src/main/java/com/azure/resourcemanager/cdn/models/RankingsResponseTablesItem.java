// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The RankingsResponseTablesItem model.
 */
@Fluent
public final class RankingsResponseTablesItem implements JsonSerializable<RankingsResponseTablesItem> {
    /*
     * The ranking property.
     */
    private String ranking;

    /*
     * The data property.
     */
    private List<RankingsResponseTablesPropertiesItemsItem> data;

    /**
     * Creates an instance of RankingsResponseTablesItem class.
     */
    public RankingsResponseTablesItem() {
    }

    /**
     * Get the ranking property: The ranking property.
     * 
     * @return the ranking value.
     */
    public String ranking() {
        return this.ranking;
    }

    /**
     * Set the ranking property: The ranking property.
     * 
     * @param ranking the ranking value to set.
     * @return the RankingsResponseTablesItem object itself.
     */
    public RankingsResponseTablesItem withRanking(String ranking) {
        this.ranking = ranking;
        return this;
    }

    /**
     * Get the data property: The data property.
     * 
     * @return the data value.
     */
    public List<RankingsResponseTablesPropertiesItemsItem> data() {
        return this.data;
    }

    /**
     * Set the data property: The data property.
     * 
     * @param data the data value to set.
     * @return the RankingsResponseTablesItem object itself.
     */
    public RankingsResponseTablesItem withData(List<RankingsResponseTablesPropertiesItemsItem> data) {
        this.data = data;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (data() != null) {
            data().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("ranking", this.ranking);
        jsonWriter.writeArrayField("data", this.data, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RankingsResponseTablesItem from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RankingsResponseTablesItem if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RankingsResponseTablesItem.
     */
    public static RankingsResponseTablesItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RankingsResponseTablesItem deserializedRankingsResponseTablesItem = new RankingsResponseTablesItem();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("ranking".equals(fieldName)) {
                    deserializedRankingsResponseTablesItem.ranking = reader.getString();
                } else if ("data".equals(fieldName)) {
                    List<RankingsResponseTablesPropertiesItemsItem> data
                        = reader.readArray(reader1 -> RankingsResponseTablesPropertiesItemsItem.fromJson(reader1));
                    deserializedRankingsResponseTablesItem.data = data;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRankingsResponseTablesItem;
        });
    }
}
