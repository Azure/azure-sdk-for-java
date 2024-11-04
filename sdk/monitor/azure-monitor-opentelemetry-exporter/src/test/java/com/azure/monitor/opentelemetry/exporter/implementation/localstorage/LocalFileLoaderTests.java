// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalFileLoaderTests {

    private static final String GZIPPED_RAW_BYTES_WITHOUT_IKEY = "gzipped-raw-bytes-without-ikey.trn";
    private static final String GZIPPED_RAW_BYTES_WITHOUT_INGESTION_ENDPOINT
        = "gzipped-raw-bytes-without-ingestion-endpoint.trn";
    private static final String CONNECTION_STRING
        = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=http://foo.bar/";

    @TempDir
    File tempFolder;

    @Test
    public void testPersistedFileWithoutInstrumentationKey() throws IOException {
        File persistedFile = new File(tempFolder, GZIPPED_RAW_BYTES_WITHOUT_IKEY);
        byte[] bytes = Resources.readBytes(GZIPPED_RAW_BYTES_WITHOUT_IKEY);
        Files.write(persistedFile.toPath(), bytes);
        assertThat(persistedFile.exists()).isTrue();

        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        localFileCache.addPersistedFile(persistedFile);

        LocalFileLoader localFileLoader = new LocalFileLoader(localFileCache, tempFolder, null, false);
        LocalFileLoader.PersistedFile loadedPersistedFile = localFileLoader.loadTelemetriesFromDisk();
        assertThat(loadedPersistedFile).isNull();
        assertThat(persistedFile.exists()).isFalse(); // verify the old formatted trn is deleted successfully.
    }

    @Test
    public void testPersistedFileWithoutIngestionEndpoint() throws IOException {

        File persistedFile = new File(tempFolder, GZIPPED_RAW_BYTES_WITHOUT_INGESTION_ENDPOINT);
        byte[] bytes = Resources.readBytes(GZIPPED_RAW_BYTES_WITHOUT_INGESTION_ENDPOINT);
        Files.write(persistedFile.toPath(), bytes);
        assertThat(persistedFile.exists()).isTrue();

        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        localFileCache.addPersistedFile(persistedFile);

        LocalFileLoader localFileLoader = new LocalFileLoader(localFileCache, tempFolder, null, false);
        LocalFileLoader.PersistedFile loadedPersistedFile = localFileLoader.loadTelemetriesFromDisk();
        assertThat(loadedPersistedFile).isNull();
        assertThat(persistedFile.exists()).isFalse(); // verify the old formatted trn is deleted successfully.
    }

    @Test
    public void testDeleteFilePermanentlyOnSuccess() throws Exception {
        HttpClient mockedClient = getMockHttpClientSuccess();
        HttpPipelineBuilder pipelineBuilder
            = new HttpPipelineBuilder().httpClient(mockedClient).tracer(new NoopTracer());
        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileWriter localFileWriter = new LocalFileWriter(50, localFileCache, tempFolder, null, false);
        LocalFileLoader localFileLoader = new LocalFileLoader(localFileCache, tempFolder, null, false);

        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(pipelineBuilder.build(), null);

        // persist 10 files to disk
        for (int i = 0; i < 10; i++) {
            localFileWriter.writeToDisk(CONNECTION_STRING,
                singletonList(ByteBuffer.wrap("hello world".getBytes(UTF_8))), "original error message");
        }

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(10);

        List<File> files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(10);

        int expectedCount = 10;

        // send persisted files one by one and then delete it permanently.
        for (int i = 0; i < 10; i++) {
            LocalFileLoader.PersistedFile persistedFile = localFileLoader.loadTelemetriesFromDisk();
            CompletableResultCode completableResultCode
                = telemetryPipeline.send(singletonList(persistedFile.rawBytes), persistedFile.connectionString,
                    new LocalFileSenderTelemetryPipelineListener(localFileLoader, persistedFile.file));
            completableResultCode.join(10, SECONDS);
            assertThat(completableResultCode.isSuccess()).isEqualTo(true);

            // sleep 1 second to wait for delete to complete
            Thread.sleep(1000);

            files = FileUtil.listTrnFiles(tempFolder);
            assertThat(files.size()).isEqualTo(--expectedCount);
        }

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(0);
    }

    @Test
    public void testDeleteFilePermanentlyOnFailure() {
        HttpClient mockedClient
            = httpRequest -> Mono.error(() -> new Exception("this is expected to be logged by the operation logger"));
        HttpPipelineBuilder pipelineBuilder
            = new HttpPipelineBuilder().httpClient(mockedClient).tracer(new NoopTracer());
        LocalFileCache localFileCache = new LocalFileCache(tempFolder);

        LocalFileLoader localFileLoader = new LocalFileLoader(localFileCache, tempFolder, null, false);
        LocalFileWriter localFileWriter = new LocalFileWriter(50, localFileCache, tempFolder, null, false);

        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(pipelineBuilder.build(), null);

        // persist 10 files to disk
        for (int i = 0; i < 10; i++) {
            localFileWriter.writeToDisk(CONNECTION_STRING,
                singletonList(ByteBuffer.wrap("hello world".getBytes(UTF_8))), "original error message");
        }

        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(10);

        List<File> files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(10);

        // fail to send persisted files and expect them to be kept on disk
        for (int i = 0; i < 10; i++) {
            LocalFileLoader.PersistedFile persistedFile = localFileLoader.loadTelemetriesFromDisk();
            assertThat(persistedFile.connectionString).isEqualTo(CONNECTION_STRING);

            CompletableResultCode completableResultCode
                = telemetryPipeline.send(singletonList(persistedFile.rawBytes), persistedFile.connectionString,
                    new LocalFileSenderTelemetryPipelineListener(localFileLoader, persistedFile.file));
            completableResultCode.join(10, SECONDS);
            assertThat(completableResultCode.isSuccess()).isEqualTo(false);
        }

        files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files.size()).isEqualTo(10);
        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(10);
    }

    private static HttpClient getMockHttpClientSuccess() {
        return new MockHttpClient(request -> {
            return Mono.just(new MockHttpResponse(request, 200));
        });
    }

    private static class MockHttpClient implements HttpClient {
        private final Function<HttpRequest, Mono<HttpResponse>> handler;

        MockHttpClient(Function<HttpRequest, Mono<HttpResponse>> handler) {
            this.handler = handler;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            return handler.apply(httpRequest);
        }
    }
}
