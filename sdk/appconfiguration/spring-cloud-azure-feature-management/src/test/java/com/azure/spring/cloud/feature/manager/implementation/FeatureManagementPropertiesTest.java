// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.manager.FilterNotFoundException;
import com.azure.spring.cloud.feature.manager.implementation.models.DynamicFeature;
import com.azure.spring.cloud.feature.manager.implementation.models.Feature;
import com.azure.spring.cloud.feature.manager.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.models.FeatureVariant;

public class FeatureManagementPropertiesTest {

    private static final String FEATURE_KEY = "TestFeature";

    private static final String FILTER_NAME = "Filter1";

    private static final String PARAM_1_NAME = "param1";

    private static final String PARAM_1_VALUE = "testParam";

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

        FeatureManagementProperties properties = new FeatureManagementProperties();
        properties.putAll(testMap);
        assertNotNull(properties.getFeatureManagement());
        assertEquals(1, properties.getFeatureManagement().size());
        assertNotNull(properties.getFeatureManagement().get(FEATURE_KEY));
        Feature feature = properties.getFeatureManagement().get(FEATURE_KEY);
        assertEquals(FEATURE_KEY, feature.getKey());
        assertEquals(1, feature.getEnabledFor().size());
        FeatureFilterEvaluationContext zeroth = feature.getEnabledFor().get(0);
        assertEquals(FILTER_NAME, zeroth.getName());
        assertEquals(1, zeroth.getParameters().size());
        assertEquals(PARAM_1_VALUE, zeroth.getParameters().get(PARAM_1_NAME));
    }

    @Test
    public void isEnabledPeriodSplit() throws InterruptedException, ExecutionException, FilterNotFoundException {
        LinkedHashMap<String, Object> features = new LinkedHashMap<>();
        LinkedHashMap<String, Object> featuresOn = new LinkedHashMap<>();

        featuresOn.put("A", true);
        features.put("Beta", featuresOn);

        FeatureManagementProperties properties = new FeatureManagementProperties();
        properties.putAll(features);

        assertTrue(properties.getOnOff().get("Beta.A"));
    }

    @Test
    public void isEnabledInvalid() throws InterruptedException, ExecutionException, FilterNotFoundException {
        LinkedHashMap<String, Object> features = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> featuresOn = new LinkedHashMap<String, Object>();

        featuresOn.put("A", 5);
        features.put("Beta", featuresOn);

        FeatureManagementProperties properties = new FeatureManagementProperties();
        properties.putAll(features);

        assertNull(properties.getOnOff().getOrDefault("Beta.A", null));
        assertEquals(0, properties.size());
    }

    @Test
    public void bootstrapConfiguration() {
        HashMap<String, Object> features = new HashMap<String, Object>();
        features.put("FeatureU", false);
        Feature featureV = new Feature();
        HashMap<Integer, FeatureFilterEvaluationContext> filterMapper = new HashMap<Integer, FeatureFilterEvaluationContext>();

        FeatureFilterEvaluationContext enabledFor = new FeatureFilterEvaluationContext();
        enabledFor.setName("Random");

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("chance", "50");

        enabledFor.setParameters(parameters);
        filterMapper.put(0, enabledFor);
        featureV.setEnabledFor(filterMapper);
        features.put("FeatureV", featureV);

        FeatureManagementProperties properties = new FeatureManagementProperties();
        properties.putAll(features);

        assertNotNull(properties.getOnOff());
        assertNotNull(properties.getFeatureManagement());

        assertEquals(properties.getOnOff().get("FeatureU"), false);
        Feature feature = properties.getFeatureManagement().get("FeatureV");
        assertEquals(feature.getEnabledFor().size(), 1);
        FeatureFilterEvaluationContext ffec = feature.getEnabledFor().get(0);
        assertEquals(ffec.getName(), "Random");
        assertEquals(ffec.getParameters().size(), 1);
        assertEquals(ffec.getParameters().get("chance"), "50");
    }

    @Test
    public void featureVariantLoadTest() {
        HashMap<String, Object> features = new HashMap<String, Object>();

        DynamicFeature df = new DynamicFeature();
        df.setAssigner("Microsoft.Targeting");

        FeatureVariant fv = new FeatureVariant();
        fv.setName("TestVariant");
        fv.setDefault(true);
        fv.setConfigurationReference("config.reference");

        LinkedHashMap<String, Object> assignmentParameters = new LinkedHashMap<>();

        assignmentParameters.put("User", "Doe");

        fv.setAssignmentParameters(assignmentParameters);

        Map<String, FeatureVariant> variants = new HashMap<>();

        variants.put("TestVariant", fv);

        df.setVariants(variants);

        features.put("TestDynamicFeature", df);

        FeatureManagementProperties properties = new FeatureManagementProperties();
        properties.putAll(features);

        assertNotNull(properties.getDynamicFeatures().get("TestDynamicFeature"));
    }
}
