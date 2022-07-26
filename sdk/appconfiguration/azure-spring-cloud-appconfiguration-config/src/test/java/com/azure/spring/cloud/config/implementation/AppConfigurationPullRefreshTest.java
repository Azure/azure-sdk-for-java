// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
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
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class AppConfigurationPullRefreshTest {
    
    @Mock
    private ApplicationEventPublisher publisher;

    private AppConfigurationProperties clientProperties;

    private AppConfigurationProviderProperties providerProperties;

    private List<ConfigStore> configStores;

    private Duration refreshInterval = Duration.ofMinutes(10);

    private RefreshEventData eventData;

    @Mock
    private ClientFactory clientFactoryMock;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);

        clientProperties = new AppConfigurationProperties();
        clientProperties.setRefreshInterval(refreshInterval);
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
                Mockito.eq(clientFactoryMock), Mockito.any(), Mockito.eq(refreshInterval)))
                .thenReturn(eventData);

            AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientProperties, providerProperties,
                clientFactoryMock);
            assertFalse(refresh.refreshConfigurations().get());
        }
    }

    @Test
    public void refreshUpdate() throws InterruptedException, ExecutionException {
        eventData.setMessage("Updated");
        try (MockedStatic<AppConfigurationRefreshUtil> refreshUtils = Mockito
            .mockStatic(AppConfigurationRefreshUtil.class)) {
            refreshUtils.when(() -> AppConfigurationRefreshUtil.refreshStoresCheck(Mockito.eq(providerProperties),
                Mockito.eq(clientFactoryMock), Mockito.any(), Mockito.eq(refreshInterval)))
                .thenReturn(eventData);

            AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientProperties, providerProperties,
                clientFactoryMock);
            refresh.setApplicationEventPublisher(publisher);
            assertTrue(refresh.refreshConfigurations().get());
        }
    }
}
