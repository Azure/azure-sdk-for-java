// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processes a single partition.
 */
public class SamplePartitionProcessor {
    private final Logger logger = LoggerFactory.getLogger(SamplePartitionProcessor.class);
    private final ConcurrentLinkedQueue<EventsCounter> currentCounters = new ConcurrentLinkedQueue<>();
    private final ArrayList<EventsCounter> allCounters = new ArrayList<>();
    private final AtomicBoolean isStopped = new AtomicBoolean();

    public List<EventsCounter> getCounters() {
        return allCounters;
    }

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

    public void onEvents(PartitionContext context, EventData event) {
        if (isStopped.get()) {
            System.out.printf("OnEvents: Already stopped partition %s%n", context.getPartitionId());
            return;
        }

        final EventsCounter eventsCounter = currentCounters.peek();
        if (eventsCounter == null) {
            throw new RuntimeException("Expected a current counter for partition: " + context.getPartitionId());
        }

        eventsCounter.increment();
    }

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

    public void onError(PartitionContext context, Throwable error) {
        logger.warn("PartitionId[{}] onError", context.getPartitionId(), error);
    }

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
