// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

class UpdateStateWorkItem extends WorkItem<Void> {
    final DeliveryState deliveryState;

    UpdateStateWorkItem(final CompletableFuture<Void> completableFuture, DeliveryState expectedOutcome, Duration timeout) {
        super(completableFuture, new TimeoutTracker(timeout, true));
        this.deliveryState = expectedOutcome;
    }

    UpdateStateWorkItem(final CompletableFuture<Void> completableFuture, DeliveryState expectedOutcome, final TimeoutTracker tracker) {
        super(completableFuture, tracker);
        this.deliveryState = expectedOutcome;
    }

    public DeliveryState getDeliveryState() {
        return this.deliveryState;
    }
}
