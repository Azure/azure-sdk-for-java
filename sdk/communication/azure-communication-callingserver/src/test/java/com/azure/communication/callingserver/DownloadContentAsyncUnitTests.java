// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.util.Arrays;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

public class DownloadContentAsyncUnitTests {
    @Test
    public void downloadStream() throws IOException {
        String contents = "VideoContents";
        CallingServerAsyncClient callingServerClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateDownloadResult(contents), 200)
            )));
        
        Flux<ByteBuffer> fluxByteBuffer = callingServerClient.downloadStream("https://url.com", new HttpRange(contents.length()));
        
        String resultContents = new String(fluxByteBuffer.next().block().array(), StandardCharsets.UTF_8);
        assertEquals("VideoContents", resultContents);
    }

    @Test
    public void downloadStreamWithResponse() throws IOException {
        String contents = "VideoContents";
        CallingServerAsyncClient callingServerClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateDownloadResult(contents), 200)
            )));
        
        Response<Flux<ByteBuffer>> fluxByteBufferResponse = callingServerClient.downloadStreamWithResponse("https://url.com", new HttpRange(contents.length())).block();
        assertEquals(200, fluxByteBufferResponse.getStatusCode());
        Flux<ByteBuffer> fluxByteBuffer = fluxByteBufferResponse.getValue();
        String resultContents = new String(fluxByteBuffer.next().block().array(), StandardCharsets.UTF_8);
        assertEquals("VideoContents", resultContents);
    }
    
    @Test
    public void downloadStreamWithResponseThrowException() throws IOException {
        String contents = "VideoContents";
        CallingServerAsyncClient callingServerClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>("", 416)
            )));
        
        Response<Flux<ByteBuffer>> fluxByteBufferResponse = callingServerClient.downloadStreamWithResponse("https://url.com", new HttpRange(contents.length())).block();
        Flux<ByteBuffer> fluxByteBuffer = fluxByteBufferResponse.getValue();
        assertThrows(NullPointerException.class, () -> fluxByteBuffer.next().block());
    }

    @Test
    public void downloadToWithResponse() throws IOException {
        String contents = "VideoContents";
        CallingServerAsyncClient callingServerClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateDownloadResult(contents), 200)
            )));
        String fileName = "./" + UUID.randomUUID().toString().replace("-", "") + ".txt";
        Path path = FileSystems.getDefault().getPath(fileName);
        ParallelDownloadOptions options = new ParallelDownloadOptions().setMaxConcurrency(1).setBlockSize(512L);
        File file = null;

        try {
            Response<Void> response = callingServerClient.downloadToWithResponse("https://url.com", path, options, true).block();
            assertEquals(200, response.getStatusCode());
            
            file = path.toFile();
            assertTrue(file.exists(), "file does not exist");
            BufferedReader br = new BufferedReader(new FileReader(file));        
            assertEquals("VideoContents", br.readLine());
            br.close();
        } finally {
            if (file != null && file.exists()) {
                file.delete(); 
            }
        }
    }
}
