// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.internal.Conflict;
import com.azure.cosmos.internal.ResourceResponse;
import com.azure.cosmos.internal.Conflict;
import com.azure.cosmos.internal.ResourceResponse;

public class CosmosAsyncConflictResponse extends CosmosResponse<CosmosConflictProperties> {
    private CosmosAsyncContainer container;
    private CosmosAsyncConflict conflictClient;

    CosmosAsyncConflictResponse(ResourceResponse<Conflict> response, CosmosAsyncContainer container) {
        super(response);
        this.container = container;
        if(response.getResource() == null){
            super.setProperties(null);
        }else{
            super.setProperties(new CosmosConflictProperties(response.getResource().toJson()));
            conflictClient = new CosmosAsyncConflict(response.getResource().getId(), container);
        }
    }

    CosmosAsyncContainer getContainer() {
        return container;
    }

    /**
     * Get conflict client
     * @return the cosmos conflict client
     */
    public CosmosAsyncConflict getConflict() {
        return conflictClient;
    }

    /**
     * Get conflict properties object representing the resource on the server
     * @return the conflict properties
     */
    public CosmosConflictProperties getProperties() {
        return this.getProperties();
    }
}
