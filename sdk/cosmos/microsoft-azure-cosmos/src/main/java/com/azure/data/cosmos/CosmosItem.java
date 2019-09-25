// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * The Cosmos synchronous item.
 */
public class CosmosItem {
    private final CosmosContainer container;
    private final CosmosAsyncItem asyncItem;
    private final String id;
    private final Object partitionKey;

    /**
     * Instantiates a new Cosmos sync item.
     *
     * @param id the id
     * @param partitionKey the partition key
     * @param cosmosContainer the cosmos sync container
     * @param item the item
     */
    CosmosItem(String id, Object partitionKey, CosmosContainer cosmosContainer, CosmosAsyncItem item) {
        this.id = id;
        this.partitionKey = partitionKey;
        this.container = cosmosContainer;
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
    public CosmosItemResponse read(CosmosItemRequestOptions options) throws CosmosClientException {
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
    public CosmosItemResponse replace(Object item, CosmosItemRequestOptions options) throws CosmosClientException {
        return container.mapItemResponseAndBlock(asyncItem.replace(item, options));
    }

    /**
     * Delete cosmos sync item response.
     *
     * @param options the options
     * @return the cosmos sync item response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosItemResponse delete(CosmosItemRequestOptions options) throws CosmosClientException {
        return container.mapItemResponseAndBlock(asyncItem.delete(options));
    }


}
