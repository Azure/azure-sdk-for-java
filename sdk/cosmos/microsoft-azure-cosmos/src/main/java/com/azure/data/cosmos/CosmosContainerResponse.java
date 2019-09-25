// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * The synchronous cosmos container response
 */
public class CosmosContainerResponse extends CosmosResponse<CosmosContainerProperties> {

    private final CosmosAsyncContainerResponse responseWrapper;
    private final CosmosContainer container;

    CosmosContainerResponse(CosmosAsyncContainerResponse response, CosmosDatabase database, CosmosClient client) {
        super(response.properties());
        this.responseWrapper = response;
        if (responseWrapper.container() != null) {
            this.container = new CosmosContainer(responseWrapper.container().id(), database, responseWrapper.container());
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
    public CosmosContainer container() {
        return container;
    }
}
