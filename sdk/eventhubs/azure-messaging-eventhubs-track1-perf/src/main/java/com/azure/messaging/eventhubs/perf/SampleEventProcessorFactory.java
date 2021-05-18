// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventprocessorhost.IEventProcessorFactory;
import com.microsoft.azure.eventprocessorhost.PartitionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class SampleEventProcessorFactory implements IEventProcessorFactory<SamplePartitionProcessor> {
    private final Map<String, CountDownLatch> numberOfEventsPerPartition;
    private final ConcurrentHashMap<String, SamplePartitionProcessor> processorMap = new ConcurrentHashMap<>();

    public SampleEventProcessorFactory(Map<String, CountDownLatch> numberOfEventsPerPartition) {
        this.numberOfEventsPerPartition = numberOfEventsPerPartition;
    }

    @Override
    public SamplePartitionProcessor createEventProcessor(PartitionContext context) {
        final String partitionId = context.getPartitionId();

        System.out.printf("Claimed partition: %s%n", partitionId);

        final CountDownLatch numberOfEvents = numberOfEventsPerPartition.get(partitionId);
        if (numberOfEvents == null) {
            throw new RuntimeException("Unable to get a countdown for: " + partitionId);
        }

        return processorMap.computeIfAbsent(partitionId, key -> new SamplePartitionProcessor(numberOfEvents));
    }
}

