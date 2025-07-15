// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.azure.spring.cloud.feature.management.models.Variant;

import jakarta.servlet.http.HttpServletRequest;
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
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"), Mockito.isNull()))
            .thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", null);
    }

    @Test
    public void setSavedValue() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("setAttribute"), Mockito.isNull()))
            .thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", null);

        // The second time should return the same value, but not increase the non-snapshot count.
        assertTrue(featureManagerSnapshot.isEnabledAsync("setAttribute").block());
        verify(featureManager, times(1)).isEnabledAsync("setAttribute", null);
    }

    @Test
    public void featureDisabled() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("featureDisabled"), Mockito.isNull()))
            .thenReturn(Mono.just(false));

        assertFalse(featureManagerSnapshot.isEnabledAsync("featureDisabled").block());
        verify(featureManager, times(1)).isEnabledAsync("featureDisabled", null);
    }

    @Test
    public void featureEnabledWithException() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("featureEnabledWithException"), Mockito.isNull()))
            .thenReturn(Mono.error(new RuntimeException("Test Exception")));

        assertThrows(RuntimeException.class,
            () -> featureManagerSnapshot.isEnabledAsync("featureEnabledWithException").block());
        verify(featureManager, times(1)).isEnabledAsync("featureEnabledWithException", null);
    }

    @Test
    public void featureEnabledMultipleTimes() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("featureEnabledMultipleTimes"), Mockito.isNull()))
            .thenReturn(Mono.just(true));

        assertTrue(featureManagerSnapshot.isEnabledAsync("featureEnabledMultipleTimes").block());
        verify(featureManager, times(1)).isEnabledAsync("featureEnabledMultipleTimes", null);

        assertTrue(featureManagerSnapshot.isEnabledAsync("featureEnabledMultipleTimes").block());
        verify(featureManager, times(1)).isEnabledAsync("featureEnabledMultipleTimes", null);

        assertTrue(featureManagerSnapshot.isEnabledAsync("featureEnabledMultipleTimes", null).block());
    }

    @Test
    public void featureDisabledMultipleTimes() throws InterruptedException, ExecutionException {
        when(featureManager.isEnabledAsync(Mockito.matches("featureDisabledMultipleTimes"), Mockito.isNull()))
            .thenReturn(Mono.just(false));
        when(featureManager.isEnabledAsync(Mockito.matches("featureDisabledMultipleTimes"),
            Mockito.matches("my Context")))
                .thenReturn(Mono.just(false));

        assertFalse(featureManagerSnapshot.isEnabled("featureDisabledMultipleTimes"));
        verify(featureManager, times(1)).isEnabledAsync("featureDisabledMultipleTimes", null);

        assertFalse(featureManagerSnapshot.isEnabled("featureDisabledMultipleTimes"));
        verify(featureManager, times(1)).isEnabledAsync("featureDisabledMultipleTimes", null);

        assertFalse(featureManagerSnapshot.isEnabled("featureDisabledMultipleTimes", "my Context"));
        verify(featureManager, times(1)).isEnabledAsync("featureDisabledMultipleTimes", null);
    }

    @Test
    public void getVariantAsync() throws InterruptedException, ExecutionException {
        Variant variant = new Variant("variant1", true);
        when(featureManager.getVariantAsync(Mockito.matches("featureWithVariant"), Mockito.matches("my Context")))
            .thenReturn(Mono.just(variant));

        assertEquals(variant, featureManagerSnapshot.getVariantAsync("featureWithVariant", "my Context").block());
        verify(featureManager, times(1)).getVariantAsync("featureWithVariant", "my Context");
    }

    @Test
    public void getVariantAsyncWithException() throws InterruptedException, ExecutionException {
        when(featureManager.getVariantAsync(Mockito.matches("featureWithVariantException"), Mockito.isNull()))
            .thenReturn(Mono.error(new RuntimeException("Test Exception")));

        assertThrows(RuntimeException.class,
            () -> featureManagerSnapshot.getVariantAsync("featureWithVariantException").block());
        verify(featureManager, times(1)).getVariantAsync("featureWithVariantException", null);
    }

    @Test
    public void getVariant() {
        Variant variant = new Variant("variant1", true);
        when(featureManager.getVariantAsync(Mockito.matches("featureWithVariant"), Mockito.isNull()))
            .thenReturn(Mono.just(variant));

        assertEquals(variant, featureManagerSnapshot.getVariant("featureWithVariant"));
        verify(featureManager, times(1)).getVariantAsync("featureWithVariant", null);
    }

    @Test
    public void getVariantWithException() {
        when(featureManager.getVariant(Mockito.matches("featureWithVariantException"), Mockito.isNull()))
            .thenThrow(new RuntimeException("Test Exception"));

        assertThrows(RuntimeException.class, () -> featureManagerSnapshot.getVariant("featureWithVariantException"));
        verify(featureManager, times(1)).getVariantAsync("featureWithVariantException", null);
    }

    @Test
    public void getVariantMultipleTimes() {
        Variant variant = new Variant("variant1", true);
        when(featureManager.getVariantAsync(Mockito.matches("featureWithVariantMultipleTimes"), Mockito.isNull()))
            .thenReturn(Mono.just(variant));
        when(featureManager.getVariantAsync(Mockito.matches("featureWithVariantMultipleTimes"),
            Mockito.matches("my Context"))).thenReturn(Mono.just(variant));

        assertEquals(variant, featureManagerSnapshot.getVariant("featureWithVariantMultipleTimes"));
        verify(featureManager, times(1)).getVariantAsync("featureWithVariantMultipleTimes", null);

        assertEquals(variant, featureManagerSnapshot.getVariant("featureWithVariantMultipleTimes"));
        verify(featureManager, times(1)).getVariantAsync("featureWithVariantMultipleTimes", null);

        assertEquals(variant, featureManagerSnapshot.getVariant("featureWithVariantMultipleTimes", "my Context"));
        verify(featureManager, times(1)).getVariantAsync("featureWithVariantMultipleTimes", null);
    }

}
