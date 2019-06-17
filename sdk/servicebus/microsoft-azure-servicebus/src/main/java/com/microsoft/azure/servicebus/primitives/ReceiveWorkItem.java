// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

class ReceiveWorkItem extends WorkItem<Collection<MessageWithDeliveryTag>> {
    private final int maxMessageCount;

    ReceiveWorkItem(CompletableFuture<Collection<MessageWithDeliveryTag>> completableFuture, Duration timeout, final int maxMessageCount) {
        super(completableFuture, timeout);
        this.maxMessageCount = maxMessageCount;
    }

    public int getMaxMessageCount() {
        return this.maxMessageCount;
    }
}
