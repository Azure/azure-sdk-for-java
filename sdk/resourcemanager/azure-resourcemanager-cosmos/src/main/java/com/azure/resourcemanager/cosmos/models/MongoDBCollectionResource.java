// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Cosmos DB MongoDB collection resource object.
 */
@Fluent
public class MongoDBCollectionResource implements JsonSerializable<MongoDBCollectionResource> {
    /*
     * Name of the Cosmos DB MongoDB collection
     */
    private String id;

    /*
     * A key-value pair of shard keys to be applied for the request.
     */
    private Map<String, String> shardKey;

    /*
     * List of index keys
     */
    private List<MongoIndex> indexes;

    /*
     * Analytical TTL.
     */
    private Integer analyticalStorageTtl;

    /*
     * Parameters to indicate the information about the restore
     */
    private ResourceRestoreParameters restoreParameters;

    /*
     * Enum to indicate the mode of resource creation.
     */
    private CreateMode createMode;

    /**
     * Creates an instance of MongoDBCollectionResource class.
     */
    public MongoDBCollectionResource() {
    }

    /**
     * Get the id property: Name of the Cosmos DB MongoDB collection.
     * 
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id property: Name of the Cosmos DB MongoDB collection.
     * 
     * @param id the id value to set.
     * @return the MongoDBCollectionResource object itself.
     */
    public MongoDBCollectionResource withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the shardKey property: A key-value pair of shard keys to be applied for the request.
     * 
     * @return the shardKey value.
     */
    public Map<String, String> shardKey() {
        return this.shardKey;
    }

    /**
     * Set the shardKey property: A key-value pair of shard keys to be applied for the request.
     * 
     * @param shardKey the shardKey value to set.
     * @return the MongoDBCollectionResource object itself.
     */
    public MongoDBCollectionResource withShardKey(Map<String, String> shardKey) {
        this.shardKey = shardKey;
        return this;
    }

    /**
     * Get the indexes property: List of index keys.
     * 
     * @return the indexes value.
     */
    public List<MongoIndex> indexes() {
        return this.indexes;
    }

    /**
     * Set the indexes property: List of index keys.
     * 
     * @param indexes the indexes value to set.
     * @return the MongoDBCollectionResource object itself.
     */
    public MongoDBCollectionResource withIndexes(List<MongoIndex> indexes) {
        this.indexes = indexes;
        return this;
    }

    /**
     * Get the analyticalStorageTtl property: Analytical TTL.
     * 
     * @return the analyticalStorageTtl value.
     */
    public Integer analyticalStorageTtl() {
        return this.analyticalStorageTtl;
    }

    /**
     * Set the analyticalStorageTtl property: Analytical TTL.
     * 
     * @param analyticalStorageTtl the analyticalStorageTtl value to set.
     * @return the MongoDBCollectionResource object itself.
     */
    public MongoDBCollectionResource withAnalyticalStorageTtl(Integer analyticalStorageTtl) {
        this.analyticalStorageTtl = analyticalStorageTtl;
        return this;
    }

    /**
     * Get the restoreParameters property: Parameters to indicate the information about the restore.
     * 
     * @return the restoreParameters value.
     */
    public ResourceRestoreParameters restoreParameters() {
        return this.restoreParameters;
    }

    /**
     * Set the restoreParameters property: Parameters to indicate the information about the restore.
     * 
     * @param restoreParameters the restoreParameters value to set.
     * @return the MongoDBCollectionResource object itself.
     */
    public MongoDBCollectionResource withRestoreParameters(ResourceRestoreParameters restoreParameters) {
        this.restoreParameters = restoreParameters;
        return this;
    }

    /**
     * Get the createMode property: Enum to indicate the mode of resource creation.
     * 
     * @return the createMode value.
     */
    public CreateMode createMode() {
        return this.createMode;
    }

    /**
     * Set the createMode property: Enum to indicate the mode of resource creation.
     * 
     * @param createMode the createMode value to set.
     * @return the MongoDBCollectionResource object itself.
     */
    public MongoDBCollectionResource withCreateMode(CreateMode createMode) {
        this.createMode = createMode;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (id() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property id in model MongoDBCollectionResource"));
        }
        if (indexes() != null) {
            indexes().forEach(e -> e.validate());
        }
        if (restoreParameters() != null) {
            restoreParameters().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(MongoDBCollectionResource.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeMapField("shardKey", this.shardKey, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("indexes", this.indexes, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeNumberField("analyticalStorageTtl", this.analyticalStorageTtl);
        jsonWriter.writeJsonField("restoreParameters", this.restoreParameters);
        jsonWriter.writeStringField("createMode", this.createMode == null ? null : this.createMode.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MongoDBCollectionResource from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MongoDBCollectionResource if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the MongoDBCollectionResource.
     */
    public static MongoDBCollectionResource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MongoDBCollectionResource deserializedMongoDBCollectionResource = new MongoDBCollectionResource();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedMongoDBCollectionResource.id = reader.getString();
                } else if ("shardKey".equals(fieldName)) {
                    Map<String, String> shardKey = reader.readMap(reader1 -> reader1.getString());
                    deserializedMongoDBCollectionResource.shardKey = shardKey;
                } else if ("indexes".equals(fieldName)) {
                    List<MongoIndex> indexes = reader.readArray(reader1 -> MongoIndex.fromJson(reader1));
                    deserializedMongoDBCollectionResource.indexes = indexes;
                } else if ("analyticalStorageTtl".equals(fieldName)) {
                    deserializedMongoDBCollectionResource.analyticalStorageTtl = reader.getNullable(JsonReader::getInt);
                } else if ("restoreParameters".equals(fieldName)) {
                    deserializedMongoDBCollectionResource.restoreParameters
                        = ResourceRestoreParameters.fromJson(reader);
                } else if ("createMode".equals(fieldName)) {
                    deserializedMongoDBCollectionResource.createMode = CreateMode.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedMongoDBCollectionResource;
        });
    }
}
