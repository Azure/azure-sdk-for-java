// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AzureEventHubsDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The AzureEventHubsDataFeedSource model.
 */
@Immutable
public final class AzureEventHubsDataFeedSource extends DataFeedSource {
    /*
     * Azure EventHubs connection string
     */
    private final String connectionString;
    /*
     * Azure EventHubs consumer group
     */
    private final String consumerGroup;

    static {
        AzureEventHubsDataFeedSourceAccessor.setAccessor(
            new AzureEventHubsDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(AzureEventHubsDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a AzureEventHubsDataFeedSource instance
     *
     * @param connectionString the Azure EventHub connection string.
     * @param consumerGroup the Azure EventHub consumer group.
     */
    public AzureEventHubsDataFeedSource(final String connectionString, final String consumerGroup) {
        this.connectionString = connectionString;
        this.consumerGroup = consumerGroup;
    }

    /**
     * Gets the Azure EventHub consumer group.
     *
     * @return the consumerGroup value.
     */
    public String getConsumerGroup() {
        return this.consumerGroup;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
