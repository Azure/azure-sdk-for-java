// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Note: although this class is public it is not part of our public API and may change in future.
 */
public class ItemOperations {

    /**
     * Note: although this method is public, it is not part of public API and may change in future.
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
        return readManyAsync(container.asyncContainer, itemKeyList, classType).block();
    }

    /**
     * Note: although this method is public, it is not part of public API and may change in future.
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


        FeedOptions options = new FeedOptions();
        options.setMaxDegreeOfParallelism(-1);
        return readManyInternal(container, itemKeyList, new FeedOptions(), classType);
    }

    static <T> Mono<FeedResponse<T>> readManyInternal(CosmosAsyncContainer container,
                                                      List<Pair<String, PartitionKey>> itemKeyList,
                                                      FeedOptions options,
                                                      Class<T> classType) {
        return container.getDatabase()
            .getDocClientWrapper()
            .readMany(itemKeyList, container.getLink(), options, classType);
    }
}
