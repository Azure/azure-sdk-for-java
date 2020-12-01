// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * This class provides a Netty-based implementation for the {@link HttpClient} interface. Creating an instance of this
 * class can be achieved by using the {@link NettyAsyncHttpClientBuilder} class, which offers Netty-specific API for
 * features such as {@link NettyAsyncHttpClientBuilder#eventLoopGroup(EventLoopGroup) thread pooling}, {@link
 * NettyAsyncHttpClientBuilder#wiretap(boolean) wiretapping}, {@link NettyAsyncHttpClientBuilder#proxy(ProxyOptions)
 * setProxy configuration}, and much more.
 *
 * @see HttpClient
 * @see NettyAsyncHttpClientBuilder
 */
class NettyAsyncHttpClient implements HttpClient {
    private final boolean disableBufferCopy;

    final reactor.netty.http.client.HttpClient nettyClient;

    /**
     * Creates default NettyAsyncHttpClient.
     */
    NettyAsyncHttpClient() {
        this(reactor.netty.http.client.HttpClient.create(), false);
    }

    /**
     * Creates NettyAsyncHttpClient with provided http client.
     *
     * @param nettyClient the reactor-netty http client
     * @param disableBufferCopy Determines whether deep cloning of response buffers should be disabled.
     */
    NettyAsyncHttpClient(reactor.netty.http.client.HttpClient nettyClient, boolean disableBufferCopy) {
        this.nettyClient = nettyClient;
        this.disableBufferCopy = disableBufferCopy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        Objects.requireNonNull(request.getHttpMethod(), "'request.getHttpMethod()' cannot be null.");
        Objects.requireNonNull(request.getUrl(), "'request.getUrl()' cannot be null.");
        Objects.requireNonNull(request.getUrl().getProtocol(), "'request.getUrl().getProtocol()' cannot be null.");

        boolean eagerlyReadResponse = (boolean) context.getData("azure-eagerly-read-response").orElse(false);

        return nettyClient
            .request(HttpMethod.valueOf(request.getHttpMethod().toString()))
            .uri(request.getUrl().toString())
            .send(bodySendDelegate(request))
            .responseConnection(responseDelegate(request, disableBufferCopy, eagerlyReadResponse))
            .single();
    }

    /**
     * Delegate to send the request content.
     *
     * @param restRequest the Rest request contains the body to be sent
     * @return a delegate upon invocation sets the request body in reactor-netty outbound object
     */
    private static BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> bodySendDelegate(
        final HttpRequest restRequest) {
        return (reactorNettyRequest, reactorNettyOutbound) -> {
            for (HttpHeader header : restRequest.getHeaders()) {
                if (header.getValue() != null) {
                    reactorNettyRequest.header(header.getName(), header.getValue());
                }
            }
            if (restRequest.getBody() != null) {
                Flux<ByteBuf> nettyByteBufFlux = restRequest.getBody().map(Unpooled::wrappedBuffer);
                return reactorNettyOutbound.send(nettyByteBufFlux);
            } else {
                return reactorNettyOutbound;
            }
        };
    }

    /**
     * Delegate to receive response.
     *
     * @param restRequest the Rest request whose response this delegate handles
     * @param disableBufferCopy Flag indicating if the network response shouldn't be buffered.
     * @param eagerlyReadResponse Flag indicating if the network response should be eagerly read into memory.
     * @return a delegate upon invocation setup Rest response object
     */
    private static BiFunction<HttpClientResponse, Connection, Publisher<HttpResponse>> responseDelegate(
        final HttpRequest restRequest, final boolean disableBufferCopy, final boolean eagerlyReadResponse) {
        return (reactorNettyResponse, reactorNettyConnection) -> {
            /*
             * If we are eagerly reading the response into memory we can ignore the disable buffer copy flag as we
             * MUST deep copy the buffer to ensure it can safely be used downstream.
             */
            if (eagerlyReadResponse) {
                // Setup the body flux and dispose the connection once it has been received.
                Flux<ByteBuffer> body = reactorNettyConnection.inbound().receive().asByteBuffer()
                    .doFinally(ignored -> closeConnection(reactorNettyConnection));

                return FluxUtil.collectBytesInByteBufferStream(body)
                    .map(bytes -> new BufferedReactorNettyHttpResponse(reactorNettyResponse, restRequest, bytes));

            } else {
                return Mono.just(new ReactorNettyHttpResponse(reactorNettyResponse, reactorNettyConnection, restRequest,
                    disableBufferCopy));
            }
        };
    }

    static ByteBuffer deepCopyBuffer(ByteBuf byteBuf) {
        ByteBuffer buffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(buffer);
        buffer.rewind();
        return buffer;
    }

    static void closeConnection(Connection reactorNettyConnection) {
        if (!reactorNettyConnection.isDisposed()) {
            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
        }
    }
}
