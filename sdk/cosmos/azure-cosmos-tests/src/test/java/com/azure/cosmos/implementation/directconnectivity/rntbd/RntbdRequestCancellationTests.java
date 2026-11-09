// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryInfo;
import com.azure.cosmos.implementation.cpu.CpuLoadHistory;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class RntbdRequestCancellationTests {
    private static final String CANCELLATION_PRONE = "health check failed due to channel being cancellation prone";

    @DataProvider
    public Object[][] cancellationGates() {
        return new Object[][] {
            { 2, Duration.ofSeconds(11), false },
            { 3, Duration.ofSeconds(5), false },
            { 3, Duration.ofSeconds(11), true }
        };
    }

    @Test(groups = "unit", dataProvider = "cancellationGates", timeOut = 10000)
    public void cancellationHealthCheckUsesRealPendingRequests(int cancellations, Duration readAge,
                                                              boolean unhealthy) throws Exception {
        try (MockedStatic<CpuMemoryMonitor> cpu = Mockito.mockStatic(CpuMemoryMonitor.class);
             Fixture fixture = new Fixture()) {
            cpu.when(CpuMemoryMonitor::getCpuLoad).thenReturn(mock(CpuLoadHistory.class));
            fixture.cancel(cancellations);
            fixture.readAge = readAge;

            assertThat(fixture.manager.getTimestamps().cancellationCount()).isEqualTo(cancellations);
            assertThat(fixture.manager.getTimestamps().transitTimeoutCount()).isZero();
            assertThat(fixture.manager.pendingRequestCount()).isZero();
            String reason = fixture.checker.isHealthyWithFailureReason(fixture.channel).get(1, TimeUnit.SECONDS);
            if (unhealthy) {
                assertThat(reason).contains(CANCELLATION_PRONE, "cancellationCountSinceLastSuccessfulRead: 3");
            } else {
                assertThat(reason).isEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            }

            fixture.channel.pipeline().fireUserEventTriggered(IdleStateEvent.READER_IDLE_STATE_EVENT);
            fixture.channel.runPendingTasks();
            assertThat(fixture.channel.isActive()).isEqualTo(!unhealthy);
            assertThat(fixture.address.getHealthStatus() == Uri.HealthStatus.Unhealthy).isEqualTo(unhealthy);
        }
    }

    @Test(groups = "unit", timeOut = 10000)
    public void readCompletionResetsCancellationsAndChannelsDoNotShareCounts() throws Exception {
        try (MockedStatic<CpuMemoryMonitor> cpu = Mockito.mockStatic(CpuMemoryMonitor.class);
             Fixture first = new Fixture();
             Fixture second = new Fixture()) {
            cpu.when(CpuMemoryMonitor::getCpuLoad).thenReturn(mock(CpuLoadHistory.class));
            first.cancel(2);
            second.cancel(2);
            first.readAge = Duration.ofSeconds(11);
            second.readAge = Duration.ofSeconds(11);
            assertThat(first.manager.getTimestamps().cancellationCount()).isEqualTo(2);
            assertThat(second.manager.getTimestamps().cancellationCount()).isEqualTo(2);
            assertThat(first.checker.isHealthyWithFailureReason(first.channel).get(1, TimeUnit.SECONDS))
                .isEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            assertThat(second.checker.isHealthyWithFailureReason(second.channel).get(1, TimeUnit.SECONDS))
                .isEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);

            first.manager.getTimestamps().channelReadCompleted();
            assertThat(first.manager.getTimestamps().cancellationCount()).isZero();
            first.cancel(1);
            assertThat(first.manager.getTimestamps().cancellationCount()).isEqualTo(1);
            assertThat(second.manager.getTimestamps().cancellationCount()).isEqualTo(2);
            assertThat(first.checker.isHealthyWithFailureReason(first.channel).get(1, TimeUnit.SECONDS))
                .isEqualTo(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
        }
    }

    @Test(groups = "unit", timeOut = 10000)
    public void transitExpirationDoesNotCountAsCancellation() {
        try (Fixture fixture = new Fixture()) {
            RntbdRequestRecord record = fixture.write();
            assertThat(record.expire()).isTrue();
            assertThat(record.isCancelled()).isFalse();
            assertThat(fixture.manager.getTimestamps().transitTimeoutCount()).isEqualTo(1);
            assertThat(fixture.manager.getTimestamps().cancellationCount()).isZero();
            assertThat(fixture.manager.pendingRequestCount()).isZero();
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final Uri address = Uri.create("rntbd://localhost:12345/replica/");
        private final RntbdRequestTimer timer =
            new RntbdRequestTimer(Duration.ofMinutes(1).toNanos(), Duration.ofMillis(10).toNanos());
        private final RntbdClientChannelHealthChecker checker;
        private final RntbdRequestManager manager;
        private final EmbeddedChannel channel;
        private Duration readAge = Duration.ZERO;

        private Fixture() {
            RntbdEndpoint.Config config = new RntbdEndpoint.Config(
                new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build(),
                mock(SslContext.class), LogLevel.INFO);
            assertThat(config.cancellationCountSinceLastReadThreshold()).isEqualTo(3);
            assertThat(config.nonRespondingChannelReadDelayTimeLimitInNanos())
                .isEqualTo(Duration.ofSeconds(10).toNanos());
            ClientTelemetry telemetry = mock(ClientTelemetry.class);
            ClientTelemetryInfo info = mock(ClientTelemetryInfo.class);
            when(telemetry.getClientTelemetryInfo()).thenReturn(info);
            when(info.getMachineId()).thenReturn("cancellation-test");
            checker = new RntbdClientChannelHealthChecker(config, telemetry);
            RntbdConnectionStateListener listener =
                new RntbdConnectionStateListener(mock(RntbdEndpoint.class), null, null);
            listener.onBeforeSendRequest(address);
            manager = spy(new RntbdRequestManager(checker, config, listener, null));
            // Control only read age. Counts come from the real manager's cancelled pending records.
            Mockito.doAnswer(invocation -> {
                RntbdClientChannelHealthChecker.Timestamps snapshot =
                    spy(new RntbdClientChannelHealthChecker.Timestamps(manager.getTimestamps()));
                when(snapshot.lastChannelReadTime()).thenReturn(Instant.now().minus(readAge));
                return snapshot;
            }).when(manager).snapshotTimestamps();
            channel = new EmbeddedChannel(manager);
        }

        private RntbdRequestRecord write() {
            Object outbound;
            while ((outbound = channel.readOutbound()) != null) {
                assertThat(outbound).isSameAs(RntbdHealthCheckRequest.MESSAGE);
                ReferenceCountUtil.release(outbound);
            }
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
            RntbdRequestRecord record = new AsyncRntbdRequestRecord(new RntbdRequestArgs(request, address), timer);
            channel.writeOutbound(record);
            assertThat(channel.<RntbdRequestRecord>readOutbound()).isSameAs(record);
            assertThat(manager.pendingRequestCount()).isEqualTo(1);
            return record;
        }

        private void cancel(int count) {
            for (int i = 0; i < count; i++) {
                assertThat(write().cancel(true)).isTrue();
            }
        }

        @Override
        public void close() {
            channel.finishAndReleaseAll();
            timer.close();
        }
    }
}
