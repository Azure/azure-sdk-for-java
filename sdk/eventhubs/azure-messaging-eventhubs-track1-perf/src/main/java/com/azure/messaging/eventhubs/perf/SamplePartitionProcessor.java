// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Processes a single partition.
 */
public class SamplePartitionProcessor implements IEventProcessor {
    private final ClientLogger logger = new ClientLogger(SamplePartitionProcessor.class);
    private final AtomicReference<PartitionCounter> currentCounter = new AtomicReference<>();
    private final ArrayList<PartitionCounter> allCounters = new ArrayList<>();

    /**
     * Creates an instance for that partition id.
     */
    public SamplePartitionProcessor() {
    }

    @Override
    public void onOpen(PartitionContext context) {
        logger.verbose("PartitionId[{}] OnOpen", context.getPartitionId());

        final PartitionCounter counter = new PartitionCounter(context.getPartitionId());
        if (!currentCounter.compareAndSet(null, counter)) {
            throw logger.logThrowableAsError(new RuntimeException("There shouldn't be a current counter on open. Id: "
                + context.getPartitionId()));
        }

        counter.start();
    }

    @Override
    public void onClose(PartitionContext context, CloseReason reason) {
        logger.info("PartitionId[{}] OnClose {}", context.getPartitionId(), reason);

        final PartitionCounter counter = this.currentCounter.getAndSet(null);
        if (counter == null) {
            throw logger.logThrowableAsError(new RuntimeException("There was no existing counter to close."));
        }

        counter.stop();
        allCounters.add(counter);
    }

    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) {
        final PartitionCounter partitionCounter = currentCounter.get();
        if (partitionCounter == null) {
            throw logger.logThrowableAsError(new RuntimeException("Expected a current counter for partition: "
                + context.getPartitionId()));
        }

        for (EventData event : events) {
            partitionCounter.increment();
        }
    }

    @Override
    public void onError(PartitionContext context, Throwable error) {
        logger.warning("PartitionId[{}] Error[{}]", context.getPartitionId(), error);
    }

    public String getResults() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allCounters.size(); i++) {
            final PartitionCounter partitionCounter = allCounters.get(i);
            final String formatted = String.format("%d\t%s\t%s\t%s%n", i, partitionCounter.getPartitionId(),
                partitionCounter.totalEvents(), partitionCounter.elapsedTime());
            builder.append(formatted);
        }

        return builder.toString();
    }
}
