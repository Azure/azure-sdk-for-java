// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROXY;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL_INITIALIZER;

/**
 * A pool of Netty channels that can be reused for requests to the same remote address.
 */
public final class Netty4ConnectionPool implements Closeable {

    /**
     * An attribute key to mark a channel as new, so that certain handlers (e.g. proxy, ssl)
     * are only added once.
     */
    public static final AttributeKey<AtomicBoolean> NEW_CHANNEL_KEY = AttributeKey.valueOf("new-channel");
    private static final AttributeKey<OffsetDateTime> CHANNEL_CREATION_TIME
        = AttributeKey.valueOf("channel-creation-time");
    private static final AttributeKey<Netty4ConnectionPoolKey> CONNECTION_POOL_KEY
        = AttributeKey.valueOf("connection-pool-key");

    private static final ClientLogger LOGGER = new ClientLogger(Netty4ConnectionPool.class);
    private static final String CLOSED_POOL_ERROR_MESSAGE = "Connection pool has been closed.";

    private final ConcurrentMap<Netty4ConnectionPoolKey, PerRoutePool> pool = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, OffsetDateTime> channelIdleSince = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Bootstrap bootstrap;
    private final int maxConnectionsPerRoute;
    private final long idleTimeoutNanos;
    private final long maxLifetimeNanos;
    private final Duration pendingAcquireTimeout;
    private final int maxPendingAcquires;
    private final Future<?> cleanupTask;

    private final ChannelInitializationProxyHandler channelInitializationProxyHandler;
    private final Consumer<SslContextBuilder> sslContextModifier;
    private final AtomicReference<List<AuthenticateChallenge>> proxyChallenges;
    private final HttpProtocolVersion maximumHttpVersion;

    public Netty4ConnectionPool(Bootstrap bootstrap,
        ChannelInitializationProxyHandler channelInitializationProxyHandler,
        Consumer<SslContextBuilder> sslContextModifier, int maxConnectionsPerRoute, Duration connectionIdleTimeout,
        Duration maxConnectionLifetime, Duration pendingAcquireTimeout, int maxPendingAcquires,
        HttpProtocolVersion maximumHttpVersion) {
        this.bootstrap = bootstrap;
        this.channelInitializationProxyHandler = channelInitializationProxyHandler;
        this.sslContextModifier = sslContextModifier;
        this.proxyChallenges = new AtomicReference<>();
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        this.idleTimeoutNanos = durationToNanos(connectionIdleTimeout);
        this.maxLifetimeNanos = durationToNanos(maxConnectionLifetime);
        this.pendingAcquireTimeout = pendingAcquireTimeout;
        this.maxPendingAcquires = maxPendingAcquires;
        this.maximumHttpVersion = maximumHttpVersion;

        if (this.idleTimeoutNanos > 0) {
            EventLoopGroup eventLoopGroup = bootstrap.config().group();
            // This scheduled task cleans up idle connections periodically.
            // The 30-second interval is a trade-off between precision and performance.
            // Running it more frequently would be more precise but add more overhead.
            // This means a connection may stay idle for up to (idleTimeout + 30s) before being closed,
            // which is an acceptable behavior for preventing resource leaks.
            this.cleanupTask
                = eventLoopGroup.scheduleAtFixedRate(this::cleanupIdleConnections, 30, 30, TimeUnit.SECONDS);
        } else {
            this.cleanupTask = null;
        }
    }

    /**
     * Acquires a channel for the given remote address from the pool.
     *
     * @param key The composite key representing the connection route.
     * @param isHttps Flag indicating whether connections for this route should be secured using TLS/SSL.
     * @return A {@link Future} that will be notified when a channel is acquired.
     * @throws IllegalStateException if the connection pool has been closed.
     */
    public Future<Channel> acquire(Netty4ConnectionPoolKey key, boolean isHttps) {
        if (closed.get()) {
            throw LOGGER.throwableAtError().log(CLOSED_POOL_ERROR_MESSAGE, IllegalStateException::new);
        }

        PerRoutePool perRoutePool = pool.computeIfAbsent(key, k -> new PerRoutePool(k, isHttps));
        return perRoutePool.acquire();
    }

    /**
     * Releases a channel back to the pool.
     * The channel pipeline must be cleaned of request-specific handlers before releasing.
     *
     * @param channel The channel to release.
     */
    public void release(Channel channel) {
        if (channel == null) {
            return;
        }
        if (closed.get()) {
            channel.close();
            return;
        }

        Netty4ConnectionPoolKey key = channel.attr(CONNECTION_POOL_KEY).get();
        if (key != null) {
            PerRoutePool perRoutePool = pool.get(key);
            if (perRoutePool != null) {
                channel.attr(CONNECTION_POOL_KEY).set(null);
                perRoutePool.release(channel);
            } else {
                channel.close();
            }
        } else {
            channel.close();
        }
    }

    /**
     * Periodically cleans up connections that have been idle for too long.
     */
    private void cleanupIdleConnections() {
        if (idleTimeoutNanos <= 0 || channelIdleSince.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (Iterator<Map.Entry<Channel, OffsetDateTime>> it = channelIdleSince.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Channel, OffsetDateTime> entry = it.next();
            if (Duration.between(entry.getValue(), now).toNanos() >= idleTimeoutNanos) {
                it.remove();
                if (entry.getKey().isActive()) {
                    entry.getKey().close();
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            if (cleanupTask != null) {
                cleanupTask.cancel(false);
            }
            pool.values().forEach(PerRoutePool::close);
            pool.clear();
            channelIdleSince.clear();
        }
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    private static long durationToNanos(Duration duration) {
        return (duration == null || duration.isNegative() || duration.isZero()) ? -1 : duration.toNanos();
    }

    /**
     * Manages connections and pending acquirers for a single route.
     */
    private class PerRoutePool {
        private final Deque<Channel> idleConnections = new ConcurrentLinkedDeque<>();
        private final Deque<Promise<Channel>> pendingAcquirers = new ConcurrentLinkedDeque<>();
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final Netty4ConnectionPoolKey key;
        private final SocketAddress route;
        private final boolean isHttps;

        PerRoutePool(Netty4ConnectionPoolKey key, boolean isHttps) {
            this.key = key;
            this.route = key.getConnectionTarget();
            this.isHttps = isHttps;
        }

        //TODO: Maybe this should be using CAS atomics and loops instead of synchronized
        // in case the http-netty4 is also used in async flows instead of just sync
        synchronized Future<Channel> acquire() {
            if (closed.get()) {
                return bootstrap.config()
                    .group()
                    .next()
                    .newFailedFuture(new IllegalStateException(CLOSED_POOL_ERROR_MESSAGE));
            }

            Channel channel;
            while ((channel = idleConnections.poll()) != null) {
                if (isHealthy(channel)) {
                    // Acquired an existing healthy connection. activeConnections count is not
                    // yet incremented for idle channels, so we do it here.
                    activeConnections.incrementAndGet();
                    channel.attr(NEW_CHANNEL_KEY).set(new AtomicBoolean(false));
                    channelIdleSince.remove(channel);
                    channel.attr(CONNECTION_POOL_KEY).set(key);
                    return channel.eventLoop().newSucceededFuture(channel);
                }
                // Unhealthy idle connection was found and discarded. Don't decrement activeConnections
                // as it was already decremented when the channel was released to the idle queue.
                channel.close();
            }

            // No idle connections available, create a new one.
            if (activeConnections.get() < maxConnectionsPerRoute) {
                return createNewConnection();
            }

            // Pool is full, queue the request if there is space.
            if (pendingAcquirers.size() >= maxPendingAcquires) {
                return bootstrap.config()
                    .group()
                    .next()
                    .newFailedFuture(CoreException.from("Pending acquisition queue is full."));
            }

            Promise<Channel> promise = bootstrap.config().group().next().newPromise();
            promise.addListener(future -> {
                if (future.isCancelled()) {
                    pendingAcquirers.remove(promise);
                }
            });
            pendingAcquirers.offer(promise);

            if (pendingAcquireTimeout != null) {
                bootstrap.config().group().schedule(() -> {
                    if (!promise.isDone()) {
                        promise.tryFailure(
                            CoreException.from("Connection acquisition timed out after " + pendingAcquireTimeout));
                    }
                }, pendingAcquireTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }
            return promise;
        }

        synchronized void release(Channel channel) {
            if (!isHealthy(channel)) {
                activeConnections.decrementAndGet();
                channel.close();

                // Since a connection slot has been freed, we should try to create a new,
                // healthy connection for any request that might be waiting.
                satisfyWaiterWithNewConnection();
                return;
            }

            // The channel is healthy. Now, check if anyone is waiting for a connection.
            Promise<Channel> waiter;
            while ((waiter = pendingAcquirers.poll()) != null) {
                // A waiter exists, hand over this channel directly.
                // The activeConnections count remains the same (one leaves, one joins).
                if (waiter.trySuccess(channel)) {
                    return;
                }
            }

            // Channel is healthy and no waiters, return it to the idle queue.
            activeConnections.decrementAndGet();
            idleConnections.offer(channel);
            if (idleTimeoutNanos > 0) {
                channelIdleSince.put(channel, OffsetDateTime.now(ZoneOffset.UTC));
            }
        }

        private void satisfyWaiterWithNewConnection() {
            // This method MUST be called from within a thread-safe block/method.
            if (activeConnections.get() < maxConnectionsPerRoute) {
                Promise<Channel> waiter = pendingAcquirers.poll();
                if (waiter != null) {
                    // A waiter exists, and we have capacity, create a new connection for them.
                    Future<Channel> newConnectionFuture = createNewConnection();
                    newConnectionFuture.addListener(future -> {
                        if (future.isSuccess()) {
                            waiter.trySuccess((Channel) future.getNow());
                        } else {
                            waiter.tryFailure(future.cause());
                        }
                    });
                }
            }
        }

        private Future<Channel> createNewConnection() {
            // This method MUST be called from within a thread-safe block/method.
            activeConnections.incrementAndGet();
            Bootstrap newConnectionBootstrap = bootstrap.clone();

            newConnectionBootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel channel) throws SSLException {
                    ChannelPipeline pipeline = channel.pipeline();
                    // Test whether proxying should be applied to this Channel. If so, add it.
                    // Proxy detection MUST use the final destination address from the key.
                    boolean hasProxy = channelInitializationProxyHandler.test(key.getFinalDestination());
                    if (hasProxy) {
                        ProxyHandler proxyHandler = channelInitializationProxyHandler.createProxy(proxyChallenges);
                        pipeline.addFirst(PROXY, proxyHandler);
                    }

                    // Add SSL handling if the request is HTTPS.
                    if (isHttps) {
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) key.getFinalDestination();
                        SslContext ssl = buildSslContext();
                        // SSL handling is added last here. This is done as proxying could require SSL handling too.
                        channel.pipeline()
                            .addLast(SSL, ssl.newHandler(channel.alloc(), inetSocketAddress.getHostString(),
                                inetSocketAddress.getPort()));
                        channel.pipeline().addLast(SSL_INITIALIZER, new Netty4SslInitializationHandler());
                    }
                }
            });

            Promise<Channel> promise = newConnectionBootstrap.config().group().next().newPromise();
            newConnectionBootstrap.connect(route).addListener(future -> {
                if (!future.isSuccess()) {
                    synchronized (this) {
                        activeConnections.decrementAndGet();
                        satisfyWaiterWithNewConnection();
                    }
                    promise.setFailure(future.cause());
                    return;
                }

                Channel newChannel = ((ChannelFuture) future).channel();
                ProxyHandler proxyHandler = (ProxyHandler) newChannel.pipeline().get(PROXY);

                if (proxyHandler != null) {
                    // Wait for the proxy handshake to complete if proxy is being used.
                    proxyHandler.connectFuture().addListener(proxyFuture -> {
                        if (proxyFuture.isSuccess()) {
                            if (!newChannel.isActive()) {
                                promise.setFailure(new ClosedChannelException());

                                synchronized (this) {
                                    activeConnections.decrementAndGet();
                                    satisfyWaiterWithNewConnection();
                                }
                                return;
                            }
                            newChannel.attr(NEW_CHANNEL_KEY).set(new AtomicBoolean(true));
                            newChannel.attr(CHANNEL_CREATION_TIME).set(OffsetDateTime.now(ZoneOffset.UTC));
                            newChannel.attr(CONNECTION_POOL_KEY).set(key);
                            promise.setSuccess(newChannel);
                        } else {
                            promise.setFailure(proxyFuture.cause());
                            newChannel.close();

                            synchronized (this) {
                                activeConnections.decrementAndGet();
                                satisfyWaiterWithNewConnection();
                            }
                        }
                    });
                } else {
                    newChannel.attr(NEW_CHANNEL_KEY).set(new AtomicBoolean(true));
                    newChannel.attr(CHANNEL_CREATION_TIME).set(OffsetDateTime.now(ZoneOffset.UTC));
                    newChannel.attr(CONNECTION_POOL_KEY).set(key);
                    promise.setSuccess(newChannel);
                }
            });
            return promise;
        }

        private SslContext buildSslContext() throws SSLException {
            SslContextBuilder sslContextBuilder
                = SslContextBuilder.forClient().endpointIdentificationAlgorithm("HTTPS");
            if (maximumHttpVersion == HttpProtocolVersion.HTTP_2) {
                // If HTTP/2 is the maximum version, we need to ensure that ALPN is enabled.
                SslProvider sslProvider = SslContext.defaultClientProvider();
                ApplicationProtocolConfig.SelectorFailureBehavior selectorBehavior;
                ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedBehavior;
                if (sslProvider == SslProvider.JDK) {
                    selectorBehavior = ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT;
                    selectedBehavior = ApplicationProtocolConfig.SelectedListenerFailureBehavior.FATAL_ALERT;
                } else {
                    // Netty OpenSslContext doesn't support FATAL_ALERT, use NO_ADVERTISE and ACCEPT
                    // instead.
                    selectorBehavior = ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE;
                    selectedBehavior = ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
                }

                sslContextBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(
                        new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN, selectorBehavior,
                            selectedBehavior, ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1));
            }
            if (sslContextModifier != null) {
                // Allow the caller to modify the SslContextBuilder before it is built.
                sslContextModifier.accept(sslContextBuilder);
            }

            return sslContextBuilder.build();
        }

        private boolean isHealthy(Channel channel) {
            if (!channel.isActive() || !channel.isWritable()) {
                return false;
            }

            if (maxLifetimeNanos > 0) {
                OffsetDateTime creationTime = channel.attr(CHANNEL_CREATION_TIME).get();
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                if (creationTime != null && Duration.between(creationTime, now).toNanos() >= maxLifetimeNanos) {
                    return false;
                }
            }
            return true;
        }

        synchronized void close() {
            Channel channel;
            while ((channel = idleConnections.poll()) != null) {
                channel.close();
            }
            Promise<Channel> waiter;
            while ((waiter = pendingAcquirers.poll()) != null) {
                waiter.tryFailure(new IOException(CLOSED_POOL_ERROR_MESSAGE));
            }
        }
    }

}
