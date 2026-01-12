// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.util.AttributeKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_CODEC;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_RESPONSE;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Netty4PipelineCleanupHandler}.
 */
public class Netty4PipelineCleanupHandlerTests {
    private final ReleaseTrackingConnectionPool connectionPool = new ReleaseTrackingConnectionPool();

    private static final Object OBJECT = new Object();
    private TestMockChannel testChannel;
    private AtomicReference<Throwable> errorReference;

    @BeforeEach
    public void setup() {
        testChannel = new TestMockChannel(new MockEventLoop());
        testChannel.attr(AttributeKey.valueOf("channel-lock")).set(new ReentrantLock());
        testChannel.attr(AttributeKey.valueOf("pipeline-owner-token")).set(OBJECT);
        testChannel.config.setAutoRead(false);
        errorReference = new AtomicReference<>();
    }

    @Test
    public void cleanupWhenPooledAndActiveReleasesChannel() {
        testChannel.setActive(true);
        testChannel.pipeline().addLast(HTTP_CODEC, new MockChannelHandler());
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);

        handler.cleanup(ctx, false);

        assertEquals(1, connectionPool.releaseCountTracker.get(testChannel));
        assertEquals(1, testChannel.getCloseCallCount());
        assertNull(testChannel.pipeline().get(HTTP_CODEC));
        assertFalse(testChannel.config().isAutoRead());
    }

    @Test
    public void cleanupWhenForceCloseClosesChannel() {
        testChannel.setActive(true);
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);

        handler.cleanup(ctx, true);

        assertEquals(1, testChannel.getCloseCallCount());
        assertNull(connectionPool.releaseCountTracker.get(testChannel));
    }

    @Test
    public void cleanupWhenNonPooledClosesChannel() {
        testChannel.setActive(true);
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(null, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);

        handler.cleanup(ctx, false);

        assertEquals(1, testChannel.getCloseCallCount());
    }

    @Test
    public void cleanupWhenChannelInactiveClosesChannel() {
        testChannel.setActive(false);
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);

        handler.cleanup(ctx, false);

        assertEquals(1, testChannel.getCloseCallCount());
        assertNull(connectionPool.releaseCountTracker.get(testChannel));
    }

    @Test
    public void cleanupWhenHttp2PreservesHttpCodec() {
        testChannel.setActive(true);
        testChannel.attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).set(HttpProtocolVersion.HTTP_2);
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        populatePipelineWithStandardHandlers(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);

        handler.cleanup(ctx, false);

        assertNotNull(testChannel.pipeline().get(HTTP_CODEC));
        assertNull(testChannel.pipeline().get(HTTP_RESPONSE));
        assertEquals(1, connectionPool.releaseCountTracker.get(testChannel));
    }

    @Test
    public void cleanupIsIdempotent() {
        testChannel.setActive(true);
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);

        handler.cleanup(ctx, true);
        handler.cleanup(ctx, true);

        assertEquals(1, testChannel.getCloseCallCount());
    }

    @Test
    public void exceptionCaughtSetsErrorAndClosesChannel() {
        testChannel.setActive(true);
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);
        Throwable testException = new IOException("Test Exception");

        handler.exceptionCaught(ctx, testException);

        assertEquals(testException, errorReference.get());
        assertEquals(1, testChannel.getCloseCallCount());
        assertNull(connectionPool.releaseCountTracker.get(testChannel));
    }

    @Test
    public void exceptionCaughtStillClosesChannel() {
        testChannel.setActive(true);
        Netty4PipelineCleanupHandler handler
            = new Netty4PipelineCleanupHandler(connectionPool, new AtomicReference<>(), OBJECT);
        testChannel.pipeline().addLast(handler);
        ChannelHandlerContext ctx = testChannel.pipeline().context(handler);
        Throwable testException = new IOException("Test Exception");

        handler.exceptionCaught(ctx, testException);

        assertEquals(1, testChannel.getCloseCallCount());
        assertNull(connectionPool.releaseCountTracker.get(testChannel));
    }

    @Test
    public void channelInactiveSchedulesAndExecutesCleanup() {
        testChannel.setActive(true);
        assertTrue(testChannel.isActive());
        Netty4PipelineCleanupHandler handler = new Netty4PipelineCleanupHandler(connectionPool, errorReference, OBJECT);
        testChannel.pipeline().addLast(handler);

        testChannel.close();

        assertEquals(1, testChannel.getCloseCallCount(), "close() should be called once.");
        assertNull(connectionPool.releaseCountTracker.get(testChannel));
    }

    private void populatePipelineWithStandardHandlers(Netty4PipelineCleanupHandler handler) {
        testChannel.pipeline().addLast(PROGRESS_AND_TIMEOUT, new MockChannelHandler());
        testChannel.pipeline().addLast(HTTP_RESPONSE, new MockChannelHandler());
        testChannel.pipeline().addLast(HTTP_CODEC, new MockChannelHandler());
        testChannel.pipeline().addLast(handler);
    }

    private static class MockChannelHandler extends ChannelHandlerAdapter {
    }

    private static class TestMockChannel extends AbstractChannel {
        private static final ChannelMetadata METADATA = new ChannelMetadata(false);
        private final ChannelConfig config = new DefaultChannelConfig(this);
        private final AtomicInteger closeCallCount = new AtomicInteger(0);
        private final EventLoop eventLoop;

        private volatile boolean active;
        private volatile boolean open = true;

        protected TestMockChannel(EventLoop eventLoop) {
            super(null);
            this.eventLoop = eventLoop;
        }

        @Override
        public EventLoop eventLoop() {
            return this.eventLoop;
        }

        @Override
        public ChannelConfig config() {
            return this.config;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public ChannelMetadata metadata() {
            return METADATA;
        }

        @Override
        protected AbstractUnsafe newUnsafe() {
            return new AbstractUnsafe() {
                @Override
                public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                    active = true;
                    promise.setSuccess();
                }
            };
        }

        @Override
        protected boolean isCompatible(EventLoop loop) {
            return loop == this.eventLoop;
        }

        @Override
        protected SocketAddress localAddress0() {
            return null;
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return null;
        }

        @Override
        protected void doBind(SocketAddress localAddress) {
        }

        @Override
        protected void doDisconnect() {
            active = false;
        }

        @Override
        protected void doClose() {
            active = false;
            open = false;
            closeCallCount.incrementAndGet();
        }

        @Override
        protected void doBeginRead() {
        }

        @Override
        protected void doWrite(ChannelOutboundBuffer in) {
        }

        public void setActive(boolean isActive) {
            this.active = isActive;
        }

        public int getCloseCallCount() {
            return closeCallCount.get();
        }
    }

    private static class MockEventLoop extends DefaultEventLoop {
        @Override
        public void execute(Runnable task) {
            if (task == null) {
                throw new NullPointerException("task");
            }
            task.run();
        }
    }

    private static final class ReleaseTrackingConnectionPool extends Netty4ConnectionPool {
        private final Map<Channel, Integer> releaseCountTracker = new ConcurrentHashMap<>();

        ReleaseTrackingConnectionPool() {
            super(null, null, null, 0, Duration.ZERO, Duration.ZERO, Duration.ZERO, 0, null);
        }

        @Override
        public void release(Channel channel) {
            releaseCountTracker.compute(channel, (k, v) -> v == null ? 1 : v + 1);
            super.release(channel);
        }
    }
}
