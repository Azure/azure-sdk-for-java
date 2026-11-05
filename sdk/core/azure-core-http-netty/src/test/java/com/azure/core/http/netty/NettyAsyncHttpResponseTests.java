// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.netty.implementation.NettyAsyncHttpResponse;
import com.azure.core.http.netty.mocking.MockChannel;
import com.azure.core.http.netty.mocking.MockConnection;
import com.azure.core.http.netty.mocking.MockEventLoop;
import com.azure.core.http.netty.mocking.MockHttpClientResponse;
import com.azure.core.http.netty.mocking.MockNettyInbound;
import com.azure.core.util.FluxUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.Attribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.http.client.HttpClientResponse;
import reactor.test.StepVerifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link NettyAsyncHttpResponse}.
 */
public class NettyAsyncHttpResponseTests {
    private static final HttpRequest REQUEST = new HttpRequest(HttpMethod.GET, "https://example.com");
    private static final String HELLO = "hello";
    private static final byte[] HELLO_BYTES = HELLO.getBytes(StandardCharsets.UTF_8);

    @Test
    public void getStatusCode() {
        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(null, HttpResponseStatus.OK);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(reactorNettyResponse, null, REQUEST, false, false);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void getHeaders() {
        HttpHeaders headers = new DefaultHttpHeaders().add("aHeader", "aValue").add("anotherHeader", "anotherValue");

        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(headers, null);

        com.azure.core.http.HttpHeaders actualHeaders
            = new NettyAsyncHttpResponse(reactorNettyResponse, null, REQUEST, false, false).getHeaders();

        assertEquals("aValue", actualHeaders.getValue(HttpHeaderName.fromString("aHeader")));
        assertEquals("anotherValue", actualHeaders.getValue(HttpHeaderName.fromString("anotherHeader")));
    }

    @Test
    public void getBody() {
        ByteBufFlux byteBufFlux
            = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT);

        NettyInbound nettyInbound = new MockNettyInbound(byteBufFlux);
        Connection connection = new MockConnection(nettyInbound, true);
        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(new DefaultHttpHeaders(), null);

        NettyAsyncHttpResponse response
            = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST, false, false);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(response.getBody()))
            .assertNext(actual -> assertArrayEquals(HELLO_BYTES, actual))
            .verifyComplete();
    }

    @Test
    public void getBodyAsByteArray() {
        ByteBufFlux byteBufFlux
            = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT);

        NettyInbound nettyInbound = new MockNettyInbound(byteBufFlux);
        Connection connection = new MockConnection(nettyInbound, true);
        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(new DefaultHttpHeaders(), null);
        NettyAsyncHttpResponse response
            = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST, false, false);

        StepVerifier.create(response.getBodyAsByteArray())
            .assertNext(actual -> assertArrayEquals(HELLO_BYTES, actual))
            .verifyComplete();
    }

    @Test
    public void getBodyAsString() {
        ByteBufFlux byteBufFlux
            = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT);
        HttpHeaders headers = new DefaultHttpHeaders().add("aHeader", "aValue").add("anotherHeader", "anotherValue");

        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(headers, null);
        NettyInbound nettyInbound = new MockNettyInbound(byteBufFlux);
        Connection connection = new MockConnection(nettyInbound, true);
        NettyAsyncHttpResponse response
            = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST, false, false);

        StepVerifier.create(response.getBodyAsString())
            .assertNext(actual -> assertEquals(HELLO, actual))
            .verifyComplete();
    }

    @Test
    public void getBodyAsStringWithCharset() {
        ByteBufFlux byteBufFlux
            = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT);

        NettyInbound nettyInbound = new MockNettyInbound(byteBufFlux);
        Connection connection = new MockConnection(nettyInbound, true);
        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(new DefaultHttpHeaders(), null);
        NettyAsyncHttpResponse response
            = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST, false, false);

        StepVerifier.create(response.getBodyAsString(StandardCharsets.UTF_8))
            .assertNext(actual -> assertEquals(HELLO, actual))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void asynchronousWritesOwnHeapBuffersAndDoNotBlock(boolean disableBufferCopy) throws Exception {
        byte[] expected = new byte[8193];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = (byte) i;
        }
        ByteBuf networkBuffer = ByteBufAllocator.DEFAULT.directBuffer(expected.length).writeBytes(expected);
        ByteBufFlux body
            = ByteBufFlux.fromInbound(Flux.just(networkBuffer).doFinally(ignored -> networkBuffer.release()));
        MockEventLoop eventLoop = new MockEventLoop();
        NettyAsyncHttpResponse response = response(body, disableBufferCopy, eventLoop);
        DeferredWriteChannel channel = new DeferredWriteChannel();
        CompletableFuture<Void> transfer = response.writeBodyToAsync(channel).toFuture();
        try {
            assertFalse(transfer.isDone());
            assertEquals(0, networkBuffer.refCnt());
            assertEquals(0, eventLoop.getExecuteCount());
            while (channel.pending.get() != null) {
                channel.completeWrite();
            }
            transfer.get(5, TimeUnit.SECONDS);
            assertArrayEquals(expected, channel.output.toByteArray());
            assertEquals(1, eventLoop.getExecuteCount());
        } finally {
            transfer.cancel(true);
            if (networkBuffer.refCnt() > 0) {
                networkBuffer.release();
            }
        }
    }

    @Test
    public void asynchronousWriteFailureClosesResponse() {
        ByteBuf networkBuffer = ByteBufAllocator.DEFAULT.directBuffer(3).writeBytes(new byte[] { 1, 2, 3 });
        ByteBufFlux body
            = ByteBufFlux.fromInbound(Flux.just(networkBuffer).doFinally(ignored -> networkBuffer.release()));
        MockEventLoop eventLoop = new MockEventLoop();
        DeferredWriteChannel channel = new DeferredWriteChannel();
        IOException expected = new IOException("Destination failed");

        StepVerifier.create(response(body, true, eventLoop).writeBodyToAsync(channel))
            .then(() -> channel.failWrite(expected))
            .expectErrorMatches(error -> error == expected)
            .verify(Duration.ofSeconds(5));
        assertEquals(0, networkBuffer.refCnt());
        assertEquals(1, eventLoop.getExecuteCount());
    }

    @Test
    public void cancellingAsynchronousWriteStopsBodyAndClosesResponse() {
        AtomicBoolean cancelled = new AtomicBoolean();
        ByteBuf networkBuffer = ByteBufAllocator.DEFAULT.directBuffer(8193).writeZero(8193);
        ByteBufFlux body = ByteBufFlux.fromInbound(Flux.just(networkBuffer)
            .doFinally(ignored -> networkBuffer.release())
            .concatWith(Flux.never())
            .doOnCancel(() -> cancelled.set(true)));
        MockEventLoop eventLoop = new MockEventLoop();
        DeferredWriteChannel channel = new DeferredWriteChannel();
        CompletableFuture<Void> transfer = response(body, true, eventLoop).writeBodyToAsync(channel).toFuture();

        assertFalse(transfer.isDone());
        assertTrue(transfer.cancel(true));
        assertTrue(cancelled.get());
        assertEquals(0, networkBuffer.refCnt());
        assertEquals(1, eventLoop.getExecuteCount());
        channel.completeWrite();
        assertNull(channel.pending.get());
        assertTrue(channel.isOpen());
    }

    private static NettyAsyncHttpResponse response(ByteBufFlux body, boolean disableBufferCopy,
        MockEventLoop eventLoop) {
        Connection connection
            = new MockConnection(new MockNettyInbound(body), new MockChannel((Attribute<String>) null) {
                @Override
                public EventLoop eventLoop() {
                    return eventLoop;
                }
            });
        return new NettyAsyncHttpResponse(new MockHttpClientResponse(new DefaultHttpHeaders(), null), connection,
            REQUEST, disableBufferCopy, false);
    }

    private static final class DeferredWriteChannel implements AsynchronousByteChannel {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final AtomicReference<Runnable> pending = new AtomicReference<>();
        private Consumer<Throwable> fail;
        private boolean open = true;

        @Override
        public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
            assertFalse(src.isDirect(), "Asynchronous file channels must not receive scoped direct buffers");
            assertNull(pending.get());
            pending.set(() -> {
                int count = Math.min(4096, src.remaining());
                for (int i = 0; i < count; i++) {
                    output.write(src.get());
                }
                handler.completed(count, attachment);
            });
            fail = error -> handler.failed(error, attachment);
        }

        @Override
        public Future<Integer> write(ByteBuffer src) {
            throw new AssertionError("The response must not wait on a Future");
        }

        @Override
        public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<Integer> read(ByteBuffer dst) {
            throw new UnsupportedOperationException();
        }

        private void completeWrite() {
            Runnable completion = pending.getAndSet(null);
            assertNotNull(completion);
            completion.run();
        }

        private void failWrite(Throwable error) {
            assertNotNull(pending.getAndSet(null));
            fail.accept(error);
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

    @ParameterizedTest
    @MethodSource("verifyDisposalSupplier")
    public void verifyDisposal(String methodName, Class<?>[] argumentTypes, Object[] argumentValues)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = NettyAsyncHttpResponse.class.getMethod(methodName, argumentTypes);
        ByteBufFlux byteBufFlux
            = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT);
        HttpHeaders headers = new DefaultHttpHeaders().add("aHeader", "aValue").add("anotherHeader", "anotherValue");

        HttpClientResponse reactorNettyResponse = new MockHttpClientResponse(headers, null);
        NettyInbound nettyInbound = new MockNettyInbound(byteBufFlux);

        MockEventLoop eventLoop = new MockEventLoop();
        Connection connection = new MockConnection(nettyInbound, new MockChannel((Attribute<String>) null) {
            @Override
            public EventLoop eventLoop() {
                return eventLoop;
            }
        });

        NettyAsyncHttpResponse response
            = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST, false, false);

        Object object = method.invoke(response, argumentValues);
        if (object instanceof Mono) {
            ((Mono<?>) object).block();
        } else if (object instanceof Flux) {
            ((Flux<?>) object).blockLast();
        }

        assertEquals(1, eventLoop.getExecuteCount());
    }

    private static Stream<Arguments> verifyDisposalSupplier() {
        return Stream.of(Arguments.of("getBody", null, null), Arguments.of("getBodyAsByteArray", null, null),
            Arguments.of("getBodyAsString", null, null),
            Arguments.of("getBodyAsString", new Class<?>[] { Charset.class }, new Object[] { StandardCharsets.UTF_8 }),
            Arguments.of("close", null, null));
    }
}
