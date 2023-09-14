// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.Percentile;
import com.azure.spring.cloud.feature.management.implementation.models.VariantAssignmentGroups;
import com.azure.spring.cloud.feature.management.implementation.models.VariantAssignmentUsers;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.TargetingException;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

/**
 * Evaluator for Dynamic Feature and Feature Filters.
 */
public final class VariantAssignment {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantAssignment.class);

    private final TargetingContextAccessor contextAccessor;

    private final TargetingEvaluationOptions evaluationOptions;
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a feature.
     * 
     * @param contextAccessor Context for evaluating the users/groups.
     */
    VariantAssignment(TargetingContextAccessor contextAccessor) {
        this(contextAccessor, new TargetingEvaluationOptions());
    }

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

        if (validateTargetingContext(targetingContext)) {
            LOGGER.warn("No targeting context available for targeting evaluation.");
            return null;
        }

        for (VariantAssignmentUsers users : allocation.getUsers().values()) {
            for (String user : users.getUsers().values()) {
                if (user.equals(targetingContext.getUserId())) {
                    return getVariant(variants, users.getVariant());
                }
            }
        }

        for (VariantAssignmentGroups groups : allocation.getGroups().values()) {
            for (String group : groups.getGroups().values()) {
                if (targetingContext.getGroups().contains(group)) {
                    return getVariant(variants, groups.getVariant());
                }
            }
        }

        String contextId = "";
        double value = isTargetedPercentage(contextId);

        for (Percentile percentile : allocation.getPercentile().values()) {
            if (percentile.getFrom().doubleValue() <= value && percentile.getTo().doubleValue() > value) {
                return getVariant(variants, percentile.getVariant());
            }
        }

        return getVariant(variants, allocation.getDefaultWhenEnabled());
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

    @SuppressWarnings("unchecked")
    private Variant assignVariant(VariantReference variant) {
        LinkedHashMap<String, Object> thing = new LinkedHashMap<String, Object>();
        try {
            thing = MAPPER.readValue(variant.getConfigurationValue(), LinkedHashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new Variant(variant.getName(), thing);
    }

    private boolean validateTargetingContext(TargetingFilterContext targetingContext) {
        boolean hasUserDefined = StringUtils.hasText(targetingContext.getUserId());
        boolean hasGroupsDefined = targetingContext.getGroups() != null;
        boolean hasAtLeastOneGroup = false;

        if (hasGroupsDefined) {
            hasAtLeastOneGroup = targetingContext.getGroups().stream().anyMatch(group -> StringUtils.hasText(group));
        }

        return (!hasUserDefined && !(hasGroupsDefined && hasAtLeastOneGroup));
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
