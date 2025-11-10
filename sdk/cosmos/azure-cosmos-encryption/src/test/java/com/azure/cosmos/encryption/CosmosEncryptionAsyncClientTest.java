// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.guava27.Strings;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CosmosEncryptionAsyncClientTest implements ITest {
    protected static Logger logger = LoggerFactory.getLogger(CosmosEncryptionAsyncClientTest.class.getSimpleName());
    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor cosmosClientBuilderAccessor =
        ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private final static AtomicInteger instancesUsed = new AtomicInteger(0);

    private final CosmosClientBuilder clientBuilder;
    private String testName;
    private volatile Map<Integer, String> activeClientsAtBegin = new HashMap<>();

    public CosmosEncryptionAsyncClientTest() {
        this(new CosmosClientBuilder());
    }

    public CosmosEncryptionAsyncClientTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public final CosmosClientBuilder getClientBuilder() {
        return this.clientBuilder;
    }

    @BeforeClass(groups = {"fast", "long", "direct", "multi-master", "encryption"}, timeOut = SUITE_SETUP_TIMEOUT)

    public void beforeClassSetupLeakDetection() {
        if (instancesUsed.getAndIncrement() == 0) {
            this.activeClientsAtBegin = RxDocumentClientImpl.getActiveClientsSnapshot();
            this.logMemoryUsage("BEFORE");
        }
    }

    private void logMemoryUsage(String name) {
        long pooledDirectBytes = PooledByteBufAllocator.DEFAULT.metric()
                                                               .directArenas().stream()
                                                               .mapToLong(io.netty.buffer.PoolArenaMetric::numActiveBytes)
                                                               .sum();

        long used = PlatformDependent.usedDirectMemory();
        long max  = PlatformDependent.maxDirectMemory();
        logger.info("MEMORY USAGE: {}:{}", this.getClass().getCanonicalName(), name);
        logger.info("Netty Direct Memory: {}/{}/{} bytes", used, pooledDirectBytes, max);
        for (BufferPoolMXBean pool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            logger.info("Pool {}: used={} bytes, capacity={} bytes, count={}",
                pool.getName(), pool.getMemoryUsed(), pool.getTotalCapacity(), pool.getCount());
        }
    }

    @AfterClass(groups = {"fast", "long", "direct", "multi-master", "encryption"}, timeOut = SUITE_SETUP_TIMEOUT)
    public void afterClassSetupLeakDetection() {
        if (instancesUsed.decrementAndGet() == 0) {
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
                    + this.getClass().getCanonicalName()
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
            this.logMemoryUsage("AFTER");
        }
    }

    @Override
    public final String getTestName() {
        return this.testName;
    }

    @BeforeMethod(alwaysRun = true)
    public final void setTestName(Method method, Object[] row) {
        String testClassAndMethodName = Strings.lenientFormat("%s::%s",
            method.getDeclaringClass().getSimpleName(),
            method.getName());

        ConnectionPolicy connectionPolicy = cosmosClientBuilderAccessor.getConnectionPolicy(clientBuilder);
        Configs configs = cosmosClientBuilderAccessor.getConfigs(clientBuilder);
        if (connectionPolicy != null && configs != null) {
            String connectionMode = connectionPolicy.getConnectionMode() == ConnectionMode.DIRECT
                ? "Direct " + configs.getProtocol()
                : "Gateway";

            ConsistencyLevel consistencyLevel = cosmosClientBuilderAccessor.getConsistencyLevel(clientBuilder);
            this.testName = Strings.lenientFormat("%s[%s with %s consistency]",
                testClassAndMethodName,
                connectionMode,
                consistencyLevel);
        } else {
            this.testName = testClassAndMethodName;
        }

        String suffix = this.resolveTestNameSuffix(row);
        if (suffix != null && !suffix.isEmpty()) {
            this.testName += "(" + suffix + ")";
        }
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }

    public String resolveTestNameSuffix(Object[] row) {
        return "";
    }
}
