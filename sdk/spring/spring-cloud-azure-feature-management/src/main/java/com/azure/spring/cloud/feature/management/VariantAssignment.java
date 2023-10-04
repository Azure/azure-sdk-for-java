// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.GroupAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.PercentileAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.UserAllocation;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.TargetingException;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;

import reactor.core.publisher.Flux;

/**
 * Evaluator for Dynamic Feature and Feature Filters.
 */
public final class VariantAssignment {

    private final TargetingContextAccessor contextAccessor;

    private final TargetingEvaluationOptions evaluationOptions;

    /**
     * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a feature.
     * 
     * @param contextAccessor Context for evaluating the users/groups.
     * @param options enables customization of the filter.
     */
    VariantAssignment(TargetingContextAccessor contextAccessor, TargetingEvaluationOptions options) {
        this.contextAccessor = contextAccessor;
        this.evaluationOptions = options;
    }

    /**
     * Assigns a Variant based on the allocations
     * @param <T> type of the variant
     * @param allocation Allocation percentage of the variants
     * @param variants List of the possible variants.
     * @return Variant object containing an instance of the type
     */
    Variant assignVariant(Allocation allocation, Collection<VariantReference> variants) {
        TargetingFilterContext targetingContext = new TargetingFilterContext();

        contextAccessor.configureTargetingContext(targetingContext);

        String targetedUser = targetingContext.getUserId();

        for (UserAllocation users : allocation.getUsers().values()) {
            for (String user : users.getUsers().values()) {
                if (targetedUser != null) {
                    if (evaluationOptions.isIgnoreCase()) {
                        user = user.toLowerCase(Locale.getDefault());
                        targetedUser = targetedUser.toLowerCase(Locale.getDefault());
                    }

                    if (user.equals(targetedUser)) {
                        return getVariant(variants, users.getVariant());
                    }
                }
            }
        }

        for (GroupAllocation groups : allocation.getGroups().values()) {
            for (String group : groups.getGroups().values()) {
                if (evaluationOptions.isIgnoreCase()) {
                    group = group.toLowerCase(Locale.getDefault());
                }

                for (String targetedGroup : targetingContext.getGroups()) {
                    if (targetedGroup != null) {
                        if (evaluationOptions.isIgnoreCase()) {
                            targetedGroup = targetedGroup.toLowerCase(Locale.getDefault());
                        }
                        if (targetedGroup.equals(group)) {
                            return getVariant(variants, groups.getVariant());
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
        double value = isTargetedPercentage(contextId);

        for (PercentileAllocation percentile : allocation.getPercentile().values()) {
            if (percentile.getFrom().doubleValue() <= value && percentile.getTo().doubleValue() > value) {
                return getVariant(variants, percentile.getVariant());
            }
        }

        if (StringUtils.hasText(allocation.getDefaultWhenEnabled())) {
            return getVariant(variants, allocation.getDefaultWhenEnabled());
        }

        return null;
    }

    /**
     * Returns a variant for the given name.
     * @param <T> Type of the variant
     * @param variants List of the Variant References which can be returned
     * @param variantName Name of the assigned variant
     * @return Variant object containing an instance of the type
     */
    Variant getVariant(Collection<VariantReference> variants, String variantName) {
        return Flux.fromStream(
            variants.stream().filter(variant -> variant.getName().equals(variantName))
                .map(variant -> this.assignVariant(variant)))
            .blockFirst();
    }

    private Variant assignVariant(VariantReference variant) {
        return new Variant(variant.getName(), variant.getConfigurationValue());
    }

    /**
     * Computes the percentage that the contextId falls into.
     * 
     * @param contextId Id of the context being targeted
     * @return the bucket value of the context id
     * @throws TargetingException Unable to create hash of target context
     */
    private double isTargetedPercentage(String contextId) {
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

}
