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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static Flux<ByteBuffer> fluxOfChunks(byte[]... chunks) {
        ByteBuffer[] buffers = new ByteBuffer[chunks.length];
        for (int i = 0; i < chunks.length; i++) {
            buffers[i] = ByteBuffer.wrap(chunks[i]);
        }
        return Flux.fromArray(buffers);
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
    public void getHeaderValueByHttpHeaderNameUsesInheritedDefault() {
        HttpHeaders h = headers(CUSTOM_HEADER, "value");
        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, h, new byte[0]), fluxOf(new byte[0]));

        assertEquals("value", wrapper.getHeaderValue(CUSTOM_HEADER));
        assertNull(wrapper.getHeaderValue(HttpHeaderName.fromString("nonexistent")));
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
    public void getBodyAsByteArrayConcatenatesMultipleChunks() {
        byte[] chunkA = bytes("hello ");
        byte[] chunkB = bytes("world");
        byte[] expected = bytes("hello world");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]), fluxOfChunks(chunkA, chunkB));

        StepVerifier.create(wrapper.getBodyAsByteArray())
            .expectNextMatches(b -> Arrays.equals(expected, b))
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
    }

    @Test
    public void getBodyAsBinaryDataWorksWhenContentLengthHeaderMissing() {
        byte[] decoded = bytes("payload");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), bytes("ignored")), fluxOf(decoded));

        BinaryData data = wrapper.getBodyAsBinaryData();
        assertArrayEquals(decoded, data.toBytes());
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
    public void inheritedWriteBodyToWritesDecodedBytesAndClosesOriginal() throws IOException {
        byte[] decoded = bytes("write me");
        AtomicInteger closeCount = new AtomicInteger();
        HttpResponse original = trackingResponse(closeCount, new HttpHeaders(), bytes("encoded"));
        DecodedResponse wrapper = new DecodedResponse(original, fluxOf(decoded));

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        try (WritableByteChannel channel = Channels.newChannel(sink)) {
            wrapper.writeBodyTo(channel);
        }

        assertArrayEquals(decoded, sink.toByteArray());
        assertEquals(1, closeCount.get());
    }

    @Test
    public void inheritedWriteBodyToAsyncWritesDecodedBytes() {
        byte[] decoded = bytes("async write");
        DecodedResponse wrapper
            = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]), fluxOf(decoded));

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        ByteArrayAsynchronousChannel asyncChannel = new ByteArrayAsynchronousChannel(sink);

        StepVerifier.create(wrapper.writeBodyToAsync(asyncChannel)).verifyComplete();
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

    @Test
    public void closeDelegatesToWrappedResponse() {
        AtomicInteger closeCount = new AtomicInteger();
        HttpResponse original = trackingResponse(closeCount, new HttpHeaders(), new byte[0]);
        DecodedResponse wrapper = new DecodedResponse(original, fluxOf(new byte[0]));

        wrapper.close();
        assertEquals(1, closeCount.get());
        wrapper.close();
        assertEquals(2, closeCount.get());
    }

    @Test
    public void closeDoesNotSubscribeToDecodedBody() {
        AtomicInteger subscribeCount = new AtomicInteger();
        Flux<ByteBuffer> decoded = fluxOf(bytes("decoded")).doOnSubscribe(s -> subscribeCount.incrementAndGet());

        DecodedResponse wrapper = new DecodedResponse(mockResponse(200, new HttpHeaders(), new byte[0]), decoded);
        wrapper.close();

        assertEquals(0, subscribeCount.get());
    }

    private static HttpResponse trackingResponse(AtomicInteger closeCount, HttpHeaders headers, byte[] body) {
        MockHttpResponse delegate = new MockHttpResponse(newRequest(), 200, headers, body);
        return new HttpResponse(delegate.getRequest()) {
            @Override
            public int getStatusCode() {
                return delegate.getStatusCode();
            }

            @Override
            @SuppressWarnings("deprecation")
            public String getHeaderValue(String name) {
                return delegate.getHeaderValue(name);
            }

            @Override
            public HttpHeaders getHeaders() {
                return delegate.getHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return delegate.getBody();
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return delegate.getBodyAsByteArray();
            }

            @Override
            public Mono<String> getBodyAsString() {
                return delegate.getBodyAsString();
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return delegate.getBodyAsString(charset);
            }

            @Override
            public void close() {
                closeCount.incrementAndGet();
                delegate.close();
            }
        };
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

    private static final class ByteArrayAsynchronousChannel implements java.nio.channels.AsynchronousByteChannel {
        private final ByteArrayOutputStream sink;
        private volatile boolean open = true;

        ByteArrayAsynchronousChannel(ByteArrayOutputStream sink) {
            this.sink = sink;
        }

        @Override
        public <A> void read(ByteBuffer dst, A attachment,
            java.nio.channels.CompletionHandler<Integer, ? super A> handler) {
            handler.failed(new UnsupportedOperationException("read not supported"), attachment);
        }

        @Override
        public java.util.concurrent.Future<Integer> read(ByteBuffer dst) {
            java.util.concurrent.CompletableFuture<Integer> future = new java.util.concurrent.CompletableFuture<>();
            future.completeExceptionally(new UnsupportedOperationException("read not supported"));
            return future;
        }

        @Override
        public <A> void write(ByteBuffer src, A attachment,
            java.nio.channels.CompletionHandler<Integer, ? super A> handler) {
            int written = src.remaining();
            byte[] data = new byte[written];
            src.get(data);
            sink.write(data, 0, data.length);
            handler.completed(written, attachment);
        }

        @Override
        public java.util.concurrent.Future<Integer> write(ByteBuffer src) {
            int written = src.remaining();
            byte[] data = new byte[written];
            src.get(data);
            sink.write(data, 0, data.length);
            return java.util.concurrent.CompletableFuture.completedFuture(written);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }
    }
}
