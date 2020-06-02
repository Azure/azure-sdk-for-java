// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.QueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.List;

final public class ItemOperations {

    /**
     * Note: although this method is public, this API may change in future.
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
     * Note: although this method is public, this API may change in future.
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


        QueryRequestOptions options = new QueryRequestOptions();
        options.setMaxDegreeOfParallelism(-1);
        return readManyInternal(container, itemKeyList, new QueryRequestOptions(), classType);
    }

    static <T> Mono<FeedResponse<T>> readManyInternal(CosmosAsyncContainer container,
                                                      List<Pair<String, PartitionKey>> itemKeyList,
                                                      QueryRequestOptions options,
                                                      Class<T> classType) {

        return CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase())
            .readMany(itemKeyList, BridgeInternal.getLink(container), options, classType);
    }

    private ItemOperations() {}
}
