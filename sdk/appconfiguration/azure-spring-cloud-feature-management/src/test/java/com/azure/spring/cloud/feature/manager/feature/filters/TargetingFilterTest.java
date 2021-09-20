// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.feature.filters.TargetingFilter;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;

import reactor.core.publisher.Mono;

@RunWith(MockitoJUnitRunner.class)
public class TargetingFilterTest {

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String AUDIENCE = "Audience";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String REQUIRED_PARAMETER = "Value cannot be null.";

    private static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    @Mock
    private ITargetingContextAccessor contextAccessor;

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void validateDefaultRollout() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, -1);

        context.setParameters(parameters);

        when(contextAccessor.getContextAsync()).thenReturn(Mono.just(new TargetingContext()));

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor, options);

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

        TargetingFilter filter = new TargetingFilter(contextAccessor, options);

        Exception exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience : " + REQUIRED_PARAMETER, exception.getMessage());
    }
}
