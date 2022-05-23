// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import reactor.core.publisher.Mono;

public class DynamicFeatureManagerSnapshotTest {

    @Mock
    DynamicFeatureManager dynamicFeatureManager;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    DynamicFeatureManagerSnapshot dynamicFeatureManagerSnapshot;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void initialLoad() throws InterruptedException, ExecutionException {
        when(dynamicFeatureManager.getVariantAsync(Mockito.matches("myVariant"), Mockito.any()))
            .thenReturn(Mono.just(new Object()));

        Object returnValue = dynamicFeatureManagerSnapshot.getVariantAsync("myVariant", Object.class).block();
        assertNotNull(returnValue);
        verify(dynamicFeatureManager, times(1)).getVariantAsync("myVariant", Object.class);
    }

    @Test
    public void initialAlreadyExists() throws InterruptedException, ExecutionException {
        when(dynamicFeatureManager.getVariantAsync(Mockito.matches("exitingVariant"), Mockito.any()))
            .thenReturn(Mono.just(new Object()));

        Object returnValue = dynamicFeatureManagerSnapshot.getVariantAsync("exitingVariant", Object.class).block();
        assertNotNull(returnValue);
        verify(dynamicFeatureManager, times(1)).getVariantAsync("exitingVariant", Object.class);

        returnValue = dynamicFeatureManagerSnapshot.getVariantAsync("exitingVariant", Object.class).block();
        assertNotNull(returnValue);
        verify(dynamicFeatureManager, times(1)).getVariantAsync("exitingVariant", Object.class);
    }

    @Test
    public void invalidType() throws InterruptedException, ExecutionException {
        when(dynamicFeatureManager.getVariantAsync(Mockito.matches("exitingVariant"), Mockito.any()))
            .thenReturn(Mono.just(1));

        Object returnValue = dynamicFeatureManagerSnapshot.getVariantAsync("exitingVariant", Integer.class).block();
        assertEquals(1, returnValue);
        returnValue = dynamicFeatureManagerSnapshot.getVariantAsync("exitingVariant", String.class).block();
        assertNull(returnValue);
        verify(dynamicFeatureManager, times(1)).getVariantAsync("exitingVariant", Integer.class);
    }

}
