// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.manager.entities.Feature;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.DynamicFeature;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureDefinition;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.FeatureVariant;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.IFeatureVariantAssigner;
import com.azure.spring.cloud.feature.manager.entities.featurevariants.IFeatureVariantAssignerMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled.
 */
@Component("FeatureManagement")
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManager extends HashMap<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private static final long serialVersionUID = -5941681857165566018L;

    private static final String FEATURE_FLAGS_PREFIX = "feature-flags";

    private static final String DYNAMIC_FEATURE_PREFIX = "dynamic-features";

    @Autowired
    private transient ApplicationContext context;

    @Autowired
    private FeatureVariantProperties variantProperties;

    private transient FeatureManagementConfigProperties properties;

    private transient Map<String, Feature> featureManagement;

    private Map<String, Boolean> onOff;

    private HashMap<String, Object> featureFlags;

    private transient Map<String, DynamicFeature> dynamicFeatures;

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

    public FeatureManager(FeatureManagementConfigProperties properties) {
        this.properties = properties;
        featureManagement = new HashMap<>();
        dynamicFeatures = new HashMap<>();
        onOff = new HashMap<>();
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
    public Mono<Boolean> isEnabledAsync(String feature) throws FilterNotFoundException {
        return Mono.just(checkFeatures(feature));
    }

    public <T> Mono<T> getVariantAsync(String feature, Class<T> returnClass) throws FilterNotFoundException {
        return Mono.just(generateVariant(feature, returnClass));
    }

    private boolean checkFeatures(String feature) throws FilterNotFoundException {
        if (featureManagement == null || onOff == null) {
            return false;
        }

        Boolean boolFeature = onOff.get(feature);

        // This is a workaround to make sure the new Feature Management library works with the old config provider. The
        // new config provider doesn't prepend feature flags with `featureManagement.` any more so it isn't removed.
        if (boolFeature == null && onOff.get("featureManagement." + feature) != null) {
            boolFeature = onOff.get("featureManagement." + feature);
        }

        if (boolFeature != null) {
            return boolFeature;
        }

        Feature featureItem = featureManagement.get(feature);
        if (featureItem == null) {
            return false;
        }

        return featureItem.getEnabledFor().values().stream().filter(Objects::nonNull)
            .filter(featureFilter -> featureFilter.getName() != null)
            .map(featureFilter -> isFeatureOn(featureFilter, feature)).findAny().orElse(false);
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

    @SuppressWarnings("unchecked")
    private <T> T generateVariant(String featureName, Class<T> type) {

        if (!StringUtils.hasText(featureName)) {
            throw new IllegalArgumentException("Feature Variant is empty or null.");
        }

        FeatureVariant variant = null;

        DynamicFeature dynamicFeature = dynamicFeatures.get(featureName);

        if (dynamicFeature == null) {
            throw new FeatureManagementException(
                "The feature variant " + featureName + " has no defined Dynnamic Feature.");
        }

        FeatureDefinition featureDefinition = new FeatureDefinition(featureName, dynamicFeature);

        FeatureVariant defaultVariant = validateDynamicFeature(featureDefinition, featureName);

        IFeatureVariantAssignerMetadata assigner = null;

        try {
            assigner = (IFeatureVariantAssignerMetadata) context.getBean(featureDefinition.getAssigner());
        } catch (NoSuchBeanDefinitionException e) {
            throw new FeatureManagementException("The feature variant assigner " + featureDefinition.getAssigner()
                + " specified for feature " + featureName + " was not found.");
        }

        if (assigner instanceof IFeatureVariantAssigner) {
            variant = ((IFeatureVariantAssigner) assigner).assignVariantAsync(featureDefinition).block();
        }

        if (variant == null) {
            variant = defaultVariant;

        }

        String reference = variant.getConfigurationReference();

        String[] parts = reference.split(":");

        Object configurations = null;

        for (String part : parts) {
            if (configurations == null) {
                configurations = variantProperties.get(part);
            } else if (configurations instanceof HashMap) {
                configurations = ((HashMap<String, Object>) configurations).get(part);
            }
        }

        return MAPPER.convertValue(configurations, type);
    }

    private FeatureVariant validateDynamicFeature(FeatureDefinition featureDefinition, String featureName) {
        if (!StringUtils.hasText(featureDefinition.getAssigner())) {
            throw new FeatureManagementException(
                "Missing Feature Variant assigner name for the feature " + featureName);
        }

        if (featureDefinition.getVariants() == null || featureDefinition.getVariants().size() == 0) {
            throw new FeatureManagementException(
                "No Variants are registered for the feature " + featureName);
        }

        FeatureVariant defaultVariant = null;

        for (FeatureVariant v : featureDefinition.getVariants()) {
            if (v.getDefault()) {
                if (defaultVariant != null) {
                    throw new FeatureManagementException(
                        "Multiple default variants are registered for the feature " + featureName);
                }
                defaultVariant = v;
            }

            if (!StringUtils.hasText(v.getConfigurationReference())) {
                throw new FeatureManagementException("The variant " + v.getName() + " for the feature " + featureName
                    + " does not have a configuration reference.");
            }
        }

        if (defaultVariant == null) {
            throw new FeatureManagementException("A default variant cannot be found for the feature " + featureName);
        }
        return defaultVariant;
    }

    @SuppressWarnings("unchecked")
    private void addToFeatures(Map<? extends String, ? extends Object> features, String key, String combined,
        Boolean ignoreNonPrefixed) {
        if (ignoreNonPrefixed && !((combined + key).startsWith(FEATURE_FLAGS_PREFIX)
            || (combined + key).startsWith(DYNAMIC_FEATURE_PREFIX))) {
            return;
        }
        Object featureKey = features.get(key);
        if (!combined.isEmpty() && !combined.endsWith(".")) {
            combined += ".";
        }
        if (featureKey instanceof Boolean) {
            key = removeFeaturePrefix(combined + key);
            onOff.put(key, (Boolean) featureKey);
        } else {
            Feature feature = null;
            DynamicFeature dynamicFeature = null;
            try {
                feature = MAPPER.convertValue(featureKey, Feature.class);
                dynamicFeature = MAPPER.convertValue(featureKey, DynamicFeature.class);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Found invalid feature {} with value {}.", combined + key, featureKey.toString());
            }
            // When coming from a file "feature.flag" is not a possible flag name
            if (dynamicFeature != null && StringUtils.hasText(dynamicFeature.getAssigner())
                && dynamicFeature.getVariants().size() > 0) {
                key = removeFeaturePrefix(combined + key);
                dynamicFeatures.put(key, dynamicFeature);
            } else if (feature != null && feature.getEnabledFor() == null && feature.getKey() == null) {
                if (LinkedHashMap.class.isAssignableFrom(featureKey.getClass())) {
                    features = (LinkedHashMap<String, Object>) featureKey;
                    for (String fKey : features.keySet()) {
                        addToFeatures(features, fKey, combined + key, ignoreNonPrefixed);
                    }
                }
            } else {
                if (feature != null) {
                    key = removeFeaturePrefix(key);
                    feature.setKey(key);
                    featureManagement.put(key, feature);
                }
            }
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m == null) {
            return;
        }

        // Need to reset or switch between on/off to conditional doesn't work
        featureManagement = new HashMap<>();
        onOff = new HashMap<>();
        dynamicFeatures = new HashMap<>();

        boolean ignoreNonPrefexed = false;

        for (String key : m.keySet()) {
            if (key.startsWith(FEATURE_FLAGS_PREFIX)) {
                ignoreNonPrefexed = true;
                break;
            }
        }

        for (String key : m.keySet()) {
            addToFeatures(m, key, "", ignoreNonPrefexed);
        }
    }

    /**
     * Returns the names of all features flags
     *
     * @return a set of all feature names
     */
    public Set<String> getAllFeatureNames() {
        Set<String> allFeatures = new HashSet<>();

        allFeatures.addAll(onOff.keySet());
        allFeatures.addAll(featureManagement.keySet());
        return allFeatures;
    }

    /**
     * @return the featureManagement
     */
    Map<String, Feature> getFeatureManagement() {
        return featureManagement;
    }

    /**
     * @return the featureFlags
     */
    public HashMap<String, Object> getFeatureFlags() {
        return featureFlags;
    }

    /**
     * @param featureFlags the featureFlags to set
     */
    public void setFeatureFlags(HashMap<String, Object> featureFlags) {
        this.featureFlags = featureFlags;
    }

    /**
     * @return the onOff
     */
    Map<String, Boolean> getOnOff() {
        return onOff;
    }

    /**
     * A prefix is added before feature flags and dynamic features to distinguish between the two. Returns the original
     * name if it doesn't have the prefix for backwards compatibility.
     * 
     * @param featureName full name of the feature.
     * @return feature name without a prefix if it had one.
     */
    private String removeFeaturePrefix(String featureName) {
        if (featureName.startsWith(FEATURE_FLAGS_PREFIX)) {
            return featureName.substring(FEATURE_FLAGS_PREFIX.length() + 1);
        } else if (featureName.startsWith(DYNAMIC_FEATURE_PREFIX)) {
            return featureName.substring(DYNAMIC_FEATURE_PREFIX.length() + 1);
        }
        return featureName;
    }

}
