// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.evaluators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.feature.manager.TargetingException;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureVariant;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.IFeatureVariantAssigner;
import com.azure.spring.cloud.feature.manager.feature.filters.TargetingFilter;
import com.azure.spring.cloud.feature.manager.targeting.Audience;
import com.azure.spring.cloud.feature.manager.targeting.GroupRollout;
import com.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.azure.spring.cloud.feature.manager.targeting.TargetingContext;
import com.azure.spring.cloud.feature.manager.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.manager.targeting.TargetingFilterSettings;

import reactor.core.publisher.Mono;

public class TargetingEvaluator extends TargetingFilter implements IFeatureVariantAssigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetingEvaluator.class);

    protected static final String USERS = "users";

    protected static final String GROUPS = "groups";

    protected static final String AUDIENCE = "Audience";

    private static final String OUT_OF_RANGE = "The value is out of the accepted range.";

    public TargetingEvaluator(ITargetingContextAccessor contextAccessor) {
        super(contextAccessor);
    }

    public TargetingEvaluator(ITargetingContextAccessor contextAccessor, TargetingEvaluationOptions options) {
        super(contextAccessor, options);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition) throws TargetingException {
        TargetingContext targetingContext = contextAccessor.getContextAsync().block();

        if (targetingContext == null) {
            LOGGER.warn("No targeting context available for targeting evaluation.");
            return Mono.justOrEmpty(null);
        }

        TargetingFilterSettings settings = new TargetingFilterSettings();

        List<FeatureVariant> variants = featureDefinition.getVariants();

        validateVarientSettings(variants);

        HashMap<String, Double> totalGroupPerentages = new HashMap<>();
        double totalDefaultPercentage = 0;

        for (FeatureVariant variant : variants) {

            LinkedHashMap<String, Object> parameters = variant.getAssignmentParameters();

            if (parameters != null) {
                Object audienceObject = parameters.get(AUDIENCE);
                if (audienceObject != null) {
                    parameters = (LinkedHashMap<String, Object>) audienceObject;
                }

                this.<String>updateValueFromMapToList(parameters, USERS);
                updateValueFromMapToList(parameters, GROUPS);

                settings.setAudience(OBJECT_MAPPER.convertValue(parameters, Audience.class));
            }

            Audience audience = settings.getAudience();

            if (targetingContext.getUserId() != null
                && audience.getUsers() != null
                && audience.getUsers().stream()
                    .anyMatch(user -> compareStrings(targetingContext.getUserId(), user))) {
                return Mono.just(variant);
            }

            if (targetingContext.getGroups() != null && audience.getGroups() != null) {
                for (String group : targetingContext.getGroups()) {
                    Optional<GroupRollout> groupRollout = audience.getGroups().stream()
                        .filter(g -> compareStrings(g.getName(), group)).findFirst();

                    if (groupRollout.isPresent()) {
                        String audienceContextId = targetingContext.getUserId() + "\n" + featureDefinition.getName()
                            + "\n"
                            + group;

                        double chance = totalGroupPerentages.getOrDefault(group, (double) 0);

                        if (isTargetedPercentage(audienceContextId) < groupRollout.get().getRolloutPercentage() +
                            chance) {
                            return Mono.just(variant);
                        }
                        totalGroupPerentages.put(group, chance + groupRollout.get().getRolloutPercentage());
                    }
                }
            }

            String defaultContextId = targetingContext.getUserId() + "\n" + featureDefinition.getName();

            if (isTargetedPercentage(defaultContextId) < settings.getAudience().getDefaultRolloutPercentage() +
                totalDefaultPercentage) {
                return Mono.just(variant);
            }
            totalDefaultPercentage += settings.getAudience().getDefaultRolloutPercentage();
        }

        return Mono.justOrEmpty(null);
    }

    @SuppressWarnings("unchecked")
    protected void validateVarientSettings(List<FeatureVariant> varientSettings) throws TargetingException {
        String paramName = "";
        String reason = "";

        Map<String, Double> groupUsed = new HashMap<>();

        for (FeatureVariant variant : varientSettings) {
            TargetingFilterSettings settings = new TargetingFilterSettings();
            LinkedHashMap<String, Object> parameters = variant.getAssignmentParameters();

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

            List<GroupRollout> groups = audience.getGroups();

            if (groups != null) {
                for (int index = 0; index < groups.size(); index++) {
                    GroupRollout groupRollout = groups.get(index);
                    Double currentSize = groupUsed.getOrDefault(groupRollout.getName(), (double) 0);
                    currentSize += groupRollout.getRolloutPercentage();
                    if (currentSize > 100) {
                        paramName = groupRollout.getName();
                        reason = OUT_OF_RANGE;

                        throw new TargetingException(paramName + " : " + reason);
                    }
                    groupUsed.put(groupRollout.getName(), currentSize);
                }
            }
        }
    }
}
