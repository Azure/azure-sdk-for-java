// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.http;

import com.azure.core.v2.util.BinaryData;
import com.azure.core.v2.util.FluxUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class HttpRequestTests {

    private static final String BODY = "this is a sample body";
    private static final Flux<ByteBuffer> BODY_FLUX = Flux.defer(
        () -> Flux.fromStream(Stream.of(BODY.split("")).map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))));
    private static final byte[] BODY_BYTES = BODY.getBytes(StandardCharsets.UTF_8);
    private static final long BODY_LENGTH = BODY_BYTES.length;

    @Test
    public void constructor() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());
        assertNull(request.getBody());
        assertNull(request.getBodyAsBinaryData());
    }

    @Test
    public void constructorWithHeaders() throws MalformedURLException {
        final HttpHeaders httpHeaders = new HttpHeaders();
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"), httpHeaders);
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());
        assertSame(httpHeaders, request.getHeaders());
        assertNull(request.getBody());
        assertNull(request.getBodyAsBinaryData());
    }

    @Test
    public void constructorWithFluxBody() throws MalformedURLException {
        final HttpHeaders httpHeaders = new HttpHeaders();
        final HttpRequest request
            = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"), httpHeaders, BODY_FLUX);
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());

        assertSame(httpHeaders, request.getHeaders());
        assertSame(BODY_FLUX, request.getBody());
        assertNull(getContentLength(request));
        assertEquals(BODY, request.getBodyAsBinaryData().toString());
    }

    @ParameterizedTest(name = "[{index}] {displayName}") // BinaryData.toString would trigger buffering.
    @MethodSource("getBinaryDataBodyVariants")
    public void constructorWithBinaryDataBody(BinaryData data, Long expectedContentLength)
        throws MalformedURLException {

        final HttpRequest request
            = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"), new HttpHeaders(), data);

        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());

        assertSame(data, request.getBodyAsBinaryData());
        assertEquals(expectedContentLength, getContentLength(request));
        if (data != null) {
            assertArraysEqual(BODY_BYTES, FluxUtil.collectBytesInByteBufferStream(request.getBody()).block());
        } else {
            assertNull(request.getBody());
        }
    }

    @ParameterizedTest(name = "[{index}] {displayName}") // BinaryData.toString would trigger buffering.
    @MethodSource("getBinaryDataBodyVariants")
    public void testSetBodyAsBinaryData(BinaryData data, Long expectedContentLength) {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(data);

        assertSame(data, request.getBodyAsBinaryData());
        assertEquals(expectedContentLength, getContentLength(request));
        if (data != null) {
            assertArraysEqual(BODY_BYTES, FluxUtil.collectBytesInByteBufferStream(request.getBody()).block());
        } else {
            assertNull(request.getBody());
        }
    }

    @Test
    public void testSetBodyAsString() {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(BODY);

        assertEquals(BODY_LENGTH, getContentLength(request));
        assertEquals(BODY, request.getBodyAsBinaryData().toString());
        assertArraysEqual(BODY_BYTES, FluxUtil.collectBytesInByteBufferStream(request.getBody()).block());
    }

    @Test
    public void testSetBodyAsByteArray() {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(BODY_BYTES);

        assertEquals(BODY_LENGTH, getContentLength(request));
        assertEquals(BODY, request.getBodyAsBinaryData().toString());
        assertArraysEqual(BODY_BYTES, FluxUtil.collectBytesInByteBufferStream(request.getBody()).block());
    }

    @Test
    public void testSetBodyAsFlux() {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(BODY_FLUX);

        assertNull(getContentLength(request));
        assertSame(BODY_FLUX, request.getBody());
        assertNull(getContentLength(request));
        assertEquals(BODY, request.getBodyAsBinaryData().toString());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testClone() throws IOException {
        final HttpHeaders headers = new HttpHeaders().set("my-header", "my-value").set("other-header", "other-value");

        final HttpRequest request
            = new HttpRequest(HttpMethod.PUT, createUrl("http://request.url"), headers, Flux.empty());

        final HttpRequest bufferedRequest = request.copy();

        assertNotSame(request, bufferedRequest);

        assertEquals(request.getHttpMethod(), bufferedRequest.getHttpMethod());
        assertEquals(request.getUrl(), bufferedRequest.getUrl());

        assertNotSame(request.getHeaders(), bufferedRequest.getHeaders());
        assertEquals(request.getHeaders().getSize(), bufferedRequest.getHeaders().getSize());
        for (HttpHeader clonedHeader : bufferedRequest.getHeaders()) {
            for (HttpHeader originalHeader : request.getHeaders()) {
                assertNotSame(clonedHeader, originalHeader);
            }

            assertEquals(clonedHeader.getValue(), request.getHeaders().getValue(clonedHeader.getName()));
        }

        assertSame(request.getBody(), bufferedRequest.getBody());
        assertSame(request.getBodyAsBinaryData(), request.getBodyAsBinaryData());
    }

    private static Stream<Arguments> getBinaryDataBodyVariants() {
        return Stream.of(
            // body, expectedContentLength
            Arguments.of(null, null), Arguments.of(BinaryData.fromString(BODY), BODY_LENGTH),
            Arguments.of(BinaryData.fromBytes(BODY_BYTES), BODY_LENGTH),
            Arguments.of(BinaryData.fromFlux(BODY_FLUX, null, false).block(), null),
            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(BODY_BYTES)), null),
            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(BODY_BYTES), BODY_LENGTH), BODY_LENGTH));
    }

    private Long getContentLength(HttpRequest request) {
        String contentLengthValue = request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
        return contentLengthValue == null ? null : Long.parseLong(contentLengthValue);
    }
}
