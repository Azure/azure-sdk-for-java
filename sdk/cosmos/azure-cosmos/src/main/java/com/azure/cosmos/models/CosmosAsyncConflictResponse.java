// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncConflict;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

/**
 * The type Cosmos async conflict response.
 */
public class CosmosAsyncConflictResponse extends CosmosResponse<CosmosConflictProperties> {
    private final CosmosAsyncContainer container;
    private final CosmosAsyncConflict conflictClient;

    CosmosAsyncConflictResponse(ResourceResponse<Conflict> response, CosmosAsyncContainer container) {
        super(response);
        this.container = container;
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
            conflictClient = null;
        } else {
            CosmosConflictProperties props = new CosmosConflictProperties(bodyAsString);
            super.setProperties(props);
            conflictClient = BridgeInternal.createCosmosAsyncConflict(ModelBridgeInternal.getResourceFromResourceWrapper(props).getId(), container);
        }
    }

    CosmosAsyncContainer getContainer() {
        return container;
    }

    /**
     * Get conflict client
     *
     * @return the cosmos conflict client
     */
    public CosmosAsyncConflict getConflict() {
        return conflictClient;
    }

    /**
     * Get conflict properties object representing the resource on the server
     *
     * @return the conflict properties
     */
    public CosmosConflictProperties getProperties() {
        return this.getProperties();
    }
}
