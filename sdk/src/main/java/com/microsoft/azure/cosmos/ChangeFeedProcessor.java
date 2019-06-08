/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmos.changefeed.HealthMonitor;
import com.microsoft.azure.cosmos.changefeed.LeaseStoreManager;
import com.microsoft.azure.cosmos.changefeed.PartitionLoadBalancingStrategy;
import com.microsoft.azure.cosmos.changefeed.PartitionProcessor;
import com.microsoft.azure.cosmos.changefeed.PartitionProcessorFactory;
import com.microsoft.azure.cosmos.changefeed.internal.ChangeFeedProcessorBuilderImpl;

import reactor.core.publisher.Mono;
import java.util.concurrent.ExecutorService;

/**
 * Simple host for distributing change feed events across observers and thus allowing these observers scale.
 * It distributes the load across its instances and allows dynamic scaling:
 *   - Partitions in partitioned collections are distributed across instances/observers.
 *   - New instance takes leases from existing instances to make distribution equal.
 *   - If an instance dies, the leases are distributed across remaining instances.
 * It's useful for scenario when partition count is high so that one host/VM is not capable of processing that many change feed events.
 * Client application needs to implement {@link ChangeFeedObserver} and register processor implementation with {@link ChangeFeedProcessor}.
 * <p>
 * It uses auxiliary document collection for managing leases for a partition.
 * Every EventProcessorHost instance is performing the following two tasks:
 *     1) Renew Leases: It keeps track of leases currently owned by the host and continuously keeps on renewing the leases.
 *     2) Acquire Leases: Each instance continuously polls all leases to check if there are any leases it should acquire
 *     for the system to get into balanced state.
 * <p>
 * {@code
 * ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
 *     .withHostName(hostName)
 *     .withFeedContainerClient(feedContainer)
 *     .withLeaseContainerClient(leaseContainer)
 *     .withChangeFeedObserver(SampleObserverImpl.class)
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
     * Helper static method to build {@link ChangeFeedProcessor} instances
     * as logical representation of the Azure Cosmos DB database service.
     * <p>
     * {@code
     *
     *  ChangeFeedProcessor.Builder()
     *       .withHostName("SampleHost")
     *       .withFeedContainerClient(feedContainer)
     *       .withLeaseContainerClient(leaseContainer)
     *       .withChangeFeedObserver(SampleObserverImpl.class)
     *       .build();
     * }
     *
     * @return a builder definition instance.
     */
    static BuilderDefinition Builder() {
        return new ChangeFeedProcessorBuilderImpl();
    }

    interface BuilderDefinition {
        /**
         * Sets the host name.
         *
         * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique name.
         * @return current Builder.
         */
        BuilderDefinition withHostName(String hostName);

        /**
         * Sets and existing {@link CosmosContainer} to be used to read from the monitored collection.
         *
         * @param feedContainerClient the instance of {@link CosmosContainer} to be used.
         * @return current Builder.
         */
        BuilderDefinition withFeedContainerClient(CosmosContainer feedContainerClient);

        /**
         * Sets the {@link ChangeFeedProcessorOptions} to be used.
         *
         * @param changeFeedProcessorOptions the change feed processor options to use.
         * @return current Builder.
         */
        BuilderDefinition withProcessorOptions(ChangeFeedProcessorOptions changeFeedProcessorOptions);

        /**
         * Sets the {@link ChangeFeedObserverFactory} to be used to generate {@link ChangeFeedObserver}
         *
         * @param observerFactory The instance of {@link ChangeFeedObserverFactory} to use.
         * @return current Builder.
         */
        BuilderDefinition withChangeFeedObserverFactory(ChangeFeedObserverFactory observerFactory);

        /**
         * Sets an existing {@link ChangeFeedObserver} type to be used by a {@link ChangeFeedObserverFactory} to process changes.
         *
         * @param type the type of {@link ChangeFeedObserver} to be used.
         * @return current Builder.
         */
        BuilderDefinition withChangeFeedObserver(Class<? extends ChangeFeedObserver> type);

        /**
         * Sets an existing {@link CosmosContainer} to be used to read from the leases collection.
         *
         * @param leaseCosmosClient the instance of {@link CosmosContainer} to use.
         * @return current Builder.
         */
        BuilderDefinition withLeaseContainerClient(CosmosContainer leaseCosmosClient);

        /**
         * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration asynchronously.
         *
         * @return an instance of {@link ChangeFeedProcessor}.
         */
        Mono<ChangeFeedProcessor> build();
    }
}
