// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @Mock
    private RefreshEventData eventDataMock;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;
    
    @Mock
    private AppConfigurationRefreshUtil refreshUtilMock;
    
    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void refreshNoChange() throws InterruptedException, ExecutionException {
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(eventDataMock);

        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            (long) 0, replicaLookUpMock, refreshUtilMock);
        assertFalse(refresh.refreshConfigurations().block());
       
    }

    @Test
    public void refreshUpdate() throws InterruptedException, ExecutionException {
        when(eventDataMock.getMessage()).thenReturn("Updated");
        when(eventDataMock.getDoRefresh()).thenReturn(true);
        when(refreshUtilMock.refreshStoresCheck(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(eventDataMock);

        AppConfigurationPullRefresh refresh = new AppConfigurationPullRefresh(clientFactoryMock, refreshInterval,
            (long) 0, replicaLookUpMock, refreshUtilMock);
        refresh.setApplicationEventPublisher(publisher);
        assertTrue(refresh.refreshConfigurations().block());
        
    }
}
