// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosAsyncContainerResponse extends CosmosResponse<CosmosContainerProperties> {

    private CosmosAsyncContainer container;

    CosmosAsyncContainerResponse(ResourceResponse<DocumentCollection> response, CosmosAsyncDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.setProperties(null);
        }else{
            super.setProperties(new CosmosContainerProperties(response));
            container = new CosmosAsyncContainer(this.getProperties().getId(), database);
        }
    }

    /**
     * Gets the progress of an index transformation, if one is underway.
     *
     * @return the progress of an index transformation.
     */
    public long getIndexTransformationProgress() {
        return resourceResponseWrapper.getIndexTransformationProgress();
    }

    /**
     * Gets the progress of lazy indexing.
     *
     * @return the progress of lazy indexing.
     */
    long getLazyIndexingProgress() {
        return resourceResponseWrapper.getLazyIndexingProgress();
    }

    /**
     * Gets the container properties
     * @return the cosmos container properties
     */
    public CosmosContainerProperties getProperties() {
        return super.getProperties();
    }

    /**
     * Gets the Container object
     * @return the Cosmos container object
     */
    public CosmosAsyncContainer getContainer() {
        return container;
    }
}
