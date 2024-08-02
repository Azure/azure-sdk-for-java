// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener.ungzip;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils.deserialize;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils.toMetricsData;
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

        Thread.sleep(1000);

        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileLoader localFileLoader =
            new LocalFileLoader(localFileCache, tempFolder, LocalStorageStats.noop(), false);

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(1);

        // load expected telemetry items from the file and split bytes by newline
        byte[] expected = Resources.readBytes("request_body_result_to_206_status_code.txt");
        List<TelemetryItem> expectedTelemetryItems = deserialize(expected);

        // load the actual telemetry raw bytes from disk
        LocalFileLoader.PersistedFile file = localFileLoader.loadTelemetriesFromDisk();
        assertThat(file.connectionString).isEqualTo(CONNECTION_STRING);

        // un-gzip raw bytes back to original raw bytes
        byte[] ungzippedRawBytes = ungzip(file.rawBytes.array());

        // deserialize back to List<TelemetryItem>
        List<TelemetryItem> actualTelemetryItems = deserialize(ungzippedRawBytes);
        assertThat(actualTelemetryItems.size()).isEqualTo(expectedTelemetryItems.size());

        sort(expectedTelemetryItems);
        sort(actualTelemetryItems);

        for (int i = 0; i < actualTelemetryItems.size(); i++) {
            MetricsData expectedMetricsData = toMetricsData(expectedTelemetryItems.get(i).getData().getBaseData());
            MetricsData actualMetricsData = toMetricsData(actualTelemetryItems.get(i).getData().getBaseData());

            // verify metric name
            assertThat(expectedMetricsData.getMetrics().get(0).getName()).startsWith("to_be_persisted_offline_metric2" + i);
            assertThat(actualMetricsData.getMetrics().get(0).getName()).startsWith("to_be_persisted_offline_metric2" + i);
            assertThat(expectedMetricsData.getMetrics().get(0).getName()).isEqualTo(actualMetricsData.getMetrics().get(0).getName());

            // verify metric value
            assertThat(expectedMetricsData.getMetrics().get(0).getValue()).isEqualTo(actualMetricsData.getMetrics().get(0).getValue());

            // verify metric count
            assertThat(expectedMetricsData.getMetrics().get(0).getCount()).isEqualTo(actualMetricsData.getMetrics().get(0).getCount());

            // verify metric properties
            assertThat(expectedMetricsData.getProperties().get("state")).isEqualTo("to_be_persisted_offline");
            assertThat(actualMetricsData.getProperties().get("state")).isEqualTo("to_be_persisted_offline");
            assertThat(expectedMetricsData.getProperties().get("state")).isEqualTo(actualMetricsData.getProperties().get("state"));
        }

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(0);
    }

    private static void sort(List<TelemetryItem> telemetryItems) {
        telemetryItems.sort(Comparator.comparing(telemetryItem -> toMetricsData(telemetryItem.getData().getBaseData()).getMetrics().get(0).getName()));
    }
}
