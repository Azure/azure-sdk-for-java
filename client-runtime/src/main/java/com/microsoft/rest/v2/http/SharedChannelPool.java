/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Netty channel pool implementation shared between multiple requests.
 *
 * Requests with the same host, port, and scheme share the same internal
 * pool. All the internal pools for all the requests have a fixed size limit.
 * This channel pool should be shared between multiple Netty adapters.
 */
public class SharedChannelPool implements ChannelPool {
    private static final AttributeKey<URI> CHANNEL_URI = AttributeKey.newInstance("channel-uri");
    private final Bootstrap bootstrap;
    private final ChannelPoolHandler handler;
    private boolean closed = false;
    private final int poolSize;
    private final Queue<ChannelRequest> requests;
    private final ConcurrentMultiHashMap<URI, Channel> available;
    private final ConcurrentMultiHashMap<URI, Channel> leased;
    private final Object sync = -1;
    private final SslContext sslContext;
    private final ExecutorService executor;

    EventLoopGroup eventLoopGroup() {
        return bootstrap.config().group();
    }

    /**
     * Creates an instance of the shared channel pool.
     * @param bootstrap the bootstrap to create channels
     * @param handler the handler to apply to the channels on creation, acquisition and release
     * @param size the upper limit of total number of channels
     */
    public SharedChannelPool(final Bootstrap bootstrap, final ChannelPoolHandler handler, int size) {
        this.bootstrap = bootstrap.clone().handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                assert ch.eventLoop().inEventLoop();
                handler.channelCreated(ch);
            }
        });
        this.handler = handler;
        this.poolSize = size;
        this.requests = new ConcurrentLinkedDeque<>();
        this.available = new ConcurrentMultiHashMap<>();
        this.leased = new ConcurrentMultiHashMap<>();
        try {
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
        this.executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (!closed) {
                    try {
                        final ChannelRequest request;
                        final ChannelFuture channelFuture;
                        synchronized (requests) {
                            while (requests.isEmpty() && !closed) {
                                requests.wait(1000);
                            }
                        }
                        request = requests.poll();

                        if (leased.size() >= poolSize && !closed) {
                            synchronized (sync) {
                                sync.wait();
                            }
                        }
                        if (closed) {
                            break;
                        }
                        synchronized (sync) {
                            if (available.containsKey(request.uri)) {
                                Channel channel = available.poll(request.uri);
                                handler.channelAcquired(channel);
                                request.promise.setSuccess(channel);
                                leased.put(request.uri, channel);
                            } else {
                                // Create a new channel - remove an available one if size overflows
                                if (available.size() > 0 && available.size() + leased.size() >= poolSize) {
                                    available.poll().close();
                                }
                                int port;
                                if (request.uri.getPort() < 0) {
                                    port = "https".equals(request.uri.getScheme()) ? 443 : 80;
                                } else {
                                    port = request.uri.getPort();
                                }
                                channelFuture = SharedChannelPool.this.bootstrap.clone().connect(request.uri.getHost(), port);

                                channelFuture.channel().attr(CHANNEL_URI).set(request.uri);

                                // Apply SSL handler for https connections
                                if ("https".equalsIgnoreCase(request.uri.getScheme())) {
                                    channelFuture.channel().pipeline().addFirst(sslContext.newHandler(channelFuture.channel().alloc(), request.uri.getHost(), port));
                                }

                                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                                    @Override
                                    public void operationComplete(Future<? super Void> future) throws Exception {
                                        if (channelFuture.isSuccess()) {
                                            handler.channelAcquired(channelFuture.channel());
                                            leased.put(request.uri, channelFuture.channel());
                                            request.promise.setSuccess(channelFuture.channel());
                                        } else {
                                            request.promise.setFailure(channelFuture.cause());
                                            throw new RuntimeException(channelFuture.cause());
                                        }
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * Acquire a channel for a URI.
     * @param uri the URI the channel acquired should be connected to
     * @return the future to a connected channel
     */
    public Future<Channel> acquire(URI uri) {
        return this.acquire(uri, this.bootstrap.config().group().next().<Channel>newPromise());
    }

    /**
     * Acquire a channel for a URI.
     * @param uri the URI the channel acquired should be connected to
     * @param promise the writable future to a connected channel
     * @return the future to a connected channel
     */
    public Future<Channel> acquire(URI uri, final Promise<Channel> promise) {
        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.promise = promise;
        int port;
        if (uri.getPort() < 0) {
            port = "https".equals(uri.getScheme()) ? 443 : 80;
        } else {
            port = uri.getPort();
        }
        try {
            channelRequest.uri = new URI(String.format("%s://%s:%d", uri.getScheme(), uri.getHost(), port));
            requests.add(channelRequest);
            synchronized (requests) {
                requests.notify();
            }
        } catch (URISyntaxException e) {
            promise.setFailure(e);
        }
        return channelRequest.promise;
    }

    @Override
    public Future<Channel> acquire() {
        throw new UnsupportedOperationException("Please pass host & port to shared channel pool.");
    }

    @Override
    public Future<Channel> acquire(Promise<Channel> promise) {
        throw new UnsupportedOperationException("Please pass host & port to shared channel pool.");
    }

    @Override
    public Future<Void> release(final Channel channel) {
        return this.release(channel, this.bootstrap.config().group().next().<Void>newPromise());
    }

    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {
        try {
            handler.channelReleased(channel);
        } catch (Exception e) {
            promise.setFailure(e);
            return promise;
        }
        promise.setSuccess(null);
        synchronized (sync) {
            leased.remove(channel.attr(CHANNEL_URI).get(), channel);
            available.put(channel.attr(CHANNEL_URI).get(), channel);
            sync.notify();
        }
        return promise;
    }

    @Override
    public void close() {
        closed = true;
        executor.shutdown();
        synchronized (sync) {
            for (Channel channel : leased.values()) {
                channel.close();
            }
            for (Channel channel : available.values()) {
                channel.close();
            }
        }
    }

    private static class ChannelRequest {
        private URI uri;
        private Promise<Channel> promise;
    }
}
