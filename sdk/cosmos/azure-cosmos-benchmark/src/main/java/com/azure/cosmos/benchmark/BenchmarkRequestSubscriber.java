// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedResponse;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkRequestSubscriber<T> extends BaseSubscriber<T> {
    final static Logger logger = LoggerFactory.getLogger(BenchmarkRequestSubscriber.class);
    private final StopWatch durationTimer = new StopWatch();
    private Meter successMeter;
    private Meter failureMeter;
    private Semaphore concurrencyControlSemaphore;
    private AtomicLong count;
    private int thresholdForDiagnosticsInMs;
    public Timer.Context context;

    public BenchmarkRequestSubscriber(Meter successMeter,
                                      Meter failureMeter,
                                      Semaphore concurrencyControlSemaphore,
                                      AtomicLong count,
                                      int thresholdForDiagnosticsInMs) {
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
        this.concurrencyControlSemaphore = concurrencyControlSemaphore;
        this.count = count;
        this.thresholdForDiagnosticsInMs = thresholdForDiagnosticsInMs;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        super.hookOnSubscribe(subscription);
        durationTimer.start();
    }

    @Override
    protected void hookOnNext(T value) {
        if (durationTimer.isStarted()) {
            durationTimer.stop();
        }

        if (durationTimer != null && durationTimer.getTime() > thresholdForDiagnosticsInMs) {
            if (value instanceof CosmosItemResponse) {
                CosmosItemResponse itemResponse = (CosmosItemResponse) value;
                logger.info("Request taking longer than {}ms diagnostic = {}", thresholdForDiagnosticsInMs, itemResponse.getDiagnostics().toString());
            } else if (value instanceof FeedResponse) {
                durationTimer.reset();
                durationTimer.start();
                FeedResponse feedResponse = (FeedResponse) value;
                logger.info("Request taking longer than {}ms diagnostic = {}", thresholdForDiagnosticsInMs, feedResponse.getCosmosDiagnostics().toString());
            }
        }

        logger.debug("hookOnNext: {}, count:{}", value, count.get());
    }

    @Override
    protected void hookOnComplete() {
        context.stop();
        successMeter.mark();
        concurrencyControlSemaphore.release();

        synchronized (count) {
            count.incrementAndGet();
            count.notify();
        }
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        context.stop();
        failureMeter.mark();
        logger.error("Encountered failure {} on thread {}" ,
            throwable.getMessage(), Thread.currentThread().getName(), throwable);
        concurrencyControlSemaphore.release();

        synchronized (count) {
            count.incrementAndGet();
            count.notify();
        }
    }
}
