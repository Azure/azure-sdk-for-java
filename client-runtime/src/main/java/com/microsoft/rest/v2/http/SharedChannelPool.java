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
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.reactivex.annotations.Nullable;
import io.reactivex.exceptions.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Netty channel pool implementation shared between multiple requests.
 *
 * Requests with the same host, port, and scheme share the same internal
 * pool. All the internal pools for all the requests have a fixed size limit.
 * This channel pool should be shared between multiple Netty adapters.
 */
class SharedChannelPool implements ChannelPool {
    private static final AttributeKey<URI> CHANNEL_URI = AttributeKey.newInstance("channel-uri");
    private static final AttributeKey<ZonedDateTime> CHANNEL_AVAILABLE_SINCE = AttributeKey.newInstance("channel-available-since");
    private static final AttributeKey<ZonedDateTime> CHANNEL_LEASED_SINCE = AttributeKey.newInstance("channel-leased-since");
    private static final AttributeKey<ZonedDateTime> CHANNEL_CREATED_SINCE = AttributeKey.newInstance("channel-created-since");
    private static final AttributeKey<ZonedDateTime> CHANNEL_CLOSED_SINCE = AttributeKey.newInstance("channel-closed-since");
    private final Bootstrap bootstrap;
    private final ChannelPoolHandler handler;
    private final int poolSize;
    private final AtomicInteger channelCount = new AtomicInteger(0);
    private final SharedChannelPoolOptions poolOptions;
    private final Queue<ChannelRequest> requests;
    private final ConcurrentMultiHashMap<URI, Channel> available;
    private final ConcurrentMultiHashMap<URI, Channel> leased;
    private final Object sync = new Object();
    private final SslContext sslContext;
    private final ExecutorService executor;
    private volatile boolean closed = false;
    private final Logger logger = LoggerFactory.getLogger(SharedChannelPool.class);

    private boolean isChannelHealthy(Channel channel) {
        try {
            if (!channel.isActive()) {
                return false;
            } else if (channel.pipeline().get("HttpResponseDecoder") == null && channel.pipeline().get("HttpClientCodec") == null) {
                return false;
            } else {
                ZonedDateTime channelAvailableSince = channel.attr(CHANNEL_AVAILABLE_SINCE).get();
                if (channelAvailableSince == null) {
                    channelAvailableSince = channel.attr(CHANNEL_LEASED_SINCE).get();
                }
                final long channelIdleDurationInSec = ChronoUnit.SECONDS.between(channelAvailableSince, ZonedDateTime.now(ZoneOffset.UTC));
                return channelIdleDurationInSec < this.poolOptions.idleChannelKeepAliveDurationInSec();
            }
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Creates an instance of the shared channel pool.
     * @param bootstrap the bootstrap to create channels
     * @param handler the handler to apply to the channels on creation, acquisition and release
     * @param size the upper limit of total number of channels
     * @param options optional settings for the pool
     */
    SharedChannelPool(final Bootstrap bootstrap, final ChannelPoolHandler handler, int size, SharedChannelPoolOptions options) {
        this.poolOptions = options.clone();
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
            sslContext = SslContextBuilder.forClient().build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SharedChannelPool-worker");
            thread.setDaemon(true);
            return thread;
        });

        executor.submit(() -> {
            while (!closed) {
                try {
                    final ChannelRequest request;
                    // Synchronizing just to be notified when requests is non-empty
                    synchronized (requests) {
                        while (requests.isEmpty() && !closed) {
                            requests.wait();
                        }
                    }
                    // requests must be non-empty based on the above condition
                    request = requests.remove();

                    synchronized (sync) {
                        while (channelCount.get() >= poolSize && available.size() == 0 && !closed) {
                            sync.wait();
                        }

                        if (closed) {
                            break;
                        }

                        // Try to retrieve a healthy channel from pool
                        boolean foundHealthyChannelInPool = false;
                        while (available.containsKey(request.channelURI)) {
                            Channel channel = available.poll(request.channelURI);
                            if (isChannelHealthy(channel)) {
                                handler.channelAcquired(channel);
                                request.promise.setSuccess(channel);
                                leased.put(request.channelURI, channel);
                                foundHealthyChannelInPool = true;
                                channel.attr(CHANNEL_LEASED_SINCE).set(ZonedDateTime.now(ZoneOffset.UTC));
                                logger.debug("Channel picked up from pool: {}", channel.id());
                                break;
                            } else {
                                logger.debug("Channel disposed from pool due to timeout or half closure: {}", channel.id());
                                closeChannel(channel);
                            }
                        }
                        if (!foundHealthyChannelInPool) {
                            // Not found a healthy channel in pool. Create a new channel - remove an available one if size overflows
                            while (available.size() > 0 && channelCount.get() >= poolSize) {
                                Channel nextAvailable = available.poll();
                                logger.debug("Channel disposed due to overflow: {}", nextAvailable.id());
                                closeChannel(nextAvailable);
                            }
                            int port;
                            if (request.destinationURI.getPort() < 0) {
                                port = "https".equals(request.destinationURI.getScheme()) ? 443 : 80;
                            } else {
                                port = request.destinationURI.getPort();
                            }
                            channelCount.incrementAndGet();
                            SharedChannelPool.this.bootstrap.clone().connect(request.destinationURI.getHost(), port).addListener((ChannelFuture f) -> {
                                if (f.isSuccess()) {
                                    Channel channel = f.channel();
                                    channel.attr(CHANNEL_URI).set(request.channelURI);

                                    // Apply SSL handler for https connections
                                    if ("https".equalsIgnoreCase(request.destinationURI.getScheme())) {
                                        channel.pipeline().addFirst(sslContext.newHandler(channel.alloc(), request.destinationURI.getHost(), port));
                                    }

                                    if (request.proxy != null) {
                                        channel.pipeline().addFirst("HttpProxyHandler", new HttpProxyHandler(request.proxy.address()));
                                    }

                                    leased.put(request.channelURI, channel);
                                    channel.attr(CHANNEL_CREATED_SINCE).set(ZonedDateTime.now(ZoneOffset.UTC));
                                    channel.attr(CHANNEL_LEASED_SINCE).set(ZonedDateTime.now(ZoneOffset.UTC));
                                    logger.debug("Channel created: {}", channel.id());
                                    handler.channelAcquired(channel);
                                    request.promise.setSuccess(channel);
                                } else {
                                    request.promise.setFailure(f.cause());
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    throw Exceptions.propagate(e);
                }
            }
        });
    }

    /**
     * Creates an instance of the shared channel pool.
     * @param bootstrap the bootstrap to create channels
     * @param handler the handler to apply to the channels on creation, acquisition and release
     * @param size the upper limit of total number of channels
     */
    SharedChannelPool(final Bootstrap bootstrap, final ChannelPoolHandler handler, int size) {
        this(bootstrap, handler, size, new SharedChannelPoolOptions());
    }

    /**
     * Acquire a channel for a URI.
     * @param uri the URI the channel acquired should be connected to
     * @return the future to a connected channel
     */
    public Future<Channel> acquire(URI uri, @Nullable Proxy proxy) {
        return this.acquire(uri, proxy, this.bootstrap.config().group().next().<Channel>newPromise());
    }

    /**
     * Acquire a channel for a URI.
     * @param uri the URI the channel acquired should be connected to
     * @param promise the writable future to a connected channel
     * @return the future to a connected channel
     */
    public Future<Channel> acquire(URI uri, @Nullable Proxy proxy, final Promise<Channel> promise) {
        if (closed) {
            throw new RejectedExecutionException("SharedChannelPool is closed");
        }

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.promise = promise;
        channelRequest.proxy = proxy;
        int port;
        if (uri.getPort() < 0) {
            port = "https".equals(uri.getScheme()) ? 443 : 80;
        } else {
            port = uri.getPort();
        }
        try {
            channelRequest.destinationURI = new URI(String.format("%s://%s:%d", uri.getScheme(), uri.getHost(), port));

            if (proxy == null) {
                channelRequest.channelURI = channelRequest.destinationURI;
            } else {
                InetSocketAddress address = (InetSocketAddress) proxy.address();
                channelRequest.channelURI = new URI(String.format("%s://%s:%d", uri.getScheme(), address.getHostString(), address.getPort()));
            }

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

    private Future<Void> closeChannel(final Channel channel) {
        channel.attr(CHANNEL_CLOSED_SINCE).set(ZonedDateTime.now(ZoneOffset.UTC));
        logger.debug("Channel initiated to close: " + channel.id());
        // Closing a channel doesn't change the channel count
        return channel.close().addListener(f -> {
            if (!f.isSuccess()) {
                logger.warn("Possible channel leak: failed to close " + channel.id(), f.cause());
            }
        });
    }

    /**
     * Closes the channel and releases it back to the pool.
     * @param channel the channel to close and release.
     * @return a Future representing the operation.
     */
    public Future<Void> closeAndRelease(final Channel channel) {
        return closeChannel(channel).addListener(future -> {
            synchronized (sync) {
                leased.remove(channel.attr(CHANNEL_URI).get(), channel);
                channelCount.decrementAndGet();
                logger.debug("Channel closed and released out of pool: " + channel.id());
                sync.notify();
            }
        });
    }

    @Override
    public Future<Void> release(final Channel channel) {
        try {
            handler.channelReleased(channel);
            synchronized (sync) {
                leased.remove(channel.attr(CHANNEL_URI).get(), channel);
                if (isChannelHealthy(channel)) {
                    available.put(channel.attr(CHANNEL_URI).get(), channel);
                    channel.attr(CHANNEL_AVAILABLE_SINCE).set(ZonedDateTime.now(ZoneOffset.UTC));
                    logger.debug("Channel released to pool: " + channel.id());
                } else {
                    channelCount.decrementAndGet();
                    logger.debug("Channel broken on release, dispose: " + channel.id());
                }
                sync.notify();
            }
        } catch (Exception e) {
            return bootstrap.config().group().next().newFailedFuture(e);
        }
        return bootstrap.config().group().next().newSucceededFuture(null);
    }

    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {
        return release(channel).addListener(f -> {
            if (f.isSuccess()) {
                promise.setSuccess(null);
            } else {
                promise.setFailure(f.cause());
            }
        });
    }

    @Override
    public void close() {
        closed = true;
        executor.shutdownNow();
        synchronized (requests) {
            while (!requests.isEmpty()) {
                requests.remove().promise.setFailure(new CancellationException("Channel pool was closed"));
            }
        }
    }

    private static class ChannelRequest {
        private URI destinationURI;
        private URI channelURI;
        private Proxy proxy;
        private Promise<Channel> promise;
    }

    /**
     * Used to print a current overview of the channels in this pool.
     */
    public void dump() {
        synchronized (sync) {
            logger.info(String.format("---- %s: size %d, keep alive (sec) %d ----", toString(), poolSize, poolOptions.idleChannelKeepAliveDurationInSec()));
            logger.info("Channel\tState\tFor\tAge\tURL");
            List<Channel> closed = new ArrayList<>();
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            for (Channel channel : leased.values()) {
                if (channel.hasAttr(CHANNEL_CLOSED_SINCE)) {
                    closed.add(channel);
                    continue;
                }
                long stateFor = ChronoUnit.SECONDS.between(channel.attr(CHANNEL_LEASED_SINCE).get(), now);
                long age = ChronoUnit.SECONDS.between(channel.attr(CHANNEL_CREATED_SINCE).get(), now);
                logger.info(String.format("%s\tLEASE\t%ds\t%ds\t%s", channel.id(), stateFor, age, channel.attr(CHANNEL_URI).get()));
            }
            for (Channel channel : available.values()) {
                if (channel.hasAttr(CHANNEL_CLOSED_SINCE)) {
                    closed.add(channel);
                    continue;
                }
                long stateFor = ChronoUnit.SECONDS.between(channel.attr(CHANNEL_AVAILABLE_SINCE).get(), now);
                long age = ChronoUnit.SECONDS.between(channel.attr(CHANNEL_CREATED_SINCE).get(), now);
                logger.info(String.format("%s\tAVAIL\t%ds\t%ds\t%s", channel.id(), stateFor, age, channel.attr(CHANNEL_URI).get()));
            }
            for (Channel channel : closed) {
                long stateFor = ChronoUnit.SECONDS.between(channel.attr(CHANNEL_CLOSED_SINCE).get(), now);
                long age = ChronoUnit.SECONDS.between(channel.attr(CHANNEL_CREATED_SINCE).get(), now);
                logger.info(String.format("%s\tCLOSE\t%ds\t%ds\t%s", channel.id(), stateFor, age, channel.attr(CHANNEL_URI).get()));
            }
            logger.info("Active channels: " + channelCount.get() + " Leaked channels: " + (channelCount.get() - leased.size() - available.size()));
        }
    }
}
