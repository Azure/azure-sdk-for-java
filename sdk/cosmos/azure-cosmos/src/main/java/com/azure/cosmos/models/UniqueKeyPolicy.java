// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the unique key policy configuration for specifying uniqueness constraints on items in the
 * container in the Azure Cosmos DB service.
 */
public final class UniqueKeyPolicy {
    private List<UniqueKey> uniqueKeys;

    private JsonSerializable jsonSerializable;

    /**
     * Instantiates a new Unique key policy.
     */
    public UniqueKeyPolicy() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the Unique Key policy.
     */
    UniqueKeyPolicy(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Constructor.
     *
     * @param objectNode the json string that represents the Unique Key policy.
     */
    UniqueKeyPolicy(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets or sets container of {@link UniqueKey} that guarantee uniqueness of items in container
     * in the Azure Cosmos DB service.
     *
     * @return the unique keys.
     */
    public List<UniqueKey> getUniqueKeys() {
        if (this.uniqueKeys == null) {
            this.uniqueKeys = this.jsonSerializable.getList(Constants.Properties.UNIQUE_KEYS, UniqueKey.class);
            if (this.uniqueKeys == null) {
                this.uniqueKeys = new ArrayList<>();
            }
        }
        return this.uniqueKeys;
    }

    /**
     * Unique keys unique key policy.
     *
     * @param uniqueKeys the unique keys
     * @return the unique key policy
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public UniqueKeyPolicy setUniqueKeys(List<UniqueKey> uniqueKeys) {
        if (uniqueKeys == null) {
            throw new IllegalArgumentException("uniqueKeys cannot be null.");
        }
        this.uniqueKeys = uniqueKeys;
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        if (this.uniqueKeys != null) {
            for (UniqueKey uniqueKey : uniqueKeys) {
                uniqueKey.populatePropertyBag();
            }
            this.jsonSerializable.set(
                Constants.Properties.UNIQUE_KEYS,
                uniqueKeys,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
