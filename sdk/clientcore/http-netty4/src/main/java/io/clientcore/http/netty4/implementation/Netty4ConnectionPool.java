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
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
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
import static io.clientcore.http.netty4.implementation.Netty4Utility.buildSslContext;

/**
 * A pool of Netty channels that can be reused for requests to the same remote address.
 */
public class Netty4ConnectionPool implements Closeable {

    private static final AttributeKey<PooledConnection> POOLED_CONNECTION_KEY
        = AttributeKey.valueOf("pooled-connection-key");

    private static final ClientLogger LOGGER = new ClientLogger(Netty4ConnectionPool.class);
    private static final String CLOSED_POOL_ERROR_MESSAGE = "Connection pool has been closed.";

    private final ConcurrentMap<Netty4ConnectionPoolKey, PerRoutePool> pool = new ConcurrentHashMap<>();
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

        PooledConnection pooledConnection = channel.attr(POOLED_CONNECTION_KEY).get();
        if (pooledConnection == null) {
            channel.close();
            return;
        }

        if (closed.get()) {
            pooledConnection.close();
            return;
        }

        PerRoutePool perRoutePool = pool.get(pooledConnection.key);
        if (perRoutePool != null) {
            perRoutePool.release(pooledConnection);
        } else {
            pooledConnection.close();
        }
    }

    /**
     * Periodically cleans up connections that have been idle for too long.
     */
    private void cleanupIdleConnections() {
        if (idleTimeoutNanos <= 0 || pool.isEmpty()) {
            return;
        }

        for (PerRoutePool perRoutePool : pool.values()) {
            perRoutePool.cleanup();
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
        }
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    private static long durationToNanos(Duration duration) {
        return (duration == null || duration.isNegative() || duration.isZero()) ? -1 : duration.toNanos();
    }

    /**
     * A wrapper for a Netty Channel that holds pooling-related metadata.
     */
    private static final class PooledConnection {
        private final Channel channel;
        private final Netty4ConnectionPoolKey key;
        private final OffsetDateTime creationTime;
        private volatile OffsetDateTime idleSince;

        PooledConnection(Channel channel, Netty4ConnectionPoolKey key) {
            this.channel = channel;
            this.key = key;
            this.creationTime = OffsetDateTime.now(ZoneOffset.UTC);
            channel.attr(POOLED_CONNECTION_KEY).set(this);
        }

        private boolean isActiveAndWriteable() {
            return channel.isActive() && channel.isWritable();
        }

        private void close() {
            channel.close();
        }
    }

    /**
     * Manages connections and pending acquirers for a single route.
     */
    private class PerRoutePool {
        private final Deque<PooledConnection> idleConnections = new ConcurrentLinkedDeque<>();
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

        Future<Channel> acquire() {
            if (closed.get()) {
                return bootstrap.config()
                    .group()
                    .next()
                    .newFailedFuture(new IllegalStateException(CLOSED_POOL_ERROR_MESSAGE));
            }

            // First, optimistically try to acquire an existing idle connection.
            while (true) {
                PooledConnection connection = idleConnections.poll();
                if (connection == null) {
                    break;
                }

                if (isHealthy(connection)) {
                    // Acquired an existing healthy connection. activeConnections count is not
                    // yet incremented for idle channels, so we do it here.
                    activeConnections.incrementAndGet();
                    connection.idleSince = null; // Mark as active
                    return connection.channel.eventLoop().newSucceededFuture(connection.channel);
                }

                // Unhealthy idle connection was found and discarded. Don't decrement activeConnections
                // as it was already decremented when the channel was released to the idle queue.
                connection.close();
            }

            // No idle connections. Try to create a new one or queue the request.
            while (true) {
                int currentActive = activeConnections.get();
                if (currentActive < maxConnectionsPerRoute) {
                    // Try to reserve a slot for a new connection.
                    if (activeConnections.compareAndSet(currentActive, currentActive + 1)) {
                        return createNewConnection();
                    }
                    // CAS failed, another thread changed the count. Loop to retry.
                } else {
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
                                promise.tryFailure(CoreException
                                    .from("Connection acquisition timed out after " + pendingAcquireTimeout));
                            }
                        }, pendingAcquireTimeout.toMillis(), TimeUnit.MILLISECONDS);
                    }
                    return promise;
                }
            }
        }

        void release(PooledConnection connection) {
            if (!isHealthy(connection)) {
                activeConnections.decrementAndGet();
                connection.close();
                // A slot has been freed. Try to satisfy a waiting acquirer with a new connection.
                satisfyWaiterWithNewConnection();
                return;
            }

            // The channel is healthy. Now, check if anyone is waiting for a connection.
            while (true) {
                Promise<Channel> waiterToNotify = pendingAcquirers.poll();
                if (waiterToNotify == null) {
                    // No waiters, return the connection to the idle queue.
                    activeConnections.decrementAndGet();
                    connection.idleSince = OffsetDateTime.now(ZoneOffset.UTC);
                    idleConnections.offer(connection);
                    break;
                }

                // A waiter exists. Fulfill the promise. Active connection count remains the same.
                if (waiterToNotify.trySuccess(connection.channel)) {
                    // Waiter was notified successfully
                    return;
                }
                // If trySuccess fails, the waiter was cancelled. Loop again to find another waiter.
            }
        }

        private void satisfyWaiterWithNewConnection() {
            // This method is called when a connection slot is freed.
            while (true) {
                int currentActive = activeConnections.get();
                if (currentActive >= maxConnectionsPerRoute || pendingAcquirers.isEmpty()) {
                    return;
                }

                if (activeConnections.compareAndSet(currentActive, currentActive + 1)) {
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
                    } else {
                        // A waiter disappeared after we reserved a slot. Release the slot.
                        activeConnections.decrementAndGet();
                    }
                    return; // Exit after attempting to satisfy one waiter.
                }
                // CAS failed, another thread is operating. Loop to re-evaluate.
            }
        }

        private Future<Channel> createNewConnection() {
            Bootstrap newConnectionBootstrap = bootstrap.clone();
            Promise<Channel> promise = newConnectionBootstrap.config().group().next().newPromise();
            newConnectionBootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel channel) throws SSLException {
                    // Create the connection wrapper and attach it to the channel.
                    new PooledConnection(channel, key);

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
                        SslContext ssl = buildSslContext(maximumHttpVersion, sslContextModifier);
                        // SSL handling is added last here. This is done as proxying could require SSL handling too.
                        channel.pipeline()
                            .addLast(SSL, ssl.newHandler(channel.alloc(), inetSocketAddress.getHostString(),
                                inetSocketAddress.getPort()));
                        channel.pipeline().addLast(SSL_INITIALIZER, new Netty4SslInitializationHandler());
                    }
                }
            });

            newConnectionBootstrap.connect(route).addListener(future -> {
                if (!future.isSuccess()) {
                    LOGGER.atError().setThrowable(future.cause()).log("Failed connection.");
                    // Connect failed, release the slot and try to satisfy a waiter.
                    activeConnections.decrementAndGet();
                    satisfyWaiterWithNewConnection();
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
                                activeConnections.decrementAndGet();
                                satisfyWaiterWithNewConnection();
                                return;
                            }
                            promise.setSuccess(newChannel);
                        } else {
                            promise.setFailure(proxyFuture.cause());
                            newChannel.close();
                            activeConnections.decrementAndGet();
                            satisfyWaiterWithNewConnection();
                        }
                    });
                } else {
                    promise.setSuccess(newChannel);
                }
            });
            return promise;
        }

        private boolean isHealthy(PooledConnection connection) {
            if (!connection.isActiveAndWriteable()) {
                return false;
            }

            if (maxLifetimeNanos > 0) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                if (Duration.between(connection.creationTime, now).toNanos() >= maxLifetimeNanos) {
                    return false;
                }
            }
            return true;
        }

        void cleanup() {
            if (idleConnections.isEmpty()) {
                return;
            }

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            for (Iterator<PooledConnection> it = idleConnections.iterator(); it.hasNext();) {
                PooledConnection connection = it.next();
                if (connection.idleSince != null
                    && Duration.between(connection.idleSince, now).toNanos() >= idleTimeoutNanos) {
                    it.remove();
                    connection.close();
                }
            }
        }

        void close() {
            PooledConnection connection;
            while ((connection = idleConnections.poll()) != null) {
                connection.close();
            }
            Promise<Channel> waiter;
            while ((waiter = pendingAcquirers.poll()) != null) {
                waiter.tryFailure(new IOException(CLOSED_POOL_ERROR_MESSAGE));
            }
        }
    }

}
