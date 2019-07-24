// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Conflict;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosConflictResponse extends CosmosResponse<CosmosConflictProperties> {
    private CosmosContainer container;
    private CosmosConflict conflictClient;

    CosmosConflictResponse(ResourceResponse<Conflict> response, CosmosContainer container) {
        super(response);
        this.container = container;
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosConflictProperties(response.getResource().toJson()));
            conflictClient = new CosmosConflict(response.getResource().id(), container);
        }
    }

    CosmosContainer getContainer() {
        return container;
    }

    /**
     * Get conflict client
     * @return the cosmos conflict client
     */
    public CosmosConflict conflict() {
        return conflictClient;
    }

    /**
     * Get conflict properties object representing the resource on the server
     * @return the conflict properties
     */
    public CosmosConflictProperties properties() {
        return resourceSettings();
    }
}