// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.netty.implementation.NettyAsyncHttpResponse;
import com.azure.core.util.FluxUtil;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.http.client.HttpClientResponse;
import reactor.test.StepVerifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link NettyAsyncHttpResponse}.
 */
public class NettyAsyncHttpResponseTests {
    private static final HttpRequest REQUEST = new HttpRequest(HttpMethod.GET, "https://example.com");
    private static final String HELLO = "hello";
    private static final byte[] HELLO_BYTES = HELLO.getBytes(StandardCharsets.UTF_8);

    @Test
    public void getStatusCode() {
        HttpClientResponse reactorNettyResponse = mock(HttpClientResponse.class);
        when(reactorNettyResponse.status()).thenReturn(HttpResponseStatus.OK);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(reactorNettyResponse, null, REQUEST, false);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void getHeaders() {
        HttpHeaders headers = new DefaultHttpHeaders()
            .add("aHeader", "aValue")
            .add("anotherHeader", "anotherValue");

        HttpClientResponse reactorNettyResponse = mock(HttpClientResponse.class);
        when(reactorNettyResponse.responseHeaders()).thenReturn(headers);

        com.azure.core.http.HttpHeaders actualHeaders = new NettyAsyncHttpResponse(
            reactorNettyResponse, null, REQUEST, false)
            .getHeaders();

        assertEquals("aValue", actualHeaders.getValue("aHeader"));
        assertEquals("anotherValue", actualHeaders.getValue("anotherHeader"));
    }

    @Test
    public void getBody() {
        ByteBufFlux byteBufFlux = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8,
            ByteBufAllocator.DEFAULT);

        NettyInbound nettyInbound = mock(NettyInbound.class);
        when(nettyInbound.receive()).thenReturn(byteBufFlux);

        Connection connection = mock(Connection.class);
        when(connection.inbound()).thenReturn(nettyInbound);
        when(connection.isDisposed()).thenReturn(true);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(null, connection, REQUEST, false);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(response.getBody()))
            .assertNext(actual -> assertArrayEquals(HELLO_BYTES, actual))
            .verifyComplete();
    }

    @Test
    public void getBodyAsByteArray() {
        ByteBufFlux byteBufFlux = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8,
            ByteBufAllocator.DEFAULT);

        NettyInbound nettyInbound = mock(NettyInbound.class);
        when(nettyInbound.receive()).thenReturn(byteBufFlux);

        Connection connection = mock(Connection.class);
        when(connection.inbound()).thenReturn(nettyInbound);
        when(connection.isDisposed()).thenReturn(true);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(null, connection, REQUEST, false);

        StepVerifier.create(response.getBodyAsByteArray())
            .assertNext(actual -> assertArrayEquals(HELLO_BYTES, actual))
            .verifyComplete();
    }

    @Test
    public void getBodyAsString() {
        ByteBufFlux byteBufFlux = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8,
            ByteBufAllocator.DEFAULT);
        HttpHeaders headers = new DefaultHttpHeaders()
            .add("aHeader", "aValue")
            .add("anotherHeader", "anotherValue");

        HttpClientResponse reactorNettyResponse = mock(HttpClientResponse.class);
        when(reactorNettyResponse.responseHeaders()).thenReturn(headers);

        NettyInbound nettyInbound = mock(NettyInbound.class);
        when(nettyInbound.receive()).thenReturn(byteBufFlux);

        Connection connection = mock(Connection.class);
        when(connection.inbound()).thenReturn(nettyInbound);
        when(connection.isDisposed()).thenReturn(true);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST,
            false);

        StepVerifier.create(response.getBodyAsString())
            .assertNext(actual -> assertEquals(HELLO, actual))
            .verifyComplete();
    }

    @Test
    public void getBodyAsStringWithCharset() {
        ByteBufFlux byteBufFlux = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8,
            ByteBufAllocator.DEFAULT);

        NettyInbound nettyInbound = mock(NettyInbound.class);
        when(nettyInbound.receive()).thenReturn(byteBufFlux);

        Connection connection = mock(Connection.class);
        when(connection.inbound()).thenReturn(nettyInbound);
        when(connection.isDisposed()).thenReturn(true);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(null, connection, REQUEST, false);

        StepVerifier.create(response.getBodyAsString(StandardCharsets.UTF_8))
            .assertNext(actual -> assertEquals(HELLO, actual))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("verifyDisposalSupplier")
    public void verifyDisposal(String methodName, Class<?>[] argumentTypes, Object[] argumentValues)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = NettyAsyncHttpResponse.class.getMethod(methodName, argumentTypes);
        ByteBufFlux byteBufFlux = ByteBufFlux.fromString(Mono.just("hello"), StandardCharsets.UTF_8,
            ByteBufAllocator.DEFAULT);
        HttpHeaders headers = new DefaultHttpHeaders()
            .add("aHeader", "aValue")
            .add("anotherHeader", "anotherValue");

        HttpClientResponse reactorNettyResponse = mock(HttpClientResponse.class);
        when(reactorNettyResponse.responseHeaders()).thenReturn(headers);

        NettyInbound nettyInbound = mock(NettyInbound.class);
        when(nettyInbound.receive()).thenReturn(byteBufFlux);

        EventLoop eventLoop = mock(EventLoop.class);
        doNothing().when(eventLoop).execute(any());

        Channel channel = mock(Channel.class);
        when(channel.eventLoop()).thenReturn(eventLoop);

        Connection connection = mock(Connection.class);
        when(connection.inbound()).thenReturn(nettyInbound);
        when(connection.channel()).thenReturn(channel);

        NettyAsyncHttpResponse response = new NettyAsyncHttpResponse(reactorNettyResponse, connection, REQUEST,
            false);

        Object object = method.invoke(response, argumentValues);
        if (object instanceof Mono) {
            ((Mono<?>) object).block();
        } else if (object instanceof Flux) {
            ((Flux<?>) object).blockLast();
        }

        verify(eventLoop, times(1)).execute(any());
    }

    private static Stream<Arguments> verifyDisposalSupplier() {
        return Stream.of(
            Arguments.of("getBody", null, null),
            Arguments.of("getBodyAsByteArray", null, null),
            Arguments.of("getBodyAsString", null, null),
            Arguments.of("getBodyAsString", new Class<?>[]{Charset.class}, new Object[]{StandardCharsets.UTF_8}),
            Arguments.of("close", null, null)
        );
    }
}
