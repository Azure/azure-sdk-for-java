// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a unique key on that enforces uniqueness constraint on documents in the collection in the Azure Cosmos DB service.
 *
 * 1) For partitioned collections, the value of partition key is implicitly a part of each unique key.
 * 2) Uniqueness constraint is also enforced for missing values.
 * For instance, if unique key policy defines a unique key with single property path, there could be only one document that has missing value for this property.
 * @see UniqueKeyPolicy
 */
public class UniqueKey extends JsonSerializable {
    private List<String> paths;

    public UniqueKey() {
        super();
    }

    UniqueKey(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the paths, a set of which must be unique for each document in the Azure Cosmos DB service.
     *
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the document,
     * such as "/name/first".
     *
     * @return the unique paths.
     */
    public Collection<String> paths() {
        if (this.paths == null) {
            this.paths = super.getList(Constants.Properties.PATHS, String.class);

            if (this.paths == null) {
                this.paths = new ArrayList<String>();
            }
        }

        return this.paths;
    }


    /**
     * Sets the paths, a set of which must be unique for each document in the Azure Cosmos DB service.
     *
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the document,
     * such as "/name/first".
     *
     * @param paths the unique paths.
     * @return the Unique Key.
     */
    public UniqueKey paths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    @Override
    void populatePropertyBag() {
        if (paths != null) {
            super.set(Constants.Properties.PATHS, paths);
        }
    }
}
