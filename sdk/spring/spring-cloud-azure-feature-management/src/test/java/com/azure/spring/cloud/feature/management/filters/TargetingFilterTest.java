// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.azure.spring.cloud.feature.management.implementation.TestConfiguration;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.TargetingException;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

@SpringBootTest(classes = { TestConfiguration.class, SpringBootTest.class })
public class TargetingFilterTest {

    private static final String USERS = "Users";

    private static final String GROUPS = "Groups";

    private static final String AUDIENCE = "Audience";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    @Test
    public void targetedUser() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        Map<String, Object> excludes = new LinkedHashMap<>();
        Map<String, String> excludedGroups = new LinkedHashMap<>();

        excludes.put(GROUPS, excludedGroups);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Doe", null));

        assertTrue(filter.evaluate(context));
    }
    
    @Test
    public void targetedUserLower() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS.toLowerCase(), users);
        parameters.put(GROUPS.toLowerCase(), new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        Map<String, Object> excludes = new LinkedHashMap<>();
        Map<String, String> excludedGroups = new LinkedHashMap<>();

        excludes.put(GROUPS, excludedGroups);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Doe", null));

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void nottargetedUser() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("John", null));

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedGroup() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "100");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor(null, targetedGroups));

        assertTrue(filter.evaluate(context));
    }
    
    @Test
    public void targetedGroupLower() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "100");
        groups.put("0", g1);

        parameters.put(USERS.toLowerCase(), new LinkedHashMap<String, Object>());
        parameters.put(GROUPS.toLowerCase(), groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor(null, targetedGroups));

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void notTargetedGroup() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "100");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g2");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor(null, targetedGroups));

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedGroupFiftyPass() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "50");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Jane", targetedGroups));

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void targetedGroupFiftyFalse() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "50");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Doe", targetedGroups));

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void targetedAudience() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> audienceObject = new LinkedHashMap<String, Object>();
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        audienceObject.put(AUDIENCE, parameters);
        context.setParameters(audienceObject);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Doe", null));

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void validateDefaultRollout() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, -1);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("John", null));

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

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "-1");
        groups.put("0", g1);

        parameters.put(GROUPS, groups);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Joe", null));

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

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);
        parameters.put("Exclusion", emptyExclusion());

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("doe", null), options);

        assertTrue(filter.evaluate(context));
    }

    @Test
    public void targetedNull() {
        TargetingEvaluationOptions options = new TargetingEvaluationOptions();
        options.setIgnoreCase(true);

        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        context.setParameters(null);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("doe", null), options);
        assertFalse(filter.evaluate(context));
    }

    @Test
    public void excludeUser() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<>();

        Map<String, String> users = new LinkedHashMap<>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("Doe", null));

        assertTrue(filter.evaluate(context));

        // Now the users is excluded
        Map<String, Object> excludes = new LinkedHashMap<>();
        Map<String, String> excludedUsers = new LinkedHashMap<>();
        excludedUsers.put("0", "Doe");

        excludes.put(USERS, excludedUsers);
        parameters.put("Exclusion", excludes);

        context.setParameters(parameters);

        assertFalse(filter.evaluate(context));
    }

    @Test
    public void excludeGroup() {
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, Object> groups = new LinkedHashMap<String, Object>();
        Map<String, String> g1 = new LinkedHashMap<String, String>();
        g1.put("name", "g1");
        g1.put("rolloutPercentage", "100");
        groups.put("0", g1);

        parameters.put(USERS, new LinkedHashMap<String, Object>());
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(parameters);
        context.setFeatureName("testFeature");

        ArrayList<String> targetedGroups = new ArrayList<String>();
        targetedGroups.add("g1");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor(null, targetedGroups));

        assertTrue(filter.evaluate(context));

        // Now the users is excluded
        Map<String, Object> excludes = new LinkedHashMap<>();
        Map<String, String> excludedGroups = new LinkedHashMap<>();
        excludedGroups.put("0", "g1");

        excludes.put(GROUPS, excludedGroups);
        parameters.put("Exclusion", excludes);

        context.setParameters(parameters);

        assertFalse(filter.evaluate(context));
    }

    private Map<String, Object> emptyExclusion() {
        Map<String, Object> excludes = new LinkedHashMap<>();
        List<String> excludedUsers = new ArrayList<>();
        List<String> excludedGroups = new ArrayList<>();
        excludes.put(USERS, excludedUsers);
        excludes.put(GROUPS, excludedGroups);
        return excludes;
    }
}
