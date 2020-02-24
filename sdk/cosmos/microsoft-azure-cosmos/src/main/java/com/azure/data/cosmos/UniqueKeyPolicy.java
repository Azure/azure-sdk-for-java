// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents the unique key policy configuration for specifying uniqueness constraints on documents in the
 * collection in the Azure Cosmos DB service.
 */
public class UniqueKeyPolicy extends JsonSerializable {
    private List<UniqueKey> uniqueKeys;

    public UniqueKeyPolicy() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the Unique Key policy.
     */
    UniqueKeyPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets or sets collection of {@link UniqueKey} that guarantee uniqueness of documents in collection
     * in the Azure Cosmos DB service.
     *
     * @return the unique keys.
     */
    public Collection<UniqueKey> uniqueKeys() {
        if (this.uniqueKeys == null) {
            this.uniqueKeys = super.getList(Constants.Properties.UNIQUE_KEYS, UniqueKey.class);
            if (this.uniqueKeys == null) {
                this.uniqueKeys = new ArrayList<>();
            }
        }
        return this.uniqueKeys;
    }

    public UniqueKeyPolicy uniqueKeys(List<UniqueKey> uniqueKeys) {
        if (uniqueKeys == null) {
            throw new IllegalArgumentException("uniqueKeys cannot be null.");
        }
        this.uniqueKeys = uniqueKeys;
        return this;
    }

    @Override
    void populatePropertyBag() {
        if (this.uniqueKeys != null) {
            for(UniqueKey uniqueKey: uniqueKeys) {
                uniqueKey.populatePropertyBag();
            }
            super.set(Constants.Properties.UNIQUE_KEYS, uniqueKeys);
        }
    }
}
