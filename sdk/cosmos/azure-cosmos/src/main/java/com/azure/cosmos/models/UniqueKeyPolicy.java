// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the unique key policy configuration for specifying uniqueness constraints on documents in the
 * collection in the Azure Cosmos DB service.
 */
public final class UniqueKeyPolicy extends JsonSerializable {
    private List<UniqueKey> uniqueKeys;

    /**
     * Instantiates a new Unique key policy.
     */
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
    public List<UniqueKey> getUniqueKeys() {
        if (this.uniqueKeys == null) {
            this.uniqueKeys = super.getList(Constants.Properties.UNIQUE_KEYS, UniqueKey.class);
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

    @Override
    protected void populatePropertyBag() {
        super.populatePropertyBag();
        if (this.uniqueKeys != null) {
            for (UniqueKey uniqueKey : uniqueKeys) {
                uniqueKey.populatePropertyBag();
            }
            super.set(Constants.Properties.UNIQUE_KEYS, uniqueKeys);
        }
    }
}
