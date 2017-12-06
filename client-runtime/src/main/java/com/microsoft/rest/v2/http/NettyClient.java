/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.reactivex.*;
import io.reactivex.functions.LongConsumer;
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
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
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
                public void subscribe(final SingleEmitter<HttpResponse> emitter) {
                    channelPool.acquire(channelAddress).addListener(new GenericFutureListener<Future<? super Channel>>() {
                        @Override
                        public void operationComplete(Future<? super Channel> cf) {
                            if (!cf.isSuccess()) {
                                emitter.onError(cf.cause());
                                return;
                            }

                            try {
                                final Channel channel = (Channel) cf.getNow();

                                HttpClientInboundHandler inboundHandler = channel.pipeline().get(HttpClientInboundHandler.class);
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
                                inboundHandler.responseEmitter = emitter;

                                final DefaultFullHttpRequest raw;
                                if (request.body() == null || request.body().contentLength() == 0) {
                                    raw = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                            HttpMethod.valueOf(request.httpMethod()),
                                            request.url());
                                } else {
                                    ByteBuf requestContent;
                                    if (request.body() instanceof ByteArrayHttpRequestBody) {
                                        requestContent = Unpooled.wrappedBuffer(((ByteArrayHttpRequestBody) request.body()).content());
                                    } else if (request.body() instanceof FileRequestBody) {
                                        FileSegment segment = ((FileRequestBody) request.body()).content();
                                        requestContent = ByteBufAllocator.DEFAULT.buffer(segment.length());
                                        requestContent.writeBytes(segment.fileChannel(), segment.offset(), segment.length());
                                    } else {
                                        throw new IllegalArgumentException("Only ByteArrayRequestBody or FileRequestBody are supported");
                                    }
                                    raw = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                            HttpMethod.valueOf(request.httpMethod()),
                                            request.url(),
                                            requestContent);
                                }

                                for (HttpHeader header : request.headers()) {
                                    raw.headers().add(header.name(), header.value());
                                }
                                raw.headers().set(HEADER_CONTENT_LENGTH, raw.content().readableBytes());
                                channel.writeAndFlush(raw).addListener(new GenericFutureListener<Future<? super Void>>() {
                                    @Override
                                    public void operationComplete(Future<? super Void> v) throws Exception {
                                        if (v.isSuccess()) {
                                            channel.read();
                                        } else {
                                            emitter.onError(v.cause());
                                        }
                                    }
                                });
                            } catch (Throwable t) {
                                emitter.onError(t);
                            }
                        }
                    });
                }
            });
        }
    }

    private static final class HttpContentFlowable extends Flowable<ByteBuf> {
        final Queue<HttpContent> queuedContent = new ArrayDeque<>();
        final Subscription handlerSubscription;
        long chunksRequested = 0;
        Subscriber<? super ByteBuf> subscriber;

        HttpContentFlowable(Subscription subscription) {
            handlerSubscription = subscription;
        }

        long chunksRequested() { return chunksRequested; }

        @Override
        protected void subscribeActual(Subscriber<? super ByteBuf> s) {
            if (subscriber != null) {
                throw new IllegalStateException("Multiple subscription not allowed for response content.");
            }

            subscriber = s;

            s.onSubscribe(new Subscription() {
                @Override
                public void request(long l) {
                    // TODO: does this need a lock?
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

        // TODO: onError
    }

    private static final class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {
        private HttpContentFlowable contentEmitter;
        private SingleEmitter<HttpResponse> responseEmitter;
        private NettyAdapter adapter;

        private HttpClientInboundHandler(NettyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            adapter.channelPool.release(ctx.channel());
            // TODO: need to emit error using the appropriate emitter depending on whether a response has been given yet
            responseEmitter.onError(cause);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//            System.out.println("channelReadComplete");
            if (contentEmitter != null && contentEmitter.chunksRequested() != 0) {
                System.out.println("ctx.channel().read()");
                ctx.channel().read();
            }
        }

        @Override
        public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
//            System.out.println("channelRead");
            if (msg instanceof io.netty.handler.codec.http.HttpResponse) {
                io.netty.handler.codec.http.HttpResponse response = (io.netty.handler.codec.http.HttpResponse) msg;

                if (response.decoderResult().isFailure()) {
                    exceptionCaught(ctx, response.decoderResult().cause());
                    return;
                }

                contentEmitter = new HttpContentFlowable(new Subscription() {
                    @Override
                    public void request(long n) {
                        System.out.println("channelRead.Subscription.request()");
                        ctx.channel().read();
                    }

                    @Override
                    public void cancel() {
                        System.out.println("channelRead.Subscription.cancel()");
                        ctx.channel().close();
                    }
                });

                responseEmitter.onSuccess(new NettyResponse(response, contentEmitter));
            }

            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                contentEmitter.onReceivedContent(content);
            }

            if (msg instanceof LastHttpContent) {
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
