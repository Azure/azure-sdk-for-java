//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.Variant;
import com.azure.spring.cloud.feature.management.VariantProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Allocation;
import com.azure.spring.cloud.feature.management.implementation.models.Percentile;
import com.azure.spring.cloud.feature.management.implementation.models.VariantAssignmentGroups;
import com.azure.spring.cloud.feature.management.implementation.models.VariantAssignmentUsers;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.TargetingException;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;

import reactor.core.publisher.Flux;

/**
 * Evaluator for Dynamic Feature and Feature Filters.
 */
public final class VariantAssignment {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantAssignment.class);

    private TargetingContextAccessor contextAccessor;

    private TargetingEvaluationOptions evaluationOptions;

    private final ObjectProvider<VariantProperties> propertiesProvider;

    /**
     * `Microsoft.TargetingFilter` evaluates a user/group/overall rollout of a feature.
     * 
     * @param contextAccessor Context for evaluating the users/groups.
     */
    public VariantAssignment(TargetingContextAccessor contextAccessor,
        ObjectProvider<VariantProperties> propertiesProvider) {
        this(contextAccessor, new TargetingEvaluationOptions(), propertiesProvider);
    }

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

    
    public <T> Variant<T> assignVariant(Allocation allocation, List<VariantReference> variants) {
        TargetingFilterContext targetingContext = new TargetingFilterContext();

        contextAccessor.configureTargetingContext(targetingContext);
        
        if (validateTargetingContext(targetingContext)) {
            LOGGER.warn("No targeting context available for targeting evaluation.");
            return null;
        }
        
        for (VariantAssignmentUsers users : allocation.getUsers()) {
            for (String user : users.getUsers()) {
                if (user.equals(targetingContext.getUserId())) {
                    return getVariant(variants, users.getVariant());
                }
            }
        }
        
        for (VariantAssignmentGroups groups: allocation.getGroups()) {
            for (String group: groups.getGroups()) {
                if (targetingContext.getGroups().contains(group)) {
                    return getVariant(variants, groups.getVariant());
                }
            }
        }
        
        String contextId = "";
        double value = isTargetedPercentage(contextId);
        
        for (Percentile percentile: allocation.getPercentile()) {
            if (percentile.getFrom().doubleValue() <= value && percentile.getTo().doubleValue() > value) {
                return getVariant(variants, percentile.getVariant());
            }
        }
        
        return getVariant(variants, allocation.getDefaultWhenEnabled());
    }


    public <T> Variant<T> getVariant(List<VariantReference> variants, String variantName) {
        return Flux.fromStream(
            variants.stream().filter(variant -> variant.getName().equals(variantName))
                .map(variant -> this.<T>assignVariant(variant)))
            .blockFirst();
    }

    private <T> Variant<T> assignVariant(VariantReference variant) {
        String reference = variant.getConfigurationReference();

        String[] parts = reference.split("\\.");

        String methodName = "get" + parts[0];
        Method method = null;
        Map<String, T> variantMap = null;

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
            variantMap = (Map<String, T>) method.invoke(variantProperties.get());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String message = "Failed invoking " + methodName + " in " + variantProperties.getClass()
                + ". Make sure it exists and is publicly accessible.";
            throw new FeatureManagementException(message, e);
        }

        return new Variant<T>(variant.getName(), variantMap.get(parts[1]));
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