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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.PipelineUtil.deserializeTelemetryItem;
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
            TelemetryItem item = TestUtils.createMetricTelemetry(metricName, i, CONNECTION_STRING);
            item.setTime(OffsetDateTime.parse("2024-05-31T00:00:00.00Z"));
            telemetryItems.add(item);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(
            () -> {
                telemetryItemExporter.send(telemetryItems);
            });

        telemetryItemExporter.flush();

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        Thread.sleep(1000);

        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileLoader localFileLoader =
            new LocalFileLoader(localFileCache, tempFolder, LocalStorageStats.noop(), false);

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(1);

        String expected = Resources.readString("request_body_result_to_206_status_code.txt");
        List<TelemetryItem> expectedTelemetryItems = deserializeTelemetryItem(expected.getBytes());
        LocalFileLoader.PersistedFile file = localFileLoader.loadTelemetriesFromDisk();
        assertThat(file.connectionString).isEqualTo(CONNECTION_STRING);
        List<TelemetryItem> actualTelemetryItems = deserializeTelemetryItem(file.rawBytes.array());
        actualTelemetryItems.sort(Comparator.comparing(TelemetryItem::getName));
        assertThat(actualTelemetryItems).isEqualTo(expectedTelemetryItems);

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(0);
    }
}
