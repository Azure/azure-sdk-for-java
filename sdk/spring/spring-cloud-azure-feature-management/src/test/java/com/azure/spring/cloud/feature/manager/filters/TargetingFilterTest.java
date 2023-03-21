// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.azure.spring.cloud.feature.manager.TestConfiguration;
import com.azure.spring.cloud.feature.manager.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.models.TargetingException;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;

@SpringBootTest(classes = {TestConfiguration.class, SpringBootTest.class})
public class TargetingFilterTest {

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String AUDIENCE = "Audience";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String REQUIRED_PARAMETER = "Value cannot be null.";

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

        Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        Map<String, String> users = new LinkedHashMap<String, String>();
        users.put("0", "Doe");

        parameters.put(USERS, users);
        parameters.put(GROUPS, new LinkedHashMap<String, Object>());
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 0);

        context.setParameters(null);
        context.setFeatureName("testFeature");

        TargetingFilter filter = new TargetingFilter(new TargetingFilterTestContextAccessor("doe", null), options);

        Exception exception = assertThrows(TargetingException.class, () -> filter.evaluate(context));
        assertEquals("Audience : " + REQUIRED_PARAMETER, exception.getMessage());
    }

    class TargetingFilterTestContextAccessor implements TargetingContextAccessor {

        private String user;

        private ArrayList<String> groups;

        TargetingFilterTestContextAccessor(String user, ArrayList<String> groups) {
            this.user = user;
            this.groups = groups;
        }

        @Override
        public void configureTargetingContext(TargetingContext context) {
            context.setUserId(user);
            context.setGroups(groups);
        }

    }
}
