package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Represents a Mock event processor.
 */
public class MockEventProcessor {
    private Consumer<MockErrorContext> processError;
    private Consumer<MockEventContext> processEvent;
    private boolean process;
    private double maxEventsPerSecondPerPartition;
    private int maxEventsPerSecond;
    private int partitions;
    private Duration errorAfter;
    private boolean errorRaised;
    ReentrantLock errorLock;

    private MockEventContext[] mockEventContexts;
    private int[] eventsRaised;
    private long startTime;

    private final AtomicReference<ScheduledFuture<?>> runner = new AtomicReference<>();
    private final AtomicReference<ScheduledExecutorService> scheduler = new AtomicReference<>();

    /**
     * Creates an instance of a mock event processor
     *
     * @param partitions the number of partitions
     * @param maxEventsPerSecond the maximum events per second to send, optional.
     * @param errorAfter the duration after which processor should error out, optional.
     * @param processError the consumer to process the error.
     * @param processEvent the consumer to process the event.
     */
    public MockEventProcessor(int partitions, int maxEventsPerSecond, Duration errorAfter,
                              Consumer<MockErrorContext> processError, Consumer<MockEventContext> processEvent) {
        this.processError = processError;
        this.processEvent = processEvent;
        this.partitions = partitions;
        this.maxEventsPerSecond = maxEventsPerSecond;
        this.maxEventsPerSecondPerPartition = ((double) maxEventsPerSecond) / partitions;
        this.errorAfter = errorAfter;
        this.errorLock = new ReentrantLock();

        mockEventContexts = new MockEventContext[partitions];
        IntStream.range(0, partitions).boxed().forEach(integer -> {
            mockEventContexts[integer] = new MockEventContext(integer, "Hello");
        });
        this.eventsRaised = new int[partitions];
    }

    /**
     * Starts the event processor.
     */
    public synchronized void start() {
        eventsRaised = new int[eventsRaised.length];
        process = true;
        errorRaised = false;
        startTime = System.nanoTime();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        scheduler.set(executor);

        Double jitterInMillis =
            ThreadLocalRandom.current().nextDouble() * TimeUnit.SECONDS.toMillis(0);

        runner.set(scheduler.get().schedule(this::processEvents,
            jitterInMillis.longValue(), TimeUnit.MILLISECONDS));
    }

    private Mono<Void> processEvents() {
        while (process) {
            for (int i =0 ; i < partitions; i++) {
                process(i);
            }
        }
        return Mono.empty();
    }

    private void process(int partition) {
        MockEventContext mockEventContext = mockEventContexts[partition];

        if (maxEventsPerSecond > 0) {
            while (process) {
                long elapsedTime = (System.nanoTime() - startTime);
                if (errorAfter != null && !errorRaised
                    && (errorAfter.compareTo(Duration.ofNanos(elapsedTime)) < 0 )) {
                    errorLock.lock();
                    if (!errorRaised) {
                        processError(partition, new IllegalStateException("Test Exception"));
                        errorRaised = true;
                    }
                }
                int eventsSent = eventsRaised[partition];
                double targetEventsSent = ((double) (elapsedTime / 1_000_000_000))
                    * maxEventsPerSecondPerPartition;
                if (eventsSent < targetEventsSent) {
                    processEvent.accept(mockEventContext);
                    eventsRaised[partition]++;
                } else {
                    try {
                        Thread.sleep((long) ((1 / maxEventsPerSecondPerPartition) * 1000));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            while (process) {
                if (errorAfter != null && !errorRaised
                    && (errorAfter.compareTo(Duration.ofNanos((System.nanoTime() - startTime))) < 0 )) {
                    errorLock.lock();
                    if (!errorRaised) {
                        processError(partition, new IllegalStateException("Test Exception"));
                        errorRaised = true;
                    }
                }
                processEvent.accept(mockEventContext);
                eventsRaised[partition]++;
            }
        }
    }

    private void processError(int partition, Throwable throwable) {
        processError.accept(new MockErrorContext(partition, throwable));
        stop();
    }

    /**
     * Stops the Event Processor.
     */
    public synchronized void stop() {
        runner.get().cancel(true);
        scheduler.get().shutdown();
        this.process = false;
    }
}
