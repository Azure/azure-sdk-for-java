// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.PartitionKey;

public class CosmosSyncItemResponse {
    private final CosmosItemResponse responseWrapper;
    private final CosmosSyncItem item;


    CosmosSyncItemResponse(CosmosItemResponse response, PartitionKey partitionKey, CosmosSyncContainer container) {
        this.responseWrapper = response;
        if (responseWrapper.item() != null) {
            this.item = new CosmosSyncItem(responseWrapper.item().id(), partitionKey, container, responseWrapper.item());
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
     * Gets the CosmosItem
     *
     * @return the cosmos item
     */
    public CosmosSyncItem item() {
        return item;
    }
}