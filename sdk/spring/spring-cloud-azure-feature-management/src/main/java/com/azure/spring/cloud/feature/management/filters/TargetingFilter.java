// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.implementation.FeatureFilterUtils;
import com.azure.spring.cloud.feature.management.implementation.targeting.Audience;
import com.azure.spring.cloud.feature.management.implementation.targeting.GroupRollout;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.TargetingException;
import com.azure.spring.cloud.feature.management.targeting.TargetingContext;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * `Microsoft.TargetingFilter` enables evaluating a user/group/overall rollout of a feature.
 */
public class TargetingFilter implements FeatureFilter, ContextualFeatureFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetingFilter.class);

    /**
     * users field in the filter
     */
    private static final String USERS = "Users";

    /**
     * groups field in the filter
     */
    private static final String GROUPS = "Groups";

    /**
     * Audience in the filter
     */
    private static final String AUDIENCE = "Audience";

    /**
     * Audience that always returns false
     */
    private static final String EXCLUSION_CAMEL = "Exclusion";

    /**
     * Error message for when the total Audience value is greater than 100 percent.
     */
    private static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    private static final String REQUIRED_PARAMETER = "Value cannot be null.";

    /**
     * Object Mapper for converting configurations to features
     */
    protected static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    /**
     * Accessor for identifying the current user/group when evaluating
     */
    private final TargetingContextAccessor contextAccessor;

    /**
     * Options for evaluating the filter
     */
    private final TargetingEvaluationOptions options;

    /**
     * Filter for targeting a user/group/percentage of users.
     *
     * @param contextAccessor Accessor for identifying the current user/group when evaluating
     */
    public TargetingFilter(TargetingContextAccessor contextAccessor) {
        this.contextAccessor = contextAccessor;
        this.options = new TargetingEvaluationOptions();
    }

    /**
     * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a feature.
     *
     * @param contextAccessor Context for evaluating the users/groups.
     * @param options enables customization of the filter.
     */
    public TargetingFilter(TargetingContextAccessor contextAccessor, TargetingEvaluationOptions options) {
        this.contextAccessor = contextAccessor;
        this.options = options;
    }

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        return evaluate(context, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context, Object appContext) {

        if (context == null) {
            throw new IllegalArgumentException("Targeting Context not configured.");
        }

        TargetingContext targetingContext = new TargetingFilterContext();
        if (appContext != null && appContext instanceof TargetingContext) {
            // Use this if, there is an appContext + the contextualAccessor, or there is no contextAccessor.
            targetingContext = (TargetingContext) appContext;
        } else if (contextAccessor != null) {
            // If this is the only one provided just use it.
            contextAccessor.configureTargetingContext(targetingContext);
        }

        if (validateTargetingContext(targetingContext)) {
            LOGGER.warn("No targeting context available for targeting evaluation.");
            return false;
        }

        Map<String, Object> parameters = context.getParameters();

        Object audienceObject = parameters.get(AUDIENCE);
        if (audienceObject != null) {
            parameters = (Map<String, Object>) audienceObject;
        }

        FeatureFilterUtils.updateValueFromMapToList(parameters, USERS, true);
        FeatureFilterUtils.updateValueFromMapToList(parameters, GROUPS, true);

        Audience audience;
        String exclusionValue = FeatureFilterUtils.getKeyCase(parameters, EXCLUSION_CAMEL);
        String exclusionUserValue = FeatureFilterUtils.getKeyCase((Map<String, Object>) parameters.get(exclusionValue),
            "Users");
        String exclusionGroupsValue = FeatureFilterUtils
            .getKeyCase((Map<String, Object>) parameters.get(exclusionValue), "Groups");

        if (((Map<String, Object>) parameters.getOrDefault(exclusionValue, new HashMap<>()))
            .get(exclusionUserValue) instanceof List) {
            audience = OBJECT_MAPPER.convertValue(parameters, Audience.class);
        } else {
            // When it comes from a file exclusions can be a map instead of a list.
            Map<String, List<String>> exclusionMap = (Map<String, List<String>>) parameters.remove(exclusionValue);
            if (exclusionMap == null) {
                exclusionMap = new HashMap<>();
            }

            Object users = exclusionMap.get(exclusionUserValue);
            Object groups = exclusionMap.get(exclusionGroupsValue);

            Map<String, Object> exclusion = new HashMap<>();

            if (users instanceof Map) {
                exclusion.put(USERS, new ArrayList<>(((Map<String, String>) users).values()));
            }
            if (groups instanceof Map) {
                exclusion.put(GROUPS, new ArrayList<>(((Map<String, String>) groups).values()));
            }
            parameters.put(exclusionValue, exclusion);

            audience = OBJECT_MAPPER.convertValue(parameters, Audience.class);
        }

        validateSettings(audience);

        // Need to Check denied first
        if (targetUser(targetingContext.getUserId(), audience.getExclusion().getUsers())) {
            return false;
        }

        if (targetingContext.getGroups() != null && audience.getExclusion().getGroups() != null) {
            for (String group : targetingContext.getGroups()) {
                Optional<String> groupRollout = audience.getExclusion().getGroups().stream()
                    .filter(g -> equals(g, group)).findFirst();
                if (groupRollout.isPresent()) {
                    return false;
                }
            }
        }

        // Check if Allowed
        if (targetUser(targetingContext.getUserId(), audience.getUsers())) {
            return true;
        }

        if (targetingContext.getGroups() != null && audience.getGroups() != null) {
            for (String group : targetingContext.getGroups()) {
                if (targetGroup(audience, targetingContext, context, group)) {
                    return true;
                }
            }
        }

        String defaultContextId = targetingContext.getUserId() + "\n" + context.getFeatureName();

        return isTargeted(defaultContextId, audience.getDefaultRolloutPercentage());
    }

    private boolean targetUser(String userId, Collection<String> users) {
        return userId != null && users != null && users.stream().anyMatch(user -> equals(userId, user));
    }

    private boolean targetGroup(Audience audience, TargetingContext targetingContext,
        FeatureFilterEvaluationContext context, String group) {
        Optional<GroupRollout> groupRollout = audience.getGroups().stream()
            .filter(g -> equals(g.getName(), group)).findFirst();

        if (groupRollout.isPresent()) {
            String userId = "";
            if (targetingContext.getUserId() != null) {
                userId = targetingContext.getUserId();
            }
            String audienceContextId = userId + "\n" + context.getFeatureName() + "\n" + group;

            if (isTargeted(audienceContextId, groupRollout.get().getRolloutPercentage())) {
                return true;
            }
        }
        return false;
    }

    private boolean validateTargetingContext(TargetingContext targetingContext) {
        boolean hasUserDefined = StringUtils.hasText(targetingContext.getUserId());
        boolean hasGroupsDefined = targetingContext.getGroups() != null;
        boolean hasAtLeastOneGroup = false;

        if (hasGroupsDefined) {
            hasAtLeastOneGroup = targetingContext.getGroups().stream().anyMatch(group -> StringUtils.hasText(group));
        }

        return (!hasUserDefined && !(hasGroupsDefined && hasAtLeastOneGroup));
    }

    private boolean isTargeted(String contextId, double percentage) {
        return FeatureFilterUtils.isTargetedPercentage(contextId) < percentage;
    }

    /**
     * Validates the settings of a targeting filter.
     *
     * @param audience targeting filter settings
     * @throws TargetingException when a required parameter is missing or percentage value is greater than 100.
     */
    void validateSettings(Audience audience) {
        String paramName = "";
        String reason = "";

        if (audience == null) {
            paramName = AUDIENCE;
            reason = REQUIRED_PARAMETER;

            throw new TargetingException(paramName + " : " + reason);
        }

        if (audience.getDefaultRolloutPercentage() < 0 || audience.getDefaultRolloutPercentage() > 100) {
            paramName = AUDIENCE + "." + audience.getDefaultRolloutPercentage();
            reason = OUT_OF_RANGE;

            throw new TargetingException(paramName + " : " + reason);
        }

        if (audience.getGroups() != null) {
            List<GroupRollout> groups = new ArrayList<GroupRollout>(audience.getGroups());
            for (int index = 0; index < groups.size(); index++) {
                GroupRollout groupRollout = groups.get(index);
                if (groupRollout.getRolloutPercentage() < 0 || groupRollout.getRolloutPercentage() > 100) {
                    paramName = AUDIENCE + "[" + index + "]." + groups.get(index).getRolloutPercentage();
                    reason = OUT_OF_RANGE;

                    throw new TargetingException(paramName + " : " + reason);
                }
            }
        }
    }

    /**
     * Checks if two strings are equal, ignores case if configured to.
     *
     * @param s1 string to compare
     * @param s2 string to compare
     * @return true if the strings are equal
     */
    private boolean equals(String s1, String s2) {
        if (options.isIgnoreCase()) {
            return s1.equalsIgnoreCase(s2);
        }
        return s1.equals(s2);
    }
}
