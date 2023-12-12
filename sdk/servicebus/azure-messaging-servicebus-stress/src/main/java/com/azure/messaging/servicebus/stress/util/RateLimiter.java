// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.util;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter implements AutoCloseable {
    private final int maxConcurrency;
    private final AtomicInteger inFlight = new AtomicInteger(0);
    private final AtomicInteger bucket = new AtomicInteger(0);
    private final Timer replenishTimer;

    public RateLimiter(int maxRps, int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;

        replenishTimer = new Timer("replenish");
        replenishTimer.schedule(new TimerTask() {
            public void run() {
                bucket.set(maxRps);
            }
        }, 0, 1000L);
    }

    public Mono<Void> acquire() {
        if (tryAcquire()) {
            return Mono.empty();
        }

        return Mono.delay(Duration.ofMillis(10))
            .repeat(() -> !tryAcquire())
            .then();
    }

    public boolean tryAcquire() {
        int concurrency;
        do {
            concurrency = inFlight.get();
            if (concurrency == maxConcurrency) {
                return false;
            }

        } while (!inFlight.compareAndSet(concurrency, concurrency + 1));

        int remaining;
        do {
            remaining = bucket.get();
            if (remaining == 0) {
                inFlight.decrementAndGet();
                return false;
            }
        } while (!bucket.compareAndSet(remaining, remaining - 1));

        return true;
    }

    public void release() {
        inFlight.decrementAndGet();
    }

    @Override
    public void close() {
        replenishTimer.cancel();
    }
}
