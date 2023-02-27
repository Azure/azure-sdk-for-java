// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

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
import org.springframework.context.ApplicationEventPublisher;

import com.azure.spring.cloud.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class AppConfigurationPullRefreshTest {

    @Mock
    private ApplicationEventPublisher publisher;

    private final Duration refreshInterval = Duration.ofMinutes(10);

    private RefreshEventData eventData;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        eventData = new RefreshEventData();
    }

    @AfterEach
    public void cleanupMethod() throws Exception {
        MockitoAnnotations.openMocks(this).close();
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
                (long) 0);
            assertFalse(refresh.refreshConfigurations().get());
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
                (long) 0);
            refresh.setApplicationEventPublisher(publisher);
            assertTrue(refresh.refreshConfigurations().get());
        }
    }
}
