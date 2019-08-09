// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.ResourceResponse;

/**
 * The synchronous cosmos container response
 */
public class CosmosSyncContainerResponse {

    private final CosmosContainerResponse responseWrapper;
    private final CosmosSyncContainer container;

    CosmosSyncContainerResponse(CosmosContainerResponse response, CosmosSyncDatabase database, CosmosSyncClient client) {
        this.responseWrapper = response;
        if (responseWrapper.container() != null) {
            this.container = new CosmosSyncContainer(responseWrapper.container().id(), database, responseWrapper.container());
        } else {
            // Delete will have null container client in response
            this.container = null;
        }
    }

    /**
     * Gets the progress of an index transformation, if one is underway.
     *
     * @return the progress of an index transformation.
     */
    public long indexTransformationProgress() {
        return responseWrapper.indexTransformationProgress();
    }

    /**
     * Gets the container properties
     *
     * @return the cosmos container properties
     */
    public CosmosContainerProperties properties() {
        return responseWrapper.properties();
    }

    /**
     * Gets the Container object
     *
     * @return the Cosmos container object
     */
    public CosmosSyncContainer container() {
        return container;
    }
}
