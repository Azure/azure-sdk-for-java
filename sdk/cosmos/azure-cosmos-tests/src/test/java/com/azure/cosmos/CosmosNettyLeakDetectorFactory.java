// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.StackTraceUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClassListener;
import org.testng.IExecutionListener;
import org.testng.ITestClass;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CosmosNettyLeakDetectorFactory
    extends ResourceLeakDetectorFactory implements IExecutionListener, IClassListener {

    protected static Logger logger = LoggerFactory.getLogger(CosmosNettyLeakDetectorFactory.class.getSimpleName());
    private final static List<String> identifiedLeaks = new ArrayList<>();
    private final static Object staticLock = new Object();
    private static volatile boolean isLeakDetectionDisabled = false;
    private static volatile boolean isInitialized = false;

    private volatile Map<Integer, String> activeClientsAtBegin = new HashMap<>();

    public CosmosNettyLeakDetectorFactory() {
    }

    @Override
    public void onExecutionStart() {
        ingestIntoNetty();
    }

    @Override
    public void onBeforeClass(ITestClass testClass) {
        if (Configs.isClientLeakDetectionEnabled()) {
            this.activeClientsAtBegin = RxDocumentClientImpl.getActiveClientsSnapshot();
            this.logMemoryUsage("BEFORE CLASS", testClass.getRealClass().getCanonicalName());
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        if (Configs.isClientLeakDetectionEnabled()) {
            Map<Integer, String> leakedClientSnapshotNow = RxDocumentClientImpl.getActiveClientsSnapshot();
            StringBuilder sb = new StringBuilder();
            Map<Integer, String> leakedClientSnapshotAtBegin = activeClientsAtBegin;

            for (Integer clientId : leakedClientSnapshotNow.keySet()) {
                if (!leakedClientSnapshotAtBegin.containsKey(clientId)) {
                    // this client was leaked in this class
                    sb
                        .append("CosmosClient [")
                        .append(clientId)
                        .append("] leaked. Callstack of initialization:\n")
                        .append(leakedClientSnapshotNow.get(clientId))
                        .append("\n\n");
                }
            }

            if (sb.length() > 0) {
                String msg = "COSMOS CLIENT LEAKS detected in test class: "
                    + testClass.getRealClass().getCanonicalName()
                    + "\n\n"
                    + sb;

                logger.error(msg);
                // fail(msg);
            }

            List<String> nettyLeaks = CosmosNettyLeakDetectorFactory.resetIdentifiedLeaks();
            if (nettyLeaks.size() > 0) {
                sb.append("\n");
                for (String leak : nettyLeaks) {
                    sb.append(leak).append("\n");
                }

                String msg = "NETTY LEAKS detected in test class: "
                    + this.getClass().getCanonicalName()
                    + sb;

                logger.error(msg);
                // fail(msg);
            }
            this.logMemoryUsage("AFTER CLASS", testClass.getRealClass().getCanonicalName());
        }
    }

    private void logMemoryUsage(String name, String className) {
        long pooledDirectBytes = PooledByteBufAllocator.DEFAULT.metric()
                                                               .directArenas().stream()
                                                               .mapToLong(io.netty.buffer.PoolArenaMetric::numActiveBytes)
                                                               .sum();

        long used = PlatformDependent.usedDirectMemory();
        long max  = PlatformDependent.maxDirectMemory();
        logger.info("MEMORY USAGE: {}:{}", className, name);
        logger.info("Netty Direct Memory: {}/{}/{} bytes", used, pooledDirectBytes, max);
        for (BufferPoolMXBean pool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            logger.info("Pool {}: used={} bytes, capacity={} bytes, count={}",
                pool.getName(), pool.getMemoryUsed(), pool.getTotalCapacity(), pool.getCount());
        }
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
            System.setProperty("io.netty.leakDetection.targetRecords", "256");
            // install custom reporter
            ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(new CosmosNettyLeakDetectorFactory());

            logger.info(
                "NETTY LEAK detection initialized, CosmosClient leak detection enabled: {}",
                Configs.isClientLeakDetectionEnabled());
            isInitialized = true;
        }
    }

    public static List<String> resetIdentifiedLeaks() {
        // Run GC to force finalizers to run - only in finalizers Netty would actually detect any leaks.
        System.gc();
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        synchronized (staticLock) {
            List<String> leaksSnapshot = new ArrayList<>(identifiedLeaks);

            identifiedLeaks.clear();
            return leaksSnapshot;
        }
    }

    public static AutoCloseable createDisableLeakDetectionScope() {
        synchronized (staticLock) {
            logger.info("Disabling Leak detection: {}", StackTraceUtil.currentCallStack());
            isLeakDetectionDisabled = true;

            return new DisableLeakDetectionScope();
        }
    }

    @Override
    public <T> ResourceLeakDetector<T> newResourceLeakDetector(
        Class<T> resource, int samplingInterval, long maxActive) {

        return new ResourceLeakDetector<T>(resource, samplingInterval, maxActive) {
            @Override
            protected void reportTracedLeak(String resourceType, String records) {
                if (!isLeakDetectionDisabled) {
                    synchronized (staticLock) {
                        if (!isLeakDetectionDisabled) {
                            String msg = "NETTY LEAK (traced) type="
                                + resourceType
                                + " records=\n"
                                + records;

                            identifiedLeaks.add(msg);
                            logger.error(msg);
                        }
                    }
                }
            }

            @Override
            protected void reportUntracedLeak(String resourceType) {
                if (!isLeakDetectionDisabled) {
                    synchronized (staticLock) {
                        if (!isLeakDetectionDisabled) {
                            String msg = "NETTY LEAK (untraced) type=" + resourceType;

                            identifiedLeaks.add(msg);
                            logger.error(msg);
                        }
                    }
                }
            }

            @Override
            protected void reportInstancesLeak(String resourceType) {
                if (!isLeakDetectionDisabled) {
                    synchronized (staticLock) {
                        if (!isLeakDetectionDisabled) {
                            String msg = "NETTY LEAK (instances) type=" + resourceType;

                            identifiedLeaks.add(msg);
                            logger.error(msg);

                        }
                    }
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
