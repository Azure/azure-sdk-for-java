// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.MockHttpResponse;
import com.azure.core.v2.util.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamResponseTest {
    private static final int RESPONSE_CODE = 206;

    private final AtomicInteger closeCalls = new AtomicInteger();
    private Response<?> response;
    private final HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com");
    private byte[] responseValue;
    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        responseValue = new byte[128];
        fillArray(responseValue);
        response = new MockHttpResponse(request, RESPONSE_CODE, headers, responseValue) {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
                super.close();
            }
        };
    }

    @Test
    public void testCtors() {
        createStreamResponses().forEach(streamResponse -> {
            assertEquals(RESPONSE_CODE, streamResponse.getStatusCode());
            assertSame(headers, streamResponse.getHeaders());
            assertSame(request, streamResponse.getRequest());
        });
    }

    @Test
    public void closeDelegates() {
        StreamResponse streamResponse = new StreamResponse(response);

        streamResponse.close();

        assertEquals(1, closeCalls.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void closeDisposesFlux() {
        AtomicBoolean wasRead = new AtomicBoolean(false);
        Flux<ByteBuffer> value
            = Flux.using(() -> wasRead, ignored -> Flux.just(ByteBuffer.wrap(responseValue)), read -> read.set(true));
        StreamResponse streamResponse = new StreamResponse(request, RESPONSE_CODE, headers, value);

        streamResponse.close();

        assertTrue(wasRead.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void closeDisposesOnce() {
        AtomicInteger numberOfReads = new AtomicInteger();
        Flux<ByteBuffer> value = Flux.using(() -> numberOfReads, ignored -> Flux.just(ByteBuffer.wrap(responseValue)),
            AtomicInteger::incrementAndGet);
        StreamResponse streamResponse = new StreamResponse(request, RESPONSE_CODE, headers, value);

        streamResponse.close();
        streamResponse.close();

        assertEquals(1, numberOfReads.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void valueConsumptionDisposes() {
        AtomicInteger numberOfReads = new AtomicInteger();
        Flux<ByteBuffer> value = Flux.using(() -> numberOfReads, ignored -> Flux.just(ByteBuffer.wrap(responseValue)),
            AtomicInteger::incrementAndGet);
        StreamResponse streamResponse = new StreamResponse(request, RESPONSE_CODE, headers, value);

        streamResponse.getValue().then().block(); // This marks StreamResponse as consumed and increments numberOfReads
        streamResponse.close(); // Check that close is no-op after reading value.

        assertEquals(1, numberOfReads.get());
    }

    @Test
    public void consumingValueClosesResponse() {
        StreamResponse streamResponse = new StreamResponse(response);

        streamResponse.getValue().then().block();

        assertEquals(1, closeCalls.get());
    }

    @Test
    public void responseIsClosedOneTime() {
        StreamResponse streamResponse = new StreamResponse(response);

        streamResponse.close();
        streamResponse.close();

        assertEquals(1, closeCalls.get());
    }

    @Test
    public void closeIsNoopAfterConsumption() {
        StreamResponse streamResponse = new StreamResponse(response);

        streamResponse.getValue().then().block();
        streamResponse.close();

        assertEquals(1, closeCalls.get());
    }

    @Test
    public void transferToAsyncChannel() {
        createStreamResponses().forEach(streamResponse -> {
            try {
                Path tempFile = Files.createTempFile("streamresponsetest", null);
                tempFile.toFile().deleteOnExit();

                StepVerifier.create(Mono.using(
                    () -> IOUtils
                        .toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
                    streamResponse::writeValueToAsync, channel -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    })).verifyComplete();

                assertArraysEqual(responseValue, Files.readAllBytes(tempFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void transferToWriteableChannel() {
        createStreamResponses().forEach(streamResponse -> {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            streamResponse.writeValueTo(Channels.newChannel(bos));

            assertArraysEqual(responseValue, bos.toByteArray());
        });
    }

    @SuppressWarnings("deprecation")
    public Stream<StreamResponse> createStreamResponses() {
        return Stream.of(new StreamResponse(request, RESPONSE_CODE, headers, Flux.just(ByteBuffer.wrap(responseValue))),
            new StreamResponse(response));
    }
}
