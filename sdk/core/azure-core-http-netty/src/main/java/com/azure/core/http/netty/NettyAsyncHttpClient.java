// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.NettyAsyncHttpBufferedResponse;
import com.azure.core.http.netty.implementation.NettyAsyncHttpResponse;
import com.azure.core.http.netty.implementation.NettyToAzureCoreHttpHeadersWrapper;
import com.azure.core.http.netty.implementation.ResponseTimeoutHandler;
import com.azure.core.http.netty.implementation.WriteTimeoutHandler;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.proxy.ProxyConnectException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import static com.azure.core.http.netty.implementation.Utility.closeConnection;

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
    private static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";
    private final boolean disableBufferCopy;

    final reactor.netty.http.client.HttpClient nettyClient;

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

        reactor.netty.http.client.HttpClient client = this.nettyClient;
        Optional<Object> requestResponseTimeout = context.getData(AZURE_RESPONSE_TIMEOUT);
        if (requestResponseTimeout.isPresent()) {
            client = nettyClient.doAfterRequest((req, connection) -> {
                // remove write timeout handler and client-level response timeout handler before adding request-level
                // response timeout handler
                long responseTimeoutMillis = ((Duration) requestResponseTimeout.get()).toMillis();
                connection.removeHandler(WriteTimeoutHandler.HANDLER_NAME)
                    .removeHandler(ResponseTimeoutHandler.HANDLER_NAME)
                    .addHandlerLast(ResponseTimeoutHandler.HANDLER_NAME,
                            new ResponseTimeoutHandler(responseTimeoutMillis));
            });
        }

        return client
            .request(HttpMethod.valueOf(request.getHttpMethod().toString()))
            .uri(request.getUrl().toString())
            .send(bodySendDelegate(request))
            .responseConnection(responseDelegate(request, disableBufferCopy, eagerlyReadResponse))
            .single()
            .onErrorMap(throwable -> {
                // The exception was an SSLException that was caused by a failure to connect to a proxy.
                // Extract the inner ProxyConnectException and propagate that instead.
                if (throwable instanceof SSLException) {
                    if (throwable.getCause() instanceof ProxyConnectException) {
                        return throwable.getCause();
                    }
                }

                return throwable;
            })
            .retryWhen(Retry.max(1).filter(throwable -> throwable instanceof ProxyConnectException)
                .onRetryExhaustedThrow((ignoredSpec, signal) -> signal.failure()));
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
            for (HttpHeader hdr : restRequest.getHeaders()) {
                // Reactor-Netty allows for headers with multiple values, but it treats them as separate headers,
                // therefore, we must call rb.addHeader for each value, using the same key for all of them.
                // We would ideally replace this for-loop with code akin to the code in ReactorNettyHttpResponseBase,
                // whereby we would wrap the azure-core HttpHeaders in a Netty HttpHeaders wrapper, but as of today it
                // is not possible in reactor-netty to do this without copying occurring within that library. This
                // issue has been reported to the reactor-netty team at
                // https://github.com/reactor/reactor-netty/issues/1479
                if (reactorNettyRequest.requestHeaders().contains(hdr.getName())) {
                    // The Reactor-Netty request headers include headers by default, to prevent a scenario where we end
                    // adding a header twice that isn't allowed, such as User-Agent, check against the initial request
                    // header names. If our request header already exists in the Netty request we overwrite it initially
                    // then append our additional values if it is a multi-value header.
                    final AtomicBoolean first = new AtomicBoolean(true);
                    hdr.getValuesList().forEach(value -> {
                        if (first.compareAndSet(true, false)) {
                            reactorNettyRequest.header(hdr.getName(), value);
                        } else {
                            reactorNettyRequest.addHeader(hdr.getName(), value);
                        }
                    });
                } else {
                    hdr.getValuesList().forEach(value -> reactorNettyRequest.addHeader(hdr.getName(), value));
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

                return FluxUtil.collectBytesFromNetworkResponse(body,
                    new NettyToAzureCoreHttpHeadersWrapper(reactorNettyResponse.responseHeaders()))
                    .map(bytes -> new NettyAsyncHttpBufferedResponse(reactorNettyResponse, restRequest, bytes));

            } else {
                return Mono.just(new NettyAsyncHttpResponse(reactorNettyResponse, reactorNettyConnection, restRequest,
                    disableBufferCopy));
            }
        };
    }
}
