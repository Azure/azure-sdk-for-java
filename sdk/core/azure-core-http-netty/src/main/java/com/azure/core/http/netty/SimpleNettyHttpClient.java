// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.implementation.simple.SimpleChannelPoolMap;
import com.azure.core.http.netty.implementation.simple.SimpleRequestContext;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.azure.core.http.netty.implementation.simple.SimpleNettyConstants.REQUEST_CONTEXT_KEY;

/**
 * TODO (kasobol-msft) add docs.
 */
public class SimpleNettyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleNettyHttpClient.class);

    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";

    private final ChannelPoolMap<URI, ChannelPool> channelPoolMap;

    /**
     * TODO (kasobol-msft) add docs.
     */
    public SimpleNettyHttpClient() {
        // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(r -> {
            Thread t = new Thread(r);
            // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
            t.setDaemon(true);
            return t;
        });

        channelPoolMap = new SimpleChannelPoolMap(eventLoopGroup);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        Mono<HttpResponse> responseMono = Mono.fromFuture(() -> sendInternal(request, context, eagerlyReadResponse));
        if (!eagerlyReadResponse) {
            // TODO (kasobol-msft) maybe replace with dedicated reactor friendly collector later.
            // Otherwise there's deadlock in channel handler.
            responseMono = responseMono.publishOn(Schedulers.boundedElastic());
        }
        return responseMono;
    }

    @Override
    public HttpResponse sendSynchronously(HttpRequest request, Context context) {
        try {
            boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
            return sendInternal(request, context, eagerlyReadResponse).get();
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private CompletableFuture<HttpResponse> sendInternal(
        HttpRequest request, Context context, boolean eagerlyReadResponse) {
        URL url = request.getUrl();

        // Configure the client.
        CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();
        // Make the connection attempt.
        URI channelPoolKey;
        try {
            channelPoolKey = new URI(url.getProtocol(), null, url.getHost(),
                url.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
        ChannelPool channelPool = channelPoolMap.get(channelPoolKey);

        SimpleRequestContext requestContext = new SimpleRequestContext(
            channelPool, request, responseFuture, eagerlyReadResponse);
        channelPool.acquire()
            .addListener(new ConnectionAcquiredListener(requestContext));

        return responseFuture;
    }

    private static final class ConnectionAcquiredListener implements FutureListener<Channel> {
        private final SimpleRequestContext requestContext;

        private ConnectionAcquiredListener(SimpleRequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {
            if (future.isSuccess()) {
                Channel ch = future.getNow();
                try {
                    ch.attr(REQUEST_CONTEXT_KEY).set(requestContext);

                    HttpRequest request = requestContext.getRequest();
                    HttpMethod method = mapHttpMethod(request.getHttpMethod());

                    // Prepare the HTTP request.
                    io.netty.handler.codec.http.HttpRequest nettyRequest;
                    Long contentLength = null;
                    ChunkedInput<ByteBuf> chunkedInput = null;
                    Flux<ByteBuffer> flux = null;
                    if (request.getContent() != null) {
                        BinaryDataContent binaryDataContent = BinaryDataHelper.getContent(request.getContent());
                        contentLength = binaryDataContent.getLength();
                        if (binaryDataContent instanceof ByteArrayContent
                            || binaryDataContent instanceof StringContent) {
                            byte[] requestBytes = binaryDataContent.toBytes();
                            nettyRequest = new DefaultFullHttpRequest(
                                HttpVersion.HTTP_1_1, method, request.getUrl().toString(),
                                Unpooled.wrappedBuffer(requestBytes));
                        } else if (binaryDataContent instanceof FileContent) {
                            chunkedInput = new ChunkedNioFile(((FileContent) binaryDataContent).getFile().toFile());
                            nettyRequest = new DefaultHttpRequest(
                                HttpVersion.HTTP_1_1, method, request.getUrl().toString());
                        } else if (binaryDataContent instanceof InputStreamContent) {
                            chunkedInput = new ChunkedStream(binaryDataContent.toStream());
                            nettyRequest = new DefaultHttpRequest(
                                HttpVersion.HTTP_1_1, method, request.getUrl().toString());
                        } else {
                            flux = binaryDataContent.toFluxByteBuffer();
                            nettyRequest = new DefaultHttpRequest(
                                HttpVersion.HTTP_1_1, method, request.getUrl().toString());
                        }
                    } else {
                        nettyRequest = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1, method, request.getUrl().toString());
                    }

                    for (HttpHeader header : request.getHeaders()) {
                        nettyRequest.headers().set(header.getName(), header.getValuesList());
                    }

                    nettyRequest.headers().set(HttpHeaderNames.HOST, request.getUrl().getHost());

                    Object chunkedContent = chunkedInput;
                    if (!nettyRequest.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
                        if (contentLength != null) {
                            nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
                        } else {
                            nettyRequest.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
                            chunkedContent = new HttpChunkedInput(chunkedInput);
                        }
                    }

                    if (chunkedInput != null) {
                        ch.write(nettyRequest);
                        ch.writeAndFlush(chunkedContent);
                    } else if (flux != null) {
                        boolean isChunked = contentLength == null;
                        ch.write(nettyRequest);
                        flux.doOnEach(signal -> {
                            if (signal.isOnNext()) {
                                ByteBuffer byteBuffer = signal.get();
                                if (byteBuffer != null) {
                                    ch.write(Unpooled.wrappedBuffer(byteBuffer));
                                }
                            } else if (signal.isOnComplete()) {
                                if (isChunked) {
                                    ch.writeAndFlush(new DefaultLastHttpContent());
                                } else {
                                    ch.flush();
                                }
                            } else if (signal.isOnError()) {
                                ch.close();
                                requestContext.getChannelPool().release(ch);
                                Throwable throwable = signal.getThrowable();
                                if (throwable != null) {
                                    requestContext.getResponseFuture().completeExceptionally(throwable);
                                } else {
                                    requestContext.getResponseFuture().completeExceptionally(
                                        new RuntimeException("bad reactor"));
                                }
                            }
                        }).subscribe();
                    } else  {
                        ch.writeAndFlush(nettyRequest);
                    }
                } catch (RuntimeException e) {
                    ch.close();
                    requestContext.getChannelPool().release(ch);
                    requestContext.getResponseFuture().completeExceptionally(e);
                }
            } else {
                requestContext.getResponseFuture().completeExceptionally(future.cause());
            }
        }
    }

    private static HttpMethod mapHttpMethod(com.azure.core.http.HttpMethod httpMethod) {
        switch (httpMethod) {
            case GET:
                return HttpMethod.GET;
            case POST:
                return HttpMethod.POST;
            case PUT:
                return HttpMethod.PUT;
            case PATCH:
                return HttpMethod.PATCH;
            case DELETE:
                return HttpMethod.DELETE;
            case HEAD:
                return HttpMethod.HEAD;
            case OPTIONS:
                return HttpMethod.OPTIONS;
            case TRACE:
                return HttpMethod.TRACE;
            case CONNECT:
                return HttpMethod.CONNECT;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown http method"));
        }
    }
}
