// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * The synchronous cosmos container response
 */
public class CosmosContainerResponse extends CosmosResponse<CosmosContainerProperties> {

    private final CosmosAsyncContainerResponse responseWrapper;
    private final CosmosContainer container;

    CosmosContainerResponse(CosmosAsyncContainerResponse response, CosmosDatabase database, CosmosClient client) {
        super(response.getProperties());
        this.responseWrapper = response;
        if (responseWrapper.getContainer() != null) {
            this.container = new CosmosContainer(responseWrapper.getContainer().getId(), database, responseWrapper.getContainer());
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
    public long getIndexTransformationProgress() {
        return responseWrapper.getIndexTransformationProgress();
    }

    /**
     * Gets the container properties
     *
     * @return the cosmos container properties
     */
    public CosmosContainerProperties getProperties() {
        return responseWrapper.getProperties();
    }

    /**
     * Gets the Container object
     *
     * @return the Cosmos container object
     */
    public CosmosContainer getContainer() {
        return container;
    }
}
