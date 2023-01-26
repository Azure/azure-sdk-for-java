// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.ChallengeHolder;
import com.azure.core.http.netty.implementation.HttpProxyHandler;
import com.azure.core.http.netty.implementation.NettyAsyncHttpBufferedResponse;
import com.azure.core.http.netty.implementation.NettyAsyncHttpResponse;
import com.azure.core.http.netty.implementation.ReadTimeoutHandler;
import com.azure.core.http.netty.implementation.RequestProgressReportingHandler;
import com.azure.core.http.netty.implementation.ResponseTimeoutHandler;
import com.azure.core.http.netty.implementation.WriteTimeoutHandler;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.AuthorizationChallengeHandler;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
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
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyOutbound;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.transport.AddressUtils;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static com.azure.core.http.netty.implementation.Utility.closeConnection;

/**
 * This class provides a Netty-based implementation for the {@link HttpClient} interface. Creating an instance of this
 * class can be achieved by using the {@link NettyAsyncHttpClientBuilder} class, which offers Netty-specific API for
 * features such as {@link NettyAsyncHttpClientBuilder#eventLoopGroup(EventLoopGroup) thread pooling},
 * {@link NettyAsyncHttpClientBuilder#wiretap(boolean) wiretapping},
 * {@link NettyAsyncHttpClientBuilder#proxy(ProxyOptions) setProxy configuration}, and much more.
 *
 * @see HttpClient
 * @see NettyAsyncHttpClientBuilder
 */
class NettyAsyncHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(NettyAsyncHttpClient.class);
    private static final byte[] EMPTY_BYTES = new byte[0];

    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";
    private static final String AZURE_IGNORE_RESPONSE_BODY = "azure-ignore-response-body";
    private static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";
    private static final String AZURE_EAGERLY_CONVERT_HEADERS = "azure-eagerly-convert-headers";

    final boolean disableBufferCopy;
    final long readTimeout;
    final long writeTimeout;
    final long responseTimeout;

    final boolean addProxyHandler;
    final ProxyOptions proxyOptions;
    final Pattern nonProxyHostsPattern;
    final AuthorizationChallengeHandler handler;
    final AtomicReference<ChallengeHolder> proxyChallengeHolder;

    final reactor.netty.http.client.HttpClient nettyClient;

    /**
     * Creates NettyAsyncHttpClient with provided http client.
     *
     * @param nettyClient the reactor-netty http client
     * @param disableBufferCopy Determines whether deep cloning of response buffers should be disabled.
     */
    NettyAsyncHttpClient(reactor.netty.http.client.HttpClient nettyClient, boolean disableBufferCopy,
        long readTimeout, long writeTimeout, long responseTimeout, boolean addProxyHandler, ProxyOptions proxyOptions,
        Pattern nonProxyHostsPattern, AuthorizationChallengeHandler handler,
        AtomicReference<ChallengeHolder> proxyChallengeHolder) {
        this.nettyClient = nettyClient;
        this.disableBufferCopy = disableBufferCopy;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;
        this.addProxyHandler = addProxyHandler;
        this.proxyOptions = proxyOptions;
        this.nonProxyHostsPattern = nonProxyHostsPattern;
        this.handler = handler;
        this.proxyChallengeHolder = proxyChallengeHolder;
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

        boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        boolean headersEagerlyConverted = (boolean) context.getData(AZURE_EAGERLY_CONVERT_HEADERS).orElse(false);
        long responseTimeout = context.getData(AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .map(timeoutDuration -> ((Duration) timeoutDuration).toMillis())
            .orElse(this.responseTimeout);

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();
        ConnectionObserver observer = (connection, newState) -> {
            if (newState == HttpClientState.REQUEST_PREPARED) {
                connection.addHandlerLast(WriteTimeoutHandler.HANDLER_NAME, new WriteTimeoutHandler(writeTimeout));
                if (progressReporter != null) {
                    connection.addHandlerLast(RequestProgressReportingHandler.HANDLER_NAME,
                        new RequestProgressReportingHandler(progressReporter));
                }
            } else if (newState == HttpClientState.REQUEST_SENT) {
                connection.removeHandler(WriteTimeoutHandler.HANDLER_NAME);
                if (progressReporter != null) {
                    connection.removeHandler(RequestProgressReportingHandler.HANDLER_NAME);
                }
                connection.addHandlerLast(ResponseTimeoutHandler.HANDLER_NAME,
                    new ResponseTimeoutHandler(responseTimeout));
            } else if (newState == HttpClientState.RESPONSE_RECEIVED) {
                connection.removeHandler(ResponseTimeoutHandler.HANDLER_NAME);
                connection.addHandlerLast(ReadTimeoutHandler.HANDLER_NAME, new ReadTimeoutHandler(readTimeout));
            } else if (newState == HttpClientState.RESPONSE_COMPLETED) {
                connection.removeHandler(ReadTimeoutHandler.HANDLER_NAME);
            }
        };

        reactor.netty.http.client.HttpClient configuredClient = nettyClient.observe(observer)
            .doOnChannelInit((connectionObserver, channel, remoteAddress) -> {
                if (addProxyHandler) {
                    /*
                     * Configure the request Channel to be initialized with a ProxyHandler. The ProxyHandler is the
                     * first operation in the pipeline as it needs to handle sending a CONNECT request to the proxy
                     * before any request data is sent.
                     *
                     * And in addition to adding the ProxyHandler update the Bootstrap resolver for proxy support.
                     */
                    if (shouldApplyProxy(remoteAddress, nonProxyHostsPattern)) {
                        channel.pipeline().addFirst(NettyPipeline.ProxyHandler, new HttpProxyHandler(
                            AddressUtils.replaceWithResolved(proxyOptions.getAddress()), handler,
                            proxyChallengeHolder));
                    }
                }
            });

        return configuredClient.request(toReactorNettyHttpMethod(request.getHttpMethod()))
            .uri(request.getUrl().toString())
            .send(bodySendDelegate(request))
            .responseConnection(responseDelegate(request, disableBufferCopy, eagerlyReadResponse, ignoreResponseBody,
                headersEagerlyConverted))
            .single()
            .flatMap(response -> {
                if (addProxyHandler && response.getStatusCode() == 407) {
                    return Mono.error(new ProxyConnectException("First attempt to connect to proxy failed."));
                } else {
                    return Mono.just(response);
                }
            })
            .onErrorMap(throwable -> {
                // The exception was an SSLException that was caused by a failure to connect to a proxy.
                // Extract the inner ProxyConnectException and propagate that instead.
                if (throwable instanceof SSLException && throwable.getCause() instanceof ProxyConnectException) {
                    return throwable.getCause();
                }

                return throwable;
            })
            .retryWhen(Retry.max(1).filter(throwable -> throwable instanceof ProxyConnectException)
                .onRetryExhaustedThrow((ignoredSpec, signal) -> signal.failure()));
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        try {
            return send(request, context).block();
        } catch (Exception e) {
            Throwable unwrapped = Exceptions.unwrap(e);
            if (unwrapped instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) unwrapped);
            } else if (unwrapped instanceof IOException) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException((IOException) unwrapped));
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(unwrapped));
            }
        }
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
                // Get the Netty headers from Reactor Netty and work with the Netty headers directly. This removes the
                // need to do contains checks to determine if headers added by Reactor Netty need to be overwritten.
                // Additionally, this gives direct access to the set(String, Iterable<String>) API which is more
                // performant as it only needs to validate the header name once instead of each time a value from the
                // list is added.
                // This reduces header name and header name equality checks greatly, once for getting rid of contains
                // and once for each additional value in the header.
                reactorNettyRequest.requestHeaders().set(hdr.getName(), hdr.getValuesList());
            }
            BinaryData body = restRequest.getBodyAsBinaryData();
            if (body != null) {
                BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
                if (bodyContent instanceof ByteArrayContent) {
                    // This is marginally more performant than reactorNettyOutbound.sendByteArray which
                    // adds extra operators to achieve same result.
                    // The bytes are in memory at this time anyway and Unpooled.wrappedBuffer is lightweight.
                    return reactorNettyOutbound.send(Mono.just(Unpooled.wrappedBuffer(bodyContent.toBytes())));
                } else if (bodyContent instanceof StringContent
                    || bodyContent instanceof SerializableContent) {
                    // This defers encoding final bytes until emission happens.
                    return reactorNettyOutbound.send(
                        Mono.fromSupplier(() -> Unpooled.wrappedBuffer(bodyContent.toBytes())));
                } else if (bodyContent instanceof FileContent) {
                    return sendFile(restRequest, reactorNettyOutbound, (FileContent) bodyContent);
                } else if (bodyContent instanceof InputStreamContent) {
                    return sendInputStream(reactorNettyOutbound, (InputStreamContent) bodyContent);
                } else {
                    Flux<ByteBuf> nettyByteBufFlux = restRequest.getBody().map(Unpooled::wrappedBuffer);
                    return reactorNettyOutbound.send(nettyByteBufFlux);
                }
            } else {
                return reactorNettyOutbound;
            }
        };
    }

    private static NettyOutbound sendFile(
        HttpRequest restRequest, NettyOutbound reactorNettyOutbound, FileContent fileContent) {
        // NettyOutbound uses such logic internally for ssl connections but with smaller buffer of 1KB.
        // We use simplified check here to handle https instead of original check that Netty uses
        // as other corner cases are not existent (i.e. different protocols using ssl).
        // But in case we missed them these will be still handled by Netty's logic - they'd just use 1KB chunk
        // and this check should be evolved when they're discovered.
        if (restRequest.getUrl().getProtocol().equals("https")) {
            return reactorNettyOutbound.sendUsing(
                () -> FileChannel.open(fileContent.getFile(), StandardOpenOption.READ),
                (c, fc) -> {
                    if (c.channel().pipeline().get(ChunkedWriteHandler.class) == null) {
                        c.addHandlerLast(NettyPipeline.ChunkedWriter, new ChunkedWriteHandler());
                    }

                    try {
                        return new ChunkedNioFile(
                            fc, fileContent.getPosition(), fileContent.getLength(), fileContent.getChunkSize());
                    } catch (IOException e) {
                        throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                    }
                },
                (fc) -> {
                    try {
                        fc.close();
                    } catch (IOException e) {
                        throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                    }
                });
        } else {
            // Beware of NettyOutbound.sendFile(Path) it involves extra file length lookup.
            // This is going to use zero-copy transfer if there's no ssl
            return reactorNettyOutbound.sendFile(
                fileContent.getFile(), fileContent.getPosition(), fileContent.getLength());
        }
    }

    private static NettyOutbound sendInputStream(NettyOutbound reactorNettyOutbound, InputStreamContent bodyContent) {
        return reactorNettyOutbound.sendUsing(
            bodyContent::toStream,
            (c, stream) -> {
                if (c.channel().pipeline().get(ChunkedWriteHandler.class) == null) {
                    c.addHandlerLast(NettyPipeline.ChunkedWriter, new ChunkedWriteHandler());
                }

                return new ChunkedStream(stream);
            },
            (stream) -> {
                // NO-OP. We don't close streams passed to our APIs.
            });
    }

    /**
     * Delegate to receive response.
     *
     * @param restRequest the Rest request whose response this delegate handles
     * @param disableBufferCopy Flag indicating if the network response shouldn't be buffered.
     * @param eagerlyReadResponse Flag indicating if the network response should be eagerly read into memory.
     * @param ignoreResponseBody Flag indicating if the network response should be ignored.
     * @param headersEagerlyConverted Flag indicating if the Netty HttpHeaders should be eagerly converted to Azure Core
     * HttpHeaders.
     * @return a delegate upon invocation setup Rest response object
     */
    private static BiFunction<HttpClientResponse, Connection, Mono<HttpResponse>> responseDelegate(
        HttpRequest restRequest, boolean disableBufferCopy, boolean eagerlyReadResponse, boolean ignoreResponseBody,
        boolean headersEagerlyConverted) {
        return (reactorNettyResponse, reactorNettyConnection) -> {
            // For now, eagerlyReadResponse and ignoreResponseBody works the same.
//            if (ignoreResponseBody) {
//                AtomicBoolean firstNext = new AtomicBoolean(true);
//                return reactorNettyConnection.inbound().receive()
//                    .doOnNext(ignored -> {
//                        if (!firstNext.compareAndSet(true, false)) {
//                            LOGGER.log(LogLevel.WARNING, () -> "Received HTTP response body when one wasn't expected. "
//                                + "Response body will be ignored as directed.");
//                        }
//                    })
//                    .ignoreElements()
//                    .doFinally(ignored -> closeConnection(reactorNettyConnection))
//                    .then(Mono.fromSupplier(() -> new NettyAsyncHttpBufferedResponse(reactorNettyResponse, restRequest,
//                        EMPTY_BYTES, headersEagerlyConverted)));
//            }

            /*
             * If the response is being eagerly read into memory the flag for buffer copying can be ignored as the
             * response MUST be deeply copied to ensure it can safely be used downstream.
             */
            if (eagerlyReadResponse || ignoreResponseBody) {
                // Set up the body flux and dispose the connection once it has been received.
                return reactorNettyConnection.inbound().receive().aggregate().asByteArray()
                    .doFinally(ignored -> closeConnection(reactorNettyConnection))
                    .switchIfEmpty(Mono.just(EMPTY_BYTES))
                    .map(bytes -> new NettyAsyncHttpBufferedResponse(reactorNettyResponse, restRequest, bytes,
                        headersEagerlyConverted));
            } else {
                return Mono.just(new NettyAsyncHttpResponse(reactorNettyResponse, reactorNettyConnection, restRequest,
                    disableBufferCopy, headersEagerlyConverted));
            }
        };
    }

    private static boolean shouldApplyProxy(SocketAddress socketAddress, Pattern nonProxyHostsPattern) {
        if (nonProxyHostsPattern == null) {
            return true;
        }

        if (!(socketAddress instanceof InetSocketAddress)) {
            return true;
        }

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;

        return !nonProxyHostsPattern.matcher(inetSocketAddress.getHostString()).matches();
    }

    private static HttpMethod toReactorNettyHttpMethod(com.azure.core.http.HttpMethod azureHttpMethod) {
        switch (azureHttpMethod) {
            case GET:
                return HttpMethod.GET;
            case PUT:
                return HttpMethod.PUT;
            case HEAD:
                return HttpMethod.HEAD;
            case POST:
                return HttpMethod.POST;
            case DELETE:
                return HttpMethod.DELETE;
            case PATCH:
                return HttpMethod.PATCH;
            case TRACE:
                return HttpMethod.TRACE;
            case CONNECT:
                return HttpMethod.CONNECT;
            case OPTIONS:
                return HttpMethod.OPTIONS;
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException("Unknown HttpMethod '"
                    + azureHttpMethod + "'.")); // Should never happen
        }
    }
}
