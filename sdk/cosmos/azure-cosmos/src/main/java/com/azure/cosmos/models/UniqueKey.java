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
 * Represents a unique key on that enforces uniqueness constraint on items in the container in the Azure Cosmos
 * DB service.
 * <p>
 * 1) For containers, the value of partition key is implicitly a part of each unique key.
 * <p>
 * 2) Uniqueness constraint is also enforced for missing values.
 * <p>
 * For instance, if unique key policy defines a unique key with single property path, there could be only one
 * item that has missing value for this property.
 *
 * @see UniqueKeyPolicy
 */
public final class UniqueKey {
    private List<String> paths;

    private JsonSerializable jsonSerializable;

    /**
     * Instantiates a new Unique key with paths.
     * @param paths the unique paths.
     */
    public UniqueKey(List<String> paths) {
        this.jsonSerializable = new JsonSerializable();
        this.paths = paths;
    }

    /**
     * Initializes a new instance of the UniqueKey class.
     *
     * @param jsonString the json string that represents the included path.
     */
    UniqueKey(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Initializes a new instance of the UniqueKey class.
     *
     * @param objectNode the object node that represents the included path.
     */
    UniqueKey(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets the paths, a set of which must be unique for each item in the Azure Cosmos DB service.
     * <p>
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the item,
     * such as "/name/first".
     *
     * @return the unique paths.
     */
    public List<String> getPaths() {
        if (this.paths == null) {
            this.paths = this.jsonSerializable.getList(Constants.Properties.PATHS, String.class);

            if (this.paths == null) {
                this.paths = new ArrayList<String>();
            }
        }

        return this.paths;
    }


    /**
     * Sets the paths, a set of which must be unique for each item in the Azure Cosmos DB service.
     * <p>
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the item,
     * such as "/name/first".
     *
     * @param paths the unique paths.
     * @return the Unique Key.
     */
    public UniqueKey setPaths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        if (paths != null) {
            this.jsonSerializable.set(Constants.Properties.PATHS, paths, CosmosItemSerializer.DEFAULT_SERIALIZER);
        }
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
