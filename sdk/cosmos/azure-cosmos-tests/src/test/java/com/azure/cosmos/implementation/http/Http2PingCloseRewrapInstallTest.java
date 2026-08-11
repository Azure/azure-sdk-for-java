// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.netty.http.client.HttpClientState;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ReactorNettyClient#installHttp2PingCloseRewrapHandlerIfNeeded} --
 * the per-child-stream install gate for {@link Http2PingCloseRewrapHandler}.
 * <p>
 * In production the handler is installed from reactor-netty's {@code .observe(...)} hook
 * only when ALL of the following hold:
 * <ul>
 *   <li>the connection-lifecycle state is {@link HttpClientState#STREAM_CONFIGURED}
 *       (a child stream was just opened);</li>
 *   <li>PING-health is effectively enabled -- kill-switch
 *       {@code COSMOS.HTTP2_PING_HEALTH_ENABLED} on, a positive PING interval, and HTTP/2
 *       enabled for the client;</li>
 *   <li>the channel is a child stream ({@code parent() != null}).</li>
 * </ul>
 * These tests drive the real production method with an {@link EmbeddedChannel} so the
 * disablement path -- flipping the kill-switch (or any gate input) off must be a true
 * revert-to-baseline that installs nothing on the child pipeline -- is guarded in CI
 * without needing a live HTTP/2 server.
 */
public class Http2PingCloseRewrapInstallTest {

    private static final String PING_HEALTH_ENABLED = "COSMOS.HTTP2_PING_HEALTH_ENABLED";
    private static final String PING_INTERVAL_SECONDS = "COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS";

    private String priorPingHealthEnabled;
    private String priorPingIntervalSeconds;

    @BeforeMethod(groups = "unit")
    public void before_Method() {
        // Snapshot the two PING system properties the gate reads so each test sets an
        // explicit, isolated state and never leaks into sibling tests or CI defaults.
        this.priorPingHealthEnabled = System.getProperty(PING_HEALTH_ENABLED);
        this.priorPingIntervalSeconds = System.getProperty(PING_INTERVAL_SECONDS);
    }

    @AfterMethod(groups = "unit", alwaysRun = true)
    public void after_Method() {
        restore(PING_HEALTH_ENABLED, this.priorPingHealthEnabled);
        restore(PING_INTERVAL_SECONDS, this.priorPingIntervalSeconds);
    }

    @Test(groups = "unit")
    public void installsHandler_whenStreamConfiguredAndPingHealthEnabled() {
        enablePingHealth();
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.STREAM_CONFIGURED, http2Enabled());

            // Presence AND head-of-pipeline position: the rewrap handler must see
            // channelInactive before reactor-netty's stream operations handler, so a
            // regression from addFirst(...) to addLast(...) must fail this test.
            assertThat(child.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNotNull();
            assertThat(child.pipeline().first()).isSameAs(Http2PingCloseRewrapHandler.INSTANCE);
            assertThat(child.pipeline().names().get(0)).isEqualTo(Http2PingCloseRewrapHandler.HANDLER_NAME);
        } finally {
            releaseAll(child);
        }
    }

    @Test(groups = "unit")
    public void skipsInstall_whenKillSwitchOff() {
        // Disablement path: COSMOS.HTTP2_PING_HEALTH_ENABLED=false must remove the work,
        // not just its effect -- nothing is added to the child pipeline.
        System.setProperty(PING_HEALTH_ENABLED, "false");
        System.setProperty(PING_INTERVAL_SECONDS, "1");
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.STREAM_CONFIGURED, http2Enabled());

            assertThat(child.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNull();
        } finally {
            releaseAll(child);
        }
    }

    @Test(groups = "unit")
    public void skipsInstall_whenPingIntervalNonPositive() {
        // A non-positive PING interval disables the PING sender, so there is no
        // PING-timeout close signal to rewrap -> install is skipped.
        System.setProperty(PING_HEALTH_ENABLED, "true");
        System.setProperty(PING_INTERVAL_SECONDS, "0");
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.STREAM_CONFIGURED, http2Enabled());

            assertThat(child.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNull();
        } finally {
            releaseAll(child);
        }
    }

    @Test(groups = "unit")
    public void skipsInstall_whenHttp2DisabledForClient() {
        // PING-health globally on, but HTTP/2 disabled on this client's config ->
        // isPingHealthEffectivelyEnabled is false -> no install.
        enablePingHealth();
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.STREAM_CONFIGURED, http2Disabled());

            assertThat(child.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNull();
        } finally {
            releaseAll(child);
        }
    }

    @Test(groups = "unit")
    public void skipsInstall_whenStateNotStreamConfigured() {
        // Even with PING-health fully enabled, non-STREAM_CONFIGURED lifecycle events
        // (e.g. the parent-channel CONFIGURED state) must not install the child handler.
        enablePingHealth();
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.CONFIGURED, http2Enabled());

            assertThat(child.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNull();
        } finally {
            releaseAll(child);
        }
    }

    @Test(groups = "unit")
    public void skipsInstall_andDoesNotEvaluatePredicate_whenStateNotStreamConfigured() {
        // Guards the short-circuit order: the state check must run BEFORE the PING-health
        // predicate. A null http2Cfg would NPE inside isPingHealthEffectivelyEnabled (the
        // bridge accessor dereferences it), so a non-STREAM_CONFIGURED state must return
        // early without touching the predicate -- proving the cheap state gate stays first
        // and off the hot path.
        enablePingHealth();
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.CONFIGURED, null);

            assertThat(child.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNull();
        } finally {
            releaseAll(child);
        }
    }

    @Test(groups = "unit")
    public void skipsInstall_whenChannelHasNoParent() {
        // Defensive parent() != null guard: a parent-less channel (not a real H2 child
        // stream) must not get the rewrap handler even at STREAM_CONFIGURED.
        enablePingHealth();
        EmbeddedChannel parentless = new EmbeddedChannel();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                parentless, HttpClientState.STREAM_CONFIGURED, http2Enabled());

            assertThat(parentless.pipeline().get(Http2PingCloseRewrapHandler.HANDLER_NAME)).isNull();
        } finally {
            parentless.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void install_isIdempotent() {
        // The @Sharable handler must be installed at most once per child pipeline even if
        // the observe hook fires STREAM_CONFIGURED more than once for the same stream.
        enablePingHealth();
        ChildChannel child = newChildStream();
        try {
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.STREAM_CONFIGURED, http2Enabled());
            ReactorNettyClient.installHttp2PingCloseRewrapHandlerIfNeeded(
                child, HttpClientState.STREAM_CONFIGURED, http2Enabled());

            long count = child.pipeline().toMap().values().stream()
                .filter(h -> h instanceof Http2PingCloseRewrapHandler)
                .count();
            assertThat(count).isEqualTo(1);
        } finally {
            releaseAll(child);
        }
    }

    private static void enablePingHealth() {
        System.setProperty(PING_HEALTH_ENABLED, "true");
        System.setProperty(PING_INTERVAL_SECONDS, "1");
    }

    // Explicit enabled/disabled flags so the gate's HTTP/2 condition does not depend on
    // the ambient COSMOS.HTTP2_ENABLED system property. Constructing the config also runs
    // Http2ConnectionConfig's static initializer, registering the bridge accessor the gate
    // reads.
    private static Http2ConnectionConfig http2Enabled() {
        return new Http2ConnectionConfig().setEnabled(true);
    }

    private static Http2ConnectionConfig http2Disabled() {
        return new Http2ConnectionConfig().setEnabled(false);
    }

    private static ChildChannel newChildStream() {
        ChildChannel child = new ChildChannel();
        child.setParentChannel(new EmbeddedChannel());
        return child;
    }

    private static void restore(String key, String prior) {
        if (prior == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, prior);
        }
    }

    private static void releaseAll(ChildChannel child) {
        Channel parent = child.parent();
        child.finishAndReleaseAll();
        if (parent instanceof EmbeddedChannel) {
            ((EmbeddedChannel) parent).finishAndReleaseAll();
        }
    }

    /**
     * An {@link EmbeddedChannel} whose {@link #parent()} can be set after construction,
     * mimicking an HTTP/2 child stream whose parent is the TCP connection channel. The
     * parent field is assigned post-construction (never read during the superclass
     * constructor), which avoids the captured-variable-before-super pitfall of an
     * anonymous subclass.
     */
    private static final class ChildChannel extends EmbeddedChannel {
        private Channel parentChannel;

        @Override
        public Channel parent() {
            return this.parentChannel;
        }

        void setParentChannel(Channel parent) {
            this.parentChannel = parent;
        }
    }
}
