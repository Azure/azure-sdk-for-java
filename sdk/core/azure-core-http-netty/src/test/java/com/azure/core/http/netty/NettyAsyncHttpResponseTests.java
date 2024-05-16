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
