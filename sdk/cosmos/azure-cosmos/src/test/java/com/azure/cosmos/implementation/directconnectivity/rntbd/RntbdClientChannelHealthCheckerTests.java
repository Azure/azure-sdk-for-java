// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.Future;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdClientChannelHealthCheckerTests {
    private static final long writeHangGracePeriodInNanos = 2L * 1_000_000_000L;
    private static final long readHangGracePeriodInNanos = (45L + 10L) * 1_000_000_000L;

    @DataProvider
    public static Object[][] isHealthyWithReasonArgs() {
        return new Object[][]{
                // expect failureReason
                { false },
                { true }
        };
    }

    @Test(groups = { "unit" },  dataProvider = "isHealthyWithReasonArgs")
    public void isHealthyForWriteHangTests(boolean withFailureReason) {
        SslContext sslContextMock = Mockito.mock(SslContext.class);

        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
                new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
                sslContextMock,
                LogLevel.INFO);

        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config);
        Channel channelMock = Mockito.mock(Channel.class);
        ChannelPipeline channelPipelineMock = Mockito.mock(ChannelPipeline.class);
        RntbdRequestManager rntbdRequestManagerMock = Mockito.mock(RntbdRequestManager.class);
        SingleThreadEventLoop eventLoopMock = new DefaultEventLoop();
        RntbdClientChannelHealthChecker.Timestamps timestampsMock = Mockito.mock(RntbdClientChannelHealthChecker.Timestamps.class);

        Mockito.when(channelMock.pipeline()).thenReturn(channelPipelineMock);
        Mockito.when(channelPipelineMock.get(RntbdRequestManager.class)).thenReturn(rntbdRequestManagerMock);
        Mockito.when(channelMock.eventLoop()).thenReturn(eventLoopMock);
        Mockito.when(rntbdRequestManagerMock.snapshotTimestamps()).thenReturn(timestampsMock);

        long lastChannelWriteAttemptNanoTime = System.nanoTime() - writeHangGracePeriodInNanos - 10;
        long lastChannelWriteNanoTime = lastChannelWriteAttemptNanoTime - config.sendHangDetectionTimeInNanos() - 10;

        Mockito.when(timestampsMock.lastChannelWriteAttemptNanoTime()).thenReturn(lastChannelWriteAttemptNanoTime);
        Mockito.when(timestampsMock.lastChannelWriteNanoTime()).thenReturn(lastChannelWriteNanoTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to non-responding write"));
        } else {
            Future<Boolean> healthyResult = healthChecker.isHealthy(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isFalse();
        }
    }

    @Test(groups = { "unit" }, dataProvider = "isHealthyWithReasonArgs")
    public void isHealthyForReadHangTests(boolean withFailureReason) {
        SslContext sslContextMock = Mockito.mock(SslContext.class);

        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
                new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
                sslContextMock,
                LogLevel.INFO);

        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config);
        Channel channelMock = Mockito.mock(Channel.class);
        ChannelPipeline channelPipelineMock = Mockito.mock(ChannelPipeline.class);
        RntbdRequestManager rntbdRequestManagerMock = Mockito.mock(RntbdRequestManager.class);
        SingleThreadEventLoop eventLoopMock = new DefaultEventLoop();
        RntbdClientChannelHealthChecker.Timestamps timestampsMock = Mockito.mock(RntbdClientChannelHealthChecker.Timestamps.class);

        Mockito.when(channelMock.pipeline()).thenReturn(channelPipelineMock);
        Mockito.when(channelPipelineMock.get(RntbdRequestManager.class)).thenReturn(rntbdRequestManagerMock);
        Mockito.when(channelMock.eventLoop()).thenReturn(eventLoopMock);
        Mockito.when(rntbdRequestManagerMock.snapshotTimestamps()).thenReturn(timestampsMock);

        long lastChannelWriteNanoTime = System.nanoTime() - readHangGracePeriodInNanos - 10;
        long lastChannelWriteAttemptNanoTime = lastChannelWriteNanoTime;
        long lastChannelReadNanoTime = lastChannelWriteNanoTime - config.receiveHangDetectionTimeInNanos() - 10;

        Mockito.when(timestampsMock.lastChannelWriteAttemptNanoTime()).thenReturn(lastChannelWriteAttemptNanoTime);
        Mockito.when(timestampsMock.lastChannelWriteNanoTime()).thenReturn(lastChannelWriteNanoTime);
        Mockito.when(timestampsMock.lastChannelReadNanoTime()).thenReturn(lastChannelReadNanoTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to non-responding read"));
        } else {
            Future<Boolean> healthyResult = healthChecker.isHealthy(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isFalse();
        }
    }

    @Test(groups = { "unit" }, dataProvider = "isHealthyWithReasonArgs")
    public void isHealthyForReadHangWithTransitTimeoutTests(boolean withFailureReason) {
        SslContext sslContextMock = Mockito.mock(SslContext.class);

        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
                new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
                sslContextMock,
                LogLevel.INFO);

        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config);
        Channel channelMock = Mockito.mock(Channel.class);
        ChannelPipeline channelPipelineMock = Mockito.mock(ChannelPipeline.class);
        RntbdRequestManager rntbdRequestManagerMock = Mockito.mock(RntbdRequestManager.class);
        SingleThreadEventLoop eventLoopMock = new DefaultEventLoop();
        RntbdClientChannelHealthChecker.Timestamps timestampsMock = Mockito.mock(RntbdClientChannelHealthChecker.Timestamps.class);

        Mockito.when(channelMock.pipeline()).thenReturn(channelPipelineMock);
        Mockito.when(channelPipelineMock.get(RntbdRequestManager.class)).thenReturn(rntbdRequestManagerMock);
        Mockito.when(channelMock.eventLoop()).thenReturn(eventLoopMock);
        Mockito.when(rntbdRequestManagerMock.snapshotTimestamps()).thenReturn(timestampsMock);

        long lastChannelWriteNanoTime = System.nanoTime();
        long lastChannelWriteAttemptNanoTime = lastChannelWriteNanoTime;
        long lastChannelReadNanoTime = lastChannelWriteNanoTime - config.receiveHangDetectionTimeInNanos() - 10;
        int transitTimeoutCount = config.timeoutDetectionHighFrequencyThreshold();

        Mockito.when(timestampsMock.lastChannelWriteAttemptNanoTime()).thenReturn(lastChannelWriteAttemptNanoTime);
        Mockito.when(timestampsMock.lastChannelWriteNanoTime()).thenReturn(lastChannelWriteNanoTime);
        Mockito.when(timestampsMock.lastChannelReadNanoTime()).thenReturn(lastChannelReadNanoTime);
        Mockito.when(timestampsMock.tansitTimeoutCount()).thenReturn(transitTimeoutCount);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to non-responding read"));
        } else {
            Future<Boolean> healthyResult = healthChecker.isHealthy(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isFalse();
        }
    }
}
