// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.azure.spring.cloud.feature.manager.entities.Feature;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.DynamicFeature;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureVariant;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.IFeatureVariantAssigner;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.IFeatureVariantAssignerMetadata;
import com.azure.spring.cloud.feature.manager.testobjects.BasicObject;

import reactor.core.publisher.Mono;

/**
 * Unit tests for FeatureManager.
 */
@SpringBootTest(classes = { TestConfiguration.class, SpringBootTest.class })
public class FeatureManagerTest {

    private static final String FEATURE_KEY = "TestFeature";

    private static final String FILTER_NAME = "Filter1";

    private static final String PARAM_1_NAME = "param1";

    private static final String PARAM_1_VALUE = "testParam";

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final LinkedHashMap<String, Object> EMPTY_MAP = new LinkedHashMap<>();

    @InjectMocks
    private FeatureManager featureManager;

    @Mock
    private ApplicationContext context;

    @Mock
    private FeatureManagementConfigProperties properties;

    @Mock
    private FeatureVariantProperties variantProperties;

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
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOn");
        filters.put(0, alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("On", onFeature);
        featureManager.putAll(features);

        when(context.getBean(Mockito.matches("AlwaysOn"))).thenReturn(new AlwaysOnFilter());

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
        HashMap<Integer, FeatureFilterEvaluationContext> filterMapper = new HashMap<Integer, FeatureFilterEvaluationContext>();

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
        HashMap<String, Object> features = new HashMap<String, Object>();
        Feature onFeature = new Feature();
        onFeature.setKey("Off");
        HashMap<Integer, FeatureFilterEvaluationContext> filters = new HashMap<Integer, FeatureFilterEvaluationContext>();
        FeatureFilterEvaluationContext alwaysOn = new FeatureFilterEvaluationContext();
        alwaysOn.setName("AlwaysOff");
        filters.put(0, alwaysOn);
        onFeature.setEnabledFor(filters);
        features.put("Off", onFeature);
        featureManager.putAll(features);

        when(context.getBean(Mockito.matches("AlwaysOff"))).thenThrow(new NoSuchBeanDefinitionException(""));

        FilterNotFoundException e = assertThrows(FilterNotFoundException.class,
            () -> featureManager.isEnabledAsync("Off").block());
        assertThat(e).hasMessage("Fail fast is set and a Filter was unable to be found: AlwaysOff");
    }

    @Test
    public void getVariantAsyncDefaultBasic() {
        String testString = "Basic Object";
        when(variantProperties.get("testVariantReference")).thenReturn(testString);
        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 100));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        assertEquals(testString, featureManager.getVariantAsync("testVariant", String.class).block());
    }

    @Test
    public void getVariantAsyncDefaultMultiPart() {
        String testString = "Basic Object";
        String variantName = "testVariant";

        HashMap<String, Object> config = new HashMap<>();
        config.put("LevelTwo", testString);

        when(variantProperties.get(variantName + "Reference")).thenReturn(config);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant(variantName, ":LevelTwo", EMPTY_MAP, EMPTY_MAP, 100));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(variantName, dynamicFeature);

        featureManager.putAll(params);

        assertEquals(testString, featureManager.getVariantAsync(variantName, String.class).block());
    }

    @Test
    public void getVariantNoDefault() {
        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 100));

        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        ;
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("testVariant", Object.class).block());
        assertEquals("A default variant cannot be found for the feature testVariant", e.getMessage());
    }

    @Test
    public void getVariantAsyncNonDefault() {
        String testString = "Basic Object";

        FeatureVariant variant = createFeatureVariant("testVariant2", EMPTY_MAP, EMPTY_MAP, 100);

        when(variantProperties.get("testVariant2Reference")).thenReturn(testString);

        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        variants.put("1", variant);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        assertEquals(testString, featureManager.getVariantAsync("testVariant", String.class).block());
    }

    @Test
    public void getVariantAsyncComplexObject() {
        String testString = "Basic Object";
        BasicObject testObject = new BasicObject();
        testObject.setTestValue(testString);

        FeatureVariant variant = createFeatureVariant("testVariant2", EMPTY_MAP, EMPTY_MAP, 100);

        when(variantProperties.get("testVariant2Reference")).thenReturn(testObject);

        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        assertEquals(testString,
            featureManager.getVariantAsync("testVariant", BasicObject.class).block().getTestValue());
    }

    @Test
    public void noDefinedFeatureVariantDefinition() {
        String testString = "Basic Object";
        BasicObject testObject = new BasicObject();
        testObject.setTestValue(testString);

        FeatureVariant variant = createFeatureVariant("testVariant2", EMPTY_MAP, EMPTY_MAP, 100);

        when(variantProperties.get("testVariant2Reference")).thenReturn(testObject);

        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner(null);

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("testVariant", BasicObject.class).block());
        assertNotNull(e);
        assertEquals("The feature variant testVariant has no defined Dynnamic Feature.", e.getMessage());
    }

    @Test
    public void noDefinedFeatureVariant() {
        String testString = "Basic Object";
        BasicObject testObject = new BasicObject();
        testObject.setTestValue(testString);

        FeatureVariant variant = createFeatureVariant("testVariant2", EMPTY_MAP, EMPTY_MAP, 100);

        when(variantProperties.get("testVariant2Reference")).thenReturn(testObject);

        when(context.getBean(Mockito.matches("Test.Assigner")))
            .thenThrow(new NoSuchBeanDefinitionException(IFeatureVariantAssignerMetadata.class));
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("testVariant", BasicObject.class).block());
        assertNotNull(e);
        assertEquals("The feature variant assigner Test.Assigner specified for feature testVariant was not found.",
            e.getMessage());
    }

    @Test
    public void noDefinedFeature() {
        String testString = "Basic Object";
        BasicObject testObject = new BasicObject();
        testObject.setTestValue(testString);

        FeatureVariant variant = createFeatureVariant("testVariant2", EMPTY_MAP, EMPTY_MAP, 100);

        when(variantProperties.get("testVariant2Reference")).thenReturn(testObject);

        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner(null);

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);

        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("testVariant", BasicObject.class).block());
        assertNotNull(e);
        assertEquals("The feature variant testVariant has no defined Dynnamic Feature.", e.getMessage());
    }

    @Test
    public void validateDynamicFeature() {
        String testString = "Basic Object";

        FeatureVariant variant = createFeatureVariant("testVariant2", EMPTY_MAP, EMPTY_MAP, 100);

        when(variantProperties.get("testVariant2Reference")).thenReturn(testString);

        when(context.getBean(Mockito.matches("Test.Assigner"))).thenReturn(filterMock);
        when(filterMock.assignVariantAsync(Mockito.any())).thenReturn(Mono.just(variant));

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("Test.Assigner");

        Map<String, FeatureVariant> variants = new LinkedHashMap<>();

        variants.put("0", createFeatureVariant("testVariant", EMPTY_MAP, EMPTY_MAP, 0));
        variants.get("0").setDefault(true);
        variants.put("1", variant);
        variants.get("1").setDefault(true);
        dynamicFeature.setVariants(variants);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);

        featureManager.putAll(params);
        FeatureManagementException e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("testVariant", BasicObject.class).block());
        assertNotNull(e);
        assertEquals("Multiple default variants are registered for the feature testVariant", e.getMessage());
        
        variants.get("1").setDefault(false);
        variants.get("1").setConfigurationReference(null);
        dynamicFeature.setVariants(variants);

        params = new LinkedHashMap<>();
        params.put("testVariant", dynamicFeature);
        featureManager.putAll(params);
        
        e = assertThrows(FeatureManagementException.class,
            () -> featureManager.getVariantAsync("testVariant", BasicObject.class).block());
        assertNotNull(e);
        assertEquals("The variant testVariant2 for the feature testVariant does not have a configuration reference.", e.getMessage());
    }

    class MockFilter implements FeatureFilter, IFeatureVariantAssigner {

        @Override
        public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition) {
            return null;
        }

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return false;
        }

    }

    class AlwaysOnFilter implements FeatureFilter {

        @Override
        public boolean evaluate(FeatureFilterEvaluationContext context) {
            return true;
        }

    }

    private FeatureVariant createFeatureVariant(String variantName, LinkedHashMap<String, Object> users,
        LinkedHashMap<String, Object> groups, int defautPercentage) {
        return createFeatureVariant(variantName, "", users, groups, defautPercentage);
    }

    private FeatureVariant createFeatureVariant(String variantName, String additionalRerence,
        LinkedHashMap<String, Object> users,
        LinkedHashMap<String, Object> groups, int defautPercentage) {
        FeatureVariant variant = new FeatureVariant();
        variant.setName(variantName);
        variant.setDefault(false);
        variant.setConfigurationReference(variantName + "Reference" + additionalRerence);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        parameters.put(USERS, users);
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, defautPercentage);

        variant.setAssignmentParameters(parameters);

        return variant;
    }

}
