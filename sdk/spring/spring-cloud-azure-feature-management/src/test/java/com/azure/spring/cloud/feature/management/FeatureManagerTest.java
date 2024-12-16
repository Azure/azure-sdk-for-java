// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilter;
import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.filters.TimeWindowFilter;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.models.Conditions;
import com.azure.spring.cloud.feature.management.models.Feature;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FilterNotFoundException;
import com.azure.spring.cloud.feature.management.targeting.ContextualTargetingContextAccessor;

/**
 * Unit tests for FeatureManager.
 */
@SpringBootTest(classes = { FeatureManagementTestConfigurations.class, SpringBootTest.class })
public class FeatureManagerTest {

    private FeatureManager featureManager;

    @Mock
    private ApplicationContext context;

    @Mock
    private FeatureManagementConfigProperties properties;

    @Mock
    private FeatureManagementProperties featureManagementPropertiesMock;

    @Mock
    private ContextualTargetingContextAccessor contextualAccessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(properties.isFailFast()).thenReturn(true);

        featureManager = new FeatureManager(context, featureManagementPropertiesMock, properties);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void isEnabledFeatureNotFound() {
        assertFalse(featureManager.isEnabledAsync("Non Existed Feature").block());
        verify(featureManagementPropertiesMock, times(1)).getFeatureFlags();
    }

    @Test
    public void isEnabledFeatureOff() {
        List<Feature> features = List.of(new Feature().setId("Off").setEnabled(false));
        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        assertFalse(featureManager.isEnabledAsync("Off").block());
        verify(featureManagementPropertiesMock, times(1)).getFeatureFlags();
    }

    @Test
    public void isEnabledOnBoolean() throws InterruptedException, ExecutionException, FilterNotFoundException {
        List<Feature> features = List.of(new Feature().setId("On").setEnabled(true));
        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        assertTrue(featureManager.isEnabled("On"));
        assertTrue(featureManager.isEnabledAsync("On").block());
        verify(featureManagementPropertiesMock, times(2)).getFeatureFlags();
    }

    @Test
    public void isEnabledOnContext() throws InterruptedException, ExecutionException, FilterNotFoundException {
        List<Feature> features = List.of(new Feature().setId("On").setEnabled(true).setConditions(new Conditions()
            .setClientFilters(List.of(new FeatureFilterEvaluationContext().setName("AlwaysOnContext")))));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOnContext"))).thenReturn(new AlwaysOnContextFilter());

        assertFalse(featureManager.isEnabled("On", false));
        assertFalse(featureManager.isEnabledAsync("On", false).block());
    }

    @Test
    public void isEnabledFeatureHasNoFilters() {
        List<Feature> features = List.of(new Feature().setId("NoFilters").setEnabled(false)
            .setConditions(new Conditions().setClientFilters(List.of())));
        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        assertFalse(featureManager.isEnabledAsync("NoFilters").block());
    }

    @Test
    public void isEnabledOn() throws InterruptedException, ExecutionException, FilterNotFoundException {
        List<Feature> features = List.of(new Feature().setId("On").setEnabled(true).setConditions(
            new Conditions().setClientFilters(List.of(new FeatureFilterEvaluationContext().setName("AlwaysOn")))));
        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void noFilter() throws FilterNotFoundException {
        List<Feature> features = List.of(new Feature().setId("Off").setEnabled(true).setConditions(
            new Conditions().setClientFilters(List.of(new FeatureFilterEvaluationContext().setName("AlwaysOff")))));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOff"))).thenThrow(new NoSuchBeanDefinitionException(""));

        FilterNotFoundException e = assertThrows(FilterNotFoundException.class,
            () -> featureManager.isEnabledAsync("Off").block());
        assertThat(e).hasMessage("Fail fast is set and a Filter was unable to be found: AlwaysOff");
    }

    @Test
    public void allOn() {
        List<Feature> features = List.of(new Feature().setId("On").setEnabled(true)
            .setConditions(new Conditions().setRequirementType("All")
                .setClientFilters(List.of(new FeatureFilterEvaluationContext().setName("AlwaysOn"),
                    new FeatureFilterEvaluationContext().setName("AlwaysOn")))));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter())
            .thenReturn(new AlwaysOnFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void oneOffAny() {
        List<Feature> features = List.of(new Feature().setId("On").setEnabled(true)
            .setConditions(new Conditions().setRequirementType("Any")
                .setClientFilters(List.of(new FeatureFilterEvaluationContext().setName("AlwaysOn"),
                    new FeatureFilterEvaluationContext().setName("AlwaysOff")))));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        when(context.getBean(Mockito.matches("AlwaysOff"))).thenReturn(new AlwaysOffFilter());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void oneOffAll() {
        List<Feature> features = List.of(new Feature().setId("On").setEnabled(true)
            .setConditions(new Conditions().setRequirementType("All")
                .setClientFilters(List.of(new FeatureFilterEvaluationContext().setName("AlwaysOn"),
                    new FeatureFilterEvaluationContext().setName("AlwaysOffFilter")))));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter());
        when(context.getBean(Mockito.matches("AlwaysOff"))).thenReturn(new AlwaysOffFilter());

        assertFalse(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void timeWindowFilter() {
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("Start", "Sun, 14 Jan 2024 00:00:00 GMT");
        parameters.put("End", "Mon, 15 Jan 2024 00:00:00 GMT");
        final HashMap<String, Object> pattern = new HashMap<>();
        pattern.put("Type", "Weekly");
        pattern.put("DaysOfWeek",
            List.of("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        final HashMap<String, Object> range = new HashMap<>();
        range.put("Type", "NoEnd");
        final HashMap<String, Object> recurrence = new HashMap<>();
        recurrence.put("Pattern", pattern);
        recurrence.put("Range", range);
        parameters.put("Recurrence", recurrence);

        final FeatureFilterEvaluationContext weeklyAlwaysOn = new FeatureFilterEvaluationContext();
        weeklyAlwaysOn.setName("TimeWindowFilter");
        weeklyAlwaysOn.setParameters(parameters);

        List<Feature> features = List
            .of(new Feature().setId("Alpha").setEnabled(true)
                .setConditions(new Conditions().setClientFilters(List.of(weeklyAlwaysOn))));

        when(featureManagementPropertiesMock.getFeatureFlags()).thenReturn(features);
        when(context.getBean(Mockito.matches("TimeWindowFilter"))).thenReturn(new TimeWindowFilter());

        assertTrue(featureManager.isEnabled("Alpha"));
    }

    class AlwaysOnFilter implements FeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return true;
        }

    }

    class AlwaysOnContextFilter implements ContextualFeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context, Object localContext) {
            if (localContext == Boolean.FALSE) {
                return false;
            }
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
