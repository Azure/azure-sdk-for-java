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

        DecodedResponse wrapper = new DecodedResponse(original, fluxOf(bytes("decoded")), 80L);

        assertSame(original.getRequest(), wrapper.getRequest());
        assertEquals(206, wrapper.getStatusCode());
        // Content-Length is overridden to decoded size; other headers are preserved.
        assertEquals("80", wrapper.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
        assertEquals("value", wrapper.getHeaders().getValue(CUSTOM_HEADER));
    }

    @Test
    public void getHeaderValueByStringReturnsHeaderValue() {
        HttpHeaders h = headers(CUSTOM_HEADER, "value");
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, h, new byte[0]), fluxOf(new byte[0]), 0L);

        assertEquals("value", wrapper.getHeaderValue(CUSTOM_HEADER.getCaseInsensitiveName()));
        assertNull(wrapper.getHeaderValue("nonexistent"));
    }

    @Test
    public void getBodyReturnsDecodedFlux() {
        byte[] decoded = bytes("decoded body");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded), 0L);

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
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded), 0L);

        StepVerifier.create(wrapper.getBodyAsByteArray())
            .expectNextMatches(b -> Arrays.equals(decoded, b))
            .verifyComplete();
    }

    @Test
    public void getBodyAsStringDefaultsToUtf8WhenNoCharsetSpecified() {
        // The no-arg overload routes through CoreUtils.bomAwareToString, which falls back to UTF-8 when neither a
        // BOM nor a Content-Type charset parameter is present. This test pins the "no headers, no BOM" path.
        String text = "héllo wörld – ✓";
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]),
            fluxOf(text.getBytes(StandardCharsets.UTF_8)), 0L);

        StepVerifier.create(wrapper.getBodyAsString()).expectNext(text).verifyComplete();
    }

    @Test
    public void getBodyAsStringHonorsCharsetFromContentTypeHeader() {
        // Per the base HttpResponse contract, the no-arg getBodyAsString() must honor a charset declared in the
        // response's Content-Type header. Without the bom-aware decoding the bytes would be (mis)interpreted as
        // UTF-8 and the assertion below would fail.
        String text = "ümlaut";
        byte[] iso = text.getBytes(StandardCharsets.ISO_8859_1);
        HttpHeaders h = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain; charset=ISO-8859-1");
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, h, new byte[0]), fluxOf(iso), 0L);

        StepVerifier.create(wrapper.getBodyAsString()).expectNext(text).verifyComplete();
    }

    @Test
    public void getBodyAsStringDetectsUtf8BomAndStripsIt() {
        // A leading UTF-8 BOM (EF BB BF) must be detected and stripped from the decoded string, matching the base
        // HttpResponse contract that CoreUtils.bomAwareToString implements.
        String text = "with bom";
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] payload = text.getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[bom.length + payload.length];
        System.arraycopy(bom, 0, withBom, 0, bom.length);
        System.arraycopy(payload, 0, withBom, bom.length, payload.length);
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]), fluxOf(withBom), 0L);

        StepVerifier.create(wrapper.getBodyAsString()).expectNext(text).verifyComplete();
    }

    @Test
    public void getBodyAsStringDecodesUsingProvidedCharset() {
        String text = "ümlaut";
        byte[] latin1 = text.getBytes(StandardCharsets.ISO_8859_1);
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]), fluxOf(latin1), 0L);

        StepVerifier.create(wrapper.getBodyAsString(StandardCharsets.ISO_8859_1)).expectNext(text).verifyComplete();
    }

    @Test
    public void inheritedGetBodyAsInputStreamUsesDecodedBytes() throws IOException {
        // Base getBodyAsInputStream() routes through getBodyAsByteArray(), so the override is exercised end-to-end.
        byte[] decoded = bytes("decoded stream");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded), 0L);

        try (InputStream stream = wrapper.getBodyAsInputStream().block()) {
            assertNotNull(stream);
            assertArrayEquals(decoded, readAll(stream));
        }
    }

    @Test
    public void inheritedWriteBodyToWritesDecodedBytes() throws IOException {
        byte[] decoded = bytes("write me");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded), 0L);

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        try (WritableByteChannel channel = Channels.newChannel(sink)) {
            wrapper.writeBodyTo(channel);
        }

        assertArrayEquals(decoded, sink.toByteArray());
    }

    @Test
    public void inheritedBufferReturnsResponseBackedByDecodedBytes() {
        byte[] decoded = bytes("buffered");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("encoded")), fluxOf(decoded), 0L);

        HttpResponse buffered = wrapper.buffer();
        assertNotNull(buffered);
        StepVerifier.create(buffered.getBodyAsByteArray())
            .expectNextMatches(b -> Arrays.equals(decoded, b))
            .verifyComplete();
    }

    @Test
    public void inheritedGetBodyAsBinaryDataReturnsDecodedBytes() {
        // Base HttpResponse#getBodyAsBinaryData() pulls from getBody() (our override), so the resulting BinaryData
        // must contain the decoded payload, not the original wire body. A divergent Content-Length header is set
        // to make the wire vs decoded distinction explicit and guard against regressions in header forwarding.
        byte[] decoded = bytes("decoded payload");
        long decodedSize = decoded.length;
        HttpHeaders h = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(decodedSize + 32));
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, h, bytes("encoded wire body")), fluxOf(decoded), decodedSize);

        BinaryData data = wrapper.getBodyAsBinaryData();
        assertNotNull(data);
        assertArrayEquals(decoded, data.toBytes());
    }

    @Test
    public void contentLengthIsOverriddenToDecodedSize() {
        long wireSize = 500L;
        long decodedSize = 300L;
        HttpHeaders h = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(wireSize))
            .set(CUSTOM_HEADER, "preserve-me");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, h, new byte[0]), fluxOf(new byte[0]), decodedSize);

        assertEquals(String.valueOf(decodedSize), wrapper.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
        assertEquals("preserve-me", wrapper.getHeaders().getValue(CUSTOM_HEADER));
        // Deprecated getHeaderValue must reflect the same override.
        assertEquals(String.valueOf(decodedSize),
            wrapper.getHeaderValue(HttpHeaderName.CONTENT_LENGTH.getCaseInsensitiveName()));
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
