// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.beust.jcommander.Parameter;

/**
 * Event Hubs options when a partition id is required.
 *
 * @see GetPartitionInformationTest
 */
public class EventHubsPartitionOptions extends EventHubsOptions {
    @Parameter(names = {"--partitionId"}, description = "Partition to send, receive, or get information from.",
        required = true)
    private String partitionId;

    /**
     * Gets the partition to send, receive, or get information from.
     *
     * @return The partition to send, receive, or get information from.
     */
    public String getPartitionId() {
        return partitionId;
    }
}
