// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.localstorage;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalFileSenderTests {

    private static final String TRUSTED_CONNECTION_STRING
        = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=https://foo.in.applicationinsights.azure.com/";
    private static final String UNTRUSTED_CONNECTION_STRING
        = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=https://attacker.invalid/";

    @TempDir
    File tempFolder;

    @Test
    public void shouldDiscardPersistedTelemetryWithUntrustedEndpoint() throws MalformedURLException {
        AtomicBoolean requestSent = new AtomicBoolean();
        HttpClient client = request -> {
            requestSent.set(true);
            return Mono.error(new AssertionError("request must not be sent"));
        };
        File persistedFile = persist(UNTRUSTED_CONNECTION_STRING);

        runSender(client, new URL("https://foo.in.applicationinsights.azure.com/"));

        assertThat(requestSent).isFalse();
        assertThat(persistedFile).doesNotExist(); // discarded, not left to retry for 48h
    }

    @Test
    public void shouldSendPersistedTelemetryWithTrustedEndpoint() throws MalformedURLException {
        AtomicBoolean requestSent = new AtomicBoolean();
        HttpClient client = request -> {
            requestSent.set(true);
            return Mono.error(new AssertionError("stop before actually connecting"));
        };
        persist(TRUSTED_CONNECTION_STRING);

        runSender(client, new URL("https://foo.in.applicationinsights.azure.com/"));

        assertThat(requestSent).isTrue();
    }

    private File persist(String connectionString) {
        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileWriter localFileWriter = new LocalFileWriter(50, localFileCache, tempFolder, null, false);
        localFileWriter.writeToDisk(connectionString, singletonList(ByteBuffer.wrap("hello world".getBytes(UTF_8))),
            "original error message");
        List<File> files = FileUtil.listTrnFiles(tempFolder);
        assertThat(files).hasSize(1);
        return files.get(0);
    }

    private void runSender(HttpClient client, URL trustedEndpoint) {
        HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder().httpClient(client).tracer(new NoopTracer());
        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(pipelineBuilder.build(), null);
        LocalFileCache localFileCache = new LocalFileCache(tempFolder);
        LocalFileLoader localFileLoader = new LocalFileLoader(localFileCache, tempFolder, null, false);
        LocalFileSender sender
            = new LocalFileSender(100_000, localFileLoader, telemetryPipeline, true, trustedEndpoint);
        try {
            sender.run();
        } finally {
            sender.shutdown();
        }
    }
}
