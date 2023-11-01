// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.models.BinaryData;
import com.generic.core.models.Header;
import com.generic.core.models.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.generic.core.CoreTestUtils.assertArraysEqual;
import static com.generic.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class HttpRequestTests {
    private static final String BODY = "this is a sample body";
    private static final byte[] BODY_BYTES = BODY.getBytes(StandardCharsets.UTF_8);
    private static final long BODY_LENGTH = BODY_BYTES.length;

    @Test
    public void constructor() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));

        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());
        assertNull(request.getBody());
    }

    @Test
    public void constructorWithHeaders() throws MalformedURLException {
        final Headers Headers = new Headers();
        final HttpRequest request = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"), Headers);

        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());
        assertSame(Headers, request.getHeaders());
        assertNull(request.getBody());
    }

    @ParameterizedTest(name = "[{index}] {displayName}") // BinaryData.toString would trigger buffering.
    @MethodSource("getBinaryDataBodyVariants")
    public void constructorWithBinaryDataBody(BinaryData data, Long expectedContentLength)
        throws MalformedURLException {

        final HttpRequest request = new HttpRequest(
            HttpMethod.POST, createUrl("http://request.url"), new Headers(), data);

        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(createUrl("http://request.url"), request.getUrl());

        assertSame(data, request.getBody());
        assertEquals(expectedContentLength, getContentLength(request));

        if (data != null) {
            assertArraysEqual(BODY_BYTES, request.getBody().toBytes());
        } else {
            assertNull(request.getBody());
        }
    }

    @ParameterizedTest(name = "[{index}] {displayName}") // BinaryData.toString would trigger buffering.
    @MethodSource("getBinaryDataBodyVariants")
    public void testSetBodyAsBinaryData(BinaryData data, Long expectedContentLength) {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(data);

        assertSame(data, request.getBody());
        assertEquals(expectedContentLength, getContentLength(request));

        if (data != null) {
            assertArraysEqual(BODY_BYTES, request.getBody().toBytes());
        } else {
            assertNull(request.getBody());
        }
    }

    @Test
    public void testSetBodyAsString() {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(BODY);

        assertEquals(BODY_LENGTH, getContentLength(request));
        assertEquals(BODY, request.getBody().toString());
        assertArraysEqual(BODY_BYTES, request.getBody().toBytes());
    }

    @Test
    public void testSetBodyAsByteArray() {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "http://request.url");

        request.setBody(BODY_BYTES);

        assertEquals(BODY_LENGTH, getContentLength(request));
        assertEquals(BODY, request.getBody().toString());
        assertArraysEqual(BODY_BYTES, request.getBody().toBytes());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testClone() throws IOException {
        final Headers headers = new Headers()
            .set(HttpHeaderName.fromString("my-header"), "my-value")
            .set(HttpHeaderName.fromString("other-header"), "other-value");

        final HttpRequest request = new HttpRequest(HttpMethod.PUT, createUrl("http://request.url"), headers,
            null);

        final HttpRequest bufferedRequest = request.copy();

        assertNotSame(request, bufferedRequest);
        assertEquals(request.getHttpMethod(), bufferedRequest.getHttpMethod());
        assertEquals(request.getUrl(), bufferedRequest.getUrl());
        assertNotSame(request.getHeaders(), bufferedRequest.getHeaders());
        assertEquals(request.getHeaders().getSize(), bufferedRequest.getHeaders().getSize());

        for (Header clonedHeader : bufferedRequest.getHeaders()) {
            for (Header originalHeader : request.getHeaders()) {
                assertNotSame(clonedHeader, originalHeader);
            }

            assertEquals(clonedHeader.getValue(),
                request.getHeaders().getValue(HttpHeaderName.fromString(clonedHeader.getName())));
        }

        assertSame(request.getBody(), bufferedRequest.getBody());
    }

    private static Stream<Arguments> getBinaryDataBodyVariants() {
        return Stream.of(
            // body, expectedContentLength
            Arguments.of(null, null),
            Arguments.of(BinaryData.fromString(BODY), BODY_LENGTH),
            Arguments.of(BinaryData.fromBytes(BODY_BYTES), BODY_LENGTH),
            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(BODY_BYTES)), null),
            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(BODY_BYTES), BODY_LENGTH), BODY_LENGTH)
        );
    }

    private Long getContentLength(HttpRequest request) {
        String contentLengthValue = request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
        return contentLengthValue == null ? null : Long.parseLong(contentLengthValue);
    }
}
