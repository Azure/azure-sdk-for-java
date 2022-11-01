// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.util.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link StreamResponseBase}.
 */
public class StreamResponseBaseTests {
    private static final Random RANDOM = new Random();
    private static final int RESPONSE_CODE = 206;

    private HttpResponse response;
    private final HttpRequest requestMock = Mockito.mock(HttpRequest.class);
    private byte[] responseValue;
    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        responseValue = new byte[128];
        RANDOM.nextBytes(responseValue);
        response = Mockito.spy(new MockHttpResponse(requestMock, RESPONSE_CODE, headers, responseValue));
    }

    @Test
    public void testCtors() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);

        assertEquals(RESPONSE_CODE, streamResponseBase.getStatusCode());
        assertSame(headers, streamResponseBase.getHeaders());
        assertSame(requestMock, streamResponseBase.getRequest());
    }

    @Test
    public void closeDelegates() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);

        streamResponseBase.close();

        Mockito.verify(response, Mockito.times(1)).close();
    }

    @Test
    public void consumingValueClosesResponse() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);

        streamResponseBase.getValue().then().block();

        Mockito.verify(response, Mockito.times(1)).close();
    }

    @Test
    public void responseIsClosedOneTime() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);

        streamResponseBase.close();
        streamResponseBase.close();

        Mockito.verify(response, Mockito.times(1)).close();
    }

    @Test
    public void closeIsNoopAfterConsumption() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);

        streamResponseBase.getValue().then().block();
        streamResponseBase.close();

        Mockito.verify(response, Mockito.times(1)).close();
    }

    @Test
    public void transferToAsyncChannel() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);
        try {
            Path tempFile = Files.createTempFile("streamresponsetest", null);
            tempFile.toFile().deleteOnExit();

            StepVerifier.create(Mono.using(
                () -> IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
                streamResponseBase::writeValueToAsync,
                channel -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
            ).verifyComplete();

            assertArrayEquals(responseValue, Files.readAllBytes(tempFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void transferToWriteableChannel() {
        StreamResponseBase<Void> streamResponseBase = new StreamResponseBase<>(response, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        streamResponseBase.writeValueTo(Channels.newChannel(bos));

        assertArrayEquals(responseValue, bos.toByteArray());
    }
}
