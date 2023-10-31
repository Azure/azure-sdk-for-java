// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.targeting.ContextualTargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;

/**
 * Unit tests for FeatureManager.
 */
@SpringBootTest(classes = { FeatureManagementTestConfigurations.class, SpringBootTest.class })
public class FeatureManagerVariantTest {

    private FeatureManager featureManager;

    private FeatureManager contextualFeatureManager;

    @Mock
    private ApplicationContext context;

    @Mock
    private FeatureManagementConfigProperties properties;

    @Mock
    private FeatureManagementProperties featureManagementPropertiesMock;

    @Mock
    private TargetingContextAccessor contextAccessorMock;

    @Mock
    private ContextualTargetingContextAccessor contextualAccessorMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(properties.isFailFast()).thenReturn(true);

        featureManager = new FeatureManager(context, featureManagementPropertiesMock, properties, contextAccessorMock,
            null, null, null);
        contextualFeatureManager = new FeatureManager(context, featureManagementPropertiesMock, properties, null,
            contextualAccessorMock, null, null);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void isEnabledFeatureNotFound() {
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariant("Unknown Banner"));
        assertThat(e).hasMessage("The Feature Unknown Banner can not be found.");
    }

    @Test
    public void noAssignedVariants() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Variants");
        features.put("No Variants", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariant("No Variants"));
        assertThat(e).hasMessage("The feature No Variants has no assigned Variants.");
    }

    @Test
    public void noAssigner() {
        featureManager = new FeatureManager(context, featureManagementPropertiesMock, properties, null, null, null,
            null);

        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setVariants(createVariants());
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariant("No Assigner"));
        assertThat(e).hasMessage("No Targeting Filter Context found to assign variant.");
    }

    @Test
    public void noAssignmentNoDefault() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setVariants(createVariants());
        feature.setAllocation(new Allocation());
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        assertNull(featureManager.getVariant("No Assigner"));
    }

    @Test
    public void noAssignmentDefaultEnabled() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        feature.setAllocation(allocation);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void noAssignmentDefaultDisabled() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setEvaluate(false);
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenDisabled("large");
        feature.setAllocation(allocation);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "large");
        assertEquals(result.getValue(), 9);
    }

    @Test
    public void disabledNoDefaultDisabled() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setEvaluate(false);
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        feature.setAllocation(allocation);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        Variant result = featureManager.getVariant("No Assigner");
        assertNull(result);
    }

    @Test
    public void enabledFilterDefault() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        feature.setAllocation(allocation);
        HashMap<Integer, FeatureFilterEvaluationContext> featureFilters = new HashMap<>();
        FeatureFilterEvaluationContext filter = new FeatureFilterEvaluationContext();
        filter.setFeatureName("No Assigner");
        filter.setName("AlwaysOn");
        featureFilters.put(0, filter);
        feature.setEnabledFor(featureFilters);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);

        result = featureManager.getVariantAsync("No Assigner").block();
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void enabledFilterDefaultAnyTrue() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        feature.setAllocation(allocation);
        HashMap<Integer, FeatureFilterEvaluationContext> featureFilters = new HashMap<>();
        FeatureFilterEvaluationContext filter = new FeatureFilterEvaluationContext();
        filter.setFeatureName("No Assigner");
        filter.setName("AlwaysOn");
        featureFilters.put(0, filter);
        featureFilters.put(1, filter);
        feature.setEnabledFor(featureFilters);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void enabledFilterDefaultAnyTrueJustOne() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        feature.setAllocation(allocation);
        HashMap<Integer, FeatureFilterEvaluationContext> featureFilters = new HashMap<>();
        FeatureFilterEvaluationContext alwaysOnFilter = new FeatureFilterEvaluationContext();
        alwaysOnFilter.setFeatureName("No Assigner");
        alwaysOnFilter.setName("AlwaysOn");
        featureFilters.put(0, alwaysOnFilter);
        FeatureFilterEvaluationContext alwaysOffFilter = new FeatureFilterEvaluationContext();
        alwaysOffFilter.setFeatureName("No Assigner");
        alwaysOffFilter.setName("AlwaysOff");
        featureFilters.put(0, alwaysOnFilter);
        featureFilters.put(1, alwaysOffFilter);
        feature.setEnabledFor(featureFilters);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void enabledFilterDefaultAllTrue() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        feature.setAllocation(allocation);
        HashMap<Integer, FeatureFilterEvaluationContext> featureFilters = new HashMap<>();
        FeatureFilterEvaluationContext filter = new FeatureFilterEvaluationContext();
        filter.setFeatureName("No Assigner");
        filter.setName("AlwaysOn");
        featureFilters.put(0, filter);
        featureFilters.put(1, filter);
        feature.setEnabledFor(featureFilters);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
        
        result = contextualFeatureManager.getVariant("No Assigner", false);
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void enabledFilterDefaultAllTrueJustOne() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        allocation.setDefaultWhenDisabled("large");
        feature.setAllocation(allocation);
        HashMap<Integer, FeatureFilterEvaluationContext> featureFilters = new HashMap<>();
        FeatureFilterEvaluationContext alwaysOnFilter = new FeatureFilterEvaluationContext();
        alwaysOnFilter.setFeatureName("No Assigner");
        alwaysOnFilter.setName("AlwaysOn");
        featureFilters.put(0, alwaysOnFilter);
        FeatureFilterEvaluationContext alwaysOffFilter = new FeatureFilterEvaluationContext();
        alwaysOffFilter.setFeatureName("No Assigner");
        alwaysOffFilter.setName("AlwaysOff");
        featureFilters.put(0, alwaysOnFilter);
        featureFilters.put(1, alwaysOffFilter);
        feature.setEnabledFor(featureFilters);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "large");
        assertEquals(result.getValue(), 9);
    }

    @Test
    public void enabledFilterDefaultAllTrueJustOneNoDefault() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("No Assigner");
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("small");
        feature.setAllocation(allocation);
        HashMap<Integer, FeatureFilterEvaluationContext> featureFilters = new HashMap<>();
        FeatureFilterEvaluationContext alwaysOnFilter = new FeatureFilterEvaluationContext();
        alwaysOnFilter.setFeatureName("No Assigner");
        alwaysOnFilter.setName("AlwaysOn");
        featureFilters.put(0, alwaysOnFilter);
        FeatureFilterEvaluationContext alwaysOffFilter = new FeatureFilterEvaluationContext();
        alwaysOffFilter.setFeatureName("No Assigner");
        alwaysOffFilter.setName("AlwaysOff");
        featureFilters.put(0, alwaysOnFilter);
        featureFilters.put(1, alwaysOffFilter);
        feature.setEnabledFor(featureFilters);
        features.put("No Assigner", feature);

        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertNull(result);
    }

    @Test
    public void allOnVariantOverride() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("On");
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.put(0, alwaysOn);
        filters.put(1, alwaysOn);
        feature.setEnabledFor(filters);
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("large");
        feature.setAllocation(allocation);
        features.put("On", feature);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertFalse(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void allOnVariantOverrideNoDefault() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("On");
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.put(0, alwaysOn);
        filters.put(1, alwaysOn);
        feature.setEnabledFor(filters);
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        feature.setAllocation(allocation);
        features.put("On", feature);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void allOnVariantOverrideInvalidVariant() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("On");
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.put(0, alwaysOn);
        filters.put(1, alwaysOn);
        feature.setEnabledFor(filters);
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenEnabled("mediumn");
        feature.setAllocation(allocation);
        features.put("On", feature);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void noFiltersButVariants() {
        HashMap<String, Feature> features = new HashMap<>();
        Feature feature = new Feature();
        feature.setKey("On");
        feature.setRequirementType("All");
        feature.setVariants(createVariants());
        Allocation allocation = new Allocation();
        allocation.setDefaultWhenDisabled("small");
        feature.setAllocation(allocation);
        features.put("On", feature);
        when(featureManagementPropertiesMock.getFeatureManagement()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    private Map<String, VariantReference> createVariants() {
        Map<String, VariantReference> variants = new HashMap<>();
        variants.put("small",
            new VariantReference().setName("small").setConfigurationValue(1).setStatusOverride("true"));
        variants.put("large",
            new VariantReference().setName("large").setConfigurationValue(9).setStatusOverride("false"));
        return variants;
    }

    class AlwaysOnFilter implements FeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return true;
        }

    }

    class AlwaysOffFilter implements FeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return false;
        }

    }
}
