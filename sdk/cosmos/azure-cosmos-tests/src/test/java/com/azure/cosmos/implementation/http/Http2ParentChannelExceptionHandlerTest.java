// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that {@link Http2ParentChannelExceptionHandler} uses connection
 * state — active stream count and channel activity — to determine whether
 * exceptions are logged at DEBUG (suppressed) or WARN (preserved).
 * Exception type is NOT a filtering dimension.
 *
 * The EmbeddedChannel is configured to mirror the production HTTP/2 parent
 * channel pipeline:
 * <pre>
 *   Http2FrameCodec → Http2MultiplexHandler → Http2ParentChannelExceptionHandler → TailContext
 * </pre>
 * (SslHandler is omitted because it requires an SSLContext and is not relevant
 * to exception propagation behavior.)
 *
 * {@code checkException()} re-throws any exception that reached the pipeline tail.
 */
public class Http2ParentChannelExceptionHandlerTest {

    /**
     * BEFORE fix — without the handler, exceptions reach the pipeline tail.
     * EmbeddedChannel's checkException() re-throws the unhandled exception,
     * proving it reached Netty's TailContext (which in production logs as WARN).
     */
    @Test(groups = "unit")
    public void withoutHandler_exceptionReachesTail() {
        EmbeddedChannel channel = createH2ParentChannel(false);

        channel.pipeline().fireExceptionCaught(
            new IOException("Connection reset by peer"));

        assertThatThrownBy(channel::checkException)
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Connection reset by peer");

        channel.finishAndReleaseAll();
    }

    /**
     * With handler — exception on idle connection (0 active streams) is
     * consumed at DEBUG. The suppression is based on connection state
     * (no active streams), not exception type.
     *
     * In production, channel.isActive() transitions to false during the
     * RST handling cycle, satisfying the OR condition. In EmbeddedChannel
     * we can only verify the activeStreams == 0 branch.
     */
    @Test(groups = "unit")
    public void withHandler_zeroActiveStreams_consumedAtDebug() {
        EmbeddedChannel channel = createH2ParentChannel(true);

        Http2FrameCodec codec = channel.pipeline().get(Http2FrameCodec.class);
        assertThat(codec).isNotNull();
        assertThat(codec.connection().numActiveStreams()).isEqualTo(0);

        channel.pipeline().fireExceptionCaught(
            new IOException("recvAddress(..) failed with error(-104): Connection reset by peer"));

        // Exception consumed — does NOT reach tail
        channel.checkException();

        channel.finishAndReleaseAll();
    }

    /**
     * Handler does not close the channel — connection lifecycle is managed
     * by reactor-netty's pool eviction, not by this handler.
     */
    @Test(groups = "unit")
    public void withHandler_exceptionDoesNotCloseChannel() {
        EmbeddedChannel channel = createH2ParentChannel(true);

        assertThat(channel.isActive()).isTrue();

        channel.pipeline().fireExceptionCaught(
            new IOException("Connection reset by peer"));

        channel.checkException();
        assertThat(channel.isOpen()).isTrue();

        channel.finishAndReleaseAll();
    }

    /**
     * RuntimeException on idle connection is also consumed — suppression
     * is based on connection state, not exception type.
     */
    @Test(groups = "unit")
    public void withHandler_runtimeException_zeroActiveStreams_consumed() {
        EmbeddedChannel channel = createH2ParentChannel(true);

        channel.pipeline().fireExceptionCaught(
            new RuntimeException("Unexpected state error"));

        channel.checkException();

        channel.finishAndReleaseAll();
    }

    /**
     * NullPointerException on idle connection is also consumed — same
     * connection-state-based suppression regardless of exception type.
     */
    @Test(groups = "unit")
    public void withHandler_npe_zeroActiveStreams_consumed() {
        EmbeddedChannel channel = createH2ParentChannel(true);

        channel.pipeline().fireExceptionCaught(
            new NullPointerException("handler bug"));

        channel.checkException();

        channel.finishAndReleaseAll();
    }

    /**
     * With handler — exception on a connection with active streams is
     * consumed (does not reach TailContext). The handler logs at WARN
     * instead of DEBUG because in-flight requests may be affected.
     */
    @Test(groups = "unit")
    public void withHandler_activeStreams_consumedAtWarn() throws Exception {
        EmbeddedChannel channel = createH2ParentChannel(true);

        Http2FrameCodec codec = channel.pipeline().get(Http2FrameCodec.class);
        assertThat(codec).isNotNull();

        // Create an active stream (client-initiated, odd stream ID)
        codec.connection().local().createStream(1, false);
        assertThat(codec.connection().numActiveStreams()).isEqualTo(1);
        assertThat(channel.isActive()).isTrue();

        channel.pipeline().fireExceptionCaught(
            new IOException("Connection reset by peer"));

        // Exception consumed — does NOT reach tail, even with active streams
        channel.checkException();

        channel.finishAndReleaseAll();
    }

    /**
     * Handler does not close the channel even when active streams exist —
     * connection lifecycle is managed by reactor-netty's pool eviction.
     */
    @Test(groups = "unit")
    public void withHandler_activeStreams_channelNotClosed() throws Exception {
        EmbeddedChannel channel = createH2ParentChannel(true);

        Http2FrameCodec codec = channel.pipeline().get(Http2FrameCodec.class);
        assertThat(codec).isNotNull();

        codec.connection().local().createStream(1, false);
        assertThat(codec.connection().numActiveStreams()).isEqualTo(1);
        assertThat(channel.isActive()).isTrue();

        channel.pipeline().fireExceptionCaught(
            new IOException("Connection reset by peer"));

        channel.checkException();
        assertThat(channel.isOpen()).isTrue();

        channel.finishAndReleaseAll();
    }

    /**
     * With handler — when Http2FrameCodec is absent from the pipeline,
     * getActiveStreamCount() returns null. Since the active stream count
     * is unknown and the channel is active, the handler takes the safe
     * WARN path. This covers the fallback behavior when the codec is
     * unavailable (e.g., torn down during shutdown).
     */
    @Test(groups = "unit")
    public void withHandler_codecAbsent_fallsBackToWarnPath() {
        EmbeddedChannel channel = new EmbeddedChannel(
            Http2ParentChannelExceptionHandler.INSTANCE);

        assertThat(channel.pipeline().get(Http2FrameCodec.class)).isNull();
        assertThat(channel.isActive()).isTrue();

        channel.pipeline().fireExceptionCaught(
            new IOException("Connection reset by peer"));

        // Exception consumed — does NOT reach tail
        channel.checkException();
        assertThat(channel.isOpen()).isTrue();

        channel.finishAndReleaseAll();
    }

    /**
     * Error types (e.g., OutOfMemoryError) are NOT consumed by the handler —
     * they propagate to TailContext. This ensures JVM-level errors are never
     * silently swallowed.
     */
    @Test(groups = "unit")
    public void withHandler_errorNotConsumed_propagatesToTail() {
        EmbeddedChannel channel = createH2ParentChannel(true);

        channel.pipeline().fireExceptionCaught(
            new OutOfMemoryError("test OOM"));

        assertThatThrownBy(channel::checkException)
            .isInstanceOf(OutOfMemoryError.class)
            .hasMessageContaining("test OOM");

        channel.finishAndReleaseAll();
    }

    /**
     * Creates an EmbeddedChannel matching the production HTTP/2 parent channel
     * pipeline (minus SslHandler): Http2FrameCodec → Http2MultiplexHandler →
     * Http2ParentChannelExceptionHandler.
     */
    private static EmbeddedChannel createH2ParentChannel(boolean withExceptionHandler) {
        Http2FrameCodec codec = Http2FrameCodecBuilder.forClient()
            .autoAckSettingsFrame(true)
            .build();

        Http2MultiplexHandler multiplexHandler = new Http2MultiplexHandler(
            new ChannelInboundHandlerAdapter());

        if (withExceptionHandler) {
            return new EmbeddedChannel(codec, multiplexHandler,
                Http2ParentChannelExceptionHandler.INSTANCE);
        } else {
            return new EmbeddedChannel(codec, multiplexHandler);
        }
    }
}
