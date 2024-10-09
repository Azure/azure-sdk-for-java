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

import java.util.ArrayList;
import java.util.List;

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
import com.azure.spring.cloud.feature.management.implementation.models.Conditions;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.Variant;
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
        List<Feature> features = List.of(new Feature().setId("No Variants"));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariant("No Variants"));
        assertThat(e).hasMessage("The feature No Variants has no assigned Variants.");
    }

    @Test
    public void noAssigner() {
        featureManager = new FeatureManager(context, featureManagementPropertiesMock, properties, null, null, null,
            null);
        List<Feature> features = List.of(new Feature().setId("No Assigner").setVariants(createVariants()));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariant("No Assigner"));
        assertThat(e).hasMessage("No Targeting Filter Context found to assign variant.");
    }

    @Test
    public void noAssignmentNoDefault() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants()).setAllocation(new Allocation()));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        assertNull(featureManager.getVariant("No Assigner"));
    }

    @Test
    public void noAssignmentDefaultEnabled() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setAllocation(new Allocation().setDefaultWhenEnabled("small")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void noAssignmentDefaultDisabled() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setAllocation(new Allocation().setDefaultWhenEnabled("large")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "large");
        assertEquals(result.getValue(), 9);
    }

    @Test
    public void disabledNoDefaultDisabled() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setAllocation(new Allocation()));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        Variant result = featureManager.getVariant("No Assigner");
        assertNull(result);
    }

    @Test
    public void enabledFilterDefault() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"))))
                .setAllocation(new Allocation().setDefaultWhenEnabled("small")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
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
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"))))
                .setAllocation(new Allocation().setDefaultWhenEnabled("small")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void enabledFilterDefaultAnyTrueJustOne() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOff"))))
                .setAllocation(new Allocation().setDefaultWhenEnabled("small")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "small");
        assertEquals(result.getValue(), 1);
    }

    @Test
    public void enabledFilterDefaultAllTrue() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn")))
                    .setRequirementType("All"))
                .setAllocation(new Allocation().setDefaultWhenEnabled("small")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
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
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOff")))
                    .setRequirementType("All"))
                .setAllocation(new Allocation().setDefaultWhenEnabled("small").setDefaultWhenDisabled("large")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertEquals(result.getName(), "large");
        assertEquals(result.getValue(), 9);
    }

    @Test
    public void enabledFilterDefaultAllTrueJustOneNoDefault() {
        List<Feature> features = List
            .of(new Feature().setId("No Assigner").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn")))
                    .setRequirementType("All")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        when(context.getBean(Mockito.eq("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        Variant result = featureManager.getVariant("No Assigner");
        assertNull(result);
    }

    @Test
    public void allOnVariantOverride() {
        List<Feature> features = List
            .of(new Feature().setId("On").setVariants(createVariants())
                .setConditions(new Conditions().setRequirementType("All").setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"))))
                .setAllocation(new Allocation().setDefaultWhenEnabled("large")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertFalse(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void allOnVariantOverrideNoDefault() {
        List<Feature> features = List
            .of(new Feature().setId("On").setVariants(createVariants())
                .setConditions(new Conditions().setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn")))
                    .setRequirementType("All"))
                .setAllocation(new Allocation()));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void allOnVariantOverrideInvalidVariant() {
        List<Feature> features = List.of(new Feature().setId("On").setVariants(createVariants())
            .setConditions(new Conditions()
                .setClientFilters(
                    List.of(new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn"),
                        new FeatureFilterEvaluationContext().setFeatureName("No Assigner").setName("AlwaysOn")))
                .setRequirementType("All"))
            .setAllocation(new Allocation()));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void noFiltersButVariants() {
        List<Feature> features = List
            .of(new Feature().setId("On").setVariants(createVariants()).setConditions(new Conditions().setRequirementType("All"))
                .setAllocation(new Allocation().setDefaultWhenEnabled("small")));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    private List<VariantReference> createVariants() {
        List<VariantReference> variants = new ArrayList<>();
        variants.add(new VariantReference().setName("small").setConfigurationValue(1).setStatusOverride("Enabled"));
        variants.add(new VariantReference().setName("large").setConfigurationValue(9).setStatusOverride("Disabled"));
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
