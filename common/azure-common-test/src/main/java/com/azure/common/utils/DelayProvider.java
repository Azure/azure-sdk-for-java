// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.utils;

import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

/**
 * A wrapper class for thread sleep.
 */
public class DelayProvider {
    /**
     * Puts current thread on sleep for passed milliseconds.
     * @param milliseconds time to sleep for
     */
    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Creates an observable that emits the given item after the specified time in milliseconds.
     *
     * @param event the event to emit
     * @param milliseconds the delay in milliseconds
     * @param <T> the type of event
     * @return delayed observable
     */
    public <T> Flux<T> delayedEmitAsync(T event, int milliseconds) {
        return Flux.just(event);//.delay(milliseconds, TimeUnit.MILLISECONDS, Schedulers.trampoline());
    }
}
