// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.filters;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.implementation.targeting.Audience;
import com.azure.spring.cloud.feature.manager.implementation.targeting.GroupRollout;
import com.azure.spring.cloud.feature.manager.implementation.targeting.TargetingFilterSettings;
import com.azure.spring.cloud.feature.manager.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.models.IFeatureFilter;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * `Microsoft.TargetingFilter` enables evaluating a user/group/overall rollout of a feature.
 */
public class TargetingFilter implements IFeatureFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetingFilter.class);

    /**
     * users field in the filter
     */
    protected static final String USERS = "users";

    /**
     * groups field in the filter
     */
    protected static final String GROUPS = "groups";

    /**
     * Audience in the filter
     */
    protected static final String AUDIENCE = "Audience";

    /**
     * Error message for when the total Audience value is greater than 100 percent.
     */
    protected static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    private static final String REQUIRED_PARAMETER = "Value cannot be null.";

    /**
     * Object Mapper for converting configurations to variants
     */
    protected static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    /**
     * Accessor for identifying the current user/group when evaluating
     */
    protected final ITargetingContextAccessor contextAccessor;

    /**
     * Options for evaluating the filter
     */
    protected final TargetingEvaluationOptions options;

    /**
     * Filter for targeting a user/group/percentage of users.
     * @param contextAccessor Accessor for identifying the current user/group when evaluating
     */
    public TargetingFilter(ITargetingContextAccessor contextAccessor) {
        this.contextAccessor = contextAccessor;
        this.options = new TargetingEvaluationOptions();
    }

    /**
     * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a feature.
     * @param contextAccessor Context for evaluating the users/groups.
     * @param options enables customization of the filter.
     */
    public TargetingFilter(ITargetingContextAccessor contextAccessor, TargetingEvaluationOptions options) {
        this.contextAccessor = contextAccessor;
        this.options = options;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Targeting Context not configured.");
        }

        TargetingContext targetingContext = contextAccessor.getContextAsync().block();

        if (targetingContext == null) {
            LOGGER.warn("No targeting context available for targeting evaluation.");
            return false;
        }

        TargetingFilterSettings settings = new TargetingFilterSettings();

        LinkedHashMap<String, Object> parameters = context.getParameters();

        if (parameters != null) {
            Object audienceObject = parameters.get(AUDIENCE);
            if (audienceObject != null) {
                parameters = (LinkedHashMap<String, Object>) audienceObject;
            }

            this.<String>updateValueFromMapToList(parameters, USERS);
            updateValueFromMapToList(parameters, GROUPS);

            settings.setAudience(OBJECT_MAPPER.convertValue(parameters, Audience.class));
        }

        validateSettings(settings);

        Audience audience = settings.getAudience();

        if (targetingContext.getUserId() != null
            && audience.getUsers() != null
            && audience.getUsers().stream()
                .anyMatch(user -> compareStrings(targetingContext.getUserId(), user))) {
            return true;
        }

        if (targetingContext.getGroups() != null && audience.getGroups() != null) {
            for (String group : targetingContext.getGroups()) {
                Optional<GroupRollout> groupRollout = audience.getGroups().stream()
                    .filter(g -> compareStrings(g.getName(), group)).findFirst();

                if (groupRollout.isPresent()) {
                    String audienceContextId = targetingContext.getUserId() + "\n" + context.getName() + "\n" + group;

                    if (isTargeted(audienceContextId, groupRollout.get().getRolloutPercentage())) {
                        return true;
                    }
                }
            }
        }

        String defaultContextId = targetingContext.getUserId() + "\n" + context.getFeatureName();

        return isTargeted(defaultContextId, settings.getAudience().getDefaultRolloutPercentage());
    }

    /**
     * Computes the percentage that the contextId falls into.
     * @param contextId Id of the context being targeted
     * @return the bucket value of the context id
     * @throws TargetingException Unable to create hash of target context
     */
    protected double isTargetedPercentage(String contextId) {
        byte[] hash = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(contextId.getBytes(Charset.defaultCharset()));
        } catch (NoSuchAlgorithmException e) {
            throw new TargetingException("Unable to find SHA-256 for targeting.", e);
        }

        if (hash == null) {
            throw new TargetingException("Unable to create Targeting Hash for " + contextId);
        }

        ByteBuffer wrapped = ByteBuffer.wrap(hash);
        int contextMarker = Math.abs(wrapped.getInt());

        return (contextMarker / (double) Integer.MAX_VALUE) * 100;
    }

    private boolean isTargeted(String contextId, double percentage) {
        return isTargetedPercentage(contextId) < percentage;
    }

    /**
     * Validates the settings of a targeting filter.
     * @param settings targeting filter settings
     * @throws TargetingException when a required parameter is missing or percentage value is greater than 100.
     */
    void validateSettings(TargetingFilterSettings settings) {
        String paramName = "";
        String reason = "";

        if (settings.getAudience() == null) {
            paramName = AUDIENCE;
            reason = REQUIRED_PARAMETER;

            throw new TargetingException(paramName + " : " + reason);
        }

        Audience audience = settings.getAudience();
        if (audience.getDefaultRolloutPercentage() < 0
            || audience.getDefaultRolloutPercentage() > 100) {
            paramName = AUDIENCE + "." + audience.getDefaultRolloutPercentage();
            reason = OUT_OF_RANGE;

            throw new TargetingException(paramName + " : " + reason);
        }

        List<GroupRollout> groups = audience.getGroups();
        if (groups != null) {
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
     * @param s1 string to compare
     * @param s2 string to compare
     * @return true if the strings are equal
     */
    protected boolean compareStrings(String s1, String s2) {
        if (options.isIgnoreCase()) {
            return s1.equalsIgnoreCase(s2);
        }
        return s1.equals(s2);
    }

    /**
     * Looks at the given key in the parameters and coverts it to a list if it is currently a map. Used for updating
     * fields in the targeting filter.
     * @param <T> Type of object inside of parameters for the given key
     * @param parameters map of generic objects
     * @param key key of object int the parameters map
     */
    @SuppressWarnings("unchecked")
    protected <T> void updateValueFromMapToList(LinkedHashMap<String, Object> parameters, String key) {
        Object objectMap = parameters.get(key);
        if (objectMap instanceof Map) {
            List<T> toType = ((Map<String, T>) objectMap).values().stream().collect(Collectors.toList());
            parameters.put(key, toType);
        }
    }
}
