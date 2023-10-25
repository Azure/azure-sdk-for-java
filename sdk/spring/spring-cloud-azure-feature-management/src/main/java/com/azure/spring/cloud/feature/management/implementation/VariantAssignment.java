// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.Variant;
import com.azure.spring.cloud.feature.management.VariantProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.GroupAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.PercentileAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.UserAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Evaluator for Dynamic Feature and Feature Filters.
 */
public final class VariantAssignment {

    private final TargetingContextAccessor contextAccessor;

    private final TargetingEvaluationOptions evaluationOptions;

    private final ObjectProvider<VariantProperties> propertiesProvider;

    /**
     * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a feature.
     * 
     * @param contextAccessor Context for evaluating the users/groups. 
     * @param options enables customization of the filter.
     */
    public VariantAssignment(TargetingContextAccessor contextAccessor, TargetingEvaluationOptions options,
        ObjectProvider<VariantProperties> propertiesProvider) {
        this.contextAccessor = contextAccessor;
        this.evaluationOptions = options;
        this.propertiesProvider = propertiesProvider;
    }

    /**
     * Assigns a Variant based on the allocations
     * @param allocation Allocation percentage of the variants
     * @param variants List of the possible variants.
     * @return Variant object containing an instance of the type
     */
    public String assignVariant(Allocation allocation) {
        TargetingFilterContext targetingContext = new TargetingFilterContext();

        if (contextAccessor == null) {
            throw new FeatureManagementException("No Targeting Filter Context found to assign variant.");
        }

        contextAccessor.configureTargetingContext(targetingContext);

        String targetedUser = targetingContext.getUserId();

        if (targetedUser != null) {
            for (UserAllocation users : allocation.getUsers().values()) {
                for (String user : users.getUsers().values()) {
                    if (evaluationOptions.isIgnoreCase()) {
                        user = user.toLowerCase(Locale.getDefault());
                        targetedUser = targetedUser.toLowerCase(Locale.getDefault());
                    }

                    if (user.equals(targetedUser)) {
                        return users.getVariant();
                    }
                }
            }
        }

        if (targetingContext.getGroups() != null) {
            for (String targetedGroup : targetingContext.getGroups()) {
                if (targetedGroup != null) {
                    for (GroupAllocation groups : allocation.getGroups().values()) {
                        for (String group : groups.getGroups().values()) {
                            if (evaluationOptions.isIgnoreCase()) {
                                group = group.toLowerCase(Locale.getDefault());
                            }

                            if (evaluationOptions.isIgnoreCase()) {
                                targetedGroup = targetedGroup.toLowerCase(Locale.getDefault());
                            }
                            if (targetedGroup.equals(group)) {
                                return groups.getVariant();
                            }
                        }
                    }

                }
            }
        }

        String contextId = "";
        if (targetedUser != null) {
            contextId += targetedUser + "\n";
        }
        if (StringUtils.hasText(allocation.getSeed())) {
            contextId += allocation.getSeed() + "\n";
        }
        double value = TargetingUtils.isTargetedPercentage(contextId);

        for (PercentileAllocation percentile : allocation.getPercentile().values()) {
            if (percentile.getFrom().doubleValue() <= value
                && (percentile.getTo().doubleValue() > value || 100 == percentile.getTo().doubleValue())) {
                return percentile.getVariant();
            }
        }

        if (StringUtils.hasText(allocation.getDefaultWhenEnabled())) {
            return allocation.getDefaultWhenEnabled();
        }

        return null;
    }
  
    /**
     * Returns a variant for the given name.
     * @param variants List of the Variant References which can be returned
     * @param variantName Name of the assigned variant
     * @return Variant object containing an instance of the type
     */
    public Mono<Variant> getVariant(Collection<VariantReference> variants, String variantName) {
        if (variantName == null || variants == null || variants.size() == 0) {
            return Mono.justOrEmpty(null);
        }

        Stream<Variant> streamVariant = variants.stream().filter(variant -> {
            return variant.getName().equals(variantName);
        }).map(variant -> this.makeVariant(variant));

        return Flux.fromStream(streamVariant).single();
    }

    @SuppressWarnings("unchecked")
    private Variant makeVariant(VariantReference variant) {
        if (variant.getConfigurationValue() != null) {
            return new Variant(variant.getName(), variant.getConfigurationValue());
        }

        String reference = variant.getConfigurationReference();

        String[] parts = reference.split("\\.");

        String methodName = "get" + parts[0];
        Method method = null;
        Map<String, Object> variantMap = null;

        Optional<VariantProperties> variantProperties = propertiesProvider.stream().filter(properties -> {
            try {
                properties.getClass().getMethod(methodName);
                return true;
            } catch (NoSuchMethodException | SecurityException e) {
                return false;
            }
        }).findFirst();

        if (!variantProperties.isPresent()) {
            String message = "Failed to load " + methodName + ". No ConfigurationProperties where found containing it."
                + ". Make sure it exists and is publicly accessible.";
            throw new FeatureManagementException(message);
        }

        // Dynamically Accesses @ConfigurationProperties and finds the matching method.
        try {
            method = variantProperties.get().getClass().getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException e) {
            String message = "Failed to load " + methodName + " in " + variantProperties.getClass()
                + ". Make sure it exists and is publicly accessible.";
            throw new FeatureManagementException(message, e);
        }
        // Calls method to get back an Object, this object contains multiple variants
        // each has a get method.
        try {
            variantMap = (Map<String, Object>) method.invoke(variantProperties.get());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String message = "Failed invoking " + methodName + " in " + variantProperties.getClass()
                + ". Make sure it exists and is publicly accessible.";
            throw new FeatureManagementException(message, e);
        }

        return new Variant(variant.getName(), variantMap.get(parts[1]));
    }

}
