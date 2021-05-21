// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;

import java.util.concurrent.CountDownLatch;

/**
 * Processes a single partition.
 */
public class SamplePartitionProcessor implements IEventProcessor {

    /**
     * Creates an instance for that partition id.
     *
     * @param numberOfEvents Number of Events left to receive.
     */
    public SamplePartitionProcessor() {
    }

    @Override
    public void onOpen(PartitionContext context) {
        System.out.printf("PartitionId[%s] OnOpen%n", context.getPartitionId());
    }

    @Override
    public void onClose(PartitionContext context, CloseReason reason) {
        System.out.printf("PartitionId[%s] OnClose%n", context.getPartitionId());
    }

    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) {
        for (EventData event : events) {
            if (numberOfEvents.getCount() <= 0) {
                break;
            }

            System.out.printf("PartitionId[%s] Sequence[%s]%n", context.getPartitionId(),
                event.getSystemProperties().getSequenceNumber());

            numberOfEvents.countDown();
        }
    }

    @Override
    public void onError(PartitionContext context, Throwable error) {
        System.err.printf("PartitionId[%s] Error[%s]%n", context.getPartitionId(), error);
    }
}
