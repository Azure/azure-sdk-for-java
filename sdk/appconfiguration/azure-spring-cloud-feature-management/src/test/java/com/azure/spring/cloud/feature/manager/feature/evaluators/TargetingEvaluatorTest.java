// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.evaluators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.TestConfiguration;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.DynamicFeature;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureVariant;
import com.azure.spring.cloud.feature.manager.targeting.GroupRollout;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;

import reactor.core.publisher.Mono;

@SpringBootTest(classes = { TestConfiguration.class, SpringBootTest.class })
public class TargetingEvaluatorTest {

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String AUDIENCE = "Audience";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String REQUIRED_PARAMETER = "Value cannot be null.";

    private static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    @Mock
    private ITargetingContextAccessor contextAccessor;
    
    @Mock
    private TargetingContext targetingContextMock;

    @Test
    public void targetedUser() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        targetingContext.setUserId("Doe");

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void nottargetedUser() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        targetingContext.setUserId("John");

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedGroup() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, Object> groups = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "100");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");
        targetingContext.setGroups(targetedGroups);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void notTargetedGroup() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, Object> groups = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "100");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g2");
        targetingContext.setGroups(targetedGroups);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedGroupFiftyPass() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, Object> groups = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "50");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");
        targetingContext.setUserId("Jane");
        targetingContext.setGroups(targetedGroups);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void targetedGroupFiftyFalse() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, Object> groups = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "50");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");
        targetingContext.setUserId("Doe");
        targetingContext.setGroups(targetedGroups);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedAudience() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> audienceObject = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        audienceObject.put(AUDIENCE, parameters);
        context.setParameters(audienceObject);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        targetingContext.setUserId("Doe");

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void validateDefaultRollout() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, -1);

        context.setParameters(parameters);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(new TargetingContext()));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        Exception exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience.-1.0 : " + OUT_OF_RANGE, exception.getMessage());

        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 101);

        context.setParameters(parameters);

        exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience.101.0 : " + OUT_OF_RANGE, exception.getMessage());
    }

    @Test
    public void validateGroups() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, Object> groups = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "-1");
        groups.put("0", g1);

        parameters.put(GROUPS, groups);

        context.setParameters(parameters);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(new TargetingContext()));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor);

        Exception exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience[0].-1.0 : " + OUT_OF_RANGE, exception.getMessage());

        g1.put("rolloutPercentage", "101");
        groups.put("0", g1);

        parameters.put(GROUPS, groups);

        context.setParameters(parameters);

        exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience[0].101.0 : " + OUT_OF_RANGE, exception.getMessage());

        parameters.put(GROUPS, null);

        context.setParameters(parameters);

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedUserCaseInsensitive() {
        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);

        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        targetingContext.setUserId("doe");

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void targetedNull() {
        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);

        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        LinkedHashMap<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(null);
        context.setFeatureName("testFeature");

        TargetingContext targetingContext = new TargetingContext();
        targetingContext.setUserId("doe");

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContext));

        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Exception exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience : " + REQUIRED_PARAMETER, exception.getMessage());
    }

    @Test
    public void assignVariantAsyncTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(new TargetingContext()));

        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Map<String, FeatureVariant> variants = new HashMap<String, FeatureVariant>();

        FeatureVariant variant = createFeatureVariant("testVariant", new LinkedHashMap<String, Object>(),
            new LinkedHashMap<String, Object>(), 100);
        variants.put("0", variant);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("testAssigner");
        dynamicFeature.setVariants(variants);
        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", dynamicFeature);

        Mono<FeatureVariant> result = filter.assignVariantAsync(featureDefinition);
        assertNotNull(result);
        FeatureVariant resultVariant = result.block();
        assertNotNull(resultVariant);
        assertEquals(variant.getName(), resultVariant.getName());
    }

    @Test
    public void assignVariantAsyncTwoVariantsTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(new TargetingContext()));

        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Map<String, FeatureVariant> variants = new HashMap<String, FeatureVariant>();

        FeatureVariant variant = createFeatureVariant("testVariant", new LinkedHashMap<String, Object>(),
            new LinkedHashMap<String, Object>(), 100);
        variants.put("0", variant);

        FeatureVariant zeroVariant = createFeatureVariant("zeroTestVariant", new LinkedHashMap<String, Object>(),
            new LinkedHashMap<String, Object>(), 0);
        variants.put("1", zeroVariant);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("testAssigner");
        dynamicFeature.setVariants(variants);
        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", dynamicFeature);

        Mono<FeatureVariant> result = filter.assignVariantAsync(featureDefinition);
        assertNotNull(result);
        FeatureVariant resultVariant = result.block();
        assertNotNull(resultVariant);
        assertEquals(variant.getName(), resultVariant.getName());
    }

    @Test
    public void assignVariantAsyncUseDefaultTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContextMock));
        when(targetingContextMock.getUserId()).thenReturn("Jeff");

        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Map<String, FeatureVariant> variants = new HashMap<String, FeatureVariant>();

        FeatureVariant variant = createFeatureVariant("testVariant", null,
            new LinkedHashMap<String, Object>(), 0);
        variants.put("0", variant);

        FeatureVariant zeroVariant = createFeatureVariant("zeroTestVariant", new LinkedHashMap<String, Object>(),
            new LinkedHashMap<String, Object>(), 0);
        variants.put("1", zeroVariant);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("testAssigner");
        dynamicFeature.setVariants(variants);
        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", dynamicFeature);

        Mono<FeatureVariant> result = filter.assignVariantAsync(featureDefinition);
        assertNull(result.block());
    }
    
    @Test
    public void assignVariantAsyncUserTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContextMock));
        when(targetingContextMock.getUserId()).thenReturn("Jeff");

        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Map<String, FeatureVariant> variants = new HashMap<String, FeatureVariant>();
        LinkedHashMap<String, Object> users = new LinkedHashMap<String, Object>();
        
        users.put("0", "Jane");
        users.put("1", "Jeff");

        FeatureVariant variant = createFeatureVariant("testVariant", users, new LinkedHashMap<String, Object>(), 0);
        variants.put("0", variant);

        FeatureVariant zeroVariant = createFeatureVariant("zeroTestVariant", new LinkedHashMap<String, Object>(),
            new LinkedHashMap<String, Object>(), 0);
        variants.put("1", zeroVariant);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("testAssigner");
        dynamicFeature.setVariants(variants);
        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", dynamicFeature);

        Mono<FeatureVariant> result = filter.assignVariantAsync(featureDefinition);
        assertNotNull(result);
        FeatureVariant resultVariant = result.block();
        assertNotNull(resultVariant);
        assertEquals(variant.getName(), resultVariant.getName());
    }
    
    @Test
    public void assignVariantAsyncGroupsTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContextMock));
        
        List<String> groups = new ArrayList<>();
        groups.add("Test");
        
        when(targetingContextMock.getGroups()).thenReturn(groups);

        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Map<String, FeatureVariant> variants = new HashMap<String, FeatureVariant>();
        LinkedHashMap<String, Object> variantGroups = new LinkedHashMap<String, Object>();
        
        GroupRollout gr1 = new GroupRollout();
        gr1.setName("Dev");
        gr1.setRolloutPercentage(100);
        
        GroupRollout gr2 = new GroupRollout();
        gr2.setName("Test");
        gr2.setRolloutPercentage(100);
        
        variantGroups.put("0", gr1);
        variantGroups.put("1", gr2);

        FeatureVariant variant = createFeatureVariant("testVariant", new LinkedHashMap<String, Object>(), variantGroups, 0);
        variants.put("0", variant);

        FeatureVariant zeroVariant = createFeatureVariant("zeroTestVariant", new LinkedHashMap<String, Object>(),
            new LinkedHashMap<String, Object>(), 0);
        variants.put("1", zeroVariant);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("testAssigner");
        dynamicFeature.setVariants(variants);
        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", dynamicFeature);

        Mono<FeatureVariant> result = filter.assignVariantAsync(featureDefinition);
        assertNotNull(result);
        FeatureVariant resultVariant = result.block();
        assertNotNull(resultVariant);
        assertEquals(variant.getName(), resultVariant.getName());
    }
    
    @Test
    public void assignVariantAsyncGroupBoundsTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(targetingContextMock));
        
        List<String> groups = new ArrayList<>();
        groups.add("Test");
        
        when(targetingContextMock.getGroups()).thenReturn(groups);

        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);
        TargetingEvaluator filter = new TargetingEvaluator(contextAccessor, options);

        Map<String, FeatureVariant> variants = new HashMap<String, FeatureVariant>();
        LinkedHashMap<String, Object> variantGroups = new LinkedHashMap<String, Object>();
        
        GroupRollout gr1 = new GroupRollout();
        gr1.setName("Dev");
        gr1.setRolloutPercentage(51);
        
        GroupRollout gr2 = new GroupRollout();
        gr2.setName("Test");
        gr2.setRolloutPercentage(51);
        
        variantGroups.put("0", gr1);
        variantGroups.put("1", gr2);

        FeatureVariant variant = createFeatureVariant("testVariant", new LinkedHashMap<String, Object>(), variantGroups, 0);
        variants.put("0", variant);
        variants.put("1", variant);

        DynamicFeature dynamicFeature = new DynamicFeature();
        dynamicFeature.setAssigner("testAssigner");
        dynamicFeature.setVariants(variants);
        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", dynamicFeature);
        
        Exception exception = assertThrows(TargetingException.class, () -> filter.assignVariantAsync(featureDefinition).block());
        assertEquals("Dev : The value is out of the accepted range.", exception.getMessage());
    }
    
    @Test
    public void assignVariantAsyncNullContextTest() {
        when(contextAccessor.getContextAsync()).thenReturn(Mono.justOrEmpty(null));

        FeatureDefinition featureDefinition = new FeatureDefinition("testFeature", new DynamicFeature());

        Mono<FeatureVariant> result = new TargetingEvaluator(contextAccessor).assignVariantAsync(featureDefinition);
        assertNull(result.block());
    }

    private FeatureVariant createFeatureVariant(String variantName, LinkedHashMap<String, Object> users,
        LinkedHashMap<String, Object> groups, int defautPercentage) {
        FeatureVariant variant = new FeatureVariant();
        variant.setName(variantName);
        variant.setDefault(false);
        variant.setConfigurationReference(variantName + "Reference");

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        parameters.put(USERS, users);
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, defautPercentage);

        variant.setAssignmentParameters(parameters);

        return variant;
    }
}
