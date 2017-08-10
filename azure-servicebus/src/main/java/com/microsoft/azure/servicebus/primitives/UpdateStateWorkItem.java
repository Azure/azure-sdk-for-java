// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.messaging.Outcome;

class UpdateStateWorkItem extends WorkItem<Void> {
    final Outcome outcome;

    public UpdateStateWorkItem(final CompletableFuture<Void> completableFuture, Outcome expectedOutcome, Duration timeout) {
        super(completableFuture, new TimeoutTracker(timeout, true));
        this.outcome = expectedOutcome;
    }

    public UpdateStateWorkItem(final CompletableFuture<Void> completableFuture, Outcome expectedOutcome, final TimeoutTracker tracker) {
        super(completableFuture, tracker);
        this.outcome = expectedOutcome;
    }

    public Outcome getOutcome() {
        return this.outcome;
    }
}
