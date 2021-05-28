// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processes a single partition.
 */
public class SamplePartitionProcessor implements IEventProcessor {
    private final Logger logger = LoggerFactory.getLogger(SamplePartitionProcessor.class);
    private final ConcurrentLinkedQueue<EventsCounter> currentCounters = new ConcurrentLinkedQueue<>();
    private final ArrayList<EventsCounter> allCounters = new ArrayList<>();
    private final AtomicBoolean isStopped = new AtomicBoolean();

    /**
     * Gets the event counters opened while processing this partition.
     *
     * @return the event counters opened while processing this partition.
     */
    public List<EventsCounter> getCounters() {
        return allCounters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(PartitionContext context) {
        if (isStopped.get()) {
            System.out.printf("OnOpen: Already stopped partition %s%n", context.getPartitionId());
            return;
        }

        logger.trace("PartitionId[{}] OnOpen", context.getPartitionId());

        final EventsCounter counter = new EventsCounter(context.getPartitionId());
        counter.start();
        currentCounters.add(counter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose(PartitionContext context, CloseReason reason) {
        if (isStopped.get()) {
            System.out.printf("OnClose: Already stopped partition %s%n", context.getPartitionId());
            return;
        }

        logger.info("PartitionId[{}] OnClose {}", context.getPartitionId(), reason);

        final EventsCounter lastCounter = currentCounters.poll();
        if (lastCounter == null) {
            throw new RuntimeException("There was no current counter to stop. Id: " + context.getPartitionId());
        }

        lastCounter.stop();
        allCounters.add(lastCounter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) {
        if (isStopped.get()) {
            System.out.printf("OnEvents: Already stopped partition %s%n", context.getPartitionId());
            return;
        }

        final EventsCounter eventsCounter = currentCounters.peek();
        if (eventsCounter == null) {
            throw new RuntimeException("Expected a current counter for partition: " + context.getPartitionId());
        }

        for (EventData event : events) {
            eventsCounter.increment();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(PartitionContext context, Throwable error) {
        logger.warn("PartitionId[{}] onError", context.getPartitionId(), error);
    }

    /**
     * Stops the partition processor entirely and closes any open EventCounters.
     */
    public void onStop() {
        if (isStopped.getAndSet(true)) {
            return;
        }

        EventsCounter counter = currentCounters.poll();
        while (counter != null) {
            counter.stop();
            allCounters.add(counter);
            counter = currentCounters.poll();
        }
    }
}
