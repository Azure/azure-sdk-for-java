// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedResponse;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkRequestSubscriber<T> extends BaseSubscriber<T> {
    final static Logger logger = LoggerFactory.getLogger(BenchmarkRequestSubscriber.class);
    private final Duration diagnosticsThresholdDuration;
    private volatile long startTimeInMs;
    private Meter successMeter;
    private Meter failureMeter;
    private Semaphore concurrencyControlSemaphore;
    private AtomicLong count;

    public Timer.Context context;

    public BenchmarkRequestSubscriber(Meter successMeter,
                                      Meter failureMeter,
                                      Semaphore concurrencyControlSemaphore,
                                      AtomicLong count,
                                      Duration diagnosticsThresholdDuration) {
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
        this.concurrencyControlSemaphore = concurrencyControlSemaphore;
        this.count = count;
        this.diagnosticsThresholdDuration = diagnosticsThresholdDuration;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        super.hookOnSubscribe(subscription);
        startTimeInMs = System.currentTimeMillis();
    }

    @Override
    protected void hookOnNext(T value) {
        if ((System.currentTimeMillis() - startTimeInMs) > diagnosticsThresholdDuration.toMillis()) {
            if (value instanceof CosmosItemResponse) {
                CosmosItemResponse itemResponse = (CosmosItemResponse) value;
                logger.info("Request taking longer than {}ms diagnostic = {}",
                    diagnosticsThresholdDuration.toMillis(), itemResponse.getDiagnostics().toString());
            } else if (value instanceof FeedResponse) {
                startTimeInMs = System.currentTimeMillis();
                FeedResponse feedResponse = (FeedResponse) value;
                logger.info("Request taking longer than {}ms diagnostic = {}",
                    diagnosticsThresholdDuration.toMillis(), feedResponse.getCosmosDiagnostics().toString());
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
        logger.error("Encountered failure {} on thread {}",
            throwable.getMessage(), Thread.currentThread().getName(), throwable);
        concurrencyControlSemaphore.release();

        synchronized (count) {
            count.incrementAndGet();
            count.notify();
        }
    }
}
