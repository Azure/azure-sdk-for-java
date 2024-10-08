// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
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

    /**
     * Can be called to check if a feature is enabled or disabled.
     *
     * @param context ApplicationContext
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     * @param properties FeatureManagementConfigProperties
     */
    FeatureManager(ApplicationContext context, FeatureManagementProperties featureManagementConfigurations,
        FeatureManagementConfigProperties properties) {
        this.context = context;
        this.featureManagementConfigurations = featureManagementConfigurations;
        this.properties = properties;
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
        return Mono.just(checkFeature(feature));
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
        return checkFeature(feature);
    }

    private boolean checkFeature(String featureName) throws FilterNotFoundException {
        if (featureManagementConfigurations.getFeatureFlags() == null) {
            return false;
        }

        Feature feature= featureManagementConfigurations.getFeatureFlags().stream()
            .filter(featureFlag -> featureFlag.getKey().equals(featureName)).findAny().orElse(null);

        if (feature == null || !feature.getEvaluate()) {
            return false;
        }

        Stream<FeatureFilterEvaluationContext> filters = feature.getEnabledFor().stream()
            .filter(Objects::nonNull).filter(featureFilter -> featureFilter.getName() != null);
        
        if (feature.getEnabledFor().size() == 0) {
            return feature.getEvaluate();
        }

        // All Filters must be true
        if (feature.getRequirementType().equals("All")) {
            return filters.allMatch(featureFilter -> isFeatureOn(featureFilter, featureName));
        }

        // Any Filter must be true
        return filters.anyMatch(featureFilter -> isFeatureOn(featureFilter, featureName));
    }

    private boolean isFeatureOn(FeatureFilterEvaluationContext filter, String feature) {
        try {
            FeatureFilter featureFilter = (FeatureFilter) context.getBean(filter.getName());
            filter.setFeatureName(feature);

            return featureFilter.evaluate(filter);
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

    /**
     * Returns the names of all features flags
     *
     * @return a set of all feature names
     */
    public Set<String> getAllFeatureNames() {
        return new HashSet<String>(featureManagementConfigurations.getFeatureFlags().stream().map(feature -> feature.getKey()).toList());
    }

    /**
     * @return the featureManagement
     */
    List<Feature> getFeatureManagement() {
        return featureManagementConfigurations.getFeatureFlags();
    }

}
