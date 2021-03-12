// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import reactor.core.publisher.Mono;

@RunWith(MockitoJUnitRunner.class)
public class FeatureManagerSnapshotTest {

    @Mock
    FeatureManager featureManager;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    FeatureManagerSnapshot featureManagerSnapshot;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void setAttribute() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"))).thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");
    }

    @Test
    public void setSavedValue() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"))).thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");

        // The second time should return the same value, but not increase the non-snapshot count.
        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");
    }

}
