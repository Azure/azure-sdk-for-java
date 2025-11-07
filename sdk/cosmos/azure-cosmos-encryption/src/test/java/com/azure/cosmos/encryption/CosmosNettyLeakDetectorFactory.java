// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.StackTraceUtil;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

import java.util.ArrayList;
import java.util.List;

public final class CosmosNettyLeakDetectorFactory extends ResourceLeakDetectorFactory implements IExecutionListener {
    protected static Logger logger = LoggerFactory.getLogger(CosmosNettyLeakDetectorFactory.class.getSimpleName());
    private final static List<String> identifiedLeaks = new ArrayList<>();
    private final static Object staticLock = new Object();
    private static volatile boolean isLeakDetectionDisabled = false;
    private static volatile boolean isInitialized = false;

    private CosmosNettyLeakDetectorFactory() {
    }

    @Override
    public void onExecutionStart() {
        ingestIntoNetty();
    }

    @Override
    public void onExecutionFinish() {
        // Run GC to force finalizers to run - only in finalizers Netty would actually detect any leaks.
        System.gc();
    }

    // This method must be called as early as possible in the lifecycle of a process
    // before any Netty ByteBuf has been allocated
    public static void ingestIntoNetty() {
        if (isInitialized) {
            return;
        }

        synchronized (staticLock) {
            if (isInitialized) {
                return;
            }

            // Must run before any Netty ByteBuf is allocated
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
            // sample every allocation
            System.setProperty("io.netty.leakDetection.samplingInterval", "1");
            // install custom reporter
            ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(new CosmosNettyLeakDetectorFactory());

            isInitialized = true;
        }
    }

    public static List<String> resetIdentifiedLeaks() {
        // Run GC to force finalizers to run - only in finalizers Netty would actually detect any leaks.
        System.gc();
        synchronized (staticLock) {
            List<String> leaksSnapshot = new ArrayList<>(identifiedLeaks);

            identifiedLeaks.clear();
            return leaksSnapshot;
        }
    }

    public static AutoCloseable createDisableLeakDetectionScope() {
        synchronized (staticLock) {
            logger.info("Disabling Leak detection: {}", StackTraceUtil.currentCallStack());
            return new DisableLeakDetectionScope();
        }
    }

    @Override
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(
        Class<T> resource, int samplingInterval, long maxActive) {

        return new ResourceLeakDetector<T>(resource, samplingInterval, maxActive) {
            @Override
            protected void reportTracedLeak(String resourceType, String records) {
                synchronized (staticLock) {
                    if (!isLeakDetectionDisabled) {
                        String msg = "NETTY LEAK (traced) type="
                            + resourceType
                            + "records=\n"
                            + records;

                        identifiedLeaks.add(msg);
                        logger.error(msg);
                    }
                }
            }

            @Override
            protected void reportUntracedLeak(String resourceType) {
                synchronized (staticLock) {
                    String msg = "NETTY LEAK (untraced) type="  + resourceType;

                    identifiedLeaks.add(msg);
                    logger.error(msg);
                }
            }

            @Override
            protected void reportInstancesLeak(String resourceType) {
                synchronized (staticLock) {
                    String msg = "NETTY LEAK (instances) type=" + resourceType;

                    identifiedLeaks.add(msg);
                    logger.error(msg);
                }
            }
        };
    }

    private static final class DisableLeakDetectionScope implements AutoCloseable {
        @Override
        public void close() {
            synchronized (staticLock) {
                isLeakDetectionDisabled = false;
                logger.info("Leak detection enabled again.");
            }
        }
    }
}
