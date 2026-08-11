// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerSdkStatsTest {

    private CustomerSdkStats customerSdkStats;
    private static final ConnectionString CONNECTION_STRING
        = ConnectionString.parse("InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF");
    private static final String SDK_VERSION = "java:3.5.1";
    private static final String CLOUD_ROLE = "TestRole";
    private static final String CLOUD_ROLE_INSTANCE = "TestInstance";

    @BeforeEach
    public void init() {
        customerSdkStats = new CustomerSdkStats("unknown", "java", "3.5.1");
    }

    @Test
    public void testIncrementSuccessCount() {
        Map<String, Long> itemCountsByType = new HashMap<>();
        itemCountsByType.put("REQUEST", 5L);
        itemCountsByType.put("DEPENDENCY", 3L);

        customerSdkStats.incrementSuccessCount(itemCountsByType);

        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(5);
        assertThat(customerSdkStats.getSuccessCount("DEPENDENCY")).isEqualTo(3);
        assertThat(customerSdkStats.getSuccessCount("TRACE")).isEqualTo(0);
    }

    @Test
    public void testIncrementSuccessCountMultipleTimes() {
        Map<String, Long> batch1 = Collections.singletonMap("REQUEST", 3L);
        Map<String, Long> batch2 = Collections.singletonMap("REQUEST", 7L);

        customerSdkStats.incrementSuccessCount(batch1);
        customerSdkStats.incrementSuccessCount(batch2);

        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(10);
    }

    @Test
    public void testIncrementDroppedCount() {
        Map<String, Long> itemCountsByType = new HashMap<>();
        itemCountsByType.put("TRACE", 10L);
        Map<String, Long> successItems = Collections.emptyMap();
        Map<String, Long> failureItems = Collections.emptyMap();

        customerSdkStats.incrementDroppedCount(itemCountsByType, "402", "Exceeded daily quota", successItems,
            failureItems);

        assertThat(customerSdkStats.getDroppedCount("TRACE", "402")).isEqualTo(10);
    }

    @Test
    public void testIncrementDroppedCountWithTelemetrySuccess() {
        Map<String, Long> itemCountsByType = new HashMap<>();
        itemCountsByType.put("REQUEST", 8L);
        Map<String, Long> successItems = Collections.singletonMap("REQUEST", 5L);
        Map<String, Long> failureItems = Collections.singletonMap("REQUEST", 3L);

        customerSdkStats.incrementDroppedCount(itemCountsByType, "402", "Exceeded daily quota", successItems,
            failureItems);

        // Total dropped for REQUEST with code 402 should be 8 (5 success + 3 failure)
        assertThat(customerSdkStats.getDroppedCount("REQUEST", "402")).isEqualTo(8);
    }

    @Test
    public void testIncrementRetryCount() {
        Map<String, Long> itemCountsByType = new HashMap<>();
        itemCountsByType.put("DEPENDENCY", 15L);
        itemCountsByType.put("REQUEST", 5L);

        customerSdkStats.incrementRetryCount(itemCountsByType, "429", "Too many requests");

        assertThat(customerSdkStats.getRetryCount("DEPENDENCY", "429")).isEqualTo(15);
        assertThat(customerSdkStats.getRetryCount("REQUEST", "429")).isEqualTo(5);
    }

    @Test
    public void testIncrementRetryCountClientTimeout() {
        Map<String, Long> itemCountsByType = Collections.singletonMap("TRACE", 20L);

        customerSdkStats.incrementRetryCount(itemCountsByType, CustomerSdkStats.RETRY_CODE_CLIENT_TIMEOUT,
            "Timeout exception");

        assertThat(customerSdkStats.getRetryCount("TRACE", CustomerSdkStats.RETRY_CODE_CLIENT_TIMEOUT)).isEqualTo(20);
    }

    @Test
    public void testCollectAndResetReturnsCorrectMetrics() {
        // Add some data
        customerSdkStats.incrementSuccessCount(Collections.singletonMap("REQUEST", 10L));
        customerSdkStats.incrementDroppedCount(Collections.singletonMap("DEPENDENCY", 5L), "402",
            "Exceeded daily quota", Collections.emptyMap(), Collections.emptyMap());
        customerSdkStats.incrementRetryCount(Collections.singletonMap("TRACE", 3L), "429", "Too many requests");

        List<TelemetryItem> items
            = customerSdkStats.collectAndReset(CONNECTION_STRING, SDK_VERSION, CLOUD_ROLE, CLOUD_ROLE_INSTANCE);

        // Should have 3 metric items
        assertThat(items).hasSize(3);

        // Verify that counters are cleared
        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(0);
        assertThat(customerSdkStats.getDroppedCount("DEPENDENCY", "402")).isEqualTo(0);
        assertThat(customerSdkStats.getRetryCount("TRACE", "429")).isEqualTo(0);
    }

    @Test
    public void testCollectAndResetEmptyReturnsEmptyList() {
        List<TelemetryItem> items
            = customerSdkStats.collectAndReset(CONNECTION_STRING, SDK_VERSION, CLOUD_ROLE, CLOUD_ROLE_INSTANCE);

        assertThat(items).isEmpty();
    }

    @Test
    public void testCollectAndResetClearsCounters() {
        customerSdkStats.incrementSuccessCount(Collections.singletonMap("REQUEST", 100L));

        // First collect
        List<TelemetryItem> items1
            = customerSdkStats.collectAndReset(CONNECTION_STRING, SDK_VERSION, CLOUD_ROLE, CLOUD_ROLE_INSTANCE);
        assertThat(items1).hasSize(1);

        // Second collect should be empty
        List<TelemetryItem> items2
            = customerSdkStats.collectAndReset(CONNECTION_STRING, SDK_VERSION, CLOUD_ROLE, CLOUD_ROLE_INSTANCE);
        assertThat(items2).isEmpty();
    }

    @Test
    public void testConcurrentIncrements() throws InterruptedException {
        int threads = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    customerSdkStats.incrementSuccessCount(Collections.singletonMap("REQUEST", 1L));
                }
            });
        }

        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(customerSdkStats.getSuccessCount("REQUEST")).isEqualTo(threads * incrementsPerThread);
    }
}
