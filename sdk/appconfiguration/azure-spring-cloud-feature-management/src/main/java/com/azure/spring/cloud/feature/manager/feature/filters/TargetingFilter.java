// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.filters;

import com.azure.spring.cloud.feature.manager.FeatureFilter;
import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.targeting.Audience;
import com.azure.spring.cloud.feature.manager.targeting.GroupRollout;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.manager.targeting.TargetingFilterSettings;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class TargetingFilter implements FeatureFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetingFilter.class);

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String AUDIENCE = "Audience";

    private static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    private static final String REQUIRED_PARAMETER = "Value cannot be null.";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    private final ITargetingContextAccessor contextAccessor;
    private final TargetingEvaluationOptions options;

    public TargetingFilter(ITargetingContextAccessor contextAccessor) {
        this.contextAccessor = contextAccessor;
        this.options = new TargetingEvaluationOptions();
    }

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

        tryValidateSettings(settings);

        Audience audience = settings.getAudience();

        if (targetingContext.getUserId() != null
            && audience.getUsers() != null
            && audience.getUsers().stream()
                .anyMatch(user -> compairStrings(targetingContext.getUserId(), user))
        ) {
            return true;
        }

        if (targetingContext.getGroups() != null && audience.getGroups() != null) {
            for (String group : targetingContext.getGroups()) {
                Optional<GroupRollout> groupRollout = audience.getGroups().stream()
                    .filter(g -> compairStrings(g.getName(), group)).findFirst();

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

    private boolean isTargeted(String contextId, double percentage) {
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

        double contextPercentage = (contextMarker / (double) Integer.MAX_VALUE) * 100;
        return contextPercentage < percentage;
    }

    private void tryValidateSettings(TargetingFilterSettings settings) {
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

    private boolean compairStrings(String s1, String s2) {
        if (options.isIgnoreCase()) {
            return s1.equalsIgnoreCase(s2);
        }
        return s1.equals(s2);
    }

    @SuppressWarnings("unchecked")
    private <T> void updateValueFromMapToList(LinkedHashMap<String, Object> parameters, String key) {
        Object objectMap = parameters.get(key);
        if (objectMap instanceof Map) {
            List<T> toType = ((Map<String, T>) objectMap).values().stream().collect(Collectors.toList());
            parameters.put(key, toType);
        }
    }
}
