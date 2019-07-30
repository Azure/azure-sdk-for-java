// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosItemResponse extends CosmosResponse<CosmosItemProperties>{
    private CosmosItem itemClient;

    CosmosItemResponse(ResourceResponse<Document> response, PartitionKey partitionKey, CosmosContainer container) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosItemProperties(response.getResource().toJson()));
            itemClient = new CosmosItem(response.getResource().id(),partitionKey, container);
        }
    }

    /**
     * Gets the itemSettings
     * @return the itemSettings
     */
    public CosmosItemProperties properties() {
        return resourceSettings();
    }

    /**
     * Gets the CosmosItem
     * @return the cosmos item
     */
    public CosmosItem item() {
        return itemClient;
    }
}