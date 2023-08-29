// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilter;
import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.filters.VariantAssignment;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.implementation.models.VariantReference;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.FilterNotFoundException;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled.
 */
public class FeatureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private transient ApplicationContext context;

    private final FeatureManagementProperties featureManagementConfigurations;

    private transient FeatureManagementConfigProperties properties;

    private final ObjectProvider<VariantProperties> propertiesProvider;

    /**
     * Can be called to check if a feature is enabled or disabled.
     * 
     * @param context ApplicationContext
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     * @param properties FeatureManagementConfigProperties
     */
    FeatureManager(ApplicationContext context, FeatureManagementProperties featureManagementConfigurations,
        FeatureManagementConfigProperties properties, ObjectProvider<VariantProperties> propertiesProvider) {
        this.context = context;
        this.featureManagementConfigurations = featureManagementConfigurations;
        this.properties = properties;
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
        return Mono.just(checkFeature(feature, null));
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
    public Mono<Boolean> isEnabledAsync(String feature, Context featureContext) {
        return Mono.just(checkFeature(feature, featureContext));
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
    public Boolean isEnabled(String feature, Context featureContext) {
        return checkFeature(feature, featureContext);

    }

    public <T> Variant<T> getVariant(String feature) {
        return this.<T>generateVariant(feature, null);
    }

    public <T> Variant<T> getVariant(String feature, Context featureContext) {
        return this.<T>generateVariant(feature, featureContext);
    }

    public <T> Mono<Variant<T>> getVariantAsync(String feature) {
        return Mono.just(generateVariant(feature, null));
    }

    public <T> Mono<Variant<T>> getVariantAsync(String feature, Context featureContext) {
        return Mono.just(generateVariant(feature, featureContext));
    }

    private boolean checkFeature(String feature, Context featureContext) throws FilterNotFoundException {
        if (featureManagementConfigurations.getFeatureManagement() == null
            || featureManagementConfigurations.getOnOff() == null) {
            return false;
        }

        Boolean boolFeature = featureManagementConfigurations.getOnOff().get(feature);

        if (boolFeature != null) {
            return boolFeature;
        }

        Feature featureItem = featureManagementConfigurations.getFeatureManagement().get(feature);

        if (featureItem == null || !featureItem.getEvaluate()) {
            return false;
        }

        Stream<FeatureFilterEvaluationContext> filters = featureItem.getEnabledFor().values().stream()
            .filter(Objects::nonNull).filter(featureFilter -> featureFilter.getName() != null);

        // All Filters must be true
        if (featureItem.getRequirementType().equals("All")) {
            return filters.allMatch(featureFilter -> isFeatureOn(featureFilter, feature, featureContext));
        }

        // Any Filter must be true
        return filters.anyMatch(featureFilter -> isFeatureOn(featureFilter, feature, featureContext));
    }

    private boolean isFeatureOn(FeatureFilterEvaluationContext filter, String feature, Context featureContext) {
        try {
            Object featureFilter = context.getBean(filter.getName());

            filter.setFeatureName(feature);
            if (featureFilter instanceof FeatureFilter) {
                return ((FeatureFilter) featureFilter).evaluate(filter);
            } else if (featureFilter instanceof ContextualFeatureFilter) {
                return ((ContextualFeatureFilter) featureFilter).evaluate(filter, featureContext);
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.error("Was unable to find Filter {}. Does the class exist and set as an @Component?",
                filter.getName());
            if (properties.isFailFast()) {
                String message = "Fail fast is set and a Filter was unable to be found";
                ReflectionUtils.rethrowRuntimeException(new FilterNotFoundException(message, e, filter));
            }
        }
        return false;
    }

    private <T> Variant<T> generateVariant(String featureName, Context featureContext) {

        VariantAssignment variantAssignment = new VariantAssignment(null, propertiesProvider);

        if (!StringUtils.hasText(featureName)) {
            throw new IllegalArgumentException("Feature Variant name can not be empty or null.");
        }

        Feature feature = featureManagementConfigurations.getFeatureManagement().get(featureName);

        if (feature == null) {
            throw new FeatureManagementException("The Feature " + featureName + " can not be found.");
        }

        validateVariant(feature, featureName);

        // Disabled?
        if (!feature.getEvaluate() || feature.getEnabledFor().size() == 0) {
            return variantAssignment.getVariant(feature.getVariants(),
                feature.getAllocation().getDefautlWhenDisabled());
        }

        Stream<FeatureFilterEvaluationContext> filters = feature.getEnabledFor().values().stream()
            .filter(Objects::nonNull).filter(featureFilter -> featureFilter.getName() != null);

        boolean isEnabled = false;
        // All Filters must be true
        if (feature.getRequirementType().equals("All")) {
            isEnabled = filters
                .allMatch(featureFilter -> isFeatureOn(featureFilter, feature.getKey(), featureContext));
        } else {
            // Any Filter must be true
            isEnabled = filters
                .anyMatch(featureFilter -> isFeatureOn(featureFilter, feature.getKey(), featureContext));
        }

        if (!isEnabled) {
            return variantAssignment.getVariant(feature.getVariants(),
                feature.getAllocation().getDefautlWhenDisabled());
        }

        return variantAssignment.getVariant(feature.getVariants(), feature.getAllocation().getDefaultWhenEnabled());
    }

    private void validateVariant(Feature feature, String featureName) {
        if (feature.getVariants() == null || feature.getVariants().size() == 0) {
            throw new FeatureManagementException("No assigned Variants");
        }

        for (VariantReference variant : feature.getVariants()) {
            if (!StringUtils.hasText(variant.getName())) {
                throw new FeatureManagementException("Variant needs a name");
            }

            if (StringUtils.hasText(variant.getConfigurationReference())
                && StringUtils.hasText(variant.getConfigurationValue())) {
                throw new FeatureManagementException("Can't have a configuration reference and Configuration Value");
            }

            if (!StringUtils.hasText(variant.getConfigurationReference())
                && !StringUtils.hasText(variant.getConfigurationValue())) {
                throw new FeatureManagementException("Need a configuration reference or Configuration Value");
            }
        }
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
