// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Represents an entry in the read-only {@code materializedViews} list returned from the Azure Cosmos DB service
 * when reading a container. Each entry identifies a materialized view container derived from the source container.
 * <p>
 * Example JSON representation:
 * <pre>{@code
 * "materializedViews": [{ "id": "gsi_testcontainer1", "_rid": "TughAMEOdUI=" }]
 * }</pre>
 */
public final class CosmosGlobalSecondaryIndexView {

    @JsonProperty(Constants.Properties.ID)
    private String id;

    @JsonProperty(Constants.Properties.R_ID)
    private String resourceId;

    /**
     * Constructor
     */
    public CosmosGlobalSecondaryIndexView() {
    }

    /**
     * Gets the id of the materialized view container.
     *
     * @return the id of the materialized view container.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the resource id (_rid) of the materialized view container.
     *
     * @return the resource id of the materialized view container.
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
