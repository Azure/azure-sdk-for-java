// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomNettyLeakDetectorFactory extends ResourceLeakDetectorFactory {
    private static final Logger LOG = LoggerFactory.getLogger("NettyLeak");

    @Override
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(
        Class<T> resource, int samplingInterval, long maxActive) {

        // You can filter by resource if you only care about ByteBuf leaks:
        // if (!"io.netty.buffer.ByteBuf".equals(resource.getName())) { ... }

        return new ResourceLeakDetector<T>(resource, samplingInterval, maxActive) {
            @Override
            protected void reportTracedLeak(String resourceType, String records) {
                LOG.error("NETTY LEAK (traced) type={} records=\n{}", resourceType, records);
                // Or: throw new AssertionError(...); to fail tests
            }

            @Override
            protected void reportUntracedLeak(String resourceType) {
                LOG.error("NETTY LEAK (untraced) type={}", resourceType);
            }

            @Override
            protected void reportInstancesLeak(String resourceType) {
                LOG.warn("NETTY LEAK (instances) type={}", resourceType);
            }
        };
    }
}
