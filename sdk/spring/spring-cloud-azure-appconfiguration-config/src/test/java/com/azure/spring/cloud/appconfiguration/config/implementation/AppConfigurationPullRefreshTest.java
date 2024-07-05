// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class AppConfigurationPullRefreshTest {

    @Mock
    private ApplicationEventPublisher publisher;
    
    @Mock
    private ReplicaLookUp replicaLookUpMock;

    private final Duration refreshInterval = Duration.ofMinutes(10);

    private RefreshEventData eventData;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;
    
    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);
        eventData = new RefreshEventData();
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void refreshNoChange() throws InterruptedException, ExecutionException {
        try (MockedStatic<AppConfigurationRefreshUtil> refreshUtils = Mockito
            .mockStatic(AppConfigurationRefreshUtil.class)) {
            refreshUtils
                .when(() -> AppConfigurationRefreshUtil.refreshStoresCheck(Mockito.eq(clientFactoryMock),
                    Mockito.eq(refreshInterval), Mockito.any(), Mockito.any()))
                .thenReturn(eventData);

            AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
                (long) 0, replicaLookUpMock);
            assertFalse(refresh.refreshConfigurations().block());
        }
    }

    @Test
    public void refreshUpdate() throws InterruptedException, ExecutionException {
        eventData.setMessage("Updated");
        try (MockedStatic<AppConfigurationRefreshUtil> refreshUtils = Mockito
            .mockStatic(AppConfigurationRefreshUtil.class)) {
            refreshUtils
                .when(() -> AppConfigurationRefreshUtil.refreshStoresCheck(Mockito.eq(clientFactoryMock),
                    Mockito.eq(refreshInterval), Mockito.any(), Mockito.any()))
                .thenReturn(eventData);

            AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
                (long) 0, replicaLookUpMock);
            refresh.setApplicationEventPublisher(publisher);
            assertTrue(refresh.refreshConfigurations().block());
        }
    }
}
