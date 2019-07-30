// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosContainerResponse extends CosmosResponse<CosmosContainerProperties> {

    private CosmosContainer container;

    CosmosContainerResponse(ResourceResponse<DocumentCollection> response, CosmosDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosContainerProperties(response));
            container = new CosmosContainer(resourceSettings().id(), database);
        }
    }

    /**
     * Gets the progress of an index transformation, if one is underway.
     *
     * @return the progress of an index transformation.
     */
    public long indexTransformationProgress() {
        return resourceResponseWrapper.getIndexTransformationProgress();
    }

    /**
     * Gets the progress of lazy indexing.
     *
     * @return the progress of lazy indexing.
     */
    long lazyIndexingProgress() {
        return resourceResponseWrapper.getLazyIndexingProgress();
    }

    /**
     * Gets the container properties
     * @return the cosmos container properties
     */
    public CosmosContainerProperties properties() {
        return resourceSettings();
    }

    /**
     * Gets the Container object
     * @return the Cosmos container object
     */
    public CosmosContainer container() {
        return container;
    }
}
