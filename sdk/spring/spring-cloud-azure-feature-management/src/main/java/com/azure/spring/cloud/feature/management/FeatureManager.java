// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static com.azure.spring.cloud.feature.management.implementation.FeatureManagementConstants.ALL_REQUIREMENT_TYPE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilter;
import com.azure.spring.cloud.feature.management.filters.ContextualFeatureFilterAsync;
import com.azure.spring.cloud.feature.management.filters.FeatureFilter;
import com.azure.spring.cloud.feature.management.filters.FeatureFilterAsync;
import com.azure.spring.cloud.feature.management.implementation.FeatureFilterUtils;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.models.Allocation;
import com.azure.spring.cloud.feature.management.models.Conditions;
import com.azure.spring.cloud.feature.management.models.EvaluationEvent;
import com.azure.spring.cloud.feature.management.models.FeatureDefinition;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.FilterNotFoundException;
import com.azure.spring.cloud.feature.management.models.GroupAllocation;
import com.azure.spring.cloud.feature.management.models.PercentileAllocation;
import com.azure.spring.cloud.feature.management.models.UserAllocation;
import com.azure.spring.cloud.feature.management.models.Variant;
import com.azure.spring.cloud.feature.management.models.VariantAssignmentReason;
import com.azure.spring.cloud.feature.management.models.VariantReference;
import com.azure.spring.cloud.feature.management.targeting.TargetingContext;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;
import com.azure.spring.cloud.feature.management.telemetry.TelemetryPublisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Used to evaluate the enabled state of a feature and/or get the assigned variant of a feature, if any.
 */
public class FeatureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private static final double PERCENTILE_MAXIMUM = 100.0;

    private transient ApplicationContext context;

    private final FeatureManagementProperties featureManagementConfigurations;

    private transient FeatureManagementConfigProperties properties;

    private static final Duration DEFAULT_BLOCK_TIMEOUT = Duration.ofSeconds(100);

    private final TargetingContextAccessor contextAccessor;

    private final TargetingEvaluationOptions evaluationOptions;

    private final TelemetryPublisher telemetryPublisher;

    /**
     * Used to evaluate the enabled state of a feature and/or get the assigned variant of a feature, if any.
     *
     * @param context ApplicationContext
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     * @param properties FeatureManagementConfigProperties
     * @param contextAccessor TargetingContextAccessor
     * @param evaluationOptions TargetingEvaluationOptions
     * @param telemetryPublisher TelemetryPublisher
     */
    FeatureManager(ApplicationContext context, FeatureManagementProperties featureManagementConfigurations,
        FeatureManagementConfigProperties properties, TargetingContextAccessor contextAccessor,
        TargetingEvaluationOptions evaluationOptions, TelemetryPublisher telemetryPublisher) {
        this.context = context;
        this.featureManagementConfigurations = featureManagementConfigurations;
        this.properties = properties;
        this.contextAccessor = contextAccessor;
        this.evaluationOptions = evaluationOptions;
        this.telemetryPublisher = telemetryPublisher;
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
     * Checks to see if the feature is enabled. If enabled it check each filter, once a single filter returns true it
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

    /**
     * Returns the variant assigned to the current context.
     * 
     * @param feature Feature being checked.
     * @return Assigned Variant
     */
    public Variant getVariant(String feature) {
        return checkFeature(feature, null).block().getVariant();
    }

    /**
     * Returns the variant assigned to the current context.
     * 
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return Assigned Variant
     */
    public Variant getVariant(String feature, Object featureContext) {
        return checkFeature(feature, featureContext).block().getVariant();
    }

    /**
     * Returns the variant assigned to the current context.
     * 
     * @param feature Feature being checked.
     * @return Assigned Variant
     */
    public Mono<Variant> getVariantAsync(String feature) {
        return checkFeature(feature, null).map(event -> event.getVariant());
    }

    /**
     * Returns the variant assigned to the current context.
     * 
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return Assigned Variant
     */
    public Mono<Variant> getVariantAsync(String feature, Object featureContext) {
        return checkFeature(feature, featureContext).map(event -> event.getVariant());
    }

    private Mono<EvaluationEvent> checkFeature(String featureName, Object featureContext)
        throws FilterNotFoundException {
        List<FeatureDefinition> featureFlags = featureManagementConfigurations.getFeatureFlags();

        if (featureFlags == null) {
            return Mono.just(new EvaluationEvent(null));
        }

        FeatureDefinition featureFlag = featureFlags.stream()
            .filter(feature -> feature.getId().equals(featureName)).findAny().orElse(null);

        EvaluationEvent event = new EvaluationEvent(featureFlag);

        if (featureFlag == null) {
            LOGGER.warn("Feature flag {} not found", featureName);
            return Mono.just(event);
        }

        if (!featureFlag.isEnabled()) {
            this.assignDefaultDisabledReason(event);
            event.setEnabled(false);
            publishTelemetryIfEnabled(event);

            // If a feature flag is disabled and override can't enable it
            return Mono.just(event);
        }

        Mono<EvaluationEvent> result = this.checkFeatureFilters(event, featureContext);
        result = assignAllocation(result);
        result = result.doOnSuccess(resultEvent -> publishTelemetryIfEnabled(resultEvent));
        return result;
    }

    /**
     * Publishes telemetry if enabled for the feature.
     * 
     * @param event Evaluation event
     */
    private void publishTelemetryIfEnabled(EvaluationEvent event) {
        if (telemetryPublisher != null && event.getFeature() != null
            && event.getFeature().getTelemetry() != null
            && event.getFeature().getTelemetry().isEnabled()) {
            telemetryPublisher.publish(event);
        }
    }

    private Mono<EvaluationEvent> assignAllocation(Mono<EvaluationEvent> monoEvent) {
        return monoEvent.map(event -> {
            FeatureDefinition featureFlag = event.getFeature();

            if (featureFlag.getVariants() == null || featureFlag.getAllocation() == null) {
                return event;
            }

            if (!event.isEnabled()) {
                this.assignDefaultDisabledReason(event);
                return event;
            }
            this.assignVariant(event);
            return event;
        });
    }

    private void assignDefaultDisabledReason(EvaluationEvent event) {
        FeatureDefinition featureFlag = event.getFeature();
        event.setReason(VariantAssignmentReason.DEFAULT_WHEN_DISABLED);
        if (event.getFeature().getAllocation() == null) {
            return;
        }
        this.assignVariantOverride(event.getFeature().getVariants(),
            event.getFeature().getAllocation().getDefaultWhenDisabled(), false, event);

        if (featureFlag.getAllocation() != null) {
            String variantName = featureFlag.getAllocation().getDefaultWhenDisabled();
            event.setVariant(this.variantNameToVariant(featureFlag, variantName));
        }
    }

    private void assignDefaultEnabledVariant(EvaluationEvent event) {
        event.setReason(VariantAssignmentReason.DEFAULT_WHEN_ENABLED);
        if (event.getFeature().getAllocation() == null) {
            return;
        }
        this.assignVariantOverride(event.getFeature().getVariants(),
            event.getFeature().getAllocation().getDefaultWhenEnabled(), true, event);
        FeatureDefinition featureFlag = event.getFeature();

        if (featureFlag.getAllocation() != null) {
            event.setVariant(
                this.variantNameToVariant(featureFlag, featureFlag.getAllocation().getDefaultWhenEnabled()));
            return;
        }
    }

    private void assignVariant(EvaluationEvent event) {
        FeatureDefinition featureFlag = event.getFeature();
        if (featureFlag.getVariants().size() == 0 || featureFlag.getAllocation() == null) {
            return;
        }

        Allocation allocation = featureFlag.getAllocation();

        TargetingContext targetingContext = buildContext();

        List<String> groups = targetingContext.getGroups();
        String variantName = null;

        if (StringUtils.hasText(targetingContext.getUserId()) && allocation.getUser() != null) {
            // Loop through all user allocations
            for (UserAllocation userAllocation : allocation.getUser()) {
                if (!evaluationOptions.isIgnoreCase()
                    && userAllocation.getUsers().contains(targetingContext.getUserId())) {
                    event.setReason(VariantAssignmentReason.USER);
                    variantName = userAllocation.getVariant();
                    break;
                } else if (evaluationOptions.isIgnoreCase()
                    && userAllocation.getUsers().stream().anyMatch(targetingContext.getUserId()::equalsIgnoreCase)) {
                    event.setReason(VariantAssignmentReason.USER);
                    variantName = userAllocation.getVariant();
                    break;
                }
            }
        }
        if (variantName == null && allocation.getGroup() != null) {
            for (GroupAllocation groupAllocation : allocation.getGroup()) {
                for (String allocationGroup : groupAllocation.getGroups()) {
                    if (!evaluationOptions.isIgnoreCase() && groups.contains(allocationGroup)) {
                        event.setReason(VariantAssignmentReason.GROUP);
                        variantName = groupAllocation.getVariant();
                        break;
                    } else if (evaluationOptions.isIgnoreCase()
                        && groups.stream().anyMatch(allocationGroup::equalsIgnoreCase)) {
                        event.setReason(VariantAssignmentReason.GROUP);
                        variantName = groupAllocation.getVariant();
                        break;
                    }
                }
                if (variantName != null) {
                    break;
                }
            }
        }

        if (variantName == null) {
            String seed = allocation.getSeed();
            if (!StringUtils.hasText(seed)) {
                seed = "allocation\n" + featureFlag.getId();
            }
            String contextId = targetingContext.getUserId() + "\n" + seed;
            double box = FeatureFilterUtils.isTargetedPercentage(contextId);
            for (PercentileAllocation percentileAllocation : allocation.getPercentile()) {
                Double to = percentileAllocation.getTo();
                if ((box == PERCENTILE_MAXIMUM && to == PERCENTILE_MAXIMUM)
                    || (percentileAllocation.getFrom() <= box && box < to)) {
                    event.setReason(VariantAssignmentReason.PERCENTILE);
                    variantName = percentileAllocation.getVariant();
                    break;
                }
            }
        }

        if (variantName == null) {
            this.assignDefaultEnabledVariant(event);
            return;
        }

        event.setVariant(variantNameToVariant(featureFlag, variantName));
        assignVariantOverride(featureFlag.getVariants(), variantName, true, event);
    }

    private void assignVariantOverride(List<VariantReference> variants, String defaultVariantName, boolean status,
        EvaluationEvent event) {
        if (variants.size() == 0 || !StringUtils.hasText(defaultVariantName)) {
            return;
        }
        for (VariantReference variant : variants) {
            if (variant.getName().equals(defaultVariantName)) {
                if ("Enabled".equals(variant.getStatusOverride())) {
                    event.setEnabled(true);
                    return;
                }
                if ("Disabled".equals(variant.getStatusOverride())) {
                    event.setEnabled(false);
                    return;
                }
            }
        }
        event.setEnabled(status);
    }

    private Mono<EvaluationEvent> checkFeatureFilters(EvaluationEvent event, Object featureContext) {
        FeatureDefinition featureFlag = event.getFeature();
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
            featureFilter.setFeatureName(event.getFeature().getId());

            filterResults.add(evaluateFilter(featureFilter, featureContext, filterName));
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

    private Variant variantNameToVariant(FeatureDefinition featureFlag, String variantName) {
        for (VariantReference variant : featureFlag.getVariants()) {
            if (variant.getName().equals(variantName)) {
                return new Variant(variantName, variant.getConfigurationValue());
            }
        }
        return null;
    }

    private TargetingFilterContext buildContext() {
        TargetingFilterContext targetingContext = new TargetingFilterContext();
        if (contextAccessor != null) {
            // If this is the only one provided just use it.
            contextAccessor.configureTargetingContext(targetingContext);
            return targetingContext;
        }
        throw new FeatureManagementException("No Targeting Filter Context found to assign variant.");
    }

    /**
     * Returns the names of all features flags
     *
     * @return a set of all feature names
     */
    public Set<String> getAllFeatureNames() {
        if (featureManagementConfigurations == null || featureManagementConfigurations.getFeatureFlags() == null) {
            return Collections.emptySet();
        }
        return new HashSet<String>(
            featureManagementConfigurations.getFeatureFlags().stream().map(feature -> feature.getId()).toList());
    }

    /**
     * Enhanced filter evaluation with better error handling and logging.
     * 
     * @param featureFilter Feature filter evaluation context
     * @param featureContext Feature context
     * @param filterName Name of the filter for logging
     * @return {@code Mono<Boolean>} result of filter evaluation
     */
    private Mono<Boolean> evaluateFilter(FeatureFilterEvaluationContext featureFilter, Object featureContext,
        String filterName) {
        try {
            Object filter = context.getBean(filterName);
            if (filter instanceof FeatureFilter) {
                return Mono.just(((FeatureFilter) filter).evaluate(featureFilter));
            } else if (filter instanceof ContextualFeatureFilter) {
                return Mono.just(((ContextualFeatureFilter) filter).evaluate(featureFilter, featureContext));
            } else if (filter instanceof FeatureFilterAsync) {
                return ((FeatureFilterAsync) filter).evaluateAsync(featureFilter);
            } else if (filter instanceof ContextualFeatureFilterAsync) {
                return ((ContextualFeatureFilterAsync) filter).evaluateAsync(featureFilter, featureContext);
            } else {
                LOGGER.warn("Filter {} does not implement any known filter interface", filterName);
                return Mono.just(false);
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.error("Was unable to find Filter {}. Does the class exist and set as an @Component?", filterName, e);
            if (properties.isFailFast()) {
                String message = "Fail fast is set and a Filter was unable to be found";
                return Mono.error(new FilterNotFoundException(message, e, featureFilter));
            }
            return Mono.just(false);
        }
    }

}
