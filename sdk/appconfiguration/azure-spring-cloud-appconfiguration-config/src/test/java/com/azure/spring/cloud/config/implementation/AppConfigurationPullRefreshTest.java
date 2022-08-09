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
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.azure.spring.cloud.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class AppConfigurationPullRefreshTest {

    @Mock
    private ApplicationEventPublisher publisher;

    private AppConfigurationProviderProperties providerProperties;

    private Duration refreshInterval = Duration.ofMinutes(10);

    private RefreshEventData eventData;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);

        eventData = new RefreshEventData();
        providerProperties = new AppConfigurationProviderProperties();
    }

    @AfterEach
    public void cleanupMethod(TestInfo testInfo) throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void refreshNoChange() throws InterruptedException, ExecutionException {
        try (MockedStatic<AppConfigurationRefreshUtil> refreshUtils = Mockito
            .mockStatic(AppConfigurationRefreshUtil.class)) {
            refreshUtils.when(() -> AppConfigurationRefreshUtil.refreshStoresCheck(Mockito.eq(providerProperties),
                Mockito.eq(clientFactoryMock), Mockito.eq(refreshInterval)))
                .thenReturn(eventData);

            AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(providerProperties, clientFactoryMock,
                refreshInterval);
            assertFalse(refresh.refreshConfigurations().get());
        }
    }

    @Test
    public void refreshUpdate() throws InterruptedException, ExecutionException {
        eventData.setMessage("Updated");
        try (MockedStatic<AppConfigurationRefreshUtil> refreshUtils = Mockito
            .mockStatic(AppConfigurationRefreshUtil.class)) {
            refreshUtils.when(() -> AppConfigurationRefreshUtil.refreshStoresCheck(Mockito.eq(providerProperties),
                Mockito.eq(clientFactoryMock), Mockito.eq(refreshInterval)))
                .thenReturn(eventData);

            AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(providerProperties, clientFactoryMock,
                refreshInterval);
            refresh.setApplicationEventPublisher(publisher);
            assertTrue(refresh.refreshConfigurations().get());
        }
    }
}
