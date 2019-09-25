// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosAsyncItemResponse extends CosmosResponse<CosmosItemProperties>{
    private CosmosAsyncItem itemClient;

    CosmosAsyncItemResponse(ResourceResponse<Document> response, PartitionKey partitionKey, CosmosAsyncContainer container) {
        super(response);
        if(response.getResource() == null){
            super.properties(null);
        }else{
            super.properties(new CosmosItemProperties(response.getResource().toJson()));
            itemClient = new CosmosAsyncItem(response.getResource().id(),partitionKey, container);
        }
    }

    /**
     * Gets the itemSettings
     * @return the itemSettings
     */
    public CosmosItemProperties properties() {
        return this.properties();
    }

    /**
     * Gets the CosmosAsyncItem
     * @return the cosmos item
     */
    public CosmosAsyncItem item() {
        return itemClient;
    }
}
