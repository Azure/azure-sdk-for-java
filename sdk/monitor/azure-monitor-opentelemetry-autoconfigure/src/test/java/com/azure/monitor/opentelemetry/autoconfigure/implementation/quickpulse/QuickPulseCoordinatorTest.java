// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.IsSubscribedHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class QuickPulseCoordinatorTest {
    private static final HttpHeaderName QPS_STATUS_HEADER = HttpHeaderName.fromString("x-ms-qps-subscribed");
    private static final HttpHeaderName QPS_SERVICE_POLLING_INTERVAL_HINT
        = HttpHeaderName.fromString("x-ms-qps-service-polling-interval-hint");
    private static final HttpHeaderName QPS_SERVICE_ENDPOINT_REDIRECT
        = HttpHeaderName.fromString("x-ms-qps-service-endpoint-redirect-v2");

    @Test
    void testOnlyPings() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        QuickPulseDataCollector collector = new QuickPulseDataCollector();
        HttpHeaders headers = new HttpHeaders();
        headers.add(QPS_STATUS_HEADER, "false");
        IsSubscribedHeaders pingHeaders = new IsSubscribedHeaders(headers);
        Mockito.doReturn(pingHeaders).when(mockPingSender).ping(null);

        QuickPulseCoordinatorInitData initData = new QuickPulseCoordinatorInitDataBuilder().withDataFetcher(mockFetcher)
            .withDataSender(mockSender)
            .withPingSender(mockPingSender)
            .withCollector(collector)
            .withWaitBetweenPingsInMillis(10L)
            .withWaitBetweenPostsInMillis(10L)
            .withWaitOnErrorInMillis(10L)
            .build();

        QuickPulseCoordinator coordinator = new QuickPulseCoordinator(initData);
        Thread thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(1000);
        coordinator.stop();

        thread.join();

        Mockito.verify(mockFetcher, Mockito.never()).prepareQuickPulseDataForSend();

        Mockito.verify(mockSender, Mockito.never()).startSending();
        Mockito.verify(mockSender, Mockito.never()).getQuickPulseStatus();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        // make sure QP_IS_OFF after ping
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_OFF);
    }

    @Test
    void testOnePingAndThenOnePost() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        Mockito.doReturn(QuickPulseStatus.QP_IS_OFF).when(mockSender).getQuickPulseStatus();

        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        HttpHeaders rawHeadersPingOn = new HttpHeaders();
        rawHeadersPingOn.add(QPS_STATUS_HEADER, "true");
        IsSubscribedHeaders pingHeadersOn = new IsSubscribedHeaders(rawHeadersPingOn);
        HttpHeaders rawHeadersPingOff = new HttpHeaders();
        rawHeadersPingOff.add(QPS_STATUS_HEADER, "false");
        IsSubscribedHeaders pingHeadersOff = new IsSubscribedHeaders(rawHeadersPingOff);
        Mockito.when(mockPingSender.ping(null)).thenReturn(pingHeadersOn, pingHeadersOff);

        QuickPulseDataCollector collector = new QuickPulseDataCollector();
        QuickPulseCoordinatorInitData initData = new QuickPulseCoordinatorInitDataBuilder().withDataFetcher(mockFetcher)
            .withDataSender(mockSender)
            .withPingSender(mockPingSender)
            .withCollector(collector)
            .withWaitBetweenPingsInMillis(10L)
            .withWaitBetweenPostsInMillis(10L)
            .withWaitOnErrorInMillis(10L)
            .build();

        QuickPulseCoordinator coordinator = new QuickPulseCoordinator(initData);
        Thread thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(1000);
        coordinator.stop();

        thread.join();

        Mockito.verify(mockFetcher, Mockito.atLeast(1)).prepareQuickPulseDataForSend();

        Mockito.verify(mockSender, Mockito.times(1)).startSending();
        Mockito.verify(mockSender, Mockito.times(1)).getQuickPulseStatus();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        // Make sure QP_IS_OFF after one post and ping
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_OFF);
    }

    @Disabled("sporadically failing on CI")
    @Test
    void testOnePingAndThenOnePostWithRedirectedLink() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = Mockito.mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);

        HttpHeaders rawPingHeaders = new HttpHeaders();
        rawPingHeaders.add(QPS_STATUS_HEADER, "on");
        rawPingHeaders.add(QPS_SERVICE_ENDPOINT_REDIRECT, "https://new.endpoint.com");
        rawPingHeaders.add(QPS_SERVICE_POLLING_INTERVAL_HINT, "100");
        IsSubscribedHeaders pingHeadersOn = new IsSubscribedHeaders(rawPingHeaders);

        Mockito.doNothing().when(mockFetcher).prepareQuickPulseDataForSend();
        Mockito.doReturn(pingHeadersOn).when(mockPingSender).ping(any());
        Mockito.doReturn(QuickPulseStatus.QP_IS_OFF).when(mockSender).getQuickPulseStatus();

        QuickPulseCoordinatorInitData initData = new QuickPulseCoordinatorInitDataBuilder().withDataFetcher(mockFetcher)
            .withDataSender(mockSender)
            .withPingSender(mockPingSender)
            .withCollector(new QuickPulseDataCollector())
            .withWaitBetweenPingsInMillis(10L)
            .withWaitBetweenPostsInMillis(10L)
            .withWaitOnErrorInMillis(10L)
            .build();

        QuickPulseCoordinator coordinator = new QuickPulseCoordinator(initData);
        Thread thread = new Thread(coordinator);
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(1100);
        coordinator.stop();

        thread.join();

        Mockito.verify(mockFetcher, Mockito.atLeast(1)).prepareQuickPulseDataForSend();
        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        Mockito.verify(mockPingSender, Mockito.times(2)).ping("https://new.endpoint.com");
    }
}
