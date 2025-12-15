// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import io.netty.util.ResourceLeakDetector;

import java.util.concurrent.atomic.AtomicInteger;

final class TestResourceLeakDetector<T> extends ResourceLeakDetector<T> {
    private final AtomicInteger reportTracedLeakCount = new AtomicInteger();
    private final AtomicInteger reportUntracedLeakCount = new AtomicInteger();

    @SuppressWarnings("deprecation")
    TestResourceLeakDetector(Class<T> resource, int samplingInterval, long maxActive) {
        super(resource, samplingInterval, maxActive);
    }

    @Override
    protected void reportTracedLeak(String resourceType, String records) {
        reportTracedLeakCount.incrementAndGet();
        super.reportTracedLeak(resourceType, records);
    }

    @Override
    protected void reportUntracedLeak(String resourceType) {
        reportUntracedLeakCount.incrementAndGet();
        super.reportUntracedLeak(resourceType);
    }

    public int getReportedLeakCount() {
        return reportTracedLeakCount.get() + reportUntracedLeakCount.get();
    }
}
