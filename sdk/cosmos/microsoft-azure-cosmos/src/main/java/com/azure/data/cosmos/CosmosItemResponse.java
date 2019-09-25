// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

public class CosmosItemResponse extends CosmosResponse<CosmosItemProperties> {
    private final CosmosAsyncItemResponse responseWrapper;
    private final CosmosItem item;


    CosmosItemResponse(CosmosAsyncItemResponse response, PartitionKey partitionKey, CosmosContainer container) {
        super(response.properties());
        this.responseWrapper = response;
        if (responseWrapper.item() != null) {
            this.item = new CosmosItem(responseWrapper.item().id(), partitionKey, container, responseWrapper.item());
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
    public CosmosItemProperties properties() {
        return responseWrapper.properties();
    }

    /**
     * Gets the CosmosAsyncItem
     *
     * @return the cosmos item
     */
    public CosmosItem item() {
        return item;
    }
}
