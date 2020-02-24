// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemRequestOptions;

/**
 * The Cosmos synchronous item.
 */
public class CosmosSyncItem {
    private final CosmosSyncContainer container;
    private final CosmosItem asyncItem;
    private final String id;
    private final Object partitionKey;

    /**
     * Instantiates a new Cosmos sync item.
     *
     * @param id the id
     * @param partitionKey the partition key
     * @param cosmosSyncContainer the cosmos sync container
     * @param item the item
     */
    CosmosSyncItem(String id, Object partitionKey, CosmosSyncContainer cosmosSyncContainer, CosmosItem item) {
        this.id = id;
        this.partitionKey = partitionKey;
        this.container = cosmosSyncContainer;
        this.asyncItem = item;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /**
     * Partition key object.
     *
     * @return the object
     */
    public Object partitionKey() {
        return partitionKey;
    }

    /**
     * Read cosmos sync item response.
     *
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse read(CosmosItemRequestOptions options) throws CosmosClientException {
        return container.mapItemResponseAndBlock(asyncItem.read(options));
    }

    /**
     * Replace cosmos sync item response.
     *
     * @param item the item
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse replace(Object item, CosmosItemRequestOptions options) throws CosmosClientException {
        return container.mapItemResponseAndBlock(asyncItem.replace(item, options));
    }

    /**
     * Delete cosmos sync item response.
     *
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncItemResponse delete(CosmosItemRequestOptions options) throws CosmosClientException {
        return container.mapItemResponseAndBlock(asyncItem.delete(options));
    }


}
