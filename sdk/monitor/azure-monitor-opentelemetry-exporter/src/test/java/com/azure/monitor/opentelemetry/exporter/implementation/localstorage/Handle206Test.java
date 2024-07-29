// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.decode;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.deserialize;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class Handle206Test {

    private static final String CONNECTION_STRING =
        "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=http://foo.bar/";
    private TelemetryItemExporter telemetryItemExporter;

    @TempDir
    File tempFolder;

    @BeforeEach
    public void setup() {
        HttpClient mockedClient;
        final String[] mockedResponseBody = new String[1];
        mockedClient = httpRequest -> {
            try {
                mockedResponseBody[0] = Resources.readString("206_response_body.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Mono.just(new MockHttpResponse(httpRequest, 206, mockedResponseBody[0].getBytes()));
        };
        HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder()
            .httpClient(mockedClient)
            .tracer(new NoopTracer());

        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(pipelineBuilder.build(), null);
        telemetryItemExporter =
            new TelemetryItemExporter(
                telemetryPipeline,
                new LocalStorageTelemetryPipelineListener(
                    50, tempFolder, telemetryPipeline, LocalStorageStats.noop(), false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void statusCode206Test() throws Exception {
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String metricName = "metric" + i;
            if (i >= 20 && i <= 26) {
                metricName = "to_be_persisted_offline_metric" + i;
            }
            TelemetryItem item = TestUtils.createMetricTelemetry(metricName, i, CONNECTION_STRING, "state", "to_be_persisted_offline");
            item.setTime(OffsetDateTime.parse("2024-05-31T00:00:00.00Z"));
            telemetryItems.add(item);
        }

        telemetryItemExporter.send(telemetryItems);
        telemetryItemExporter.flush().join(30, SECONDS);

        Thread.sleep(10000);

        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileLoader localFileLoader =
            new LocalFileLoader(localFileCache, tempFolder, LocalStorageStats.noop(), false);

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(1);

        // load expected telemetry items from the file and split bytes by newline
        String expected = Resources.readString("request_body_result_to_206_status_code.txt");
        List<TelemetryItem> expectedTelemetryItems = deserialize(expected.getBytes(StandardCharsets.UTF_8));

        // load the actual telemetry raw bytes from disk
        LocalFileLoader.PersistedFile file = localFileLoader.loadTelemetriesFromDisk();
        assertThat(file.connectionString).isEqualTo(CONNECTION_STRING);

        // decode gzipped raw bytes back to original raw bytes
        byte[] decodedRawBytes = decode(file.rawBytes.array());

        // deserialize back to List<TelemetryItem>
        List<TelemetryItem> actualTelemetryItems = deserialize(decodedRawBytes);
        assertThat(actualTelemetryItems.size()).isEqualTo(expectedTelemetryItems.size());

        sort(expectedTelemetryItems);
        sort(actualTelemetryItems);

        for (int i = 0; i < actualTelemetryItems.size(); i++) {
            TelemetryItem actualItem = actualTelemetryItems.get(i);
            TelemetryItem expectedItem = expectedTelemetryItems.get(i);
            Map<String, Object> actualProperties = actualItem.getData().getBaseData().getAdditionalProperties();
            ArrayList<Map<String, Object>> actualMetricsData = (ArrayList<Map<String, Object>>) actualProperties.get("metrics");
            Map<String, Object> expectedProperties = expectedItem.getData().getBaseData().getAdditionalProperties();
            ArrayList<Map<String, Object>> expectedMetricsData = (ArrayList<Map<String, Object>>) expectedProperties.get("metrics");

            // verify metric name
            String expectedMetricName = expectedMetricsData.get(0).get("name").toString();
            String actualMetricName = actualMetricsData.get(0).get("name").toString();
            assertThat(expectedMetricName.startsWith("to_be_persisted_offline_metric2" + i)).isTrue();
            assertThat(actualMetricName.startsWith("to_be_persisted_offline_metric2" + i)).isTrue();
            assertThat(expectedMetricName).isEqualTo(actualMetricName);

            // verify metric value
            Double expectedMetricValue = (Double)expectedMetricsData.get(0).get("value");
            Double actualMetricValue = (Double)actualMetricsData.get(0).get("value");
            assertThat(expectedMetricValue).isEqualTo(actualMetricValue);

            // verify metric count
            Integer expectedMetricCount = (Integer) expectedMetricsData.get(0).get("count");
            Integer actualMetricCount = (Integer)actualMetricsData.get(0).get("count");
            assertThat(expectedMetricCount).isEqualTo(actualMetricCount);

            // verify metric properties
            Map<String, Object> actualMetricProperties = (Map<String, Object>) actualProperties.get("properties");
            Map<String, Object> expectedMetricProperties = (Map<String, Object>) expectedProperties.get("properties");
            assertThat(expectedMetricProperties.get("state")).isEqualTo(actualMetricProperties.get("state")).isEqualTo("to_be_persisted_offline");
        }

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(0);
    }

    @SuppressWarnings("unchecked")
    private static void sort(List<TelemetryItem> telemetryItems) {
        telemetryItems.sort(new Comparator<TelemetryItem>() {
            @Override
            public int compare(TelemetryItem o1, TelemetryItem o2) {
                String name1 = (String) ((List<Map<String, Object>>) o1.getData().getBaseData().getAdditionalProperties().get("metrics")).get(0).get("name");
                String name2 = (String) ((List<Map<String, Object>>) o2.getData().getBaseData().getAdditionalProperties().get("metrics")).get(0).get("name");
                return name1.compareTo(name2);
            }
        });
    }
}
