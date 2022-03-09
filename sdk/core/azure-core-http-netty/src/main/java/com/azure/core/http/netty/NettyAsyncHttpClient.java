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
import com.azure.core.http.netty.implementation.ReadTimeoutHandler;
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
import java.time.Duration;
import java.util.Objects;
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
    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";
    private static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";

    final boolean disableBufferCopy;
    final long readTimeout;
    final long writeTimeout;
    final long responseTimeout;

    final reactor.netty.http.client.HttpClient nettyClient;

    /**
     * Creates NettyAsyncHttpClient with provided http client.
     *
     * @param nettyClient the reactor-netty http client
     * @param disableBufferCopy Determines whether deep cloning of response buffers should be disabled.
     */
    NettyAsyncHttpClient(reactor.netty.http.client.HttpClient nettyClient, boolean disableBufferCopy,
        long readTimeout, long writeTimeout, long responseTimeout) {
        this.nettyClient = nettyClient;
        this.disableBufferCopy = disableBufferCopy;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;
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

        boolean effectiveEagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        long effectiveResponseTimeout = context.getData(AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .map(timeoutDuration -> ((Duration) timeoutDuration).toMillis())
            .orElse(this.responseTimeout);

        return nettyClient
            .doOnRequest((r, connection) -> addWriteTimeoutHandler(connection, writeTimeout))
            .doAfterRequest((r, connection) -> addResponseTimeoutHandler(connection, effectiveResponseTimeout))
            .doOnResponse((response, connection) -> addReadTimeoutHandler(connection, readTimeout))
            .doAfterResponseSuccess((response, connection) -> removeReadTimeoutHandler(connection))
            .request(HttpMethod.valueOf(request.getHttpMethod().toString()))
            .uri(request.getUrl().toString())
            .send(bodySendDelegate(request))
            .responseConnection(responseDelegate(request, disableBufferCopy, effectiveEagerlyReadResponse))
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
                    boolean first = true;
                    for (String value : hdr.getValuesList()) {
                        if (first) {
                            first = false;
                            reactorNettyRequest.header(hdr.getName(), value);
                        } else {
                            reactorNettyRequest.addHeader(hdr.getName(), value);
                        }
                    }
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
             * If the response is being eagerly read into memory the flag for buffer copying can be ignored as the
             * response MUST be deeply copied to ensure it can safely be used downstream.
             */
            if (eagerlyReadResponse) {
                // Set up the body flux and dispose the connection once it has been received.
                return FluxUtil.collectBytesFromNetworkResponse(
                    reactorNettyConnection.inbound().receive().asByteBuffer(),
                    new NettyToAzureCoreHttpHeadersWrapper(reactorNettyResponse.responseHeaders()))
                    .doFinally(ignored -> closeConnection(reactorNettyConnection))
                    .map(bytes -> new NettyAsyncHttpBufferedResponse(reactorNettyResponse, restRequest, bytes));
            } else {
                return Mono.just(new NettyAsyncHttpResponse(reactorNettyResponse, reactorNettyConnection, restRequest,
                    disableBufferCopy));
            }
        };
    }

    /*
     * Adds write timeout handler once the request is ready to begin sending.
     */
    private static void addWriteTimeoutHandler(Connection connection, long timeoutMillis) {
        connection.addHandlerLast(WriteTimeoutHandler.HANDLER_NAME, new WriteTimeoutHandler(timeoutMillis));
    }

    /*
     * Remove write timeout handler from the connection as the request has finished sending, then add response timeout
     * handler.
     */
    private static void addResponseTimeoutHandler(Connection connection, long timeoutMillis) {
        connection.removeHandler(WriteTimeoutHandler.HANDLER_NAME)
            .addHandlerLast(ResponseTimeoutHandler.HANDLER_NAME, new ResponseTimeoutHandler(timeoutMillis));
    }

    /*
     * Remove response timeout handler from the connection as the response has been received, then add read timeout
     * handler.
     */
    private static void addReadTimeoutHandler(Connection connection, long timeoutMillis) {
        connection.removeHandler(ResponseTimeoutHandler.HANDLER_NAME)
            .addHandlerLast(ReadTimeoutHandler.HANDLER_NAME, new ReadTimeoutHandler(timeoutMillis));
    }

    /*
     * Remove read timeout handler as the complete response has been received.
     */
    private static void removeReadTimeoutHandler(Connection connection) {
        connection.removeHandler(ReadTimeoutHandler.HANDLER_NAME);
    }
}
