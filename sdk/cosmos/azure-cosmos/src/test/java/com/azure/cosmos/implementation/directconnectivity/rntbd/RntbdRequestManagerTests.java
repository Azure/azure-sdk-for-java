// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;

public class RntbdRequestManagerTests {

    @Test(groups = { "unit" })
    public void transitTimeoutTimestampTests() throws URISyntaxException {
        SslContext sslContextMock = Mockito.mock(SslContext.class);
        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
                new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
                sslContextMock,
                LogLevel.INFO);
        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config);

        RntbdConnectionStateListener connectionStateListener = Mockito.mock(RntbdConnectionStateListener.class);

        RntbdRequestManager rntbdRequestManager = new RntbdRequestManager(
                healthChecker,
                30,
                connectionStateListener,
                Duration.ofSeconds(1).toNanos(),
                null,
                config.tcpNetworkRequestTimeoutInNanos());
        RntbdClientChannelHealthChecker.Timestamps timestamps = rntbdRequestManager.getTimestamps();

        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);

        Channel channelMock = Mockito.mock(Channel.class);
        SingleThreadEventLoop eventLoopMock = new DefaultEventLoop();
        Mockito.when(channelMock.eventLoop()).thenReturn(eventLoopMock);

        ChannelPromise defaultChannelPromise = new DefaultChannelPromise(channelMock);
        defaultChannelPromise.setSuccess();

        Mockito.when(channelHandlerContext.write(Mockito.any(), Mockito.any())).thenReturn(defaultChannelPromise);

        RntbdRequestArgs requestArgs = new RntbdRequestArgs(
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document),
                new Uri(new URI("http://localhost/replica-path").toString())
        );
        long requestTimeoutInNanos = Duration.ofMinutes(5).toNanos();
        RntbdRequestTimer requestTimer = new RntbdRequestTimer(requestTimeoutInNanos, requestTimeoutInNanos);
        RntbdRequestRecord rntbdRequestRecord = new AsyncRntbdRequestRecord(requestArgs, requestTimer);

        ChannelPromise promise = Mockito.mock(ChannelPromise.class);

        // Test transitTimeout is 0 at start point
        Instant previousLastWriteTime = timestamps.lastChannelWriteTime();
        Instant previousLastWriteAttemptTime = timestamps.lastChannelWriteAttemptTime();
        Instant previousLastChannelReadTime = timestamps.lastChannelReadTime();

        rntbdRequestManager.write(channelHandlerContext, rntbdRequestRecord, promise);

        assertThat(timestamps.transitTimeoutCount()).isZero();
        assertThat(timestamps.transitTimeoutStartingTime()).isNull();
        assertThat(timestamps.tansitTimeoutWriteCount()).isZero();
        assertThat(timestamps.lastChannelWriteTime()).isAfterOrEqualTo(previousLastWriteTime);
        assertThat(timestamps.lastChannelWriteAttemptTime()).isAfterOrEqualTo(previousLastWriteAttemptTime);
        assertThat(timestamps.lastChannelReadTime()).isEqualTo(previousLastChannelReadTime);

        // Test when a transit timeout happens, the transitTimeoutCount is increased
        rntbdRequestRecord.expire();
        assertThat(timestamps.transitTimeoutCount()).isOne();
        assertThat(timestamps.tansitTimeoutWriteCount()).isZero();
        assertThat(timestamps.transitTimeoutStartingTime()).isNotNull();
        assertThat(Duration.between(timestamps.transitTimeoutStartingTime(), Instant.now())).isLessThan(Duration.ofSeconds(5));

        // Test when there is channelRead, transitTimeout is cleared out
        previousLastWriteTime = timestamps.lastChannelWriteTime();
        previousLastWriteAttemptTime = timestamps.lastChannelWriteAttemptTime();
        previousLastChannelReadTime = timestamps.lastChannelReadTime();

        Mockito.when(channelHandlerContext.flush()).thenReturn(channelHandlerContext);

        ChannelFuture closeChannelFuture = Mockito.mock(ChannelFuture.class);
        Mockito.when(channelHandlerContext.close()).thenReturn(closeChannelFuture);
        rntbdRequestManager.channelRead(channelHandlerContext, rntbdRequestRecord);
        assertThat(timestamps.transitTimeoutCount()).isZero();
        assertThat(timestamps.transitTimeoutStartingTime()).isNull();
        assertThat(timestamps.lastChannelReadTime()).isAfterOrEqualTo(previousLastChannelReadTime);
        assertThat(timestamps.lastChannelWriteAttemptTime()).isEqualTo(previousLastWriteAttemptTime);
        assertThat(timestamps.lastChannelWriteTime()).isAfterOrEqualTo(previousLastWriteTime);
    }

    @Test(groups = { "unit" })
    public void rntbdContextResponseTests() {
        // Test when getting rntbdContext response, the lastReadTimestamp should be marked
        SslContext sslContextMock = Mockito.mock(SslContext.class);
        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
            new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
            sslContextMock,
            LogLevel.INFO);
        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config);

        RntbdConnectionStateListener connectionStateListener = Mockito.mock(RntbdConnectionStateListener.class);

        RntbdRequestManager rntbdRequestManager = new RntbdRequestManager(
            healthChecker,
            30,
            connectionStateListener,
            Duration.ofSeconds(1).toNanos(),
            null,
            config.tcpNetworkRequestTimeoutInNanos());

        RntbdClientChannelHealthChecker.Timestamps timestamps = rntbdRequestManager.getTimestamps();

        ChannelHandlerContext channelHandlerContextMock = Mockito.mock(ChannelHandlerContext.class);
        ChannelPipeline channelPipelineMock = Mockito.mock(ChannelPipeline.class);
        Mockito.when(channelPipelineMock.fireChannelRegistered()).thenReturn(channelPipelineMock);
        Mockito.when(channelHandlerContextMock.channel()).thenReturn(Mockito.mock(Channel.class));

        RntbdContextNegotiator rntbdContextNegotiatorMock = Mockito.mock(RntbdContextNegotiator.class);
        doNothing().when(rntbdContextNegotiatorMock).removeInboundHandler();
        doNothing().when(rntbdContextNegotiatorMock).removeOutboundHandler();
        Mockito.when(channelPipelineMock.get(RntbdContextNegotiator.class)).thenReturn(rntbdContextNegotiatorMock);
        Mockito.when(channelHandlerContextMock.pipeline()).thenReturn(channelPipelineMock);

        rntbdRequestManager.channelRegistered(channelHandlerContextMock);

        Instant lastReadTimestamp = timestamps.lastChannelReadTime();
        RntbdContext rntbdContextMock = Mockito.mock(RntbdContext.class);
        rntbdRequestManager.userEventTriggered(channelHandlerContextMock, rntbdContextMock);
        assertThat(timestamps.lastChannelReadTime()).isAfterOrEqualTo(lastReadTimestamp);
    }
}
