// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;

class QuickPulseCoordinatorTest {

    @Test
    void testOnlyPings() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, quickPulseConfiguration);
        Mockito.doReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF))
            .when(mockPingSender)
            .ping(null);

        QuickPulseCoordinatorInitData initData =
            new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(mockFetcher)
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

        Mockito.verify(mockFetcher, Mockito.never()).prepareQuickPulseDataForSend(null);

        Mockito.verify(mockSender, Mockito.never()).startSending();
        Mockito.verify(mockSender, Mockito.never()).getQuickPulseHeaderInfo();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        // make sure QP_IS_OFF after ping
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_OFF);

        assertThat(quickPulseConfiguration.getEtag()).isNull();

    }

    @Test
    void testOnePingAndThenOnePost() throws InterruptedException {

        QuickPulseDataFetcher mockFetcher = mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = mock(QuickPulseDataSender.class);
        Mockito.doReturn(new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF))
            .when(mockSender)
            .getQuickPulseHeaderInfo();
        QuickPulsePingSender mockPingSender = mock(QuickPulsePingSender.class);
        Mockito.when(mockPingSender.ping(null))
            .thenReturn(
                new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_ON),
                new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF));

        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, quickPulseConfiguration);
        QuickPulseCoordinatorInitData initData =
            new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(mockFetcher)
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

        Mockito.verify(mockFetcher, Mockito.atLeast(1)).prepareQuickPulseDataForSend(null);

        Mockito.verify(mockSender, Mockito.times(1)).startSending();
        Mockito.verify(mockSender, Mockito.times(1)).getQuickPulseHeaderInfo();

        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        // Make sure QP_IS_OFF after one post and ping
        assertThat(collector.getQuickPulseStatus()).isEqualTo(QuickPulseStatus.QP_IS_OFF);

        assertThat(quickPulseConfiguration.getEtag()).isNull();
    }

    @Disabled("sporadically failing on CI")
    @Test
    void testOnePingAndThenOnePostWithRedirectedLink() throws InterruptedException {
        QuickPulseDataFetcher mockFetcher = Mockito.mock(QuickPulseDataFetcher.class);
        QuickPulseDataSender mockSender = Mockito.mock(QuickPulseDataSender.class);
        QuickPulsePingSender mockPingSender = Mockito.mock(QuickPulsePingSender.class);
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();

        Mockito.doNothing().when(mockFetcher).prepareQuickPulseDataForSend(notNull());
        Mockito.doReturn(
                new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_ON, "https://new.endpoint.com", 100))
            .when(mockPingSender)
            .ping(any());
        Mockito.doReturn(
                new QuickPulseHeaderInfo(QuickPulseStatus.QP_IS_OFF, "https://new.endpoint.com", 400))
            .when(mockSender)
            .getQuickPulseHeaderInfo();

        QuickPulseCoordinatorInitData initData =
            new QuickPulseCoordinatorInitDataBuilder()
                .withDataFetcher(mockFetcher)
                .withDataSender(mockSender)
                .withPingSender(mockPingSender)
                .withCollector(new QuickPulseDataCollector(true, quickPulseConfiguration))
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

        Mockito.verify(mockFetcher, Mockito.atLeast(1))
            .prepareQuickPulseDataForSend("https://new.endpoint.com");
        Mockito.verify(mockPingSender, Mockito.atLeast(1)).ping(null);
        Mockito.verify(mockPingSender, Mockito.times(2)).ping("https://new.endpoint.com");
        assertThat(quickPulseConfiguration.getEtag()).isNull();
    }
}
