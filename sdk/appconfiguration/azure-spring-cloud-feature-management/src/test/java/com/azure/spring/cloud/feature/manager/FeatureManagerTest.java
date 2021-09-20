// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.azure.spring.cloud.feature.manager.FeatureFilter;
import com.azure.spring.cloud.feature.manager.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.manager.FeatureManager;
import com.azure.spring.cloud.feature.manager.FilterNotFoundException;
import com.azure.spring.cloud.feature.manager.entities.Feature;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

/**
 * Unit tests for FeatureManager.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeatureManagerTest {

    private static final String FEATURE_KEY = "TestFeature";

    private static final String FILTER_NAME = "Filter1";

    private static final String PARAM_1_NAME = "param1";

    private static final String PARAM_1_VALUE = "testParam";
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @InjectMocks
    private FeatureManager featureManager;
    @Mock
    private ApplicationContext context;
    @Mock
    private FeatureManagementConfigProperties properties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(properties.isFailFast()).thenReturn(true);
    }

    /**
     * Tests the conversion that takes place when data comes from EnumerablePropertySource.
     */
    @Test
    public void loadFeatureManagerWithLinkedHashSet() {
        Feature f = new Feature();
        f.setKey(FEATURE_KEY);

        LinkedHashMap<String, Object> testMap = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> testFeature = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> enabledFor = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> ffec = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        ffec.put("name", FILTER_NAME);
        parameters.put(PARAM_1_NAME, PARAM_1_VALUE);
        ffec.put("parameters", parameters);
        enabledFor.put("0", ffec);
        testFeature.put("enabled-for", enabledFor);
        testMap.put(f.getKey(), testFeature);

        featureManager.putAll(testMap);
        assertNotNull(featureManager);
        assertNotNull(featureManager.getFeatureManagement());
        assertEquals(1, featureManager.getFeatureManagement().size());
        assertNotNull(featureManager.getFeatureManagement().get(FEATURE_KEY));
        Feature feature = featureManager.getFeatureManagement().get(FEATURE_KEY);
        assertEquals(FEATURE_KEY, feature.getKey());
        assertEquals(1, feature.getEnabledFor().size());
        FeatureFilterEvaluationContext zeroth = feature.getEnabledFor().get(0);
        assertEquals(FILTER_NAME, zeroth.getName());
        assertEquals(1, zeroth.getParameters().size());
        assertEquals(PARAM_1_VALUE, zeroth.getParameters().get(PARAM_1_NAME));
    }

    @Test
    public void isEnabledFeatureNotFound() throws InterruptedException, ExecutionException, FilterNotFoundException {
        assertFalse(featureManager.isEnabledAsync("Non Existed Feature").block());
    }

    @Test
    public void isEnabledFeatureOff() throws InterruptedException, ExecutionException, FilterNotFoundException {
        HashMap<String, Object> features = new HashMap<String, Object>();
        features.put("Off", false);
        featureManager.putAll(features);

        assertFalse(featureManager.isEnabledAsync("Off").block());
    }

    @Test
    public void isEnabledFeatureHasNoFilters()
        throws InterruptedException, ExecutionException, FilterNotFoundException {
        HashMap<String, Object> features = new HashMap<String, Object>();
        Feature noFilters = new Feature();
        noFilters.setKey("NoFilters");
        noFilters.setEnabledFor(new HashMap<Integer, FeatureFilterEvaluationContext>());
        features.put("NoFilters", noFilters);
        featureManager.putAll(features);

        assertFalse(featureManager.isEnabledAsync("NoFilters").block());
    }

    @Test
    public void isEnabledON() throws InterruptedException, ExecutionException, FilterNotFoundException {
        HashMap<String, Object> features = new HashMap<String, Object>();
        Feature onFeature = new Feature();
        onFeature.setKey("On");
        HashMap<Integer, FeatureFilterEvaluationContext> filters =
            new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.put(0, alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("On", onFeature);
        featureManager.putAll(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOn());

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void isEnabledPeriodSplit() throws InterruptedException, ExecutionException, FilterNotFoundException {
        LinkedHashMap<String, Object> features = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> featuresOn = new LinkedHashMap<String, Object>();

        featuresOn.put("A", true);
        features.put("Beta", featuresOn);

        featureManager.putAll(features);

        assertTrue(featureManager.isEnabledAsync("Beta.A").block());
    }

    @Test
    public void isEnabledInvalid() throws InterruptedException, ExecutionException, FilterNotFoundException {
        LinkedHashMap<String, Object> features = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> featuresOn = new LinkedHashMap<String, Object>();

        featuresOn.put("A", 5);
        features.put("Beta", featuresOn);

        featureManager.putAll(features);

        assertFalse(featureManager.isEnabledAsync("Beta.A").block());
        assertEquals(0, featureManager.size());
    }

    @Test
    public void isEnabledOnBoolean() throws InterruptedException, ExecutionException, FilterNotFoundException {
        HashMap<String, Boolean> features = new HashMap<String, Boolean>();
        features.put("On", true);
        featureManager.putAll(features);

        assertTrue(featureManager.isEnabledAsync("On").block());
    }

    @Test
    public void featureManagerNotEnabledCorrectly()
        throws InterruptedException, ExecutionException, FilterNotFoundException {
        FeatureManager featureManager = new FeatureManager(null);
        assertFalse(featureManager.isEnabledAsync("").block());
    }

    @Test
    public void bootstrapConfiguration() {
        HashMap<String, Object> features = new HashMap<String, Object>();
        features.put("FeatureU", false);
        Feature featureV = new Feature();
        HashMap<Integer, FeatureFilterEvaluationContext> filterMapper =
            new HashMap<Integer, FeatureFilterEvaluationContext>();

        FeatureFilterEvaluationContext enabledFor = new FeatureFilterEvaluationContext();
        enabledFor.setName("Random");

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("chance", "50");

        enabledFor.setParameters(parameters);
        filterMapper.put(0, enabledFor);
        featureV.setEnabledFor(filterMapper);
        features.put("FeatureV", featureV);
        featureManager.putAll(features);

        assertNotNull(featureManager.getOnOff());
        assertNotNull(featureManager.getFeatureManagement());

        assertEquals(featureManager.getOnOff().get("FeatureU"), false);
        Feature feature = featureManager.getFeatureManagement().get("FeatureV");
        assertEquals(feature.getEnabledFor().size(), 1);
        FeatureFilterEvaluationContext ffec = feature.getEnabledFor().get(0);
        assertEquals(ffec.getName(), "Random");
        assertEquals(ffec.getParameters().size(), 1);
        assertEquals(ffec.getParameters().get("chance"), "50");
        assertEquals(2, featureManager.getAllFeatureNames().size());
    }

    @Test
    public void noFilter() throws FilterNotFoundException {
        expectedEx.expect(FilterNotFoundException.class);
        expectedEx.expectMessage("Fail fast is set and a Filter was unable to be found: AlwaysOff");
        HashMap<String, Object> features = new HashMap<String, Object>();
        Feature onFeature = new Feature();
        onFeature.setKey("Off");
        HashMap<Integer, FeatureFilterEvaluationContext> filters =
            new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOff");
        filters.put(0, alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("Off", onFeature);
        featureManager.putAll(features);

        when(context.getBean(Mockito.matches("AlwaysOff"))).thenThrow(new NoSuchBeanDefinitionException(""));

        featureManager.isEnabledAsync("Off").block();
        fail();
    }

    @Component
    public class AlwaysOn implements FeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return true;
        }

    }

}
