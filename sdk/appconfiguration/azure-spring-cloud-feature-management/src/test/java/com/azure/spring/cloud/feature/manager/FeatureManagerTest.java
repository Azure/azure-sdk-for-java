// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.manager.implementation.models.Feature;
import com.azure.spring.cloud.feature.manager.models.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.models.FeatureVariant;
import com.azure.spring.cloud.feature.manager.models.IFeatureFilter;
import com.azure.spring.cloud.feature.manager.models.IFeatureVariantAssigner;

import reactor.core.publisher.Mono;

/**
 * Unit tests for FeatureManager.
 */
@SpringBootTest(classes = { TestConfiguration.class, SpringBootTest.class })
public class FeatureManagerTest {



    @InjectMocks
    private FeatureManager featureManager;

    @Mock
    private ApplicationContext context;

    @Mock
    private FeatureManagementConfigProperties properties;

    @Mock
    private FeatureManagementProperties featureManagementPropertiesMock;

    @Mock
    private MockFilter filterMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(properties.isFailFast()).thenReturn(true);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void isEnabledFeatureNotFound() {
        assertFalse(featureManager.isEnabledAsync("Non Existed Feature").block());
        verify(featureManagementPropertiesMock, times(2)).getOnOff();
        verify(featureManagementPropertiesMock, times(2)).getFeatureManagement();
    }

    @Test
    public void isEnabledFeatureOff() {
        HashMap<String, Boolean> features = new HashMap<>();
        features.put("Off", false);
        when(featureManagementPropertiesMock.getOnOff()).thenReturn(features);

        assertFalse(featureManager.isEnabledAsync("Off").block());
        verify(featureManagementPropertiesMock, times(2)).getOnOff();
        verify(featureManagementPropertiesMock, times(1)).getFeatureManagement();
    }

    @Test
    public void isEnabledOnBoolean() throws InterruptedException, ExecutionException, FilterNotFoundException {
        HashMap<String, Boolean> features = new HashMap<>();
        features.put("On", true);
        when(featureManagementPropertiesMock.getOnOff()).thenReturn(features);

        assertTrue(featureManager.isEnabledAsync("On").block());
        verify(featureManagementPropertiesMock, times(2)).getOnOff();
        verify(featureManagementPropertiesMock, times(1)).getFeatureManagement();
    }

    @Test
    public void isEnabledFeatureHasNoFilters() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature noFilters = new Feature();
        noFilters.setKey("NoFilters");
        noFilters.setEnabledFor(new HashMap<Integer, FeatureFilterEvaluationContext>());
        features.put("NoFilters", noFilters);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        assertFalse(featureManager.isEnabledAsync("NoFilters").block());
    }

    @Test
    public void isEnabledON() throws InterruptedException, ExecutionException, FilterNotFoundException {
        HashMap<String, Feature> features = new HashMap<>();
        Feature onFeature = new Feature();
        onFeature.setKey("On");
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.put(0, alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("On", onFeature);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void noFilter() throws FilterNotFoundException {
        HashMap<String, Feature> features = new HashMap<>();
        Feature onFeature = new Feature();
        onFeature.setKey("Off");
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOff");
        filters.put(0, alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("Off", onFeature);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOff"))).thenThrow(new NoSuchBeanDefinitionException(""));

        FilterNotFoundException e = assertThrows(FilterNotFoundException.class,
            () -> featureManager.isEnabledAsync("Off").block());
        assertThat(e).hasMessage("Fail fast is set and a Filter was unable to be found: AlwaysOff");
    }

    class MockFilter implements IFeatureFilter, IFeatureVariantAssigner {

        @Override
        public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition) {
            return null;
        }

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return false;
        }

    }

    class AlwaysOnFilter implements IFeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return true;
        }

    }

}
