// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

public class CosmosItemResponse<T> extends CosmosResponse<CosmosItemProperties> {
    private final CosmosAsyncItemResponse responseWrapper;

    CosmosItemResponse(CosmosAsyncItemResponse response) {
        super(response.resourceResponseWrapper, response.getProperties());
        this.responseWrapper = response;
    }

    @SuppressWarnings("unchecked")
    public T getResource(){
        return (T) responseWrapper.getResource();
    }
    
    /**
     * Gets the itemSettings
     *
     * @return the itemSettings
     */
    public CosmosItemProperties getProperties() {
        return responseWrapper.getProperties();
    }

}
