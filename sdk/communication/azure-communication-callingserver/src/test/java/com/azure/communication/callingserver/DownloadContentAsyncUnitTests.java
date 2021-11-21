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

import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.core.http.HttpRange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class DownloadContentAsyncUnitTests {

    private static final String CONTENTS = "VideoContents";
    private CallingServerAsyncClient callingServerClient;

    @BeforeEach
    public void setup() {
        callingServerClient =
            CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<>(
                Collections.singletonList(
                    new SimpleEntry<>(CallingServerResponseMocker.generateDownloadResult(CONTENTS), 200)
                )));
    }
    @Test
    public void downloadStream() {
        StepVerifier.create(
            callingServerClient.downloadStream(
                "https://url.com",
                new HttpRange(CONTENTS.length()))
        ).consumeNextWith(byteBuffer -> {
            String resultContents = new String(byteBuffer.array(), StandardCharsets.UTF_8);
            assertEquals(CONTENTS, resultContents);
        }).verifyComplete();
    }

    @Test
    public void downloadStreamWithResponse() {
        StepVerifier.create(
            callingServerClient.downloadStreamWithResponse(
                "https://url.com",
                new HttpRange(CONTENTS.length()))
        ).consumeNextWith(response -> {
            assertEquals(200, response.getStatusCode());
            verifyContents(response.getValue());
        }).verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseThrowException() {
        callingServerClient =
            CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<>(
                Collections.singletonList(
                    new SimpleEntry<>("", 416)
                )));

        StepVerifier.create(
            callingServerClient.downloadStreamWithResponse("https://url.com", new HttpRange(CONTENTS.length()))
        ).consumeNextWith(response -> {
            StepVerifier.create(response.getValue())
                .verifyError(NullPointerException.class);
        });
    }

    @Test
    public void downloadToWithResponse() throws IOException {
        String fileName = "./" + UUID.randomUUID().toString().replace("-", "") + ".txt";
        Path path = FileSystems.getDefault().getPath(fileName);
        ParallelDownloadOptions options = new ParallelDownloadOptions().setMaxConcurrency(1).setBlockSize(512L);
        File file = null;

        try {
            StepVerifier.create(callingServerClient.downloadToWithResponse("https://url.com", path, options, true))
                .consumeNextWith(response -> {
                    assertEquals(200, response.getStatusCode());
                }).verifyComplete();

            file = path.toFile();
            assertTrue(file.exists(), "file does not exist");
            BufferedReader br = new BufferedReader(new FileReader(file));
            assertEquals(CONTENTS, br.readLine());
            br.close();
        } finally {
            if (file != null && file.exists()) {
                file.delete();
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
