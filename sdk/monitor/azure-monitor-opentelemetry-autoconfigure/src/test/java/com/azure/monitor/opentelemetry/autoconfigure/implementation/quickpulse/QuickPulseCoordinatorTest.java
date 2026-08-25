// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.FilteringConfiguration;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.IsSubscribedHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QuickPulseCoordinatorTest {
    private static final long VERIFY_TIMEOUT_MILLIS = 10000;
    private static final long THREAD_JOIN_TIMEOUT_MILLIS = 10000;

    private static final HttpHeaderName QPS_STATUS_HEADER = HttpHeaderName.fromString("x-ms-qps-subscribed");
    private static final HttpHeaderName QPS_SERVICE_POLLING_INTERVAL_HINT
        = HttpHeaderName.fromString("x-ms-qps-service-polling-interval-hint");
    private static final HttpHeaderName QPS_SERVICE_ENDPOINT_REDIRECT
        = HttpHeaderName.fromString("x-ms-qps-service-endpoint-redirect-v2");

    @Test
    void testOnlyPings() throws InterruptedException {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);
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

        try {
            Mockito.verify(mockPingSender, Mockito.timeout(VERIFY_TIMEOUT_MILLIS).atLeastOnce()).ping(null);
        } finally {
            stopAndJoin(coordinator, thread);
        }

        Mockito.verify(mockFetcher, Mockito.never()).prepareQuickPulseDataForSend();

        Mockito.verify(mockSender, Mockito.never()).startSending();
        Mockito.verify(mockSender, Mockito.never()).getQuickPulseStatus();

        // make sure QP_IS_OFF after ping
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_OFF);
    }

    @Test
    void testOnePingAndThenOnePost() throws InterruptedException {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
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

        QuickPulseDataCollector collector = new QuickPulseDataCollector(configuration);
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

        try {
            Mockito.verify(mockFetcher, Mockito.timeout(VERIFY_TIMEOUT_MILLIS).atLeastOnce())
                .prepareQuickPulseDataForSend();
        } finally {
            stopAndJoin(coordinator, thread);
        }

        Mockito.verify(mockSender, Mockito.times(1)).startSending();
        Mockito.verify(mockSender, Mockito.times(1)).getQuickPulseStatus();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        // Make sure QP_IS_OFF after one post and ping
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_OFF);
    }

    private static void stopAndJoin(QuickPulseCoordinator coordinator, Thread thread) throws InterruptedException {
        coordinator.stop();
        thread.join(THREAD_JOIN_TIMEOUT_MILLIS);
        assertThat(thread.isAlive()).isFalse();
    }

    @Test
    void acceptsSameLiveMetricsDomainRedirect() {
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);
        Mockito.doReturn("https://westus.livediagnostics.monitor.azure.com/")
            .when(mockPingSender)
            .getQuickPulseEndpoint();

        QuickPulseCoordinator coordinator = createCoordinator(mockSender, mockPingSender);

        HttpHeaders rawPingHeaders = new HttpHeaders();
        rawPingHeaders.add(QPS_STATUS_HEADER, "true");
        rawPingHeaders.add(QPS_SERVICE_ENDPOINT_REDIRECT,
            "https://eastus.livediagnostics.monitor.azure.com/QuickPulseService.svc/");

        assertThat(coordinator.handleReceivedPingHeaders(new IsSubscribedHeaders(rawPingHeaders)))
            .isEqualTo(QuickPulseStatus.QP_IS_ON);
        verify(mockSender).setRedirectEndpointPrefix("https://eastus.livediagnostics.monitor.azure.com/");
    }

    @Test
    void acceptsSameHostRedirect() {
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);
        Mockito.doReturn("https://live.example.com/").when(mockPingSender).getQuickPulseEndpoint();

        QuickPulseCoordinator coordinator = createCoordinator(mockSender, mockPingSender);

        HttpHeaders rawPingHeaders = new HttpHeaders();
        rawPingHeaders.add(QPS_STATUS_HEADER, "true");
        rawPingHeaders.add(QPS_SERVICE_ENDPOINT_REDIRECT, "https://live.example.com/QuickPulseService.svc/");

        assertThat(coordinator.handleReceivedPingHeaders(new IsSubscribedHeaders(rawPingHeaders)))
            .isEqualTo(QuickPulseStatus.QP_IS_ON);
        verify(mockSender).setRedirectEndpointPrefix("https://live.example.com/");
    }

    @Test
    void rejectsCrossOriginRedirect() {
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);
        Mockito.doReturn("https://westus.livediagnostics.monitor.azure.com/")
            .when(mockPingSender)
            .getQuickPulseEndpoint();

        QuickPulseCoordinator coordinator = createCoordinator(mockSender, mockPingSender);

        HttpHeaders rawPingHeaders = new HttpHeaders();
        rawPingHeaders.add(QPS_STATUS_HEADER, "true");
        rawPingHeaders.add(QPS_SERVICE_ENDPOINT_REDIRECT, "https://attacker.invalid/QuickPulseService.svc/");

        assertThat(coordinator.handleReceivedPingHeaders(new IsSubscribedHeaders(rawPingHeaders)))
            .isEqualTo(QuickPulseStatus.QP_IS_ON);
        Mockito.verify(mockSender, Mockito.never()).setRedirectEndpointPrefix(any());
    }

    @Test
    void rejectsInvalidRedirects() {
        assertRedirectRejected("http://eastus.livediagnostics.monitor.azure.com/QuickPulseService.svc/");
        assertRedirectRejected("https://user@eastus.livediagnostics.monitor.azure.com/QuickPulseService.svc/");
        assertRedirectRejected("https://eastus.livediagnostics.monitor.azure.com:444/QuickPulseService.svc/");
        assertRedirectRejected(
            "https://evil.livediagnostics.monitor.azure.com.attacker.invalid/QuickPulseService.svc/");
        assertRedirectRejected("https://evil.live.example.com/QuickPulseService.svc/");
    }

    private static void assertRedirectRejected(String redirectLink) {
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);
        Mockito.doReturn("https://westus.livediagnostics.monitor.azure.com/")
            .when(mockPingSender)
            .getQuickPulseEndpoint();

        QuickPulseCoordinator coordinator = createCoordinator(mockSender, mockPingSender);

        HttpHeaders rawPingHeaders = new HttpHeaders();
        rawPingHeaders.add(QPS_STATUS_HEADER, "true");
        rawPingHeaders.add(QPS_SERVICE_ENDPOINT_REDIRECT, redirectLink);

        assertThat(coordinator.handleReceivedPingHeaders(new IsSubscribedHeaders(rawPingHeaders)))
            .isEqualTo(QuickPulseStatus.QP_IS_ON);
        Mockito.verify(mockSender, Mockito.never()).setRedirectEndpointPrefix(any());
    }

    private static QuickPulseCoordinator createCoordinator(QuickPulseDataSender mockSender,
        QuickPulsePingSender mockPingSender) {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
        QuickPulseCoordinatorInitData initData
            = new QuickPulseCoordinatorInitDataBuilder().withDataFetcher(mock(QuickPulseDataFetcher.class))
                .withDataSender(mockSender)
                .withPingSender(mockPingSender)
                .withCollector(new QuickPulseDataCollector(configuration))
                .withWaitBetweenPingsInMillis(10L)
                .withWaitBetweenPostsInMillis(10L)
                .withWaitOnErrorInMillis(10L)
                .build();
        return new QuickPulseCoordinator(initData);
    }

    @Disabled("sporadically failing on CI")
    @Test
    void testOnePingAndThenOnePostWithRedirectedLink() throws InterruptedException {
        AtomicReference<FilteringConfiguration> configuration = new AtomicReference<>(new FilteringConfiguration());
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
            .withCollector(new QuickPulseDataCollector(configuration))
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
