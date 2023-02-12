// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdRequestManagerTests {

    @Test
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
                Duration.ofSeconds(1).toNanos());
        RntbdClientChannelHealthChecker.Timestamps timestamps = ReflectionUtils.getTimestamps(rntbdRequestManager);

        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        ChannelFuture channelFuture = Mockito.mock(ChannelFuture.class);
        Mockito.when(channelHandlerContext.write(Mockito.any(), Mockito.any())).thenReturn(channelFuture);
        Mockito.when(channelFuture.addListener(Mockito.any())).thenReturn(channelFuture);

        RntbdRequestArgs requestArgs = new RntbdRequestArgs(
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document),
                new Uri(new URI("http://localhost/replica-path").toString())
        );
        long requestTimeoutInNanos = Duration.ofMinutes(5).toNanos();
        RntbdRequestTimer requestTimer = new RntbdRequestTimer(requestTimeoutInNanos, requestTimeoutInNanos);
        RntbdRequestRecord rntbdRequestRecord = new AsyncRntbdRequestRecord(requestArgs, requestTimer);

        ChannelPromise promise = Mockito.mock(ChannelPromise.class);

        // Test transitTimeout is 0 at start point
        rntbdRequestManager.write(channelHandlerContext, rntbdRequestRecord, promise);
        assertThat(timestamps.tansitTimeoutCount()).isZero();

        // Test when a transit timeout happens, the transitTimeoutCount is increased
        rntbdRequestRecord.expire();
        assertThat(timestamps.tansitTimeoutCount()).isOne();

        // Test when there is channelRead, transitTimeout is cleared out
        Mockito.when(channelHandlerContext.flush()).thenReturn(channelHandlerContext);
        ChannelFuture closeChannelFuture = Mockito.mock(ChannelFuture.class);
        Mockito.when(channelHandlerContext.close()).thenReturn(closeChannelFuture);
        rntbdRequestManager.channelRead(channelHandlerContext, rntbdRequestRecord);
        assertThat(timestamps.tansitTimeoutCount()).isZero();
    }
}
