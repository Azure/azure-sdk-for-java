// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DecodedResponseTests {

    private static final HttpHeaderName CUSTOM_HEADER = HttpHeaderName.fromString("x-ms-custom");

    private static HttpRequest newRequest() {
        try {
            return new HttpRequest(HttpMethod.GET, new URL("http://example.com/"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpHeaders headers(HttpHeaderName name, String value) {
        return new HttpHeaders().set(name, value);
    }

    private static MockHttpResponse mockResponse(int status, HttpHeaders headers, byte[] body) {
        return new MockHttpResponse(newRequest(), status, headers, body);
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static Flux<ByteBuffer> fluxOf(byte[] data) {
        return Flux.just(ByteBuffer.wrap(data));
    }

    @Test
    public void preservesRequestStatusCodeAndHeaders() {
        HttpHeaders h = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "100").set(CUSTOM_HEADER, "value");
        MockHttpResponse original = mockResponse(206, h, bytes("encoded"));

        DecodedResponse wrapper = new DecodedResponse(original, fluxOf(bytes("decoded")));

        assertSame(original.getRequest(), wrapper.getRequest());
        assertEquals(206, wrapper.getStatusCode());
        assertSame(h, wrapper.getHeaders());
    }

    @Test
    public void getHeaderValueByStringReturnsHeaderValue() {
        HttpHeaders h = headers(CUSTOM_HEADER, "value");
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, h, new byte[0]), fluxOf(new byte[0]));

        assertEquals("value", wrapper.getHeaderValue(CUSTOM_HEADER.getCaseInsensitiveName()));
        assertNull(wrapper.getHeaderValue("nonexistent"));
    }

    @Test
    public void getBodyReturnsDecodedFlux() {
        byte[] decoded = bytes("decoded body");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded));

        StepVerifier.create(wrapper.getBody().reduce(new ByteArrayOutputStream(), (sink, buf) -> {
            byte[] copy = new byte[buf.remaining()];
            buf.get(copy);
            sink.write(copy, 0, copy.length);
            return sink;
        })).expectNextMatches(sink -> Arrays.equals(decoded, sink.toByteArray())).verifyComplete();
    }

    @Test
    public void getBodyAsByteArrayReturnsDecodedBytes() {
        byte[] decoded = bytes("decoded body");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded));

        StepVerifier.create(wrapper.getBodyAsByteArray())
            .expectNextMatches(b -> Arrays.equals(decoded, b))
            .verifyComplete();
    }

    @Test
    public void getBodyAsStringDecodesDecodedBytesAsUtf8() {
        // The no-arg overload pins the charset to UTF-8 (storage convention) regardless of platform default. The
        // override does no Content-Type / BOM auto-detection; it always treats the decoded bytes as UTF-8.
        String text = "héllo wörld – ✓";
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]),
            fluxOf(text.getBytes(StandardCharsets.UTF_8)));

        StepVerifier.create(wrapper.getBodyAsString()).expectNext(text).verifyComplete();
    }

    @Test
    public void getBodyAsStringWithCharsetUsesProvidedCharset() {
        String text = "ümlaut";
        byte[] latin1 = text.getBytes(StandardCharsets.ISO_8859_1);
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]), fluxOf(latin1));

        StepVerifier.create(wrapper.getBodyAsString(StandardCharsets.ISO_8859_1)).expectNext(text).verifyComplete();
    }

    @Test
    public void getBodyAsBinaryDataReportsDecodedSizeNotContentLength() {
        byte[] decoded = bytes("decoded");
        HttpHeaders h = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(decoded.length + 64));
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, h, bytes("ignored")), fluxOf(decoded));

        BinaryData data = wrapper.getBodyAsBinaryData();
        assertNotNull(data);
        assertArrayEquals(decoded, data.toBytes());
        assertEquals((long) decoded.length, (long) data.getLength());
    }

    @Test
    public void inheritedGetBodyAsInputStreamUsesDecodedBytes() throws IOException {
        // Base getBodyAsInputStream() routes through getBodyAsByteArray(), so the override is exercised end-to-end.
        byte[] decoded = bytes("decoded stream");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded));

        try (InputStream stream = wrapper.getBodyAsInputStream().block()) {
            assertNotNull(stream);
            assertArrayEquals(decoded, readAll(stream));
        }
    }

    @Test
    public void inheritedWriteBodyToWritesDecodedBytes() throws IOException {
        byte[] decoded = bytes("write me");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded));

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        try (WritableByteChannel channel = Channels.newChannel(sink)) {
            wrapper.writeBodyTo(channel);
        }

        assertArrayEquals(decoded, sink.toByteArray());
    }

    @Test
    public void inheritedBufferReturnsResponseBackedByDecodedBody() {
        byte[] decoded = bytes("buffered");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded));

        HttpResponse buffered = wrapper.buffer();
        assertNotNull(buffered);
        StepVerifier.create(buffered.getBodyAsByteArray())
            .expectNextMatches(b -> Arrays.equals(decoded, b))
            .verifyComplete();
    }

    private static byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = stream.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}
