// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.shared.LocalTestServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link Netty4ConnectionPool}.
 */
@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class Netty4ConnectionPoolTests {

    private static LocalTestServer server;
    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap;
    private static Netty4ConnectionPoolKey connectionPoolKey;

    @SuppressWarnings("deprecation")
    @BeforeAll
    public static void startTestServerAndEventLoopGroup() {
        server = NettyHttpClientLocalTestServer.getServer();
        server.start();
        eventLoopGroup = new io.netty.channel.nio.NioEventLoopGroup(2);
        bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.AUTO_READ, false);
        SocketAddress socketAddress = new InetSocketAddress("localhost", server.getPort());
        connectionPoolKey = new Netty4ConnectionPoolKey(socketAddress, socketAddress);
    }

    @AfterAll
    public static void stopTestServerAndEventLoopGroup() {
        if (server != null) {
            server.stop();
        }
        if (eventLoopGroup != null && !eventLoopGroup.isShuttingDown()) {
            eventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        }
    }

    private Netty4ConnectionPool createPool(int maxConnections, Duration idleTimeout, Duration maxLifetime,
        Duration pendingAcquireTimeout, int maxPendingAcquires) {
        return new Netty4ConnectionPool(bootstrap, new ChannelInitializationProxyHandler(null), null, // No SSL context modifier needed
            maxConnections, idleTimeout, maxLifetime, pendingAcquireTimeout, maxPendingAcquires,
            HttpProtocolVersion.HTTP_1_1);
    }

    @Test
    public void releaseNullChannelDoesNotThrow() throws IOException {
        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
            assertDoesNotThrow(() -> pool.release(null));
        }
    }

    @Test
    public void releaseUnknownChannelClosesChannel() throws IOException {
        Channel unknownChannel = new NioSocketChannel();
        eventLoopGroup.register(unknownChannel);

        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
            pool.release(unknownChannel);
            // The pool doesn't know this channel, so it should close it.
            unknownChannel.closeFuture().awaitUninterruptibly();
            assertFalse(unknownChannel.isOpen());
        }
    }

    @Test
    public void releaseToClosedPoolClosesChannel() throws IOException {
        Bootstrap realBootstrap = bootstrap.clone().remoteAddress(connectionPoolKey.getConnectionTarget());
        Netty4ConnectionPool pool = new Netty4ConnectionPool(realBootstrap, new ChannelInitializationProxyHandler(null),
            null, 1, null, null, null, 1, null);

        Channel channel = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
        assertTrue(channel.isActive());

        pool.close();
        pool.release(channel);

        channel.closeFuture().awaitUninterruptibly();
        assertFalse(channel.isOpen());
    }

    @Test
    public void testAcquireAndRelease() throws IOException {
        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
            Future<Channel> future = pool.acquire(connectionPoolKey, false);
            Channel channel = future.awaitUninterruptibly().getNow();
            assertNotNull(channel);
            assertTrue(channel.isActive());
            pool.release(channel);
        }
    }

    @Test
    public void closeIsIdempotent() throws IOException {
        Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1);
        pool.close();
        assertDoesNotThrow(pool::close);
    }

    @Test
    public void poolWithNoIdleTimeoutHasNoCleanupTask()
        throws IOException, NoSuchFieldException, IllegalAccessException {
        try (Netty4ConnectionPool pool = createPool(1, null, null, Duration.ofSeconds(10), 1)) {
            Field cleanupTaskField = Netty4ConnectionPool.class.getDeclaredField("cleanupTask");
            cleanupTaskField.setAccessible(true);
            assertNull(cleanupTaskField.get(pool));
        }
    }

    @Test
    public void pendingAcquireQueueIsFull() throws IOException {
        try (Netty4ConnectionPool pool = createPool(1, null, null, Duration.ofSeconds(10), 1)) {
            Channel channel = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            assertNotNull(channel);

            Future<Channel> pendingFuture = pool.acquire(connectionPoolKey, false);
            assertFalse(pendingFuture.isDone());

            Future<Channel> failedFuture = pool.acquire(connectionPoolKey, false);
            assertTrue(failedFuture.isDone());
            assertFalse(failedFuture.isSuccess());
            assertInstanceOf(CoreException.class, failedFuture.cause());

            pool.release(channel);
            pendingFuture.awaitUninterruptibly();
            pool.release(pendingFuture.getNow());
        }
    }

    @Test
    public void cancelledPendingAcquireIsRemovedFromQueue() throws IOException, InterruptedException {
        try (Netty4ConnectionPool pool = createPool(1, null, Duration.ofSeconds(5), Duration.ofSeconds(10), 1)) {
            Channel channel1 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();

            Future<Channel> pendingFuture = pool.acquire(connectionPoolKey, false);
            pendingFuture.cancel(true);

            Thread.sleep(100);

            pool.release(channel1);

            Channel channel2 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            assertSame(channel1, channel2);

            pool.release(channel2);
        }
    }

    @Test
    public void testConnectionIsReusedForSameRemoteAddress() throws IOException {
        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
            Future<Channel> future1 = pool.acquire(connectionPoolKey, false);
            Channel channel1 = future1.awaitUninterruptibly().getNow();
            pool.release(channel1);

            Future<Channel> future2 = pool.acquire(connectionPoolKey, false);
            Channel channel2 = future2.awaitUninterruptibly().getNow();
            assertSame(channel1, channel2);
            pool.release(channel2);
        }
    }

    @Test
    public void testConnectionPoolSizeEnforced() throws IOException, InterruptedException {
        final int maxConnections = 5;
        try (Netty4ConnectionPool pool
            = createPool(maxConnections, Duration.ofSeconds(10), null, Duration.ofSeconds(10), maxConnections)) {
            List<Channel> channels = new ArrayList<>();
            for (int i = 0; i < maxConnections; i++) {
                channels.add(pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow());
            }
            assertEquals(maxConnections, channels.size());

            Future<Channel> pendingFuture = pool.acquire(connectionPoolKey, false);
            Thread.sleep(100);
            assertFalse(pendingFuture.isDone());

            pool.release(channels.get(0));
            Channel pendingChannel = pendingFuture.awaitUninterruptibly().getNow();
            assertSame(channels.get(0), pendingChannel);

            for (int i = 1; i < channels.size(); i++) {
                pool.release(channels.get(i));
            }
        }
    }

    @Test
    public void testPendingAcquireTimeout() throws IOException, InterruptedException {
        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofMillis(100), 1)) {
            Channel channel = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();

            Future<Channel> timeoutFuture = pool.acquire(connectionPoolKey, false);

            assertTrue(timeoutFuture.await(500, TimeUnit.MILLISECONDS));

            assertFalse(timeoutFuture.isSuccess());
            assertInstanceOf(CoreException.class, timeoutFuture.cause());
            assertTrue(timeoutFuture.cause().getMessage().contains("Connection acquisition timed out"));

            pool.release(channel);
        }
    }

    @Test
    public void testIdleConnectionIsCleanedUp() throws IOException, InterruptedException {
        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
            Channel channel1 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            pool.release(channel1);
            Thread.sleep(31000); // Wait for cleanup task to run (interval is 30s)
            assertFalse(channel1.isActive());

            Channel channel2 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            assertNotSame(channel1, channel2);
            pool.release(channel2);
        }
    }

    @Test
    public void testMaxConnectionLifetimeEnforced() throws IOException, InterruptedException {
        try (Netty4ConnectionPool pool
            = createPool(1, Duration.ofSeconds(10), Duration.ofMillis(500), Duration.ofSeconds(10), 1)) {
            Channel channel1 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            Thread.sleep(600);
            pool.release(channel1);
            Thread.sleep(100); // Give a moment for close to propagate
            assertFalse(channel1.isActive());

            Channel channel2 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            assertNotSame(channel1, channel2);
            pool.release(channel2);
        }
    }

    @Test
    public void testUnhealthyConnectionIsDiscarded() throws IOException {
        try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
            Channel channel1 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            pool.release(channel1);
            channel1.close().awaitUninterruptibly();

            Channel channel2 = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();
            assertNotNull(channel2);
            assertTrue(channel2.isActive());
            assertNotSame(channel1, channel2);
            pool.release(channel2);
        }
    }

    @Test
    public void testAcquireOnClosedPoolFails() throws IOException, InterruptedException {
        Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1);
        pool.close();
        Future<Channel> future = pool.acquire(connectionPoolKey, false);
        future.await();

        assertFalse(future.isSuccess());
        assertInstanceOf(IllegalStateException.class, future.cause());
    }

    @Test
    public void testSeparatePoolsForSeparateRemoteAddresses() throws IOException {
        LocalTestServer route1Server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, null);
        LocalTestServer route2Server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, null);

        try {
            route1Server.start();
            route2Server.start();

            SocketAddress address1 = new InetSocketAddress("localhost", route1Server.getPort());
            SocketAddress address2 = new InetSocketAddress("localhost", route2Server.getPort());
            Netty4ConnectionPoolKey key1 = new Netty4ConnectionPoolKey(address1, address1);
            Netty4ConnectionPoolKey key2 = new Netty4ConnectionPoolKey(address2, address2);

            try (Netty4ConnectionPool pool = createPool(1, Duration.ofSeconds(10), null, Duration.ofSeconds(10), 1)) {
                Channel channel1 = pool.acquire(key1, false).awaitUninterruptibly().getNow();
                assertNotNull(channel1);

                Channel channel2 = pool.acquire(key2, false).awaitUninterruptibly().getNow();
                assertNotNull(channel2);

                assertNotSame(channel1, channel2);

                pool.release(channel1);
                pool.release(channel2);
            }
        } finally {
            route1Server.stop();
            route2Server.stop();
        }
    }

    @Test
    public void poolDoesNotDeadlockAndRecoversCleanlyUnderSaturation() throws InterruptedException, IOException {
        final int poolSize = 10;
        final int numThreads = 20;
        final int numTasks = 100;

        final CountDownLatch latch = new CountDownLatch(numTasks);
        final AtomicInteger successCounter = new AtomicInteger(0);
        final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try (Netty4ConnectionPool pool
            = createPool(poolSize, Duration.ofSeconds(10), null, Duration.ofSeconds(2), numTasks)) {
            for (int i = 0; i < numTasks; i++) {
                executor.submit(() -> {
                    try {
                        Channel channel = pool.acquire(connectionPoolKey, false).awaitUninterruptibly().getNow();

                        // Hold the connection for a short, random time to simulate work.
                        Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));

                        pool.release(channel);
                        successCounter.incrementAndGet();
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "Test deadlocked, not all tasks completed.");

            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

            if (!exceptions.isEmpty()) {
                fail("Test tasks threw exceptions: "
                    + exceptions.stream().map(Throwable::getMessage).collect(Collectors.joining(", ")));
            }
            assertEquals(numTasks, successCounter.get(), "Mismatch in the number of successful tasks.");

            // Use reflection to check the final state of the pool's queues.
            assertDoesNotThrow(() -> {
                Field poolField = Netty4ConnectionPool.class.getDeclaredField("pool");
                poolField.setAccessible(true);
                @SuppressWarnings("unchecked")
                ConcurrentMap<?, Netty4ConnectionPool.PerRoutePool> routePools
                    = (ConcurrentMap<?, Netty4ConnectionPool.PerRoutePool>) poolField.get(pool);
                Netty4ConnectionPool.PerRoutePool perRoutePool = routePools.get(connectionPoolKey);

                Field idleField = Netty4ConnectionPool.PerRoutePool.class.getDeclaredField("idleConnections");
                idleField.setAccessible(true);
                Deque<?> idleConnections = (Deque<?>) idleField.get(perRoutePool);

                Field pendingField = Netty4ConnectionPool.PerRoutePool.class.getDeclaredField("pendingAcquirers");
                pendingField.setAccessible(true);
                Deque<?> pendingAcquirers = (Deque<?>) pendingField.get(perRoutePool);

                assertEquals(poolSize, idleConnections.size(), "Pool should be full of idle connections.");
                assertTrue(pendingAcquirers.isEmpty(), "Pending acquirers queue should be empty.");
            });
        }
    }
}
