/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * An HttpClient that is implemented using Netty.
 */
public final class NettyClient extends HttpClient {
    private final NettyAdapter adapter;
    private final Proxy proxy;

    /**
     * Creates NettyClient.
     * @param configuration the HTTP client configuration.
     * @param adapter the adapter to Netty
     */
    private NettyClient(HttpClient.Configuration configuration, NettyAdapter adapter) {
        this.adapter = adapter;
        this.proxy = configuration == null ? null : configuration.proxy();
    }

    @Override
    public Single<HttpResponse> sendRequestAsync(final HttpRequest request) {
        return adapter.sendRequestInternalAsync(request, proxy);
    }

    private static final class NettyAdapter {
        private final NioEventLoopGroup eventLoopGroup;
        private final SharedChannelPool channelPool;

        private NettyAdapter() {
            this.eventLoopGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.AUTO_READ, false);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.MINUTES.toMillis(3L));
            this.channelPool = new SharedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    ch.pipeline().addLast("HttpResponseDecoder", new HttpResponseDecoder());
                    ch.pipeline().addLast("HttpRequestEncoder", new HttpRequestEncoder());
                    ch.pipeline().addLast("HttpClientInboundHandler", new HttpClientInboundHandler(NettyAdapter.this));
                }
            }, eventLoopGroup.executorCount() * 2);
        }

        private NettyAdapter(int eventLoopGroupSize, int channelPoolSize) {
            this.eventLoopGroup = new NioEventLoopGroup(eventLoopGroupSize);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.MINUTES.toMillis(3L));
            this.channelPool = new SharedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    ch.pipeline().addLast("HttpResponseDecoder", new HttpResponseDecoder());
                    ch.pipeline().addLast("HttpRequestEncoder", new HttpRequestEncoder());
                    ch.pipeline().addLast("HttpClientInboundHandler", new HttpClientInboundHandler(NettyAdapter.this));
                }
            }, channelPoolSize);
        }

        private Single<HttpResponse> sendRequestInternalAsync(final HttpRequest request, Proxy proxy) {
            final URI channelAddress;
            try {
                if (proxy == null) {
                    channelAddress = new URI(request.url());
                } else if (proxy.address() instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) proxy.address();
                    String scheme = address.getPort() == 443
                            ? "https"
                            : "http";

                    String channelAddressString = scheme + "://" + address.getHostString() + ":" + address.getPort();
                    channelAddress = new URI(channelAddressString);
                } else {
                    throw new IllegalArgumentException(
                            "SocketAddress on java.net.Proxy must be an InetSocketAddress. Found proxy: " + proxy);
                }

                request.withHeader(io.netty.handler.codec.http.HttpHeaders.Names.HOST, channelAddress.getHost());
                request.withHeader(io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION, io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE);
            } catch (URISyntaxException e) {
                return Single.error(e);
            }

            // Creates cold observable from an emitter
            return Single.create(new SingleOnSubscribe<HttpResponse>() {
                @Override
                public void subscribe(final SingleEmitter<HttpResponse> responseEmitter) {
                    channelPool.acquire(channelAddress).addListener(new GenericFutureListener<Future<? super Channel>>() {
                        @Override
                        public void operationComplete(Future<? super Channel> cf) {
                            if (!cf.isSuccess()) {
                                if (!responseEmitter.isDisposed()) {
                                    responseEmitter.onError(cf.cause());
                                }

                                return;
                            }

                            final Channel channel = (Channel) cf.getNow();
                            final HttpClientInboundHandler inboundHandler = channel.pipeline().get(HttpClientInboundHandler.class);

                            if (responseEmitter.isDisposed()) {
                                channel.close();
                                channelPool.release(channel);
                                return;
                            }

                            inboundHandler.didEmitHttpResponse = false;
                            inboundHandler.responseEmitter = responseEmitter;
                            responseEmitter.setDisposable(new Disposable() {
                                boolean isDisposed = false;
                                @Override
                                public void dispose() {
                                    isDisposed = true;
                                    if (!inboundHandler.didEmitHttpResponse) {
                                        channel.close();
                                        channelPool.release(channel);
                                    }
                                }

                                @Override
                                public boolean isDisposed() {
                                    return isDisposed;
                                }
                            });

                            if (request.httpMethod().equalsIgnoreCase("HEAD")) {
                                // Use HttpClientCodec for HEAD operations
                                if (channel.pipeline().get("HttpClientCodec") == null) {
                                    channel.pipeline().remove(HttpRequestEncoder.class);
                                    channel.pipeline().replace(HttpResponseDecoder.class, "HttpClientCodec", new HttpClientCodec());
                                }
                            } else {
                                // Use HttpResponseDecoder for other operations
                                if (channel.pipeline().get("HttpResponseDecoder") == null) {
                                    channel.pipeline().replace(HttpClientCodec.class, "HttpResponseDecoder", new HttpResponseDecoder());
                                    channel.pipeline().addAfter("HttpResponseDecoder", "HttpRequestEncoder", new HttpRequestEncoder());
                                }
                            }

                            final DefaultHttpRequest raw = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                    HttpMethod.valueOf(request.httpMethod()),
                                    request.url());

                            for (HttpHeader header : request.headers()) {
                                raw.headers().add(header.name(), header.value());
                            }

                            channel.write(raw).addListener(new GenericFutureListener<Future<? super Void>>() {
                                @Override
                                public void operationComplete(Future<? super Void> future) throws Exception {
                                    if (!future.isSuccess() && !responseEmitter.isDisposed()) {
                                        responseEmitter.onError(future.cause());
                                    }
                                }
                            });

                            if (request.body() == null) {
                                channel.writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT)
                                        .addListener(new GenericFutureListener<Future<? super Void>>() {
                                            @Override
                                            public void operationComplete(Future<? super Void> future) throws Exception {
                                                if (future.isSuccess()) {
                                                    channel.read();
                                                } else {
                                                    responseEmitter.onError(future.cause());
                                                }
                                            }
                                        });
                            } else {
                                Flowable<ByteBuf> bodyContent;
                                if (request.body() instanceof FileRequestBody) {
                                    bodyContent = ((FileRequestBody) request.body()).pooledContent();
                                } else {
                                    bodyContent = request.body().content().map(new Function<byte[], ByteBuf>() {
                                        @Override
                                        public ByteBuf apply(byte[] bytes) throws Exception {
                                            return Unpooled.wrappedBuffer(bytes);
                                        }
                                    });
                                }
                                bodyContent.subscribeOn(Schedulers.io()).subscribe(new FlowableSubscriber<ByteBuf>() {
                                    Subscription subscription;
                                    @Override
                                    public void onSubscribe(Subscription s) {
                                        subscription = s;
                                        inboundHandler.requestContentSubscription = subscription;
                                        subscription.request(1);
                                    }

                                    GenericFutureListener<Future<? super Void>> onChannelWriteComplete =
                                            new GenericFutureListener<Future<? super Void>>() {
                                                @Override
                                                public void operationComplete(Future<? super Void> future) throws Exception {
                                                    if (!future.isSuccess()) {
                                                        subscription.cancel();
                                                        if (!responseEmitter.isDisposed()) {
                                                            responseEmitter.onError(future.cause());
                                                        }
                                                    }
                                                }
                                            };

                                    @Override
                                    public void onNext(ByteBuf buf) {
                                        channel.writeAndFlush(new DefaultHttpContent(buf))
                                                .addListener(onChannelWriteComplete);

                                        if (channel.isWritable()) {
                                            subscription.request(1);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        if (!responseEmitter.isDisposed()) {
                                            responseEmitter.onError(t);
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        channel.writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT)
                                                .addListener(new GenericFutureListener<Future<? super Void>>() {
                                                    @Override
                                                    public void operationComplete(Future<? super Void> future) throws Exception {
                                                        if (!future.isSuccess()) {
                                                            subscription.cancel();
                                                            if (!responseEmitter.isDisposed()) {
                                                                responseEmitter.onError(future.cause());
                                                            }
                                                        } else {
                                                            channel.read();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    private static final class ResponseContentFlowable extends Flowable<ByteBuf> {
        final Queue<HttpContent> queuedContent = new ArrayDeque<>();
        final Subscription handlerSubscription;
        long chunksRequested = 0;
        Subscriber<? super ByteBuf> subscriber;

        ResponseContentFlowable(Subscription subscription) {
            handlerSubscription = subscription;
        }

        long chunksRequested() {
            return chunksRequested;
        }

        @Override
        protected void subscribeActual(Subscriber<? super ByteBuf> s) {
            if (subscriber != null) {
                throw new IllegalStateException("Multiple subscription not allowed for response content.");
            }

            subscriber = s;

            s.onSubscribe(new Subscription() {
                @Override
                public void request(long l) {
                    chunksRequested += l;


                    while (!queuedContent.isEmpty() && chunksRequested > 0) {
                        emitContent(queuedContent.remove());
                    }

                    handlerSubscription.request(l);
                }

                @Override
                public void cancel() {
                    handlerSubscription.cancel();
                }
            });
        }

        private void emitContent(HttpContent data) {
            subscriber.onNext(data.content());
            if (data instanceof LastHttpContent) {
                subscriber.onComplete();
            }
            chunksRequested--;
        }

        void onReceivedContent(HttpContent data) {
            if (subscriber != null && chunksRequested > 0) {
                emitContent(data);
            } else {
                queuedContent.add(data);
            }
        }

        void onError(Throwable cause) {
            subscriber.onError(cause);
        }
    }

    private static final class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {
        private SingleEmitter<HttpResponse> responseEmitter;
        private ResponseContentFlowable contentEmitter;
        private Subscription requestContentSubscription;
        private final NettyAdapter adapter;
        private boolean didEmitHttpResponse;

        private HttpClientInboundHandler(NettyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            adapter.channelPool.release(ctx.channel());
            if (contentEmitter != null) {
                contentEmitter.onError(cause);
            } else if (responseEmitter != null && !responseEmitter.isDisposed()) {
                responseEmitter.onError(cause);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            if (contentEmitter != null && contentEmitter.chunksRequested() != 0) {
                ctx.channel().read();
            }
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            if (ctx.channel().isWritable()) {
                requestContentSubscription.request(1);
            }

            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof io.netty.handler.codec.http.HttpResponse) {
                io.netty.handler.codec.http.HttpResponse response = (io.netty.handler.codec.http.HttpResponse) msg;

                if (response.decoderResult().isFailure()) {
                    exceptionCaught(ctx, response.decoderResult().cause());
                    return;
                }

                contentEmitter = new ResponseContentFlowable(new Subscription() {
                    @Override
                    public void request(long n) {
                        ctx.channel().read();
                    }

                    @Override
                    public void cancel() {
                        if (contentEmitter != null) {
                            // FIXME: still seem to be getting when disconnecting channel this way:
                            // "An existing connection was forcibly closed by the remote host"
                            ctx.channel().disconnect();
                            contentEmitter = null;
                        }
                    }
                });

                // Prevents channel from being closed when the Single<HttpResponse> is disposed
                didEmitHttpResponse = true;
                responseEmitter.onSuccess(new NettyResponse(response, contentEmitter));
            }

            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                // channelRead can still come through even after a Subscription.cancel event
                if (contentEmitter != null) {
                    contentEmitter.onReceivedContent(content);
                }
            }

            if (msg instanceof LastHttpContent) {
                contentEmitter = null;
                adapter.channelPool.release(ctx.channel());
            }
        }
    }

    /**
     * The factory for creating a NettyClient.
     */
    public static class Factory implements HttpClient.Factory {
        private final NettyAdapter adapter;

        /**
         * Create a Netty client factory with default settings.
         */
        public Factory() {
            this.adapter = new NettyAdapter();
        }

        /**
         * Create a Netty client factory, specifying the event loop group
         * size and the channel pool size.
         * @param eventLoopGroupSize the number of event loop executors
         * @param channelPoolSize the number of pooled channels (connections)
         */
        public Factory(int eventLoopGroupSize, int channelPoolSize) {
            this.adapter = new NettyAdapter(eventLoopGroupSize, channelPoolSize);
        }

        @Override
        public HttpClient create(final Configuration configuration) {
            return new NettyClient(configuration, adapter);
        }
    }
}
