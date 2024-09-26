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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTests {

    private static final String CONNECTION_STRING
        = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=http://foo.bar/";

    private TelemetryItemExporter telemetryItemExporter;

    @TempDir
    File tempFolder;

    // TODO (trask) test with both
    private static final boolean testWithException = false;

    @BeforeEach
    public void setup() {
        HttpClient mockedClient;
        if (testWithException) {
            mockedClient = httpRequest -> Mono
                .error(() -> new Exception("this is expected to be logged by the operation logger"));
        } else {
            // 401, 403, 408, 429, 500, and 503 response codes result in storing to disk
            mockedClient = httpRequest -> Mono.just(new MockHttpResponse(httpRequest, 500));
        }
        HttpPipelineBuilder pipelineBuilder
            = new HttpPipelineBuilder().httpClient(mockedClient).tracer(new NoopTracer());

        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(pipelineBuilder.build(), null);
        telemetryItemExporter
            = new TelemetryItemExporter(telemetryPipeline, new LocalStorageTelemetryPipelineListener(50, tempFolder,
                telemetryPipeline, LocalStorageStats.noop(), false));
    }

    @Test
    public void integrationTest() throws Exception {
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TelemetryItem item = TestUtils.createMetricTelemetry("metric" + i, i, CONNECTION_STRING);
            item.setTime(OffsetDateTime.parse("2021-11-09T03:12:19.06Z"));
            telemetryItems.add(item);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < 10; j++) {
                    telemetryItemExporter.send(telemetryItems);
                }
            });
        }

        telemetryItemExporter.flush();

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        Thread.sleep(1000);

        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileLoader localFileLoader
            = new LocalFileLoader(localFileCache, tempFolder, LocalStorageStats.noop(), false);

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(100);

        String expected = Resources.readString("ungzip-source.txt").replace("\r\n", "\n");

        for (int i = 100; i > 0; i--) {
            LocalFileLoader.PersistedFile file = localFileLoader.loadTelemetriesFromDisk();
            assertThat(file.connectionString).isEqualTo(CONNECTION_STRING);
            assertThat(ungzip(file.rawBytes.array())).contains(expected);
            assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(i - 1);
        }

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(0);
    }

    private static String ungzip(byte[] rawBytes) throws Exception {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, read);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
