// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

final class TestResourceLeakDetectorFactory extends ResourceLeakDetectorFactory {
    private final Collection<TestResourceLeakDetector<?>> createdDetectors = new ConcurrentLinkedDeque<>();

    @Override
    @SuppressWarnings("deprecation") // API is deprecated but abstract
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval,
        long maxActive) {
        TestResourceLeakDetector<T> detector = new TestResourceLeakDetector<>(resource, samplingInterval, maxActive);
        createdDetectors.add(detector);
        return detector;
    }

    public int getTotalReportedLeakCount() {
        return createdDetectors.stream().mapToInt(TestResourceLeakDetector::getReportedLeakCount).sum();
    }
}
