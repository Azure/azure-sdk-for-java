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
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
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
    private static final ClientLogger LOGGER = new ClientLogger(NettyAsyncHttpClient.class);
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

    private static final int selection = Integer.parseInt(System.getProperty("selection"));

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

            if (restRequest.getContent() == null) {
                return reactorNettyOutbound;
            }

            BinaryDataContent binaryDataContent = BinaryDataHelper.getContent(restRequest.getContent());
            if (binaryDataContent instanceof ByteArrayContent || binaryDataContent instanceof StringContent) {
                switch (selection) {
                    case 1:
                        // Completed 7,970 operations in a weighted-average of 60.01s (132.82 ops/s, 0.008 s/op)
                        // Completed 7,736 operations in a weighted-average of 60.01s (128.91 ops/s, 0.008 s/op)
                        // Completed 7,531 operations in a weighted-average of 60.00s (125.52 ops/s, 0.008 s/op)
                        // Completed 7,170 operations in a weighted-average of 60.00s (119.50 ops/s, 0.008 s/op)
                        // Completed 7,731 operations in a weighted-average of 60.00s (128.85 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.send(
                            Mono.fromSupplier(() -> Unpooled.wrappedBuffer(binaryDataContent.toBytes())));
                    case 2:
                        // Completed 7,268 operations in a weighted-average of 60.02s (121.10 ops/s, 0.008 s/op)
                        // Completed 7,750 operations in a weighted-average of 60.01s (129.15 ops/s, 0.008 s/op)
                        // Completed 7,605 operations in a weighted-average of 60.01s (126.73 ops/s, 0.008 s/op)
                        // Completed 7,582 operations in a weighted-average of 60.00s (126.36 ops/s, 0.008 s/op)
                        // Completed 7,400 operations in a weighted-average of 60.00s (123.33 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.send(
                            Mono.just(Unpooled.wrappedBuffer(binaryDataContent.toBytes())));
                    case 4:
                        // Completed 7,311 operations in a weighted-average of 60.00s (121.84 ops/s, 0.008 s/op)
                        // Completed 7,729 operations in a weighted-average of 60.01s (128.79 ops/s, 0.008 s/op)
                        // Completed 7,405 operations in a weighted-average of 60.00s (123.41 ops/s, 0.008 s/op)
                        // Completed 7,560 operations in a weighted-average of 60.01s (125.99 ops/s, 0.008 s/op)
                        // Completed 7,544 operations in a weighted-average of 60.01s (125.72 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.send(
                            Mono.defer(() -> Mono.just(Unpooled.wrappedBuffer(binaryDataContent.toBytes()))));
                    case 5:
                        // Completed 7,310 operations in a weighted-average of 60.00s (121.83 ops/s, 0.008 s/op)
                        // Completed 7,171 operations in a weighted-average of 60.01s (119.49 ops/s, 0.008 s/op)
                        // Completed 7,418 operations in a weighted-average of 60.01s (123.62 ops/s, 0.008 s/op)
                        // Completed 7,664 operations in a weighted-average of 60.01s (127.72 ops/s, 0.008 s/op)
                        // Completed 7,521 operations in a weighted-average of 60.01s (125.33 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.send(
                            Mono.fromCallable(() -> Unpooled.wrappedBuffer(binaryDataContent.toBytes())));
                    case 6:
                        // Completed 7,665 operations in a weighted-average of 60.00s (127.74 ops/s, 0.008 s/op)
                        // Completed 7,735 operations in a weighted-average of 60.00s (128.91 ops/s, 0.008 s/op)
                        // Completed 7,559 operations in a weighted-average of 60.01s (125.96 ops/s, 0.008 s/op)
                        // Completed 7,527 operations in a weighted-average of 60.01s (125.43 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.sendByteArray(
                            Mono.fromSupplier(binaryDataContent::toBytes));
                    case 7:
                        // Completed 7,753 operations in a weighted-average of 60.00s (129.21 ops/s, 0.008 s/op)
                        // Completed 7,379 operations in a weighted-average of 60.00s (122.98 ops/s, 0.008 s/op)
                        // Completed 7,589 operations in a weighted-average of 60.00s (126.47 ops/s, 0.008 s/op)
                        // Completed 7,408 operations in a weighted-average of 60.01s (123.45 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.sendByteArray(
                            Mono.just(binaryDataContent.toBytes()));
                    case 8:
                        // Completed 7,290 operations in a weighted-average of 60.00s (121.50 ops/s, 0.008 s/op)
                        // Completed 7,532 operations in a weighted-average of 60.01s (125.52 ops/s, 0.008 s/op)
                        // Completed 7,566 operations in a weighted-average of 60.00s (126.09 ops/s, 0.008 s/op)
                        // Completed 7,720 operations in a weighted-average of 60.01s (128.65 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.sendByteArray(
                            Mono.defer(() -> Mono.just(binaryDataContent.toBytes())));
                    case 9:
                        // Completed 7,391 operations in a weighted-average of 60.01s (123.16 ops/s, 0.008 s/op)
                        // Completed 7,493 operations in a weighted-average of 60.00s (124.88 ops/s, 0.008 s/op)
                        // Completed 7,589 operations in a weighted-average of 60.00s (126.47 ops/s, 0.008 s/op)
                        // Completed 7,901 operations in a weighted-average of 60.01s (131.66 ops/s, 0.008 s/op)
                        return reactorNettyOutbound.sendByteArray(
                            Mono.fromCallable(binaryDataContent::toBytes));
                }
                return reactorNettyOutbound.send(
                    Mono.fromSupplier(() -> Unpooled.wrappedBuffer(binaryDataContent.toBytes())));
            } else if (binaryDataContent instanceof FileContent) {
                FileContent fileContent = (FileContent) binaryDataContent;
                // fileContent.getLength() is always not null in FileContent.
                if (restRequest.getUrl().getProtocol().equals("https")) {
                    // NettyOutbound uses such logic internally for ssl connections but with smaller buffer of 1KB.
                    return reactorNettyOutbound.sendUsing(
                        () -> FileChannel.open(fileContent.getFile(), StandardOpenOption.READ),
                        (c, fc) -> {
                            if (c.channel().pipeline().get(ChunkedWriteHandler.class) == null) {
                                c.addHandlerLast("reactor.left.chunkedWriter", new ChunkedWriteHandler());
                            }

                            try {
                                return new ChunkedNioFile(
                                    fc, fileContent.getPosition(), fileContent.getLength(), fileContent.getChunkSize());
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }
                        },
                        (fc) -> {
                            try {
                                fc.close();
                            } catch (IOException e) {
                                LOGGER.log(LogLevel.VERBOSE, () -> "Could not close file", e);
                            }
                        });
                } else {
                    // Beware of NettyOutbound.sendFile(Path) it involves extra file length lookup.
                    // This is going to use zero-copy transfer if there's no ssl
                    return reactorNettyOutbound.sendFile(
                        fileContent.getFile(), fileContent.getPosition(), fileContent.getLength());
                }
            } else if (binaryDataContent instanceof InputStreamContent) {
                return reactorNettyOutbound.sendUsing(
                    binaryDataContent::toStream,
                    (c, stream) -> {
                        if (c.channel().pipeline().get(ChunkedWriteHandler.class) == null) {
                            c.addHandlerLast("reactor.left.chunkedWriter", new ChunkedWriteHandler());
                        }

                        return new ChunkedStream(stream);
                    },
                    (stream) -> {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            LOGGER.log(LogLevel.VERBOSE, () -> "Could not close stream", e);
                        }
                    });
            } else {
                Flux<ByteBuf> nettyByteBufFlux = restRequest.getBody().map(Unpooled::wrappedBuffer);
                return reactorNettyOutbound.send(nettyByteBufFlux);
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
