// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a unique key on that enforces uniqueness constraint on documents in the collection in the Azure Cosmos
 * DB service.
 * <p>
 * 1) For partitioned collections, the value of partition key is implicitly a part of each unique key.
 * 2) Uniqueness constraint is also enforced for missing values.
 * For instance, if unique key policy defines a unique key with single property path, there could be only one
 * document that has missing value for this property.
 *
 * @see UniqueKeyPolicy
 */
public final class UniqueKey extends JsonSerializableWrapper{
    private List<String> paths;

    /**
     * Instantiates a new Unique key.
     */
    public UniqueKey() {
        this.jsonSerializable = new JsonSerializable();
    }

    UniqueKey(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Gets the paths, a set of which must be unique for each document in the Azure Cosmos DB service.
     * <p>
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the document,
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
     * Sets the paths, a set of which must be unique for each document in the Azure Cosmos DB service.
     * <p>
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the document,
     * such as "/name/first".
     *
     * @param paths the unique paths.
     * @return the Unique Key.
     */
    public UniqueKey setPaths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    @Override
    protected void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        if (paths != null) {
            this.jsonSerializable.set(Constants.Properties.PATHS, paths);
        }
    }
}
