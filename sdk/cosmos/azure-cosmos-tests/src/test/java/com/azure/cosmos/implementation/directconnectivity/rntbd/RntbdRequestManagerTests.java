// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryInfo;
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
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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

        ClientTelemetry clientTelemetryMock = Mockito.mock(ClientTelemetry.class);
        ClientTelemetryInfo clientTelemetryInfoMock = Mockito.mock(ClientTelemetryInfo.class);
        Mockito.when(clientTelemetryMock.getClientTelemetryInfo()).thenReturn(clientTelemetryInfoMock);
        Mockito.when(clientTelemetryInfoMock.getMachineId()).thenReturn("testClientVmId");

        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config, clientTelemetryMock);

        RntbdConnectionStateListener connectionStateListener = Mockito.mock(RntbdConnectionStateListener.class);

        RntbdRequestManager rntbdRequestManager = new RntbdRequestManager(
                healthChecker,
                config,
                connectionStateListener,
            null);

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

        ClientTelemetry clientTelemetryMock = Mockito.mock(ClientTelemetry.class);
        ClientTelemetryInfo clientTelemetryInfoMock = Mockito.mock(ClientTelemetryInfo.class);
        Mockito.when(clientTelemetryMock.getClientTelemetryInfo()).thenReturn(clientTelemetryInfoMock);
        Mockito.when(clientTelemetryInfoMock.getMachineId()).thenReturn("testClientVmId");

        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config, clientTelemetryMock);

        RntbdConnectionStateListener connectionStateListener = Mockito.mock(RntbdConnectionStateListener.class);

        RntbdRequestManager rntbdRequestManager = new RntbdRequestManager(
            healthChecker,
            config,
            connectionStateListener,
            null);

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

    @Test(groups = { "unit" })
    public void multipleSslHandshakeCompletionEventTest() {
        // Test for receiving multiple SslHandshakeCompletionEvent, the IdleStateHandler should be added only once
        int LONG_TIMEOUT_NANOS = 100000000;
        SslContext sslContextMock = Mockito.mock(SslContext.class);
        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
            new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
            sslContextMock,
            LogLevel.INFO);

        ClientTelemetry clientTelemetryMock = Mockito.mock(ClientTelemetry.class);

        RntbdClientChannelHealthChecker healthChecker = new RntbdClientChannelHealthChecker(config, clientTelemetryMock);

        RntbdConnectionStateListener connectionStateListener = Mockito.mock(RntbdConnectionStateListener.class);

        RntbdRequestManager rntbdRequestManager = new RntbdRequestManager(
            healthChecker,
            config,
            connectionStateListener,
            null);


        ChannelHandlerContext channelHandlerContextMock = Mockito.mock(ChannelHandlerContext.class);
        ChannelPipeline channelPipelineMock = Mockito.mock(ChannelPipeline.class);
        Mockito.when(channelHandlerContextMock.channel()).thenReturn(Mockito.mock(Channel.class));


        Mockito.when(channelHandlerContextMock.pipeline()).thenReturn(channelPipelineMock);
        Mockito.when(channelHandlerContextMock.pipeline().get(IdleStateHandler.class.toString())).thenReturn(null);

        rntbdRequestManager.channelRegistered(channelHandlerContextMock);

        SslHandshakeCompletionEvent completionEvent = Mockito.mock(SslHandshakeCompletionEvent.class);
        Mockito.when(completionEvent.isSuccess()).thenReturn(true);
        rntbdRequestManager.userEventTriggered(channelHandlerContextMock, completionEvent);
        Mockito.when(channelHandlerContextMock.pipeline().get(IdleStateHandler.class.toString())).thenReturn(new IdleStateHandler(
            LONG_TIMEOUT_NANOS,
            LONG_TIMEOUT_NANOS,
            0,
            TimeUnit.NANOSECONDS));
        rntbdRequestManager.userEventTriggered(channelHandlerContextMock, completionEvent);

        // addAfter should be called only once even the SslHandshakeCompletionEvent is received twice
        Mockito.verify(channelHandlerContextMock.pipeline(), Mockito.times(1)
        ).addAfter(
            Mockito.any(String.class),
            Mockito.any(String.class),
            Mockito.any(IdleStateHandler.class)
        );
    }
}
