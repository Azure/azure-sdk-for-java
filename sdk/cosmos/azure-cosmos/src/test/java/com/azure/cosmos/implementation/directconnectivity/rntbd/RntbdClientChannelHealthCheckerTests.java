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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class RntbdClientChannelHealthCheckerTests {
    private static final long writeHangGracePeriodInNanos = 2L * 1_000_000_000L;
    private static final long readHangGracePeriodInNanos = (45L + 10L) * 1_000_000_000L;
    private static final long recentReadWindowInNanos = 1_000_000_000L;

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

        Instant currentTime = Instant.now();
        Instant lastChannelWriteAttemptTime = Instant.now().minusNanos(writeHangGracePeriodInNanos).minusNanos(10);
        Instant lastChannelWriteTime = lastChannelWriteAttemptTime.minusNanos(config.sendHangDetectionTimeInNanos()).minusNanos(10);
        Instant lastChannelReadTime = currentTime.minusNanos(recentReadWindowInNanos).minusNanos(10);

        Mockito.when(timestampsMock.lastChannelWriteAttemptTime()).thenReturn(lastChannelWriteAttemptTime);
        Mockito.when(timestampsMock.lastChannelWriteTime()).thenReturn(lastChannelWriteTime);
        Mockito.when(timestampsMock.lastChannelReadTime()).thenReturn(lastChannelReadTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to non-responding write"));
        } else {
            healthChecker.isHealthy(channelMock)
                .addListener(((Future<Boolean>healthyResult) -> {
                    assertThat(healthyResult.isSuccess()).isTrue();
                    assertThat(healthyResult.getNow()).isFalse();
                }));
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

        Instant lastChannelWriteTime = Instant.now().minusNanos(readHangGracePeriodInNanos).minusNanos(10);
        Instant lastChannelWriteAttemptTime = lastChannelWriteTime;
        Instant lastChannelReadTime = lastChannelWriteTime.minusNanos(config.receiveHangDetectionTimeInNanos()).minusNanos(10);

        Mockito.when(timestampsMock.lastChannelWriteAttemptTime()).thenReturn(lastChannelWriteAttemptTime);
        Mockito.when(timestampsMock.lastChannelWriteTime()).thenReturn(lastChannelWriteTime);
        Mockito.when(timestampsMock.lastChannelReadTime()).thenReturn(lastChannelReadTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to non-responding read"));
        } else {
            healthChecker.isHealthy(channelMock)
                .addListener(((Future<Boolean>healthyResult) -> {
                    assertThat(healthyResult.isSuccess()).isTrue();
                    assertThat(healthyResult.getNow()).isFalse();
                }));
        }
    }

    @Test(groups = { "unit" }, dataProvider = "isHealthyWithReasonArgs")
    public void transitTimeoutTimeLimitTests(boolean withFailureReason) {
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

        Instant current = Instant.now();
        Instant lastChannelReadTime = current.minusNanos(config.timeoutDetectionTimeLimitInNanos()).minusNanos(10);
        Instant lastChannelWriteTime = lastChannelReadTime.plusSeconds(1);
        Instant lastChannelWriteAttemptTime = lastChannelWriteTime;

        Mockito.when(timestampsMock.lastChannelReadTime()).thenReturn(lastChannelReadTime);
        Mockito.when(timestampsMock.tansitTimeoutCount()).thenReturn(1);
        Mockito.when(timestampsMock.lastChannelWriteTime()).thenReturn(lastChannelWriteTime);
        Mockito.when(timestampsMock.lastChannelWriteAttemptTime()).thenReturn(lastChannelWriteAttemptTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to transit timeout detection time limit"));
        } else {
            healthChecker.isHealthy(channelMock)
                .addListener(((Future<Boolean>healthyResult) -> {
                    assertThat(healthyResult.isSuccess()).isTrue();
                    assertThat(healthyResult.getNow()).isFalse();
                }));
        }
    }

    @Test(groups = { "unit" }, dataProvider = "isHealthyWithReasonArgs")
    public void transitTimeoutHighFrequencyTests(boolean withFailureReason) {
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

        Instant current = Instant.now();
        Instant lastChannelReadTime = current.minusNanos(config.timeoutDetectionHighFrequencyTimeLimitInNanos()).minusNanos(10);
        Instant lastChannelWriteTime = lastChannelReadTime.plusSeconds(1);
        Instant lastChannelWriteAttemptTime = lastChannelWriteTime;
        int timeoutCount = config.timeoutDetectionHighFrequencyThreshold() + 1;

        Mockito.when(timestampsMock.lastChannelReadTime()).thenReturn(lastChannelReadTime);
        Mockito.when(timestampsMock.tansitTimeoutCount()).thenReturn(timeoutCount);
        Mockito.when(timestampsMock.lastChannelWriteTime()).thenReturn(lastChannelWriteTime);
        Mockito.when(timestampsMock.lastChannelWriteAttemptTime()).thenReturn(lastChannelWriteAttemptTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to transit timeout high frequency threshold hit"));
        } else {
            healthChecker.isHealthy(channelMock)
                .addListener(((Future<Boolean>healthyResult) -> {
                    assertThat(healthyResult.isSuccess()).isTrue();
                    assertThat(healthyResult.getNow()).isFalse();
                }));
        }
    }

    @Test(groups = { "unit" }, dataProvider = "isHealthyWithReasonArgs")
    public void transitTimeoutOnWriteTests(boolean withFailureReason) {
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

        Instant current = Instant.now();
        Instant lastChannelReadTime = current.minusNanos(config.timeoutDetectionOnWriteTimeLimitInNanos()).minusNanos(10);
        Instant lastChannelWriteTime = lastChannelReadTime.plusSeconds(1);
        Instant lastChannelWriteAttemptTime = lastChannelWriteTime;
        int writeTimeoutCount = config.timeoutDetectionOnWriteThreshold() + 1;

        Mockito.when(timestampsMock.lastChannelReadTime()).thenReturn(lastChannelReadTime);
        Mockito.when(timestampsMock.tansitTimeoutCount()).thenReturn(writeTimeoutCount);
        Mockito.when(timestampsMock.tansitTimeoutWriteCount()).thenReturn(writeTimeoutCount);
        Mockito.when(timestampsMock.lastChannelWriteTime()).thenReturn(lastChannelWriteTime);
        Mockito.when(timestampsMock.lastChannelWriteAttemptTime()).thenReturn(lastChannelWriteAttemptTime);

        if (withFailureReason) {
            Future<String> healthyResult = healthChecker.isHealthyWithFailureReason(channelMock);
            assertThat(healthyResult.isSuccess()).isTrue();
            assertThat(healthyResult.getNow()).isNotEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(healthyResult.getNow().contains("health check failed due to transit timeout on write threshold hit"));
        } else {
            healthChecker.isHealthy(channelMock)
                .addListener(((Future<Boolean> healthyResult) -> {
                    assertThat(healthyResult.isSuccess()).isTrue();
                    assertThat(healthyResult.getNow()).isFalse();
                }));
        }
    }
}
