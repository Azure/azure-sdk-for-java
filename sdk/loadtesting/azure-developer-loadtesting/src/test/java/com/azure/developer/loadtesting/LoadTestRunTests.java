// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.loadtesting.models.FileType;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.MetricDefinition;
import com.azure.developer.loadtesting.models.MetricDefinitions;
import com.azure.developer.loadtesting.models.MetricNamespace;
import com.azure.developer.loadtesting.models.MetricNamespaces;
import com.azure.developer.loadtesting.models.TestRunAppComponents;
import com.azure.developer.loadtesting.models.TestRunFileInfo;
import com.azure.developer.loadtesting.models.TestRunServerMetricsConfiguration;
import com.azure.developer.loadtesting.models.TestRunStatus;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("unchecked")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunTests extends LoadTestingClientTestBase {

    // Puts and Patches

    @Test
    @Order(1)
    public void beginTestRun() {

        LoadTestRun loadTestRun = new LoadTestRun().setTestId(existingTestId)
            .setDisplayName("Java SDK Sample Test Run")
            .setDescription("Sample Test Run");
        SyncPoller<LoadTestRun, LoadTestRun> poller
            = getLoadTestRunClient().beginTestRun(newTestRunId, loadTestRun, null);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<LoadTestRun> response = poller.waitForCompletion();
        LoadTestRun testRun = poller.getFinalResult();

        assertNotNull(testRun);
        assertNotNull(response.getValue());
        assertEquals(newTestRunId, testRun.getTestRunId());
        assertEquals(TestRunStatus.DONE.toString(), testRun.getStatus().toString());
    }

    @Test
    @Order(2)
    public void createOrUpdateAppComponents() {

        TestRunAppComponents appComponents = getTestRunAppComponents();
        TestRunAppComponents response = getLoadTestRunClient().createOrUpdateAppComponents(newTestRunId, appComponents);

        assertNotNull(response);
    }

    @Test
    @Order(3)
    public void createOrUpdateServerMetricsConfig() {

        TestRunServerMetricsConfiguration metricsConfig = getTestRunServerMetricsConfiguration();
        TestRunServerMetricsConfiguration response
            = getLoadTestRunClient().createOrUpdateServerMetricsConfig(newTestRunId, metricsConfig);

        assertNotNull(response);
    }

    // Gets

    @Test
    @Order(4)
    public void getTestRunFile() {

        TestRunFileInfo fileInfo = getLoadTestRunClient().getTestRunFile(newTestRunId, uploadJmxFileName);

        assertNotNull(fileInfo);
        assertEquals(uploadJmxFileName, fileInfo.getFileName());
        assertEquals(FileType.TEST_SCRIPT.toString(), fileInfo.getFileType().toString());
    }

    @Test
    @Order(5)
    public void getTestRun() {

        LoadTestRun testRun = getLoadTestRunClient().getTestRun(newTestRunId);
        assertNotNull(testRun);
        assertEquals(newTestRunId, testRun.getTestRunId());
    }

    @Test
    @Order(6)
    public void getAppComponents() {

        TestRunAppComponents appComponents = getLoadTestRunClient().getAppComponents(newTestRunId);
        assertNotNull(appComponents);
        assertEquals(newTestRunId, appComponents.getTestRunId());
        assertTrue(appComponents.getComponents().containsKey(defaultAppComponentResourceId));
        assertEquals(defaultAppComponentResourceId,
            appComponents.getComponents().get(defaultAppComponentResourceId).getResourceId());
    }

    @Test
    @Order(7)
    public void getServerMetricsConfig() {

        TestRunServerMetricsConfiguration metricsConfig = getLoadTestRunClient().getServerMetricsConfig(newTestRunId);
        assertNotNull(metricsConfig);
        assertEquals(newTestRunId, metricsConfig.getTestRunId());
        assertTrue(metricsConfig.getMetrics().containsKey(defaultServerMetricId));
        assertEquals(defaultServerMetricId, metricsConfig.getMetrics().get(defaultServerMetricId).getResourceId());
    }

    @Test
    @Order(8)
    public void listMetricNamespaces() {

        MetricNamespaces metricNamespaces = getLoadTestRunClient().getMetricNamespaces(newTestRunId);

        assertNotNull(metricNamespaces);
        List<MetricNamespace> namespaces = metricNamespaces.getValue();
        assertNotNull(namespaces);
        assertTrue(namespaces.size() > 0);
        assertTrue(namespaces.stream().anyMatch(namespace -> namespace.getName().equals("LoadTestRunMetrics")));
    }

    @Test
    @Order(9)
    public void listMetricDefinitions() {

        MetricDefinitions metricDefinitions
            = getLoadTestRunClient().getMetricDefinitions(newTestRunId, "LoadTestRunMetrics");
        assertNotNull(metricDefinitions);
        List<MetricDefinition> definitions = metricDefinitions.getValue();
        assertNotNull(definitions);
        assertTrue(definitions.size() > 0);
        assertTrue(definitions.stream()
            .anyMatch(definition -> definition.getName() != null
                && definition.getNamespace().equals("LoadTestRunMetrics")
                && definition.getDimensions() != null));
    }

    @Test
    @Order(10)
    public void listMetrics() {
        LoadTestRunClient loadTestRunClient = getLoadTestRunClient();
        LoadTestRun testRun = loadTestRunClient.getTestRun(newTestRunId);
        assertNotNull(testRun);
        String startDateTime
            = testRun.getStartDateTime().toInstant().atZone(ZoneId.of("UTC")).toOffsetDateTime().toString();
        String endDateTime
            = testRun.getEndDateTime().toInstant().atZone(ZoneId.of("UTC")).toOffsetDateTime().toString();

        String timespan = startDateTime + "/" + endDateTime;

        PagedIterable<BinaryData> metricsResponse
            = getLoadTestRunClient().listMetrics(newTestRunId, "VirtualUsers", "LoadTestRunMetrics", timespan, null);
        boolean valid = metricsResponse.stream().anyMatch(metricsBinary -> {
            AtomicBoolean found = new AtomicBoolean(false);

            try (JsonReader jsonReader = JsonProviders.createReader(metricsBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                if (jsonTree.containsKey("data")) {
                    List<Object> data = (List<Object>) jsonTree.get("data");

                    data.forEach(metric -> {
                        Map<String, Object> metricMap = (Map<String, Object>) metric;

                        if (metricMap.containsKey("value")) {
                            found.set(true);
                        }
                    });
                }
            } catch (IOException e) {
                // no-op
            }

            return found.get();
        });

        assertTrue(valid);
    }

    // Lists

    @Test
    @Order(11)
    public void listTestRuns() {

        PagedIterable<LoadTestRun> loadTestRuns = getLoadTestRunClient().listTestRuns("executedDateTime desc", null,
            existingTestId, null, null, null, null);
        boolean found = loadTestRuns.stream().anyMatch(testRun -> {
            return testRun.getTestRunId().equals(newTestRunId);

        });
        assertTrue(found);
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestRun() {
        assertDoesNotThrow(() -> {
            getLoadTestRunClient().deleteTestRunWithResponse(newTestRunId, null);
        });
    }
}
