// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.DefaultHttp2PingFrame;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Http2PingHandler}.
 * <p>
 * Covers state transitions that do not require advancing time:
 * <ul>
 *   <li>PING ACK with matching payload resets the failure counter (RFC 9113 §6.7 payload echo).</li>
 *   <li>PING ACK with mismatched payload is ignored (late ACK after timeout cannot mask degradation).</li>
 *   <li>{@code installIfAbsent} is idempotent.</li>
 *   <li>Constructor clamps non-positive interval / timeout / threshold to safe minimums.</li>
 * </ul>
 * Time-based behaviors (interval-driven PING send, timeout-driven failure increment, threshold-driven
 * close) are exercised by the integration test {@code Http2PingKeepaliveTest} under Docker with real
 * network fault injection.
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
