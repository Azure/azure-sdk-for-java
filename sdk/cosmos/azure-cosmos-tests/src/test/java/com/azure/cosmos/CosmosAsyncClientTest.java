// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.guava27.Strings;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
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

import static org.assertj.core.api.Assertions.assertThat;

public abstract class CosmosAsyncClientTest implements ITest {

    public static final String ROUTING_GATEWAY_EMULATOR_PORT = ":8081";
    public static final String COMPUTE_GATEWAY_EMULATOR_PORT = ":9999";

    protected static Logger logger = LoggerFactory.getLogger(CosmosAsyncClientTest.class.getSimpleName());
    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    private final static AtomicInteger instancesUsed = new AtomicInteger(0);
    private final CosmosClientBuilder clientBuilder;
    private String testName;
    private volatile Map<Integer, String> activeClientsAtBegin = new HashMap<>();

    public CosmosAsyncClientTest() {
        this(new CosmosClientBuilder());
    }

    public CosmosAsyncClientTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @BeforeClass(groups = {"thinclient", "fast", "long", "direct", "multi-region", "multi-master", "flaky-multi-master", "emulator",
        "emulator-vnext", "split", "query", "cfp-split", "circuit-breaker-misc-gateway", "circuit-breaker-misc-direct",
        "circuit-breaker-read-all-read-many", "fi-multi-master", "long-emulator", "fi-thinclient-multi-region", "fi-thinclient-multi-master"}, timeOut = SUITE_SETUP_TIMEOUT, alwaysRun = true)
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

    @AfterClass(groups = {"thinclient", "fast", "long", "direct", "multi-region", "multi-master", "flaky-multi-master", "emulator",
        "emulator-vnext", "split", "query", "cfp-split", "circuit-breaker-misc-gateway", "circuit-breaker-misc-direct",
        "circuit-breaker-read-all-read-many", "fi-multi-master", "long-emulator", "fi-thinclient-multi-region", "fi-thinclient-multi-master"}, timeOut = SUITE_SETUP_TIMEOUT, alwaysRun = true)
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
                String msg = "\"COSMOS CLIENT LEAKS detected in test class: "
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

                String msg = "\"NETTY LEAKS detected in test class: "
                    + this.getClass().getCanonicalName()
                    + sb;

                logger.error(msg);
                // fail(msg);
            }
            this.logMemoryUsage("AFTER");
        }
    }

    public final CosmosClientBuilder getClientBuilder() {
        return this.clientBuilder;
    }

    public final String getEndpoint() {
        return this.clientBuilder.getEndpoint();
    }

    public final ConnectionPolicy getConnectionPolicy() {
        return this.clientBuilder.getConnectionPolicy();
    }

    public final <T> CosmosItemResponse verifyExists(CosmosContainer container, String id, PartitionKey pk, Class<T> clazz) {
        return verifyExists(container, id, pk, null, clazz);
    }

    public final <T> CosmosItemResponse verifyExists(CosmosContainer container, String id, PartitionKey pk, CosmosItemRequestOptions requestOptions, Class<T> clazz) {
        CosmosItemResponse<T> response = null;
        while (response == null) {
            try {
                CosmosItemRequestOptions effectiveRequestOptions = requestOptions;

                if (effectiveRequestOptions == null) {
                    effectiveRequestOptions = new CosmosItemRequestOptions();
                    if (getConnectionPolicy().getConnectionMode() != ConnectionMode.GATEWAY) {
                        effectiveRequestOptions.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
                    }
                }

                response = container.readItem(
                    id,
                    pk,
                    effectiveRequestOptions,
                    clazz);

                assertThat(response.getDiagnostics()).isNotNull();
                assertThat(response.getDiagnostics().getDiagnosticsContext()).isNotNull();
                if (effectiveRequestOptions != null
                    && effectiveRequestOptions.getReadConsistencyStrategy() != null) {

                    assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
                        .isEqualTo(effectiveRequestOptions.getReadConsistencyStrategy());
                } else {
                    assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
                        .isEqualTo(ReadConsistencyStrategy.DEFAULT);
                }

                break;
            } catch (CosmosException cosmosError) {
                if (cosmosError.getStatusCode() != 404 || cosmosError.getSubStatusCode() != 0) {
                    throw cosmosError;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return response;
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

        this.clientBuilder.buildConnectionPolicy();
        if (this.clientBuilder.getConnectionPolicy() != null && this.clientBuilder.configs() != null) {
            String connectionMode = this.clientBuilder.getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT
                    ? "Direct " + this.clientBuilder.configs().getProtocol()
                    : this.clientBuilder.getEndpoint().contains(COMPUTE_GATEWAY_EMULATOR_PORT) ? "ComputeGW" : "Gateway";

            String template = clientBuilder.isContentResponseOnWriteEnabled() ?
                "%s[%s with %s consistency]" :
                "%s[%s with %s consistency ContentOnWriteDisabled]";

            this.testName = Strings.lenientFormat(template,
                    testClassAndMethodName,
                    connectionMode,
                    clientBuilder.getConsistencyLevel());
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
