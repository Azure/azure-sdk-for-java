// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * The strategy used by event processor for load balancing the partition ownership to distribute the event processing
 * work with other processor instances.
 *
 * @see EventProcessorClientBuilder#loadBalancingStrategy(LoadBalancingStrategy)
 */
public enum LoadBalancingStrategy {

    /**
     * The event processor will use a steady approach to claim ownership of partitions and slowly trend
     * towards a stable state where all active processors will have an even distribution of Event Hub partitions.
     * This strategy may take longer to settle into a balanced partition distribution among active processor
     * instances. This strategy is geared towards minimizing ownership contention and reducing the need to transfer
     * ownership frequently, especially when multiple instances are initialized together, until a stable state is
     * reached.
     */
    BALANCED,

    /**
     * The event processor will attempt to claim its fair share of partition ownership greedily. This enables event
     * processing of all partitions to start/resume quickly when there is an imbalance detected by the processor.
     * This may result in ownership of partitions frequently changing when multiple instances are starting up
     * but will eventually converge to a stable state.
     */
    GREEDY;
}
