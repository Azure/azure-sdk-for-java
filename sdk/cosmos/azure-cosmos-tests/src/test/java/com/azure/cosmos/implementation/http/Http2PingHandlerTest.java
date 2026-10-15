// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.DefaultHttp2PingFrame;
import io.netty.util.ReferenceCountUtil;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Http2PingHandler}.
 * <p>
 * Covers state transitions that do not require advancing real time -- the handler's
 * timeout / threshold logic reads {@code System.nanoTime()}, so the tests sidestep
 * the clock by pre-setting {@code pingOutstandingSinceNanos} (or
 * {@code lastActivityNanos}) via reflection:
 * <ul>
 *   <li>PING ACK with matching payload resets the failure counter (RFC 9113 §6.7 payload echo).</li>
 *   <li>PING ACK with mismatched payload is ignored (late ACK after timeout cannot mask degradation).</li>
 *   <li>{@code installIfAbsent} is idempotent.</li>
 *   <li>Constructor clamps non-positive interval / timeout / threshold to safe minimums.</li>
 *   <li>Runtime kill-switch toggle clears outstanding PING state (no spurious timeout on re-enable).</li>
 *   <li>{@code channelInactive} cancels the scheduled PING task (no leaked timer).</li>
 *   <li>Failed PING write increments {@code consecutiveFailures} and closes the channel at threshold.</li>
 *   <li>ACK timeout (outstanding PING aged past {@code pingTimeoutNanos}) increments
 *       {@code consecutiveFailures} and closes the channel at threshold.</li>
 * </ul>
 * The interval-driven scheduling itself (that {@code maybeSendPing} is fired by the
 * event loop every {@code pingIntervalNanos}) is exercised by the integration test
 * {@code Http2PingKeepaliveTest} under Docker with real network fault injection.
 */
public class Http2PingHandlerTest {

    @Test(groups = "unit")
    public void ackWithMatchingPayload_resetsState() throws Exception {
        Http2PingHandler handler = new Http2PingHandler(1, 1, 3);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Simulate an outstanding PING #5 that recorded one failure.
        setField(handler, "pingsSent", 5);
        setField(handler, "pingOutstandingSinceNanos", System.nanoTime());
        setField(handler, "consecutiveFailures", 1);

        channel.writeInbound(new DefaultHttp2PingFrame(5L, true));

        assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isZero();
        assertThat((int) getField(handler, "consecutiveFailures")).isZero();

        channel.finishAndReleaseAll();
    }

    @Test(groups = "unit")
    public void ackWithMismatchedPayload_doesNotResetState() throws Exception {
        Http2PingHandler handler = new Http2PingHandler(1, 1, 3);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        long outstandingAt = System.nanoTime();
        setField(handler, "pingsSent", 5);
        setField(handler, "pingOutstandingSinceNanos", outstandingAt);
        setField(handler, "consecutiveFailures", 2);

        // Late ACK for an earlier PING #3 -- must NOT clear the failure counter
        // that is tracking the currently outstanding PING #5.
        channel.writeInbound(new DefaultHttp2PingFrame(3L, true));

        assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isEqualTo(outstandingAt);
        assertThat((int) getField(handler, "consecutiveFailures")).isEqualTo(2);

        channel.finishAndReleaseAll();
    }

    @Test(groups = "unit")
    public void ackAfterTimeoutCleared_doesNotResetState() throws Exception {
        // Reproduces the race fixed by the `pingOutstandingSinceNanos != 0` guard in
        // channelRead: PING #5 is sent (pingsSent=5), timeout fires and sets
        // pingOutstandingSinceNanos=0 + consecutiveFailures=1, then a late ACK for #5
        // arrives BEFORE the next PING is sent (so pingsSent is still 5). Payload alone
        // matches; the outstanding-flag guard is what prevents masking the failure.
        Http2PingHandler handler = new Http2PingHandler(1, 1, 3);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        setField(handler, "pingsSent", 5);
        setField(handler, "pingOutstandingSinceNanos", 0L);
        setField(handler, "consecutiveFailures", 1);

        channel.writeInbound(new DefaultHttp2PingFrame(5L, true));

        assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isZero();
        assertThat((int) getField(handler, "consecutiveFailures")).isEqualTo(1);

        channel.finishAndReleaseAll();
    }

    @Test(groups = "unit")
    public void installIfAbsent_isIdempotent() {
        EmbeddedChannel channel = new EmbeddedChannel();

        Http2PingHandler.installIfAbsent(channel, 1, 1, 3);
        Http2PingHandler.installIfAbsent(channel, 1, 1, 3);

        long count = channel.pipeline().toMap().values().stream()
            .filter(h -> h instanceof Http2PingHandler)
            .count();
        assertThat(count).isEqualTo(1);

        channel.finishAndReleaseAll();
    }

    @Test(groups = "unit")
    public void constructor_clampsNonPositiveValues() throws Exception {
        Http2PingHandler handler = new Http2PingHandler(-5, 0, -1);

        assertThat((long) getField(handler, "pingIntervalNanos")).isEqualTo(TimeUnit.SECONDS.toNanos(1));
        assertThat((long) getField(handler, "pingTimeoutNanos")).isEqualTo(TimeUnit.SECONDS.toNanos(1));
        assertThat((int) getField(handler, "failureThreshold")).isEqualTo(1);
    }

    @Test(groups = "unit")
    public void killSwitchOff_clearsOutstandingPingState() throws Exception {
        // M1: When Configs.isHttp2PingHealthEnabled() flips to false, the next maybeSendPing
        // tick must clear any in-flight PING bookkeeping. Otherwise toggling the kill-switch
        // back on after a long dormant window would charge a spurious timeout from a stale
        // pingOutstandingSinceNanos that has been "outstanding" for hours.
        final String prop = "COSMOS.HTTP2_PING_HEALTH_ENABLED";
        final String prior = System.getProperty(prop);
        try {
            Http2PingHandler handler = new Http2PingHandler(1, 1, 3);
            EmbeddedChannel channel = new EmbeddedChannel(handler);

            // Simulate an in-flight PING with a partial failure already accumulated.
            setField(handler, "pingOutstandingSinceNanos", System.nanoTime());
            setField(handler, "consecutiveFailures", 2);

            // Flip kill-switch OFF and trigger the periodic tick.
            System.setProperty(prop, "false");
            ChannelHandlerContext ctx = channel.pipeline().firstContext();
            invokeMaybeSendPing(handler, ctx);

            assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isZero();
            assertThat((int) getField(handler, "consecutiveFailures")).isZero();

            // Timer must still be alive so re-enabling resumes PINGing on the same connection.
            assertThat((Object) getField(handler, "pingTask")).isNotNull();

            channel.finishAndReleaseAll();
        } finally {
            if (prior == null) {
                System.clearProperty(prop);
            } else {
                System.setProperty(prop, prior);
            }
        }
    }

    @Test(groups = "unit")
    public void channelInactive_cancelsPingTask() throws Exception {
        // M2: channelInactive must cancel the scheduled PING task. Otherwise the timer
        // would keep firing on a dead connection until GC reclaims the handler.
        Http2PingHandler handler = new Http2PingHandler(1, 1, 3);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        ScheduledFuture<?> scheduled = (ScheduledFuture<?>) getField(handler, "pingTask");
        assertThat((Object) scheduled).isNotNull();
        assertThat(scheduled.isCancelled()).isFalse();

        // Closing the EmbeddedChannel fires channelInactive on the handler.
        channel.close().syncUninterruptibly();

        assertThat(scheduled.isCancelled()).isTrue();
        assertThat((Object) getField(handler, "pingTask")).isNull();

        channel.finishAndReleaseAll();
    }

    @Test(groups = "unit")
    public void writeFailure_incrementsConsecutiveFailuresAndClosesAtThreshold() throws Exception {
        // Reviewer-flagged invariant (PR #49095): a failed PING write must count as a
        // failed health probe. Otherwise a channel stuck in a state where writes always
        // fail (e.g. H2 codec rejecting frames, stalled flow-control, queued
        // ClosedChannelException not yet propagated) but channel.isActive() stays true
        // would loop on every tick without ever reaching the close threshold.
        final int threshold = 2;
        Http2PingHandler handler = new Http2PingHandler(1, 1, threshold);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Insert an outbound handler at HEAD that fails every write. Outbound writes
        // from Http2PingHandler flow head-ward, so this intercepts them and fails the
        // promise synchronously -- producing the same listener-on-failure path the
        // handler would see if a real H2 codec rejected the PING frame.
        channel.pipeline().addFirst("failOnWrite", new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ReferenceCountUtil.release(msg);
                promise.setFailure(new IOException("simulated write failure"));
            }
        });

        ChannelHandlerContext ctx = channel.pipeline().context(handler);
        long longAgo = System.nanoTime() - TimeUnit.SECONDS.toNanos(60);

        // First tick: force idle -> PING send attempt -> write fails.
        // Expect consecutiveFailures=1, channel still open, task still scheduled.
        setField(handler, "lastActivityNanos", longAgo);
        invokeMaybeSendPing(handler, ctx);
        channel.runPendingTasks(); // run the write + listener on the embedded event loop

        assertThat((int) getField(handler, "consecutiveFailures")).isEqualTo(1);
        assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isZero();
        assertThat(channel.isOpen()).isTrue();
        assertThat((Object) getField(handler, "pingTask")).isNotNull();

        // Second tick: write fails again -> threshold reached -> task cancelled, channel closed.
        setField(handler, "lastActivityNanos", longAgo);
        invokeMaybeSendPing(handler, ctx);
        channel.runPendingTasks();

        assertThat((int) getField(handler, "consecutiveFailures")).isEqualTo(threshold);
        assertThat((Object) getField(handler, "pingTask")).isNull();
        assertThat(channel.isOpen()).isFalse();

        channel.finishAndReleaseAll();
    }

    @Test(groups = "unit")
    public void ackTimeout_incrementsConsecutiveFailuresAndClosesAtThreshold() throws Exception {
        // Reviewer-flagged invariant (PR #49095): the primary operational PING-failure
        // path -- a PING was sent, no ACK arrived within pingTimeoutNanos, and after
        // `failureThreshold` consecutive such timeouts the channel must be closed.
        // Time is sidestepped by pre-setting pingOutstandingSinceNanos to a value
        // older than pingTimeoutNanos so maybeSendPing enters the timeout branch on
        // each invocation.
        final int threshold = 2;
        Http2PingHandler handler = new Http2PingHandler(1, 1, threshold); // interval=1s, timeout=1s
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        ChannelHandlerContext ctx = channel.pipeline().context(handler);

        long pastTimeout = System.nanoTime() - TimeUnit.SECONDS.toNanos(5);

        // First tick: outstanding PING has aged past timeout -> failures=1, channel still open.
        setField(handler, "pingsSent", 1);
        setField(handler, "pingOutstandingSinceNanos", pastTimeout);
        invokeMaybeSendPing(handler, ctx);
        channel.runPendingTasks();

        assertThat((int) getField(handler, "consecutiveFailures")).isEqualTo(1);
        assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isZero();
        assertThat(channel.isOpen()).isTrue();
        assertThat((Object) getField(handler, "pingTask")).isNotNull();

        // Second tick: simulate another outstanding PING that has aged past timeout
        // -> failures=threshold -> task cancelled, channel closed.
        setField(handler, "pingsSent", 2);
        setField(handler, "pingOutstandingSinceNanos", pastTimeout);
        invokeMaybeSendPing(handler, ctx);
        channel.runPendingTasks();

        assertThat((int) getField(handler, "consecutiveFailures")).isEqualTo(threshold);
        assertThat((long) getField(handler, "pingOutstandingSinceNanos")).isZero();
        assertThat((Object) getField(handler, "pingTask")).isNull();
        assertThat(channel.isOpen()).isFalse();

        channel.finishAndReleaseAll();
    }

    private static void invokeMaybeSendPing(Http2PingHandler handler, ChannelHandlerContext ctx) throws Exception {
        Method m = Http2PingHandler.class.getDeclaredMethod("maybeSendPing", ChannelHandlerContext.class);
        m.setAccessible(true);
        m.invoke(handler, ctx);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field f = Http2PingHandler.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = Http2PingHandler.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
