// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalFileWriterTests {

    private static final String CONNECTION_STRING
        = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=http://foo.bar/";

    private LocalFileCache localFileCache;

    @TempDir
    File tempFolder;

    @BeforeEach
    public void setup() {
        localFileCache = new LocalFileCache(tempFolder);
    }

    @Test
    public void testWriteByteBuffersList() throws IOException {
        String content = Resources.readString("write-transmission.txt");

        List<ByteBuffer> byteBuffers = new ArrayList<>();
        String[] telemetries = content.split("\n");
        for (int i = 0; i < telemetries.length; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(telemetries[i].getBytes(UTF_8));
            if (i < telemetries.length - 1) {
                baos.write('\r');
            }
            byteBuffers.add(ByteBuffer.wrap(baos.toByteArray()));
        }

        assertThat(byteBuffers.size()).isEqualTo(10);

        LocalFileWriter writer = new LocalFileWriter(50, localFileCache, tempFolder, null, false);
        writer.writeToDisk(CONNECTION_STRING, byteBuffers, "original error message");
        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(1);
    }

    @Test
    public void testWriteRawByteArray() throws IOException {
        LocalFileWriter writer = new LocalFileWriter(50, localFileCache, tempFolder, null, false);
        byte[] content = Resources.readBytes("write-transmission.txt");
        writer.writeToDisk(CONNECTION_STRING, singletonList(ByteBuffer.wrap(content)), "original error message");
        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(1);
    }

    @Test
    public void testWriteUnderMultipleThreadsEnvironment() throws InterruptedException {
        String telemetry
            = "{\"ver\":1,\"name\":\"Metric\",\"time\":\"2021-06-14T17:24:28.983-0700\",\"sampleRate\":100,\"iKey\":\"00000000-0000-0000-0000-0FEEDDADBEEF\",\"tags\":{\"ai.internal.sdkVersion\":\"java:3.1.1\",\"ai.internal.nodeName\":\"test-role-name\",\"ai.cloud.roleInstance\":\"test-role-instance\"},\"data\":{\"baseType\":\"MetricData\",\"baseData\":{\"ver\":2,\"metrics\":[{\"name\":\"jvm_threads_states\",\"kind\":0,\"value\":3}],\"properties\":{\"state\":\"blocked\"}}}}";

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < 10; j++) {
                    LocalFileWriter writer = new LocalFileWriter(50, localFileCache, tempFolder, null, false);
                    writer.writeToDisk(CONNECTION_STRING, singletonList(ByteBuffer.wrap(telemetry.getBytes(UTF_8))),
                        "original error message");
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        assertThat(localFileCache.getPersistedFilesCache().size()).isEqualTo(1000);
    }
}
