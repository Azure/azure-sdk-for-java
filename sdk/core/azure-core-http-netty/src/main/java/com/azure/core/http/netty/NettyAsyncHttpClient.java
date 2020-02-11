// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.HttpProxyExceptionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.proxy.ProxyHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.NettyPipeline;
import reactor.netty.channel.BootstrapHandlers;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.tcp.TcpClient;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This class provides a Netty-based implementation for the {@link HttpClient} interface. Creating an instance of this
 * class can be achieved by using the {@link NettyAsyncHttpClientBuilder} class, which offers Netty-specific API for
 * features such as {@link NettyAsyncHttpClientBuilder#nioEventLoopGroup(NioEventLoopGroup) thread pooling}, {@link
 * NettyAsyncHttpClientBuilder#wiretap(boolean) wiretapping}, {@link NettyAsyncHttpClientBuilder#proxy(ProxyOptions)
 * setProxy configuration}, and much more.
 *
 * @see HttpClient
 * @see NettyAsyncHttpClientBuilder
 */
class NettyAsyncHttpClient implements HttpClient {

    private final NioEventLoopGroup eventLoopGroup;
    private final Supplier<ProxyHandler> proxyHandlerSupplier;
    private final Pattern nonProxyHostsPattern;
    private final boolean disableBufferCopy;

    final reactor.netty.http.client.HttpClient nettyClient;

    /**
     * Creates default NettyAsyncHttpClient.
     */
    NettyAsyncHttpClient() {
        this(reactor.netty.http.client.HttpClient.create(), null, null, null, false);
    }

    /**
     * Creates NettyAsyncHttpClient with provided http client.
     *
     * @param nettyClient the reactor-netty http client
     * @param eventLoopGroup {@link NioEventLoopGroup} that processes requests.
     * @param proxyHandlerSupplier Supplier that returns the {@link ProxyHandler} that connects to the configured
     * proxy.
     */
    NettyAsyncHttpClient(reactor.netty.http.client.HttpClient nettyClient, NioEventLoopGroup eventLoopGroup,
        Supplier<ProxyHandler> proxyHandlerSupplier, String nonProxyHosts, boolean disableBufferCopy) {
        this.nettyClient = nettyClient;
        this.eventLoopGroup = eventLoopGroup;
        this.proxyHandlerSupplier = proxyHandlerSupplier;
        this.nonProxyHostsPattern = (nonProxyHosts == null)
            ? null
            : Pattern.compile(nonProxyHosts, Pattern.CASE_INSENSITIVE);
        this.disableBufferCopy = disableBufferCopy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        Objects.requireNonNull(request.getHttpMethod(), "'request.getHttpMethod()' cannot be null.");
        Objects.requireNonNull(request.getUrl(), "'request.getUrl()' cannot be null.");
        Objects.requireNonNull(request.getUrl().getProtocol(), "'request.getUrl().getProtocol()' cannot be null.");
        return nettyClient
            .tcpConfiguration(tcpClient -> configureTcpClient(tcpClient, request.getUrl().getHost()))
            .request(HttpMethod.valueOf(request.getHttpMethod().toString()))
            .uri(request.getUrl().toString())
            .send(bodySendDelegate(request))
            .responseConnection(responseDelegate(request, disableBufferCopy))
            .single();
    }

    /*
     * Configures the underlying TcpClient that sends the request.
     */
    private TcpClient configureTcpClient(TcpClient tcpClient, String host) {
        if (eventLoopGroup != null) {
            tcpClient = tcpClient.runOn(eventLoopGroup);
        }

        // Validate that the request should be proxied.
        if (nonProxyHostsPattern == null || !nonProxyHostsPattern.matcher(host).matches()) {
            ProxyHandler proxyHandler = (proxyHandlerSupplier == null) ? null : proxyHandlerSupplier.get();
            if (proxyHandler != null) {
                /*
                 * Configure the request Channel to be initialized with a ProxyHandler. The ProxyHandler is the first
                 * operation in the pipeline as it needs to handle sending a CONNECT request to the proxy before any
                 * request data is sent.
                 */
                tcpClient = tcpClient.bootstrap(bootstrap -> BootstrapHandlers
                    .updateConfiguration(bootstrap, NettyPipeline.ProxyHandler, (connectionObserver, channel) ->
                        channel.pipeline().addFirst(NettyPipeline.ProxyHandler, proxyHandler)
                            .addLast("azure.proxy.exceptionHandler", new HttpProxyExceptionHandler())));
            }
        }

        return tcpClient;
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
     * @return a delegate upon invocation setup Rest response object
     */
    private static BiFunction<HttpClientResponse, Connection, Publisher<HttpResponse>> responseDelegate(
        final HttpRequest restRequest, final boolean disableBufferCopy) {
        return (reactorNettyResponse, reactorNettyConnection) ->
            Mono.just(new ReactorNettyHttpResponse(reactorNettyResponse, reactorNettyConnection, restRequest,
                disableBufferCopy));
    }

    static class ReactorNettyHttpResponse extends HttpResponse {

        private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(new byte[0]);
        private final HttpClientResponse reactorNettyResponse;
        private final Connection reactorNettyConnection;
        private final boolean disableBufferCopy;

        ReactorNettyHttpResponse(HttpClientResponse reactorNettyResponse, Connection reactorNettyConnection,
            HttpRequest httpRequest, boolean disableBufferCopy) {
            super(httpRequest);
            this.reactorNettyResponse = reactorNettyResponse;
            this.reactorNettyConnection = reactorNettyConnection;
            this.disableBufferCopy = disableBufferCopy;
        }

        @Override
        public int getStatusCode() {
            return reactorNettyResponse.status().code();
        }

        @Override
        public String getHeaderValue(String name) {
            return reactorNettyResponse.responseHeaders().get(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            reactorNettyResponse.responseHeaders().forEach(e -> headers.put(e.getKey(), e.getValue()));
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return bodyIntern().doFinally(s -> {
                if (!reactorNettyConnection.isDisposed()) {
                    reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                }
            }).map(byteBuf -> this.disableBufferCopy ? byteBuf.nioBuffer() : deepCopyBuffer(byteBuf));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return bodyIntern().aggregate().asByteArray().doFinally(s -> {
                if (!reactorNettyConnection.isDisposed()) {
                    reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                }
            });
        }

        @Override
        public Mono<String> getBodyAsString() {
            return bodyIntern().aggregate().asString().doFinally(s -> {
                if (!reactorNettyConnection.isDisposed()) {
                    reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                }
            });
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return bodyIntern().aggregate().asString(charset).doFinally(s -> {
                if (!reactorNettyConnection.isDisposed()) {
                    reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                }
            });
        }

        @Override
        public void close() {
            if (!reactorNettyConnection.isDisposed()) {
                reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
            }
        }

        private ByteBufFlux bodyIntern() {
            return reactorNettyConnection.inbound().receive();
        }

        // used for testing only
        Connection internConnection() {
            return reactorNettyConnection;
        }

        private static ByteBuffer deepCopyBuffer(ByteBuf byteBuf) {
            ByteBuffer buffer = ByteBuffer.allocate(byteBuf.readableBytes());
            byteBuf.readBytes(buffer);
            buffer.rewind();
            return buffer;
        }
    }
}
