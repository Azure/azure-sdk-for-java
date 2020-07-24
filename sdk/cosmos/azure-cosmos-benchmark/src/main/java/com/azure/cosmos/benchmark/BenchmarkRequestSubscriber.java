// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

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
    private Meter successMeter;
    private Meter failureMeter;
    private Semaphore concurrencyControlSemaphore;
    private AtomicLong count;
    public Timer.Context context;

    public BenchmarkRequestSubscriber(Meter successMeter, Meter failureMeter, Semaphore concurrencyControlSemaphore,  AtomicLong count) {
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
        this.concurrencyControlSemaphore = concurrencyControlSemaphore;
        this.count = count;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        super.hookOnSubscribe(subscription);
    }

    @Override
    protected void hookOnNext(T value) {
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
