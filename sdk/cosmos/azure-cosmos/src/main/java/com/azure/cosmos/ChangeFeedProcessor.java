// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedProcessorBuilderImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple host for distributing change feed events across observers, simplifying the process of reading the change feeds
 *   and distributing the processing events across multiple consumers effectively.
 * <p>
 * There are four main components of implementing the change feed processor:
 *  - The monitored container: the monitored container has the data from which the change feed is generated. Any inserts
 *    and updates to the monitored container are reflected in the change feed of the container.
 *  - The lease container: the lease container acts as a state storage and coordinates processing the change feed across
 *    multiple workers. The lease container can be stored in the same account as the monitored container or in a
 *    separate account.
 *  - The host: a host is an application instance that uses the change feed processor to listen for changes. Multiple
 *    instances with the same lease configuration can run in parallel, but each instance should have a different
 *    instance name.
 *  - The delegate: the delegate is the code that defines what you, the developer, want to do with each batch of
 *    changes that the change feed processor reads.
 * <p>
 * {@code
 * ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
 *     .hostName(hostName)
 *     .feedContainer(feedContainer)
 *     .leaseContainer(leaseContainer)
 *     .handleChanges(docs -> {
 *         for (JsonNode item : docs) {
 *             // Implementation for handling and processing of each JsonNode item goes here
 *         }
 *     })
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
     * Returns the current owner (host) and an approximation of the difference between the last processed item (defined
     *   by the state of the feed container) and the latest change in the container for each partition (lease
     *   item).
     * <p>
     * An empty map will be returned if the processor was not started or no lease items matching the current
     *   {@link ChangeFeedProcessor} instance's lease prefix could be found.
     *
     * @return a map representing the current owner and lease token, the current LSN and latest LSN, and the estimated
     *         lag, asynchronously.
     */
    Mono<Map<String, Integer>> getEstimatedLag();

    /**
     * Helper static method to build a {@link ChangeFeedProcessor} instance.
     * <p>
     * {@code
     * ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
     *     .hostName(hostName)
     *     .feedContainer(feedContainer)
     *     .leaseContainer(leaseContainer)
     *     .handleChanges(docs -> {
     *         for (JsonNode item : docs) {
     *             // Implementation for handling and processing of each JsonNode item goes here
     *         }
     *     })
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
         * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored container.
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
         * <p>
         * {@code
         * An example for how this will look like:
         *     .handleChanges(docs -> {
         *         for (JsonNode item : docs) {
         *             // Implementation for handling and processing of each JsonNode item goes here
         *         }
         *     })
         *  }
         *
         * @param consumer the {@link Consumer} to call for handling the feeds.
         * @return current Builder.
         */
        BuilderDefinition handleChanges(Consumer<List<JsonNode>> consumer);

        /**
         * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases container.
         *
         * @param leaseContainer the instance of {@link CosmosAsyncContainer} to use.
         * @return current Builder.
         */
        BuilderDefinition leaseContainer(CosmosAsyncContainer leaseContainer);

        /**
         * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration.
         *
         * @return an instance of {@link ChangeFeedProcessor}.
         */
        ChangeFeedProcessor build();
    }
}
