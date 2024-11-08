// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static com.azure.spring.cloud.feature.management.implementation.FeatureManagementConstants.ALL_REQUIREMENT_TYPE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
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
import com.azure.spring.cloud.feature.management.models.Feature;
import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.management.models.FeatureManagementException;
import com.azure.spring.cloud.feature.management.models.FilterNotFoundException;
import com.azure.spring.cloud.feature.management.models.GroupAllocation;
import com.azure.spring.cloud.feature.management.models.PercentileAllocation;
import com.azure.spring.cloud.feature.management.models.UserAllocation;
import com.azure.spring.cloud.feature.management.models.Variant;
import com.azure.spring.cloud.feature.management.models.VariantAssignmentReason;
import com.azure.spring.cloud.feature.management.models.VariantReference;
import com.azure.spring.cloud.feature.management.targeting.ContextualTargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingContext;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;
import com.azure.spring.cloud.feature.management.targeting.TargetingFilterContext;

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

    private final ContextualTargetingContextAccessor contextualAccessor;

    private final TargetingEvaluationOptions evaluationOptions;

    /**
     * Can be called to check if a feature is enabled or disabled.
     *
     * @param context ApplicationContext
     * @param featureManagementConfigurations Configuration Properties for Feature Flags
     * @param properties FeatureManagementConfigProperties
     */
    FeatureManager(ApplicationContext context, FeatureManagementProperties featureManagementConfigurations,
        FeatureManagementConfigProperties properties, TargetingContextAccessor contextAccessor,
        ContextualTargetingContextAccessor contextualAccessor, TargetingEvaluationOptions evaluationOptions) {
        this.context = context;
        this.featureManagementConfigurations = featureManagementConfigurations;
        this.properties = properties;
        this.contextAccessor = contextAccessor;
        this.contextualAccessor = contextualAccessor;
        this.evaluationOptions = evaluationOptions;
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
        return checkFeature(feature, null).map(event -> event.isEnabled()).block();
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
        return checkFeature(feature, featureContext).map(event -> event.isEnabled()).block();
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
        Feature featureFlag = featureManagementConfigurations.getFeatureFlags().stream()
            .filter(feature -> feature.getId().equals(featureName)).findAny().orElse(null);

        EvaluationEvent event = new EvaluationEvent(featureFlag);

        if (featureFlag == null) {
            LOGGER.warn("Feature flag %s not found", featureName);
            return Mono.just(event);
        }

        if (!featureFlag.isEnabled()) {
            this.assignDefaultDisabledVariant(event);
            if (featureFlag.getAllocation() != null) {
                String variantName = featureFlag.getAllocation().getDefaultWhenDisabled();
                event.setVariant(this.variantNameToVariant(featureFlag, variantName));
            }

            // If a feature flag is disabled and override can't enable it
            return Mono.just(event.setEnabled(false));
        }

        Mono<EvaluationEvent> result = this.checkFeatureFilters(event, featureContext);

        result = assignAllocation(result, context);
        return result;
    }

    private Mono<EvaluationEvent> assignAllocation(Mono<EvaluationEvent> monoEvent, Object featureContext) {

        return monoEvent.map(event -> {

            Feature featureFlag = event.getFeature();

            if (featureFlag.getVariants() == null || featureFlag.getAllocation() == null) {
                return event;
            }

            if (!event.isEnabled()) {
                this.assignDefaultDisabledVariant(event);
                event.setVariant(
                    this.variantNameToVariant(featureFlag, featureFlag.getAllocation().getDefaultWhenDisabled()));
                return event;
            }
            this.assignVariant(event, featureContext);
            return event;
        });
    }

    private void assignDefaultDisabledVariant(EvaluationEvent event) {
        event.setReason(VariantAssignmentReason.DEFAULT_WHEN_DISABLED);
        if (event.getFeature().getAllocation() == null) {
            return;
        }
        this.assignVariantOverride(event.getFeature().getVariants(),
            event.getFeature().getAllocation().getDefaultWhenDisabled(), false, event);
    }

    private void assignDefaultEnabledVariant(EvaluationEvent event) {
        event.setReason(VariantAssignmentReason.DEFAULT_WHEN_ENABLED);
        if (event.getFeature().getAllocation() == null) {
            return;
        }
        this.assignVariantOverride(event.getFeature().getVariants(),
            event.getFeature().getAllocation().getDefaultWhenEnabled(), true, event);
    }

    private void assignVariant(EvaluationEvent event, Object context) {
        Feature featureFlag = event.getFeature();
        if (featureFlag.getVariants().size() == 0 || featureFlag.getAllocation() == null) {
            return;
        }

        Allocation allocation = featureFlag.getAllocation();

        TargetingContext targetingContext = buildContext(context);

        List<String> groups = targetingContext.getGroups();
        String variantName = null;

        if (StringUtils.hasText(targetingContext.getUserId())) {
            // Loop through all user allocations
            for (UserAllocation userAllocation : allocation.getUsers()) {
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
        if (variantName != null) {
            for (GroupAllocation groupAllocation : allocation.getGroups()) {
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

        if (variantName != null) {
            String seed = allocation.getSeed();
            if (!StringUtils.hasText(seed)) {
                seed = "allocation\n" + featureFlag.getId();
            }
            String contextId = targetingContext.getUserId() + "\n" + featureFlag.getId();
            double box = FeatureFilterUtils.isTargetedPercentage(contextId);
            for (PercentileAllocation percentileAllocation : allocation.getPercentile()) {
                Double to = percentileAllocation.getTo();
                if ((box == 100 && to == 100) || (percentileAllocation.getFrom() <= box && box < to)) {
                    event.setReason(VariantAssignmentReason.PERCENTILE);
                    variantName = percentileAllocation.getVariant();
                }
            }
        }

        if (variantName == null) {
            this.assignDefaultEnabledVariant(event);
            if (featureFlag.getAllocation() != null) {
                event.setVariant(this.variantNameToVariant(featureFlag, allocation.getDefaultWhenEnabled()));
                return;
            }
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
                if (variant.getStatusOverride().equals("Enabled")) {
                    event.setEnabled(true);
                    return;
                }
                if (variant.getStatusOverride().equals("Disabled")) {
                    event.setEnabled(false);
                    return;
                }
            }
        }
        event.setEnabled(status);
    }

    private Mono<EvaluationEvent> checkFeatureFilters(EvaluationEvent event, Object featureContext) {
        Feature featureFlag = event.getFeature();
        Conditions conditions = featureFlag.getConditions();
        List<FeatureFilterEvaluationContext> featureFilters = conditions.getClientFilters();

        if (featureFilters.size() == 0) {
            return Mono.just(event.setEnabled(true));
        } else {
            event.setEnabled(conditions.getRequirementType().equals("All"));
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

    private Variant variantNameToVariant(Feature featureFlag, String variantName) {
        for (VariantReference variant : featureFlag.getVariants()) {
            if (variant.getName().equals(variantName)) {
                return new Variant(variantName, variant.getConfigurationValue());
            }
        }
        return null;
    }

    private TargetingFilterContext buildContext(Object appContext) {
        TargetingFilterContext targetingContext = new TargetingFilterContext();
        if (contextualAccessor != null && (appContext != null || contextAccessor == null)) {
            // Use this if, there is an appContext + the contextualAccessor, or there is no
            // contextAccessor.
            contextualAccessor.configureTargetingContext(targetingContext, appContext);
            return targetingContext;
        }
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
        return new HashSet<String>(
            featureManagementConfigurations.getFeatureFlags().stream().map(feature -> feature.getId()).toList());
    }

    /**
     * @return the featureManagement
     */
    List<Feature> getFeatureManagement() {
        return featureManagementConfigurations.getFeatureFlags();
    }

}
