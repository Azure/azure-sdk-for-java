// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.developer.loadtesting.models.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunAsyncTests extends LoadTestingClientTestBase {

    // Puts and Patches

    @Test
    @Order(1)
    public void beginTestRun() {
        LoadTestRun loadTestRun = new LoadTestRun().setTestId(existingTestId)
            .setDisplayName("Java SDK Sample Test Run Async")
            .setDescription("Sample Test Run Async");
        PollerFlux<LoadTestRun, LoadTestRun> poller
            = getLoadTestRunAsyncClient().beginTestRun(newTestRunIdAsync, loadTestRun);
        poller = setPlaybackPollerFluxPollInterval(poller);
        StepVerifier.create(poller.takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)).assertNext(testRun -> {
                assertNotNull(testRun);
                assertEquals(newTestRunIdAsync, testRun.getTestRunId());
                assertEquals(TestRunStatus.DONE, testRun.getStatus());
            }).verifyComplete();
    }

    @Test
    @Order(2)
    public void beginTestProfileRun() {

        TestProfileRun testProfileRun = new TestProfileRun().setTestProfileId(existingTestProfileIdAsync)
            .setDisplayName("Java SDK Sample Test Profile Run")
            .setDescription("Sample Test Profile Run");
        PollerFlux<TestProfileRun, TestProfileRun> poller
            = getLoadTestRunAsyncClient().beginTestProfileRun(newTestProfileRunIdAsync, testProfileRun);

        poller = setPlaybackPollerFluxPollInterval(poller);
        StepVerifier.create(poller.takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)).assertNext(testProfileRunResponse -> {
                assertNotNull(testProfileRunResponse);
                assertEquals(newTestProfileRunIdAsync, testProfileRunResponse.getTestProfileRunId());
                assertEquals(TestProfileRunStatus.DONE.toString(), testProfileRunResponse.getStatus().toString());
            }).verifyComplete();
    }

    @Test
    @Order(3)
    public void createOrUpdateAppComponents() {
        TestRunAppComponents appComponents = getTestRunAppComponents();
        Mono<TestRunAppComponents> monoResponse
            = getLoadTestRunAsyncClient().createOrUpdateAppComponents(newTestRunIdAsync, appComponents);

        StepVerifier.create(monoResponse).assertNext(response -> {
            assertNotNull(response);
            assertEquals(newTestRunIdAsync, response.getTestRunId());
            assertTrue(response.getComponents().containsKey(defaultAppComponentResourceId));
            assertEquals(defaultAppComponentResourceId,
                response.getComponents().get(defaultAppComponentResourceId).getResourceId());
        }).verifyComplete();
    }

    @Test
    @Order(4)
    public void createOrUpdateServerMetricsConfig() {
        TestRunServerMetricsConfiguration metricsConfig = getTestRunServerMetricsConfiguration();
        Mono<TestRunServerMetricsConfiguration> monoResponse
            = getLoadTestRunAsyncClient().createOrUpdateServerMetricsConfig(newTestRunIdAsync, metricsConfig);

        StepVerifier.create(monoResponse).assertNext(response -> {
            assertNotNull(response);
            assertEquals(newTestRunIdAsync, response.getTestRunId());
            assertTrue(response.getMetrics().containsKey(defaultServerMetricId));
            assertEquals(defaultServerMetricId, response.getMetrics().get(defaultServerMetricId).getResourceId());
        }).verifyComplete();
    }

    // Gets

    @Test
    @Order(5)
    public void getTestRunFile() {
        Mono<TestRunFileInfo> monoResponse
            = getLoadTestRunAsyncClient().getTestRunFile(newTestRunIdAsync, uploadJmxFileName);

        StepVerifier.create(monoResponse).assertNext(fileInfo -> {
            assertNotNull(fileInfo);
            assertEquals(uploadJmxFileName, fileInfo.getFileName());
            assertEquals(LoadTestingFileType.TEST_SCRIPT.toString(), fileInfo.getFileType().toString());
        }).verifyComplete();
    }

    @Test
    @Order(6)
    public void getTestRun() {
        Mono<LoadTestRun> monoResponse = getLoadTestRunAsyncClient().getTestRun(newTestRunIdAsync);

        StepVerifier.create(monoResponse).assertNext(testRun -> {
            assertNotNull(testRun);
            assertEquals(newTestRunIdAsync, testRun.getTestRunId());
        }).verifyComplete();
    }

    @Test
    @Order(7)
    public void getTestProfileRun() {
        Mono<TestProfileRun> monoResponse = getLoadTestRunAsyncClient().getTestProfileRun(newTestProfileRunIdAsync);

        StepVerifier.create(monoResponse).assertNext(testProfileRun -> {
            assertNotNull(testProfileRun);
            assertEquals(newTestProfileRunIdAsync, testProfileRun.getTestProfileRunId());
            assertTrue(testProfileRun.getRecommendations().size() > 0);
        }).verifyComplete();
    }

    @Test
    @Order(8)
    public void getAppComponents() {
        Mono<TestRunAppComponents> monoResponse = getLoadTestRunAsyncClient().getAppComponents(newTestRunIdAsync);

        StepVerifier.create(monoResponse).assertNext(appComponents -> {
            assertNotNull(appComponents);
            assertEquals(newTestRunIdAsync, appComponents.getTestRunId());
            assertTrue(appComponents.getComponents().containsKey(defaultAppComponentResourceId));
            assertEquals(defaultAppComponentResourceId,
                appComponents.getComponents().get(defaultAppComponentResourceId).getResourceId());
        }).verifyComplete();
    }

    @Test
    @Order(9)
    public void getServerMetricsConfig() {
        Mono<TestRunServerMetricsConfiguration> monoResponse
            = getLoadTestRunAsyncClient().getServerMetricsConfig(newTestRunIdAsync);

        StepVerifier.create(monoResponse).assertNext(metricsConfig -> {
            assertNotNull(metricsConfig);
            assertEquals(newTestRunIdAsync, metricsConfig.getTestRunId());
            assertTrue(metricsConfig.getMetrics().containsKey(defaultServerMetricId));
            assertEquals(defaultServerMetricId, metricsConfig.getMetrics().get(defaultServerMetricId).getResourceId());
        }).verifyComplete();
    }

    @Test
    @Order(10)
    public void listMetricNamespaces() {
        Mono<MetricNamespaces> monoResponse = getLoadTestRunAsyncClient().getMetricNamespaces(newTestRunIdAsync);

        StepVerifier.create(monoResponse).assertNext(metricNamespaces -> {
            assertNotNull(metricNamespaces);
            List<MetricNamespace> namespaces = metricNamespaces.getValue();
            assertNotNull(namespaces);
            assertTrue(namespaces.size() > 0);
            assertTrue(namespaces.stream().anyMatch(namespace -> "LoadTestRunMetrics".equals(namespace.getName())));
        }).verifyComplete();
    }

    @Test
    @Order(11)
    public void listMetricDefinitions() {
        Mono<MetricDefinitions> monoResponse
            = getLoadTestRunAsyncClient().getMetricDefinitions(newTestRunIdAsync, "LoadTestRunMetrics");

        StepVerifier.create(monoResponse).assertNext(metricDefinitions -> {
            assertNotNull(metricDefinitions);
            List<MetricDefinition> definitions = metricDefinitions.getValue();
            assertNotNull(definitions);
            assertTrue(definitions.size() > 0);
            assertTrue(definitions.stream()
                .anyMatch(definition -> definition.getName() != null
                    && "LoadTestRunMetrics".equals(definition.getNamespace())
                    && definition.getDimensions() != null));
        }).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(12)
    public void listMetrics() {
        Mono<Boolean> foundMetricsMono = getLoadTestRunAsyncClient().getTestRun(newTestRunIdAsync).flatMap(testRun -> {
            assertNotNull(testRun);
            String startDateTime
                = testRun.getStartDateTime().toInstant().atZone(ZoneId.of("UTC")).toOffsetDateTime().toString();
            String endDateTime
                = testRun.getEndDateTime().toInstant().atZone(ZoneId.of("UTC")).toOffsetDateTime().toString();
            String timespan = startDateTime + "/" + endDateTime;

            PagedFlux<BinaryData> metricsFlux = getLoadTestRunAsyncClient().listMetrics(newTestRunIdAsync,
                "VirtualUsers", "LoadTestRunMetrics", timespan, null);

            return metricsFlux.map(metricsBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(metricsBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
                    if (jsonTree.containsKey("data")) {
                        List<Object> data = (List<Object>) jsonTree.get("data");
                        return data.stream().anyMatch(metric -> {
                            Map<String, Object> metricMap = (Map<String, Object>) metric;
                            return metricMap.containsKey("value");
                        });
                    }
                } catch (IOException e) {
                    fail("Encountered exception while reading metrics data", e);
                }
                return false;
            }).any(hasValue -> hasValue);
        });

        StepVerifier.create(foundMetricsMono).expectNext(true).verifyComplete();
    }

    // Lists

    @Test
    @Order(13)
    public void listTestRuns() {
        PagedFlux<LoadTestRun> pagedFlux = getLoadTestRunAsyncClient().listTestRuns("executedDateTime desc", // orderBy
            null, // search
            existingTestId, // testId
            null, // executionFrom (OffsetDateTime)
            null, // executionTo (OffsetDateTime)
            null, // status
            null // maxPageSize (Integer)
        );

        StepVerifier.create(pagedFlux.any(testRun -> newTestRunIdAsync.equals(testRun.getTestRunId())))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @Order(14)
    public void listTestProfileRuns() {

        ArrayList<String> testProfileIds = new ArrayList<>();
        testProfileIds.add(existingTestProfileIdAsync);
        PagedFlux<TestProfileRun> pagedFlux = getLoadTestRunAsyncClient().listTestProfileRuns(null, null, null, null,
            null, null, null, testProfileIds, null);

        StepVerifier
            .create(
                pagedFlux.any(testProfileRun -> newTestProfileRunIdAsync.equals(testProfileRun.getTestProfileRunId())))
            .expectNext(true)
            .verifyComplete();
    }

    // Deletes

    @Test
    @Order(15)
    public void deleteTestRun() {
        Mono<Void> monoVoid = getLoadTestRunAsyncClient().deleteTestRun(newTestRunIdAsync);
        StepVerifier.create(monoVoid).verifyComplete();
    }

    @Test
    @Order(16)
    public void deleteTestProfileRun() {
        Mono<Void> monoVoid = getLoadTestRunAsyncClient().deleteTestProfileRun(newTestProfileRunIdAsync);
        StepVerifier.create(monoVoid).verifyComplete();
    }
}
