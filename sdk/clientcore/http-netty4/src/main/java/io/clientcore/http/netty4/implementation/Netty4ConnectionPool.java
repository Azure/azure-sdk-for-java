// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;

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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_CODEC;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROXY;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL_INITIALIZER;
import static io.clientcore.http.netty4.implementation.Netty4Utility.buildSslContext;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createHttp2Codec;

/**
 * A pool of Netty channels that can be reused for requests to the same remote address.
 */
public class Netty4ConnectionPool implements Closeable {

    /**
     * An AttributeKey referring to a channel-specific {@link ReentrantLock}.
     * <p>
     * This lock is used to ensure that the setup and cleanup of a channel's pipeline are atomic operations.
     * It protects against race conditions where a channel might be acquired from the pool and configured for a new
     * request before the cleanup from the previous request has fully completed. Each channel gets its own unique
     * lock instance, making the lock contention extremely low.
     */
    public static final AttributeKey<ReentrantLock> CHANNEL_LOCK = AttributeKey.valueOf("channel-lock");
    public static final AttributeKey<Boolean> HTTP2_GOAWAY_RECEIVED = AttributeKey.valueOf("http2-goaway-received");

    // A unique token to identify the current owner of a channel pipeline
    public static final AttributeKey<Object> PIPELINE_OWNER_TOKEN = AttributeKey.valueOf("pipeline-owner-token");

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

    @ChannelHandler.Sharable
    public static class Http2GoAwayHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Http2GoAwayFrame) {
                // A GOAWAY frame was received. Mark the channel so the pool knows
                // not to reuse it for new requests.
                ctx.channel().attr(HTTP2_GOAWAY_RECEIVED).set(true);
            }
            super.channelRead(ctx, msg);
        }
    }

    @ChannelHandler.Sharable
    public static final class SuppressProxyConnectExceptionWarningHandler extends ChannelInboundHandlerAdapter {
        private static final SuppressProxyConnectExceptionWarningHandler INSTANCE
            = new SuppressProxyConnectExceptionWarningHandler();

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof HttpProxyHandler.HttpProxyConnectException) {
                return;
            }
            ctx.fireExceptionCaught(cause);
        }
    }

    @ChannelHandler.Sharable
    private static class SslGracefulShutdownHandler extends ChannelInboundHandlerAdapter {
        private static final SslGracefulShutdownHandler INSTANCE = new SslGracefulShutdownHandler();

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof SslCloseCompletionEvent) {
                ctx.channel().close();
            }
            super.userEventTriggered(ctx, evt);
        }
    }

    @ChannelHandler.Sharable
    private static class PoolConnectionHealthHandler extends ChannelInboundHandlerAdapter {
        private static final PoolConnectionHealthHandler INSTANCE = new PoolConnectionHealthHandler();

        private static final AttributeKey<Boolean> CONNECTION_INVALIDATED
            = AttributeKey.valueOf("connection-invalidated");

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            // This event is fired when the server closes its side of the connection.
            if (evt instanceof ChannelInputShutdownEvent) {
                invalidateAndClose(ctx.channel());
            }
            super.userEventTriggered(ctx, evt);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // This is a fallback for when the connection is fully closed for any reason.
            invalidateAndClose(ctx.channel());
            super.channelInactive(ctx);
        }

        private void invalidateAndClose(Channel channel) {
            // Mark the channel as invalid. The 'isHealthy' check uses this attribute
            // to immediately reject the channel without waiting for it to be fully closed.
            channel.attr(CONNECTION_INVALIDATED).set(true);
            channel.close();
        }
    }

    @ChannelHandler.Sharable
    private static class PoolAlpnConfigurator extends ApplicationProtocolNegotiationHandler {
        private static final PoolAlpnConfigurator INSTANCE = new PoolAlpnConfigurator();

        PoolAlpnConfigurator() {
            super(ApplicationProtocolNames.HTTP_1_1);
        }

        @Override
        protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
            HttpProtocolVersion protocolVersion;
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                protocolVersion = HttpProtocolVersion.HTTP_2;
            } else {
                protocolVersion = HttpProtocolVersion.HTTP_1_1;
            }

            ctx.channel().attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).set(protocolVersion);

            ctx.pipeline().remove(this);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.fireExceptionCaught(cause);
        }
    }

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
     * Acquires a channel for the given composite key from the pool.
     *
     * @param key The composite key representing the connection route.
     * @param isHttps Flag indicating whether connections for this route should be secured using TLS/SSL.
     * @return A {@link Future} that will be notified when a channel is acquired.
     * @throws IllegalStateException if the connection pool has been closed.
     */
    public Future<Channel> acquire(Netty4ConnectionPoolKey key, boolean isHttps) {
        if (closed.get()) {
            return bootstrap.config()
                .group()
                .next()
                .newFailedFuture(new IllegalStateException(CLOSED_POOL_ERROR_MESSAGE));
        }

        PerRoutePool perRoutePool = pool.computeIfAbsent(key, k -> new PerRoutePool(k, isHttps));
        return perRoutePool.acquire();
    }

    /**
     * Releases a channel back to the connection pool.
     * The channel pipeline must be cleaned of request-specific handlers before releasing.
     * This method is not responsible for that.
     *
     * @param channel The channel to release back to the connection pool.
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
    class PerRoutePool {
        private final Deque<PooledConnection> idleConnections = new ConcurrentLinkedDeque<>();
        private final Deque<Promise<Channel>> pendingAcquirers = new ConcurrentLinkedDeque<>();
        // Counter for all connections for a specific route (active and idle).
        private final AtomicInteger totalConnections = new AtomicInteger(0);
        private final Netty4ConnectionPoolKey key;
        private final SocketAddress route;
        private final boolean isHttps;

        // A lock to protect the pool's internal state during acquire/release decisions.
        private final ReentrantLock poolLock = new ReentrantLock();

        PerRoutePool(Netty4ConnectionPoolKey key, boolean isHttps) {
            this.key = key;
            this.route = key.getConnectionTarget();
            this.isHttps = isHttps;
        }

        /**
         * Acquires a connection.
         *
         * <p>
         * This method is the entry point for getting a connection. It will first try to poll from the idle queue.
         * If it can't, it will attempt to create a new one if pool capacity is not reached. If capacity is reached,
         * it will queue the request.
         * </p>
         *
         * @return A {@link Future} that completes with a {@link Channel}.
         */
        Future<Channel> acquire() {
            if (closed.get()) {
                return bootstrap.config()
                    .group()
                    .next()
                    .newFailedFuture(new IllegalStateException(CLOSED_POOL_ERROR_MESSAGE));
            }

            poolLock.lock();
            try {
                // First, check for an available idle connection.
                PooledConnection connection = pollIdleAndCheckHealth();
                if (connection != null) {
                    // Found a valid, idle connection. Return it immediately.
                    return connection.channel.eventLoop().newSucceededFuture(connection.channel);
                }

                // No idle connections. Check if we can create a new one.
                if (totalConnections.get() < maxConnectionsPerRoute) {
                    // Increment count and create a new connection outside the lock.
                    totalConnections.getAndIncrement();
                    return createNewConnection();
                }

                // Pool is full. Queue the acquisition request.
                if (pendingAcquirers.size() >= maxPendingAcquires) {
                    return bootstrap.config()
                        .group()
                        .next()
                        .newFailedFuture(CoreException.from("Pending acquisition queue is full."));
                }

                Promise<Channel> promise = bootstrap.config().group().next().newPromise();

                if (pendingAcquireTimeout != null && pendingAcquireTimeout.toMillis() > 0) {
                    final ScheduledFuture<?> timeoutFuture = bootstrap.config().group().schedule(() -> {
                        if (promise.tryFailure(
                            CoreException.from("Connection acquisition timed out after " + pendingAcquireTimeout))) {
                            poolLock.lock();
                            try {
                                pendingAcquirers.remove(promise);
                            } finally {
                                poolLock.unlock();
                            }
                        }
                    }, pendingAcquireTimeout.toMillis(), TimeUnit.MILLISECONDS);

                    promise.addListener(f -> {
                        if (f.isDone()) {
                            timeoutFuture.cancel(false);
                        }
                    });
                }

                promise.addListener(future -> {
                    if (future.isCancelled()) {
                        poolLock.lock();
                        try {
                            pendingAcquirers.remove(promise);
                        } finally {
                            poolLock.unlock();
                        }
                    }
                });

                pendingAcquirers.offer(promise);
                return promise;
            } finally {
                poolLock.unlock();
            }
        }

        void release(PooledConnection connection) {
            poolLock.lock();
            try {
                if (!isHealthy(connection)) {
                    // The connection is unhealthy. Close it.
                    // The asynchronous closeFuture listener ('handleConnectionClosure') will eventually
                    // decrement the connection count and create a new connections for available waiters.
                    connection.close();
                    return;
                }

                // The connection is healthy. Offer it to the waiters.
                while (!pendingAcquirers.isEmpty()) {
                    Promise<Channel> waiter = pollNextWaiter();
                    if (waiter == null) {
                        // All remaining waiters were cancelled.
                        break;
                    }

                    if (waiter.trySuccess(connection.channel)) {
                        // The waiter accepted the connection, so we are done.
                        return;
                    }
                    // If the waiter didn't accept it (e.g., timed out), the loop will
                    // offer the connection to the next waiter.
                }

                // There are no pending waiters, so add the healthy connection to the idle queue.
                connection.idleSince = OffsetDateTime.now(ZoneOffset.UTC);
                idleConnections.offer(connection);
            } finally {
                poolLock.unlock();
            }
        }

        private void handleConnectionClosure() {
            poolLock.lock();
            try {
                totalConnections.getAndDecrement();

                // A slot has opened up. Loop and create new connections
                // for as long as there are waiters, and we have capacity.
                while (totalConnections.get() < maxConnectionsPerRoute && !pendingAcquirers.isEmpty()) {
                    Promise<Channel> waiter = pollNextWaiter();
                    if (waiter == null) {
                        break;
                    }

                    totalConnections.getAndIncrement();
                    createNewConnection().addListener(future -> {
                        if (future.isSuccess()) {
                            // Try to give the new channel to the waiter.
                            // If it fails (e.g., the waiter timed out in the meantime),
                            // release the brand new channel back to the pool.
                            if (!waiter.trySuccess((Channel) future.getNow())) {
                                release(((Channel) future.getNow()).attr(POOLED_CONNECTION_KEY).get());
                            }
                        } else {
                            // The connection failed, so notify the waiter.
                            // The connection's own close handler will decrement totalConnections again.
                            waiter.tryFailure(future.cause());
                        }
                    });
                }
            } finally {
                poolLock.unlock();
            }
        }

        private Promise<Channel> pollNextWaiter() {
            while (!pendingAcquirers.isEmpty()) {
                Promise<Channel> waiter = pendingAcquirers.poll();
                if (!waiter.isCancelled()) {
                    return waiter;
                }
            }
            return null;
        }

        private PooledConnection pollIdleAndCheckHealth() {
            while (!idleConnections.isEmpty()) {
                PooledConnection connection = idleConnections.poll();
                if (isHealthy(connection)) {
                    connection.idleSince = null; // Mark as active
                    return connection;
                }
                connection.close();
            }
            return null;
        }

        private Future<Channel> createNewConnection() {
            Bootstrap newConnectionBootstrap = bootstrap.clone();
            Promise<Channel> promise = newConnectionBootstrap.config().group().next().newPromise();
            newConnectionBootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel channel) throws SSLException {
                    channel.attr(CHANNEL_LOCK).set(new ReentrantLock());

                    // Create the connection wrapper and attach it to the channel.
                    new PooledConnection(channel, key);

                    ChannelPipeline pipeline = channel.pipeline(); //TODO: fix handler names
                    pipeline.addLast("poolHealthHandler", PoolConnectionHealthHandler.INSTANCE);

                    // Test whether proxying should be applied to this Channel. If so, add it.
                    // Proxy detection MUST use the final destination address from the key.
                    boolean hasProxy = channelInitializationProxyHandler.test(key.getFinalDestination());
                    if (hasProxy) {
                        ProxyHandler proxyHandler = channelInitializationProxyHandler.createProxy(proxyChallenges);
                        pipeline.addFirst(PROXY, proxyHandler);
                        pipeline.addAfter(PROXY, "clientcore.suppressproxyexception",
                            SuppressProxyConnectExceptionWarningHandler.INSTANCE);
                    }

                    // Add SSL handling if the request is HTTPS.
//                    if (isHttps) {
//                        InetSocketAddress inetSocketAddress = (InetSocketAddress) key.getFinalDestination();
//                        SslContext ssl = buildSslContext(maximumHttpVersion, sslContextModifier);
//                        pipeline.addLast(SSL, ssl.newHandler(channel.alloc(), inetSocketAddress.getHostString(),
//                            inetSocketAddress.getPort()));
//                        pipeline.addAfter(SSL, "clientcore.sslshutdown", SslGracefulShutdownHandler.INSTANCE);
//                        //pipeline.addLast(SSL_INITIALIZER, new Netty4SslInitializationHandler());
//                        pipeline.addLast("pool-alpn-configurator", PoolAlpnConfigurator.INSTANCE);
//                    }
                }
            });

            newConnectionBootstrap.connect(route).addListener(future -> {
                if (!future.isSuccess()) {
                    LOGGER.atError().setThrowable(future.cause()).log("Failed connection.");
                    handleConnectionClosure();
                    promise.setFailure(future.cause());
                    return;
                }

                Channel newChannel = ((ChannelFuture) future).channel();
                newChannel.closeFuture().addListener(closeFuture -> handleConnectionClosure());

//                Runnable connectionReadyRunner = () -> {
//                    SslHandler sslHandler = newChannel.pipeline().get(SslHandler.class);
//                    if (sslHandler != null) {
//                        sslHandler.handshakeFuture().addListener(sslFuture -> {
//                            if (sslFuture.isSuccess()) {
//                                promise.setSuccess(newChannel);
//                            } else {
//                                promise.setFailure(sslFuture.cause());
//                                newChannel.close();
//                            }
//                        });
//                        newChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
//                    } else {
//                        promise.setSuccess(newChannel);
//                    }
//                };

//                ProxyHandler proxyHandler = (ProxyHandler) newChannel.pipeline().get(PROXY);
//                if (proxyHandler != null){
//                    // Wait for the proxy handshake to complete if proxy is being used.
//                    proxyHandler.connectFuture().addListener(proxyFuture -> {
//                        if (proxyFuture.isSuccess()) {
//                            if (!newChannel.isActive()) {
//                                promise.setFailure(new ClosedChannelException());
//                                newChannel.close();
//                                return;
//                            }
//                            connectionReadyRunner.run();
//                        } else {
//                            promise.setFailure(proxyFuture.cause());
//                            newChannel.close();
//                        }
//                    });
//                } else {
//                    connectionReadyRunner.run();
//                }
                ProxyHandler proxyHandler = (ProxyHandler) newChannel.pipeline().get(PROXY);
                if (proxyHandler != null) {
                    proxyHandler.connectFuture().addListener(proxyFuture -> {
                        if (proxyFuture.isSuccess()) promise.trySuccess(newChannel);
                        else {
                            promise.tryFailure(proxyFuture.cause());
                            newChannel.close();
                        }
                    });
                } else {
                    promise.trySuccess(newChannel);
                }
            });
            return promise;
        }

        private boolean isHealthy(PooledConnection connection) {
            Channel channel = connection.channel;

            if (Boolean.TRUE.equals(channel.attr(PoolConnectionHealthHandler.CONNECTION_INVALIDATED).get())) {
                return false;
            }

            if (!connection.isActiveAndWriteable() || channel.config().isAutoRead()) {
                return false;
            }

            OffsetDateTime now = null; // To be initialized only if needed.

            if (maxLifetimeNanos > 0) {
                now = OffsetDateTime.now(ZoneOffset.UTC);
                if (Duration.between(connection.creationTime, now).toNanos() >= maxLifetimeNanos) {
                    return false;
                }
            }

            if (connection.idleSince != null && idleTimeoutNanos > 0) {
                if (now == null) {
                    now = OffsetDateTime.now(ZoneOffset.UTC);
                }
                if (Duration.between(connection.idleSince, now).toNanos() >= idleTimeoutNanos) {
                    return false;
                }
            }

            HttpProtocolVersion protocol = channel.attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).get();
            if (protocol == HttpProtocolVersion.HTTP_2) {
                return !Boolean.TRUE.equals(channel.attr(HTTP2_GOAWAY_RECEIVED).get());
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
