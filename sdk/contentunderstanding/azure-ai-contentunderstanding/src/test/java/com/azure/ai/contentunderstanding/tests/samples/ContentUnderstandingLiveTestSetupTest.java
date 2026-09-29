// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.ContentUnderstandingDefaults;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentUnderstandingLiveTestSetupTest {
    @Test
    public void reportsAllMissingDeploymentVariables() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingLiveTestSetup.getRequiredDeployments(name -> null));

        assertTrue(exception.getMessage().contains("CU_COMPLETION_MODEL_DEPLOYMENT"));
        assertTrue(exception.getMessage().contains("CU_EMBEDDING_DEPLOYMENT"));
    }

    @Test
    public void mergesRequiredDeploymentsAndRunsOnce() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        Map<String, String> configuration = createConfiguration();
        Map<String, String> currentDeployments = new LinkedHashMap<>();
        currentDeployments.put("existing-model", "existing-deployment");
        currentDeployments.put("prebuilt-analyzer-completion", "stale-deployment");
        AtomicInteger updateCount = new AtomicInteger();
        AtomicReference<Map<String, String>> updatedDeployments = new AtomicReference<>();

        setup.ensureConfigured(configuration::get, () -> new ContentUnderstandingDefaults(currentDeployments), map -> {
            updateCount.incrementAndGet();
            updatedDeployments.set(new LinkedHashMap<>(map));
            return new ContentUnderstandingDefaults(map);
        });
        setup.ensureConfigured(configuration::get, () -> {
            throw new AssertionError("Defaults should only be read once");
        }, map -> {
            throw new AssertionError("Defaults should only be updated once");
        });

        assertEquals(1, updateCount.get());
        assertEquals("existing-deployment", updatedDeployments.get().get("existing-model"));
        assertEquals("completion-deployment", updatedDeployments.get().get("gpt-5.2"));
        assertEquals("mini-deployment", updatedDeployments.get().get("gpt-5.2-mini"));
        assertEquals("completion-deployment", updatedDeployments.get().get("prebuilt-analyzer-completion"));
        assertEquals("mini-deployment", updatedDeployments.get().get("prebuilt-analyzer-completion-mini"));
        assertEquals("embedding-deployment", updatedDeployments.get().get("text-embedding-3-large"));
        assertEquals("embedding-deployment", updatedDeployments.get().get("prebuilt-analyzer-embedding"));
    }

    @Test
    public void skipsUpdateWhenDefaultsAlreadyMatch() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        Map<String, String> configuration = createConfiguration();
        Map<String, String> requiredDeployments
            = ContentUnderstandingLiveTestSetup.getRequiredDeployments(configuration::get);
        AtomicInteger updateCount = new AtomicInteger();

        setup.ensureConfigured(configuration::get, () -> new ContentUnderstandingDefaults(requiredDeployments), map -> {
            updateCount.incrementAndGet();
            return new ContentUnderstandingDefaults(map);
        });

        assertEquals(0, updateCount.get());
    }

    @Test
    public void updatesWhenDefaultsHaveNotBeenConfigured() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        Map<String, String> configuration = createConfiguration();
        AtomicInteger updateCount = new AtomicInteger();

        setup.ensureConfigured(configuration::get, () -> {
            throw httpException(400, "InvalidRequest: DefaultsNotSet");
        }, map -> {
            updateCount.incrementAndGet();
            return new ContentUnderstandingDefaults(map);
        });

        assertEquals(1, updateCount.get());
    }

    @Test
    public void unexpectedHttpErrorsPropagate() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        HttpResponseException expected = httpException(400, "InvalidRequest: Another service error");

        HttpResponseException actual
            = assertThrows(HttpResponseException.class, () -> setup.ensureConfigured(createConfiguration()::get, () -> {
                throw expected;
            }, ContentUnderstandingDefaults::new));

        assertSame(expected, actual);
    }

    @Test
    public void nullDefaultsTriggerUpdate() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        AtomicInteger updateCount = new AtomicInteger();

        setup.ensureConfigured(createConfiguration()::get, () -> null, map -> {
            updateCount.incrementAndGet();
            return new ContentUnderstandingDefaults(map);
        });

        assertEquals(1, updateCount.get());
    }

    @Test
    public void nullDeploymentMapTriggersUpdate() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        AtomicInteger updateCount = new AtomicInteger();

        setup.ensureConfigured(createConfiguration()::get, () -> new ContentUnderstandingDefaults(null), map -> {
            updateCount.incrementAndGet();
            return new ContentUnderstandingDefaults(map);
        });

        assertEquals(1, updateCount.get());
    }

    @Test
    public void failedUpdateCanBeRetried() {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        Map<String, String> configuration = createConfiguration();
        AtomicInteger updateCount = new AtomicInteger();
        RuntimeException expected = new RuntimeException("update failed");

        RuntimeException actual = assertThrows(RuntimeException.class, () -> setup.ensureConfigured(configuration::get,
            () -> new ContentUnderstandingDefaults(Collections.emptyMap()), map -> {
                updateCount.incrementAndGet();
                throw expected;
            }));
        assertSame(expected, actual);

        setup.ensureConfigured(configuration::get, () -> new ContentUnderstandingDefaults(Collections.emptyMap()),
            map -> {
                updateCount.incrementAndGet();
                return new ContentUnderstandingDefaults(map);
            });

        assertEquals(2, updateCount.get());
    }

    @Test
    public void concurrentCallsConfigureDefaultsOnce() throws Exception {
        ContentUnderstandingLiveTestSetup setup = new ContentUnderstandingLiveTestSetup();
        Map<String, String> configuration = createConfiguration();
        AtomicInteger readCount = new AtomicInteger();
        AtomicInteger updateCount = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> {
                    await(start);
                    setup.ensureConfigured(configuration::get, () -> {
                        readCount.incrementAndGet();
                        return new ContentUnderstandingDefaults(Collections.emptyMap());
                    }, map -> {
                        updateCount.incrementAndGet();
                        return new ContentUnderstandingDefaults(map);
                    });
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(1, readCount.get());
        assertEquals(1, updateCount.get());
    }

    private static Map<String, String> createConfiguration() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("CU_COMPLETION_MODEL", "gpt-5.2");
        configuration.put("CU_COMPLETION_MODEL_MINI", "gpt-5.2-mini");
        configuration.put("CU_COMPLETION_MODEL_DEPLOYMENT", "completion-deployment");
        configuration.put("CU_COMPLETION_MINI_DEPLOYMENT", "mini-deployment");
        configuration.put("CU_EMBEDDING_DEPLOYMENT", "embedding-deployment");
        return configuration;
    }

    private static HttpResponseException httpException(int statusCode, String message) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com/contentunderstanding/defaults");
        return new HttpResponseException(message, new MockHttpResponse(request, statusCode));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while coordinating live setup test.", exception);
        }
    }
}
