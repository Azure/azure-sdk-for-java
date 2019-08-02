// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosItemProperties extends Resource {

    private static final ObjectMapper mapper = Utils.getSimpleObjectMapper();

    /**
     * Initialize an empty CosmosItemProperties object.
     */
    public CosmosItemProperties() {
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the cosmos item properties with id set
     */
    public CosmosItemProperties id(String id) {
        super.id(id);
        return this;
    }

    /**
     * Initialize a CosmosItemProperties object from json string.
     *
     * @param jsonString the json string that represents the document object.
     */
    public CosmosItemProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * fromObject returns Document for compatibility with V2 sdk
     *
     * @param cosmosItem
     * @return
     */
    static Document fromObject(Object cosmosItem) {
        Document typedItem;
        if (cosmosItem instanceof CosmosItemProperties) {
            typedItem = new Document(((CosmosItemProperties) cosmosItem).toJson());
        } else {
            try {
                return new Document(CosmosItemProperties.mapper.writeValueAsString(cosmosItem));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }
        return typedItem;
    }

    static List<CosmosItemProperties> getFromV2Results(List<Document> results) {
        return results.stream().map(document -> new CosmosItemProperties(document.toJson()))
                .collect(Collectors.toList());
    }

    public <T> T getObject(Class<?> klass) throws IOException {
        return (T) mapper.readValue(this.toJson(), klass);
    }

}
