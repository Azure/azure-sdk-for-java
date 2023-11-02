// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.azure.spring.cloud.feature.management.models.Variant;

import reactor.core.publisher.Mono;

public class FeatureManagerSnapshotTest {

    @Mock
    FeatureManager featureManager;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    FeatureManagerSnapshot featureManagerSnapshot;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void setAttribute() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"))).thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute");
    }

    @Test
    public void setSavedValueAsync() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"), Mockito.matches("Context")))
            .thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute", "Context").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", "Context");

        // The second time should return the same value, but not increase the non-snapshot count.
        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute", "Context").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", "Context");
    }

    @Test
    public void setSavedValue() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"), Mockito.matches("Context")))
            .thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabled("setAttribute", "Context"));
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", "Context");

        // The second time should return the same value, but not increase the non-snapshot count.
        assertTrue(featureManagerSnapshot.isEnabled("setAttribute", "Context"));
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", "Context");
    }

    @Test
    public void getVariant() throws InterruptedException, ExecutionException {
        Variant variant = new Variant("fake-variant", "test");
        when(featureManager.getVariantAsync(Mockito.matches("getVariant"))).thenReturn(Mono.just(variant));

        Variant result = featureManagerSnapshot.getVariantAsync("getVariant").block();
        assertEquals(variant.getName(), result.getName());
        assertEquals(variant.getValue(), result.getValue());
        verify(featureManager, times(1)).getVariantAsync("getVariant");
    }

    @Test
    public void getVariantAsyncSaved() throws InterruptedException, ExecutionException {
        Variant variant = new Variant("fake-variant", "test");
        when(featureManager.getVariantAsync(Mockito.matches("getVariant"), Mockito.matches("Context")))
            .thenReturn(Mono.just(variant));

        featureManagerSnapshot.getVariantAsync("getVariant", "Context").block();
        verify(featureManager, times(1)).getVariantAsync("getVariant", "Context");

        featureManagerSnapshot.getVariantAsync("getVariant", "Context").block();
        verify(featureManager, times(1)).getVariantAsync("getVariant", "Context");
    }
    
    @Test
    public void getVariantSaved() throws InterruptedException, ExecutionException {
        Variant variant = new Variant("fake-variant", "test");
        when(featureManager.getVariantAsync(Mockito.matches("getVariant"), Mockito.matches("Context")))
            .thenReturn(Mono.just(variant));

        featureManagerSnapshot.getVariant("getVariant", "Context");
        verify(featureManager, times(1)).getVariantAsync("getVariant", "Context");

        featureManagerSnapshot.getVariant("getVariant", "Context");
        verify(featureManager, times(1)).getVariantAsync("getVariant", "Context");
    }

}
