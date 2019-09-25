// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Conflict;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosAsyncConflictResponse extends CosmosResponse<CosmosConflictProperties> {
    private CosmosAsyncContainer container;
    private CosmosAsyncConflict conflictClient;

    CosmosAsyncConflictResponse(ResourceResponse<Conflict> response, CosmosAsyncContainer container) {
        super(response);
        this.container = container;
        if(response.getResource() == null){
            super.properties(null);
        }else{
            super.properties(new CosmosConflictProperties(response.getResource().toJson()));
            conflictClient = new CosmosAsyncConflict(response.getResource().id(), container);
        }
    }

    CosmosAsyncContainer getContainer() {
        return container;
    }

    /**
     * Get conflict client
     * @return the cosmos conflict client
     */
    public CosmosAsyncConflict conflict() {
        return conflictClient;
    }

    /**
     * Get conflict properties object representing the resource on the server
     * @return the conflict properties
     */
    public CosmosConflictProperties properties() {
        return this.properties();
    }
}
