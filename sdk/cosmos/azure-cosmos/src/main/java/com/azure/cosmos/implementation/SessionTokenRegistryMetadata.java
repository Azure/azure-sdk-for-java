// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicInteger;

public class SessionTokenRegistryMetadata {

    private final AtomicInteger sessionTokenCount;

    public SessionTokenRegistryMetadata() {
        this.sessionTokenCount = new AtomicInteger(0);
    }

    public void recordSessionTokenInsertion() {
        this.sessionTokenCount.incrementAndGet();
    }

    public void recordSessionTokenEviction() {
        this.sessionTokenCount.decrementAndGet();
    }

    public int getSessionTokenCount() {
        return this.sessionTokenCount.get();
    }
}
