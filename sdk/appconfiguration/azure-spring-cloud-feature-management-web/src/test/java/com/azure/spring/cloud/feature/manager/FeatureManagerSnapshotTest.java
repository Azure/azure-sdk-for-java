// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureManagerSnapshotTest {

    @Mock
    FeatureManager featureManager;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    FeatureManagerSnapshot featureManagerSnapshot;

    @BeforeEach
    void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setAttribute() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"))).thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");
    }

    @Test
    void setSavedValue() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"))).thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");

        // The second time should return the same value, but not increase the non-snapshot count.
        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");
    }

}
