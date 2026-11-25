// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;

import reactor.core.publisher.Mono;

public class AppConfigurationPullRefreshTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ReplicaLookUp replicaLookUpMock;

    private final Duration refreshInterval = Duration.ofMinutes(10);

    @Mock
    private RefreshEventData eventDataMock;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @Mock
    private AppConfigurationRefreshUtil refreshUtilMock;

    @Mock
    private StateHolder stateHolderMock;

    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
    }

    @AfterEach
    public void cleanup() throws Exception {
        session.finishMocking();
    }

    @Test
    public void refreshNoChange() throws InterruptedException, ExecutionException {
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(eventDataMock);

        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            replicaLookUpMock, stateHolderMock, refreshUtilMock);
        assertFalse(refresh.refreshConfigurations().block());

    }

    @Test
    public void refreshUpdate() throws InterruptedException, ExecutionException {
        when(eventDataMock.getMessage()).thenReturn("Updated");
        when(eventDataMock.getDoRefresh()).thenReturn(true);
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(eventDataMock);

        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            replicaLookUpMock, stateHolderMock, refreshUtilMock);
        refresh.setApplicationEventPublisher(publisher);
        assertTrue(refresh.refreshConfigurations().block());

    }

    @Test
    public void refreshIsDeferredUntilSubscribed() {
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(eventDataMock);
        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            replicaLookUpMock, stateHolderMock, refreshUtilMock);

        Mono<Boolean> result = refresh.refreshConfigurations();

        verifyNoInteractions(refreshUtilMock);
        assertFalse(result.block(Duration.ofSeconds(5)));
        verify(refreshUtilMock).refreshStoresCheck(clientFactoryMock, refreshInterval, 30L, replicaLookUpMock);
    }

    @Test
    public void refreshRunsOffCallingThreadAndSkipsConcurrentRefresh() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> refreshThread = new AtomicReference<>();
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenAnswer(invocation -> {
                refreshThread.set(Thread.currentThread());
                started.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
                return eventDataMock;
            });
        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            replicaLookUpMock, stateHolderMock, refreshUtilMock);

        CompletableFuture<Boolean> result = refresh.refreshConfigurations().toFuture();
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertNotSame(Thread.currentThread(), refreshThread.get());
            assertFalse(result.isDone());
            assertFalse(refresh.refreshConfigurations().block(Duration.ofSeconds(5)));
            verify(refreshUtilMock).refreshStoresCheck(clientFactoryMock, refreshInterval, 30L, replicaLookUpMock);
        } finally {
            release.countDown();
            result.get(5, TimeUnit.SECONDS);
        }
        assertFalse(result.get());
    }

    @Test
    public void refreshSignalsFailureAndAllowsNextRefresh() {
        IllegalStateException failure = new IllegalStateException("Refresh failed");
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenThrow(failure).thenReturn(eventDataMock);
        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            replicaLookUpMock, stateHolderMock, refreshUtilMock);

        Mono<Boolean> result = refresh.refreshConfigurations();

        assertSame(failure, assertThrows(IllegalStateException.class, () -> result.block(Duration.ofSeconds(5))));
        verify(stateHolderMock).updateNextRefreshTime(refreshInterval, 30L);
        assertFalse(refresh.refreshConfigurations().block(Duration.ofSeconds(5)));
    }
}
