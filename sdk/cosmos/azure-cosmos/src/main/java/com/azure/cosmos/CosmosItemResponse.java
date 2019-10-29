// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

public class CosmosItemResponse extends CosmosResponse<CosmosItemProperties> {
    private final CosmosAsyncItemResponse responseWrapper;
    private final CosmosItem item;


    CosmosItemResponse(CosmosAsyncItemResponse response, PartitionKey partitionKey, CosmosContainer container) {
        super(response.resourceResponseWrapper, response.getProperties());
        this.responseWrapper = response;
        if (responseWrapper.getItem() != null) {
            this.item = new CosmosItem(responseWrapper.getItem().getId(), partitionKey, container, responseWrapper.getItem());
        } else {
            // Delete will have null container client in response
            this.item = null;
        }
    }

    /**
     * Gets the itemSettings
     *
     * @return the itemSettings
     */
    public CosmosItemProperties getProperties() {
        return responseWrapper.getProperties();
    }

    /**
     * Gets the CosmosAsyncItem
     *
     * @return the cosmos item
     */
    public CosmosItem getItem() {
        return item;
    }
}
