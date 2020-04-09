// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedProcessorBuilderImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

/**
 * Simple host for distributing change feed events across observers and thus allowing these observers scale.
 * It distributes the load across its instances and allows dynamic scaling:
 * - Partitions in partitioned collections are distributed across instances/observers.
 * - New instance takes leases from existing instances to make distribution equal.
 * - If an instance dies, the leases are distributed across remaining instances.
 * It's useful for scenario when partition count is high so that one host/VM is not capable of processing that many
 * change feed events.
 * Client application needs to implement {@link ChangeFeedObserver} and register processor implementation with
 * {@link ChangeFeedProcessor}.
 * <p>
 * It uses auxiliary document collection for managing leases for a partition.
 * Every EventProcessorHost instance is performing the following two tasks:
 * 1) Renew Leases: It keeps track of leases currently owned by the host and continuously keeps on renewing the leases.
 * 2) Acquire Leases: Each instance continuously polls all leases to check if there are any leases it should acquire
 * for the system to get into balanced state.
 * <p>
 * {@code
 * ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
 *     .hostName(hostName)
 *     .feedContainer(feedContainer)
 *     .leaseContainer(leaseContainer)
 *     .handleChanges(docs -> {
 *         // Implementation for handling and processing CosmosItemProperties list goes here
 *      })
 *     .build();
 * }
 */
public interface ChangeFeedProcessor {

    /**
     * Start listening for changes asynchronously.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> start();

    /**
     * Stops listening for changes asynchronously.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> stop();

    /**
     * Returns the state of the change feed processor.
     *
     * @return true if the change feed processor is currently active and running.
     */
    boolean isStarted();

    /**
     * Helper static method to buildAsyncClient {@link ChangeFeedProcessor} instances
     * as logical representation of the Azure Cosmos DB database service.
     * <p>
     * {@code
     * ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
     *     .hostName(hostName)
     *     .feedContainer(feedContainer)
     *     .leaseContainer(leaseContainer)
     *     .handleChanges(docs -> {
     *         // Implementation for handling and processing CosmosItemProperties list goes here
     *      })
     *     .build();
     * }
     * @return a changeFeedProcessorBuilder definition instance.
     */
    static BuilderDefinition changeFeedProcessorBuilder() {
        return new ChangeFeedProcessorBuilderImpl();
    }

    /**
     * The {@link ChangeFeedProcessor} changeFeedProcessorBuilder definitions for setting the properties.
     */
    interface BuilderDefinition {
        /**
         * Sets the host name.
         *
         * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique
         * name.
         * @return current Builder.
         */
        BuilderDefinition hostName(String hostName);

        /**
         * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored collection.
         *
         * @param feedContainer the instance of {@link CosmosAsyncContainer} to be used.
         * @return current Builder.
         */
        BuilderDefinition feedContainer(CosmosAsyncContainer feedContainer);

        /**
         * Sets the {@link ChangeFeedProcessorOptions} to be used.
         * <p>
         * Unless specifically set the default values that will be used are:
         * - maximum items per page or FeedResponse: 100
         * - lease renew interval: 17 seconds
         * - lease acquire interval: 13 seconds
         * - lease expiration interval: 60 seconds
         * - feed poll delay: 5 seconds
         * - maximum scale count: unlimited
         *
         * @param changeFeedProcessorOptions the change feed processor options to use.
         * @return current Builder.
         */
        BuilderDefinition options(ChangeFeedProcessorOptions changeFeedProcessorOptions);

        /**
         * Sets a consumer function which will be called to process changes.
         *
         * @param consumer the consumer of {@link ChangeFeedObserver} to call for handling the feeds.
         * @return current Builder.
         */
        BuilderDefinition handleChanges(Consumer<List<JsonNode>> consumer);

        /**
         * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases collection.
         *
         * @param leaseContainer the instance of {@link CosmosAsyncContainer} to use.
         * @return current Builder.
         */
        BuilderDefinition leaseContainer(CosmosAsyncContainer leaseContainer);

        /**
         * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration asynchronously.
         *
         * @return an instance of {@link ChangeFeedProcessor}.
         */
        ChangeFeedProcessor build();
    }
}
