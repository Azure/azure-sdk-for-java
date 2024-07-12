// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.models.DownloadToFileOptions;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.core.http.HttpRange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class DownloadContentAsyncUnitTests {

    private static final String CONTENTS = "VideoContents";
    private static final String AMS_ENDPOINT = "https://url.com";

    private CallRecordingAsync callRecording;

    @BeforeEach
    public void setup() {
        CallAutomationAsyncClient callingServerClient =
            CallAutomationUnitTestBase.getCallAutomationAsyncClient(new ArrayList<>(
                Collections.singletonList(
                    new SimpleEntry<>(CallAutomationUnitTestBase.generateDownloadResult(CONTENTS), 200)
                )));
        callRecording = callingServerClient.getCallRecordingAsync();
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void downloadStream() {
        StepVerifier.create(
            callRecording.downloadStream(AMS_ENDPOINT)
        ).consumeNextWith(byteBuffer -> {
            String resultContents = new String(byteBuffer.array(), StandardCharsets.UTF_8);
            assertEquals(CONTENTS, resultContents);
        }).verifyComplete();
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void downloadStreamWithResponse() {
        StepVerifier.create(
            callRecording.downloadStreamWithResponse(
                AMS_ENDPOINT,
                new HttpRange(CONTENTS.length()))
        ).consumeNextWith(response -> {
            assertEquals(200, response.getStatusCode());
            verifyContents(response.getValue());
        }).verifyComplete();
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void downloadStreamWithResponseThrowException() {
        CallAutomationAsyncClient callingServerClient =
            CallAutomationUnitTestBase.getCallAutomationAsyncClient(new ArrayList<>(
                Collections.singletonList(
                    new SimpleEntry<>("", 416)
                )));
        callRecording = callingServerClient.getCallRecordingAsync();

        StepVerifier.create(
            callRecording.downloadStreamWithResponse(AMS_ENDPOINT, new HttpRange(CONTENTS.length()))
        ).consumeNextWith(response ->
            StepVerifier.create(response.getValue()).verifyError(NullPointerException.class));
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void downloadToWithResponse() throws IOException {
        String fileName = "./" + UUID.randomUUID().toString().replace("-", "") + ".txt";
        Path path = FileSystems.getDefault().getPath(fileName);
        ParallelDownloadOptions parallelOptions = new ParallelDownloadOptions().setMaxConcurrency(1).setBlockSize(512L);
        DownloadToFileOptions options = new DownloadToFileOptions().setParallelDownloadOptions(parallelOptions).setOverwrite(true);
        File file = null;

        try {
            StepVerifier.create(callRecording.downloadToWithResponse(AMS_ENDPOINT, path, options))
                .consumeNextWith(response -> assertEquals(200, response.getStatusCode())).verifyComplete();

            file = path.toFile();
            assertTrue(file.exists(), "file does not exist");
            BufferedReader br = new BufferedReader(new FileReader(file));
            assertEquals(CONTENTS, br.readLine());
            br.close();
        } finally {
            if (file != null && file.exists()) {
                assertTrue(file.delete());
            }
        }
    }

    private void verifyContents(Flux<ByteBuffer> response) {
        StepVerifier.create(response)
            .consumeNextWith(byteBuffer -> {
                String resultContents = new String(byteBuffer.array(), StandardCharsets.UTF_8);
                assertEquals(CONTENTS, resultContents);
            }).verifyComplete();
    }
}
