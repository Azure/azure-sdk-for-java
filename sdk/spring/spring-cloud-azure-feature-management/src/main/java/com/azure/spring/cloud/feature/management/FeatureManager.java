// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static com.azure.spring.cloud.feature.management.implementation.FeatureManagementConstants.ALL_REQUIREMENT_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilter;
import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilterAsync;
import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.filters.FeatureFilterAsync;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.VariantAssignment;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.FilterNotFoundException;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled.
 */
public class FeatureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private transient ApplicationContext context;

    private final FeatureManagementProperties featureManagementConfigurations;

    private transient FeatureManagementConfigProperties properties;

    private final TargetingContextAccessor contextAccessor;

    private final TargetingEvaluationOptions evaluationOptions;

    private final ObjectProvider<VariantProperties> propertiesProvider;

    /**
     * Can be called to check if a feature is enabled or disabled.
     * 
     * @param context ApplicationContext
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     * @param properties FeatureManagementConfigProperties
     */
    FeatureManager(ApplicationContext context, FeatureManagementProperties featureManagementConfigurations,
        FeatureManagementConfigProperties properties, TargetingContextAccessor contextAccessor,
        TargetingEvaluationOptions evaluationOptions, ObjectProvider<VariantProperties> propertiesProvider) {
        this.context = context;
        this.featureManagementConfigurations = featureManagementConfigurations;
        this.properties = properties;
        this.contextAccessor = contextAccessor;
        this.evaluationOptions = evaluationOptions;
        this.propertiesProvider = propertiesProvider;
    }

    /**
     * Checks to see if the feature is enabled. If enabled it check each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     *
     * @param feature Feature being checked.
     * @return state of the feature
     * @throws FilterNotFoundException file not found
     */
    public Mono<Boolean> isEnabledAsync(String feature) {
        return checkFeature(feature, null);
    }

    /**
     * Checks to see if the feature is enabled. If enabled it check each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     *
     * @param feature Feature being checked.
     * @return state of the feature
     * @throws FilterNotFoundException file not found
     */
    public Boolean isEnabled(String feature) throws FilterNotFoundException {
        return checkFeature(feature, null).block();
    }

    /**
     * 
     * @param feature Feature being checked.
     * @return Assigned Variant
     */
    public Variant getVariant(String feature) {
        return generateVariant(feature, null).block();
    }

    /**
     * 
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return Assigned Variant
     */
    public Variant getVariant(String feature, Object featureContext) {
        return generateVariant(feature, featureContext).block();
    }

    /**
     * 
     * @param feature Feature being checked.
     * @return Assigned Variant
     */
    public Mono<Variant> getVariantAsync(String feature) {
        return generateVariant(feature, null).single();
    }

    /**
     * 
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return Assigned Variant
     */
    public Mono<Variant> getVariantAsync(String feature, Object featureContext) {
        return generateVariant(feature, featureContext).single();
    }

    private Mono<Boolean> checkFeature(String featureName, Object featureContext) throws FilterNotFoundException {
        if (featureManagementConfigurations.getFeatureManagement() == null
            || featureManagementConfigurations.getOnOff() == null) {
            return Mono.just(false);
        }

        Boolean boolFeature = featureManagementConfigurations.getOnOff().get(featureName);

        if (boolFeature != null) {
            return Mono.just(boolFeature);
        }

        Feature feature = featureManagementConfigurations.getFeatureManagement().get(featureName);

        if (feature == null || !feature.getEvaluate()) {
            return Mono.just(false);
        }

        List<Mono<Boolean>> results = new ArrayList<>();

        for (FeatureFilterEvaluationContext featureFilter : feature.getEnabledFor().values()) {
            if (StringUtils.hasText(featureFilter.getName())) {
                results.add(isFeatureOn(featureFilter, featureName, featureContext));
            }
        }

        String defaultDisabledVariant = feature.getAllocation().getDefaultWhenDisabled();
        Collection<VariantReference> variants = feature.getVariants().values();

        if (results.size() > 0) {
            return evaluateFeatureFlagResults(feature, results).map(filterAssignedValue -> {
                if (filterAssignedValue && feature.getVariants().size() > 0) {
                    // Feature is on by Feature Flags, check allocation
                    validateVariant(feature);

                    VariantAssignment variantAssignment = new VariantAssignment(contextAccessor, evaluationOptions,
                        propertiesProvider);
                    // Checking if allocation overrides true result
                    return checkDefaultOverride(feature.getVariants().values(), true,
                        variantAssignment.assignVariant(feature.getAllocation()));
                } else if (filterAssignedValue) {
                    return true;
                }
                // Feature is disabled by Feature Flags, check default disabled variant
                return checkDefaultOverride(variants, false, defaultDisabledVariant);
            });
        } else if (feature.getVariants().size() > 0 && StringUtils.hasText(defaultDisabledVariant)) {
            return Mono.just(checkDefaultOverride(variants, false, defaultDisabledVariant));

        }

        return Mono.just(false);

    }

    private Boolean checkDefaultOverride(Collection<VariantReference> variants, Boolean result,
        String defaultVariantName) {
        if (defaultVariantName == null) {
            return result;
        }
        // Should return the given value, but the default disabled variant may override.
        Iterator<VariantReference> variantsIterator = variants.iterator();
        while (variantsIterator.hasNext()) {
            VariantReference variantReference = variantsIterator.next();
            if (defaultVariantName.equals(variantReference.getName())
                && StringUtils.hasText(variantReference.getStatusOverride())) {
                // If any text is provided we assume it is a valid boolean and return true/false, invalid booleans
                // return false
                return Boolean.valueOf(variantReference.getStatusOverride());
            }
        }
        return result;
    }

    private Mono<Variant> generateVariant(String featureName, Object featureContext) {

        if (!StringUtils.hasText(featureName)) {
            throw new IllegalArgumentException("Feature Variant name can not be empty or null.");
        }

        Feature feature = featureManagementConfigurations.getFeatureManagement().get(featureName);

        if (feature == null) {
            throw new FeatureManagementException("The Feature " + featureName + " can not be found.");
        }

        validateVariant(feature);

        VariantAssignment variantAssignment = new VariantAssignment(contextAccessor, evaluationOptions,
            propertiesProvider);

        Collection<VariantReference> variants = feature.getVariants().values();
        String defaultDisabledVariant = feature.getAllocation().getDefaultWhenDisabled();

        // Disabled?
        if (!feature.getEvaluate() && StringUtils.hasText(defaultDisabledVariant)) {
            return variantAssignment.getVariant(variants, defaultDisabledVariant).single();
        } else if (!feature.getEvaluate()) {
            return Mono.justOrEmpty(null);
        } else if (feature.getEnabledFor().size() == 0) {
            return variantAssignment.getVariant(variants, variantAssignment.assignVariant(feature.getAllocation()));
        }

        List<Mono<Boolean>> results = new ArrayList<>();

        for (FeatureFilterEvaluationContext featureFilter : feature.getEnabledFor().values()) {
            if (StringUtils.hasText(featureFilter.getName())) {
                results.add(isFeatureOn(featureFilter, feature.getKey(), featureContext));
            }
        }

        return evaluateFeatureFlagResults(feature, results).flatMap(enabled -> {
            if (!enabled && StringUtils.hasText(defaultDisabledVariant)) {
                return variantAssignment.getVariant(feature.getVariants().values(), defaultDisabledVariant).single();
            } else if (!enabled) {
                return Mono.justOrEmpty(null);
            }

            return variantAssignment.getVariant(variants, variantAssignment.assignVariant(feature.getAllocation()));
        });
    }

    private Mono<Boolean> evaluateFeatureFlagResults(Feature feature, List<Mono<Boolean>> results) {
        // All Filters must be true
        if (ALL_REQUIREMENT_TYPE.equals(feature.getRequirementType())) {
            return Flux.merge(results).reduce((a, b) -> a && b).single();
        }
        // Any Filter must be true
        return Flux.merge(results).reduce((a, b) -> a || b).single();

    }

    private void validateVariant(Feature feature) {
        if (feature.getVariants() == null || feature.getVariants().size() == 0) {
            throw new FeatureManagementException("The feature " + feature.getKey() + " has no assigned Variants.");
        }

        for (VariantReference variant : feature.getVariants().values()) {
            if (!StringUtils.hasText(variant.getName())) {
                throw new FeatureManagementException("Variant needs a name");
            }

            if (variant.getConfigurationValue() == null && variant.getConfigurationReference() == null) {
                throw new FeatureManagementException(
                    "The feature " + feature.getKey() + " needs a Configuration Value or Configuration Reference.");
            }
        }
    }

    private Mono<Boolean> isFeatureOn(FeatureFilterEvaluationContext filter, String feature, Object featureContext) {
        try {
            Object featureFilter = context.getBean(filter.getName());

            filter.setFeatureName(feature);
            if (featureFilter instanceof FeatureFilter) {
                return Mono.just(((FeatureFilter) featureFilter).evaluate(filter));
            } else if (featureFilter instanceof ContextualFeatureFilter) {
                return Mono.just(((ContextualFeatureFilter) featureFilter).evaluate(filter, featureContext));
            } else if (featureFilter instanceof FeatureFilterAsync) {
                return ((FeatureFilterAsync) featureFilter).evaluateAsync(filter);
            } else if (featureFilter instanceof ContextualFeatureFilterAsync) {
                return ((ContextualFeatureFilterAsync) featureFilter).evaluateAsync(filter, featureContext);
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.error("Was unable to find Filter {}. Does the class exist and set as an @Component?",
                filter.getName());
            if (properties.isFailFast()) {
                String message = "Fail fast is set and a Filter was unable to be found";
                ReflectionUtils.rethrowRuntimeException(new FilterNotFoundException(message, e, filter));
            }
        }
        return Mono.just(false);
    }

    /**
     * Returns the names of all features flags
     *
     * @return a set of all feature names
     */
    public Set<String> getAllFeatureNames() {
        Set<String> allFeatures = new HashSet<>();

        allFeatures.addAll(featureManagementConfigurations.getOnOff().keySet());
        allFeatures.addAll(featureManagementConfigurations.getFeatureManagement().keySet());
        return allFeatures;
    }

    /**
     * @return the featureManagement
     */
    Map<String, Feature> getFeatureManagement() {
        return featureManagementConfigurations.getFeatureManagement();
    }

    /**
     * @return the onOff
     */
    Map<String, Boolean> getOnOff() {
        return featureManagementConfigurations.getOnOff();
    }

}
