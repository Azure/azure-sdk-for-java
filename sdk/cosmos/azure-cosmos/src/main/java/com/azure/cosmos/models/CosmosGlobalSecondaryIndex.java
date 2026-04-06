// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Represents an entry in the read-only {@code globalSecondaryIndexes} list returned from the Azure Cosmos DB service
 * when reading a container. Each entry identifies a globalSecondaryIndex container derived from the source container.
 * <p>
 * Example JSON representation:
 * <pre>{@code
 * "globalSecondaryIndexes": [{ "id": "gsi_testcontainer1", "_rid": "TughAMEOdUI=" }]
 * }</pre>
 */
public final class CosmosGlobalSecondaryIndex {

    @JsonProperty(Constants.Properties.ID)
    private String id;

    @JsonProperty(Constants.Properties.R_ID)
    private String resourceId;

    /**
     * Constructor
     */
    public CosmosGlobalSecondaryIndex() {
    }

    /**
     * Gets the id of the globalSecondaryIndex container.
     *
     * @return the id of the globalSecondaryIndex container.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the resource id (_rid) of the globalSecondaryIndex container.
     *
     * @return the resource id of the globalSecondaryIndex container.
     */
    public String getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }
}
