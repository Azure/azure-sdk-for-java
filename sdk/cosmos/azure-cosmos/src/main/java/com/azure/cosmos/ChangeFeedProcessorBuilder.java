// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedProcessorBuilderImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Helper class to build a {@link ChangeFeedProcessor} instance.
 *
 * {@codesnippet com.azure.cosmos.changeFeedProcessor.builder}
 */
public class ChangeFeedProcessorBuilder {
    private String hostName;
    private CosmosAsyncContainer feedContainer;
    private CosmosAsyncContainer leaseContainer;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private Consumer<List<JsonNode>> consumer;

    /**
     * Instantiates a new Cosmos a new ChangeFeedProcessor builder.
     */
    public ChangeFeedProcessorBuilder() {
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique
     * name.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored container.
     *
     * @param feedContainer the instance of {@link CosmosAsyncContainer} to be used.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder feedContainer(CosmosAsyncContainer feedContainer) {
        this.feedContainer = feedContainer;

        return this;
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases container.
     *
     * @param leaseContainer the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder leaseContainer(CosmosAsyncContainer leaseContainer) {
        this.leaseContainer = leaseContainer;

        return this;
    }

    /**
     * Sets a consumer function which will be called to process changes.
     *
     * {@codesnippet com.azure.cosmos.changeFeedProcessor.handleChanges}
     *
     * @param consumer the {@link Consumer} to call for handling the feeds.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder handleChanges(Consumer<List<JsonNode>> consumer) {
        this.consumer = consumer;

        return this;
    }

    /**
     * Sets the {@link ChangeFeedProcessorOptions} to be used.
     * Unless specifically set the default values that will be used are:
     * <ul>
     * <li>maximum items per page or FeedResponse: 100</li>
     * <li>lease renew interval: 17 seconds</li>
     * <li>lease acquire interval: 13 seconds</li>
     * <li>lease expiration interval: 60 seconds</li>
     * <li>feed poll delay: 5 seconds</li>
     * <li>maximum scale count: unlimited</li>
     * </ul>
     *
     * @param changeFeedProcessorOptions the change feed processor options to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilder options(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor buildChangeFeedProcessor() {
        if (hostName == null || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName");
        }
        if (feedContainer == null) {
            throw new IllegalArgumentException("feedContainer");
        }
        if (leaseContainer == null) {
            throw new IllegalArgumentException("leaseContainer");
        }
        if (consumer == null) {
            throw new IllegalArgumentException("consumer");
        }

        ChangeFeedProcessorBuilderImpl builder = new ChangeFeedProcessorBuilderImpl()
            .hostName(this.hostName)
            .feedContainer(this.feedContainer)
            .leaseContainer(this.leaseContainer)
            .handleChanges(this.consumer);

        if (this.changeFeedProcessorOptions != null) {
            if (this.changeFeedProcessorOptions.getLeaseRenewInterval().compareTo(this.changeFeedProcessorOptions.getLeaseExpirationInterval()) >= 0) {
                // Lease renewer task must execute at a faster frequency than expiration setting; otherwise this will
                //  force a lot of resets and lead to a poor overall performance of ChangeFeedProcessor.
                throw new IllegalArgumentException("changeFeedProcessorOptions: expecting leaseRenewInterval less than leaseExpirationInterval");
            }

            builder.options(this.changeFeedProcessorOptions);
        }

        return builder.build();
    }
}
