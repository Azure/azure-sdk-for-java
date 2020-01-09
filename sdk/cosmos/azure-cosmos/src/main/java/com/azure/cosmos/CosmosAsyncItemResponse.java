// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceResponse;

public class CosmosAsyncItemResponse extends CosmosResponse<CosmosItemProperties> {
    private final CosmosAsyncItem itemClient;

    CosmosAsyncItemResponse(ResourceResponse<Document> response, PartitionKey partitionKey,
                            CosmosAsyncContainer container) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (bodyAsString == null) {
            super.setProperties(null);
            itemClient = null;
        } else {
            CosmosItemProperties props = new CosmosItemProperties(bodyAsString);
            super.setProperties(props);
            itemClient = new CosmosAsyncItem(props.getId(), partitionKey, container);
        }
    }

    /**
     * Gets the itemSettings
     *
     * @return the itemSettings
     */
    public CosmosItemProperties getProperties() {
        return super.getProperties();
    }
    
    /**
     * Gets the CosmosAsyncItem
     *
     * @return the cosmos item
     */
    public CosmosAsyncItem getItem() {
        return itemClient;
    }
}
