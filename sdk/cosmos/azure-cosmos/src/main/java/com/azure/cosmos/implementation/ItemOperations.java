// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.List;

final public class ItemOperations {

    /**
     * Note: this method is deprecated - please use CosmosContainer.readMany instead
     * <p>
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param container   the cosmos async container
     * @param itemKeyList document id and partition key pair that needs to be read
     * @param classType   class type
     * @return a feed response of cosmos items
     */
    public static <T> FeedResponse<T> readMany(CosmosContainer container,
                                               List<Pair<String, PartitionKey>> itemKeyList,
                                               Class<T> classType) {
        return readManyAsync(CosmosBridgeInternal.getCosmosAsyncContainer(container), itemKeyList, classType).block();
    }

    /**
     * Note: this method is deprecated - please use CosmosAsyncContainer.readMany instead
     * <p>
     * Reads many documents.
     *
     * @param <T> the type parameter
     * @param container   the cosmos async container
     * @param itemKeyList document id and partition key pair that needs to be read
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public static <T> Mono<FeedResponse<T>> readManyAsync(CosmosAsyncContainer container,
                                                          List<Pair<String, PartitionKey>> itemKeyList,
                                                          Class<T> classType) {

        if (container == null) {
            throw new IllegalArgumentException("Parameter container is required and cannot be null.");
        }

        return container.readMany(itemKeyList, classType);
    }

    private ItemOperations() {}
}
