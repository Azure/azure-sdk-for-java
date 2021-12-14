package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MockEventProcessor {

    public static void main(String[] args) throws InterruptedException {
        Consumer<MockErrorContext> errorprocessor = new Consumer<MockErrorContext>() {
            @Override
            public void accept(MockErrorContext mockErrorContext) {
                System.err.println("Got Error: " + mockErrorContext.getThrowable().getMessage());
            }
        };

        Consumer<MockEventContext> eventPrcessor = new Consumer<MockEventContext>() {
            @Override
            public void accept(MockEventContext mockEventContext) {
                System.out.println("Got event: " + mockEventContext.getEventData());
            }
        };
        MockEventProcessor processor = new MockEventProcessor(2, 4, errorprocessor, eventPrcessor);

        processor.start();
        Thread.sleep(5000);
        System.out.println("Done");

    }

    private Consumer<MockErrorContext> processError;
    private Consumer<MockEventContext> processEvent;

    private MockEventContext[] mockEventContexts;
    private int[] eventsRaised;

    private final AtomicReference<ScheduledFuture<?>> runner = new AtomicReference<>();
    private final AtomicReference<ScheduledExecutorService> scheduler = new AtomicReference<>();

    public MockEventProcessor(int partitions, int maxEventsPerSecond, Consumer<MockErrorContext> processError, Consumer<MockEventContext> processEvent) {
        this.processError = processError;
        this.processEvent = processEvent;
        mockEventContexts = new MockEventContext[partitions];
        IntStream.range(0, partitions).boxed().forEach(integer -> {
            mockEventContexts[integer] = new MockEventContext(integer, "Hello");
        });
        this.eventsRaised = new int[partitions];
    }

    public synchronized void start() {

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        scheduler.set(executor);

        Double jitterInMillis =
            ThreadLocalRandom.current().nextDouble() * TimeUnit.SECONDS.toMillis(2);

        runner.set(scheduler.get().scheduleWithFixedDelay(this::processEvents,
            jitterInMillis.longValue(), Duration.ofSeconds(1).toMillis(), TimeUnit.MILLISECONDS));
    }

    private Mono<Void> processEvents() {
        boolean update = false;
        while (true) {
            for (int i =0 ; i < mockEventContexts.length; i++) {
                processEvent.accept(mockEventContexts[i]);
                eventsRaised[i]++;

                if (eventsRaised[i] > 100) {
                    update = true;
                    break;
                }
            }
            if (update) {
                processError(new IllegalStateException("Wow"));
                break;
            }

        }
        return Mono.empty();
    }

    private void processError(Throwable throwable) {
        processError.accept(new MockErrorContext(throwable));
        stop();
    }

    public synchronized void stop() {
        runner.get().cancel(true);
        scheduler.get().shutdown();
    }
}
