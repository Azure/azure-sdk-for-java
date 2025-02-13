// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static com.azure.spring.cloud.feature.management.implementation.FeatureManagementConstants.ALL_REQUIREMENT_TYPE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilter;
import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilterAsync;
import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.filters.FeatureFilterAsync;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.models.Conditions;
import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.Feature;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FilterNotFoundException;

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

    private static final Duration DEFAULT_BLOCK_TIMEOUT = Duration.ofSeconds(100);

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
        return checkFeature(feature, null).map(event -> event.isEnabled());
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
        return checkFeature(feature, null).map(event -> event.isEnabled()).block(DEFAULT_BLOCK_TIMEOUT);
    }

    /**
     * Checks to see if the feature is enabled. If enabled it check each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     *
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return state of the feature
     * @throws FilterNotFoundException file not found
     */
    public Mono<Boolean> isEnabledAsync(String feature, Object featureContext) {
        return checkFeature(feature, featureContext).map(event -> event.isEnabled());
    }

    /**
     * Checks to see if the feature is enabled. If enabled it checks each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     *
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return state of the feature
     * @throws FilterNotFoundException file not found
     */
    public Boolean isEnabled(String feature, Object featureContext) throws FilterNotFoundException {
        return checkFeature(feature, featureContext).map(event -> event.isEnabled()).block(DEFAULT_BLOCK_TIMEOUT);
    }

    private Mono<EvaluationEvent> checkFeature(String featureName, Object featureContext)
        throws FilterNotFoundException {
        Feature featureFlag = featureManagementConfigurations.getFeatureFlags().stream()
            .filter(feature -> feature.getId().equals(featureName)).findAny().orElse(null);

        EvaluationEvent event = new EvaluationEvent(featureFlag);

        if (featureFlag == null) {
            LOGGER.warn("Feature flag %s not found", featureName);
            return Mono.just(event);
        }

        if (!featureFlag.isEnabled()) {
            // If a feature flag is disabled and override can't enable it
            return Mono.just(event.setEnabled(false));
        }

        Mono<EvaluationEvent> result = this.checkFeatureFilters(event, featureContext);

        return result;
    }

    private Mono<EvaluationEvent> checkFeatureFilters(EvaluationEvent event, Object featureContext) {
        Feature featureFlag = event.getFeature();
        Conditions conditions = featureFlag.getConditions();
        List<FeatureFilterEvaluationContext> featureFilters = conditions.getClientFilters();

        if (featureFilters.size() == 0) {
            return Mono.just(event.setEnabled(true));
        } else {
            event.setEnabled(conditions.getRequirementType().equals(ALL_REQUIREMENT_TYPE));
        }

        List<Mono<Boolean>> filterResults = new ArrayList<Mono<Boolean>>();
        for (FeatureFilterEvaluationContext featureFilter : featureFilters) {
            String filterName = featureFilter.getName();

            try {
                Object filter = context.getBean(filterName);
                featureFilter.setFeatureName(featureFlag.getId());
                if (filter instanceof FeatureFilter) {
                    filterResults.add(Mono.just(((FeatureFilter) filter).evaluate(featureFilter)));
                } else if (filter instanceof ContextualFeatureFilter) {
                    filterResults
                        .add(Mono.just(((ContextualFeatureFilter) filter).evaluate(featureFilter, featureContext)));
                } else if (filter instanceof FeatureFilterAsync) {
                    filterResults.add(((FeatureFilterAsync) filter).evaluateAsync(featureFilter));
                } else if (filter instanceof ContextualFeatureFilterAsync) {
                    filterResults
                        .add(((ContextualFeatureFilterAsync) filter).evaluateAsync(featureFilter, featureContext));
                }
            } catch (NoSuchBeanDefinitionException e) {
                LOGGER.error("Was unable to find Filter {}. Does the class exist and set as an @Component?",
                    filterName);
                if (properties.isFailFast()) {
                    String message = "Fail fast is set and a Filter was unable to be found";
                    ReflectionUtils.rethrowRuntimeException(new FilterNotFoundException(message, e, featureFilter));
                }
            }
        }

        if (ALL_REQUIREMENT_TYPE.equals(featureFlag.getConditions().getRequirementType())) {
            return Flux.merge(filterResults).reduce((a, b) -> {
                return a && b;
            }).single().map(result -> {
                return event.setEnabled(result);
            });
        }
        // Any Filter must be true
        return Flux.merge(filterResults).reduce((a, b) -> a || b).single().map(result -> event.setEnabled(result));
    }

    /**
     * Returns the names of all features flags
     *
     * @return a set of all feature names
     */
    public Set<String> getAllFeatureNames() {
        return new HashSet<String>(
            featureManagementConfigurations.getFeatureFlags().stream().map(feature -> feature.getId()).toList());
    }
}
