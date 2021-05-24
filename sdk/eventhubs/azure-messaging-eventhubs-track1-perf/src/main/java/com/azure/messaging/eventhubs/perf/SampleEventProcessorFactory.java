// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventprocessorhost.IEventProcessorFactory;
import com.microsoft.azure.eventprocessorhost.PartitionContext;

import java.util.concurrent.ConcurrentHashMap;

public class SampleEventProcessorFactory implements IEventProcessorFactory<SamplePartitionProcessor> {
    private final ConcurrentHashMap<String, SamplePartitionProcessor> processorMap;

    public SampleEventProcessorFactory(ConcurrentHashMap<String, SamplePartitionProcessor> processorMap) {
        this.processorMap = processorMap;
    }

    @Override
    public SamplePartitionProcessor createEventProcessor(PartitionContext context) {
        final String partitionId = context.getPartitionId();

        System.out.printf("Claimed partition: %s%n", partitionId);

        final SamplePartitionProcessor samplePartitionProcessor = processorMap.get(partitionId);
        if (samplePartitionProcessor == null) {
            throw new RuntimeException("There should have been a processor for partition " + partitionId);
        }

        return samplePartitionProcessor;
    }
}

