// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.azure.spring.cloud.feature.management.models.Variant;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled. Returns the same
 * value in the same request.
 */
public class FeatureManagerSnapshot {

    private final FeatureManager featureManager;

    private final Map<String, Boolean> requestMap;

    private final Map<String, Variant> variantMap;

    private static final Duration DEFAULT_BLOCK_TIMEOUT = Duration.ofSeconds(100);

    /**
     * Used to evaluate whether a feature is enabled or disabled. When setup with the <code>@RequestScope</code> it will
     * return the same value for all checks of the given feature flag.
     * @param featureManager FeatureManager
     */
    FeatureManagerSnapshot(FeatureManager featureManager) {
        this.featureManager = featureManager;
        this.requestMap = new HashMap<>();
        this.variantMap = new HashMap<>();
    }

    /**
     * Checks to see if the feature is enabled. If enabled it checks each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     * <p>
     * If isEnabled has already been called on this feature in this request, it will return the same value as it did
     * before.
     *
     * @param feature Feature being checked.
     * @return state of the feature
     */
    public Mono<Boolean> isEnabledAsync(String feature) {
        return isEnabledAsync(feature, null);
    }

    /**
     * Checks to see if the feature is enabled. If enabled it checks each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     * <p>
     * If isEnabled has already been called on this feature in this request, it will return the same value as it did
     * before.
     *
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return state of the feature
     */
    public Mono<Boolean> isEnabledAsync(String feature, Object featureContext) {
        Boolean featureValue = requestMap.get(feature);
        if (featureValue != null) {
            return Mono.just(featureValue);
        }

        return featureManager.isEnabledAsync(feature, featureContext)
            .doOnSuccess((enabled) -> requestMap.put(feature, enabled));
    }

    /**
     * Checks to see if the feature is enabled. If enabled it checks each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     * <p>
     * If isEnabled has already been called on this feature in this request, it will return the same value as it did
     * before.
     *
     * @param feature Feature being checked.
     * @return state of the feature
     */
    public Boolean isEnabled(String feature) {
        return isEnabled(feature, null);
    }

    /**
     * Checks to see if the feature is enabled. If enabled it checks each filter, once a single filter returns true it
     * returns true. If no filter returns true, it returns false. If there are no filters, it returns true. If feature
     * isn't found it returns false.
     * <p>
     * If isEnabled has already been called on this feature in this request, it will return the same value as it did
     * before.
     *
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return state of the feature
     */
    public Boolean isEnabled(String feature, Object featureContext) {
        return isEnabledAsync(feature, featureContext).block(DEFAULT_BLOCK_TIMEOUT);
    }

    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariantAsync has already been called on this feature in this request, it will return the same value as it
     * did before.
     *
     * @param feature Feature being checked.
     * @return state of the feature
     */
    public Mono<Variant> getVariantAsync(String feature) {
        return getVariantAsync(feature, null);
    }

    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariantAsync has already been called on this feature in this request, it will return the same value as it
     * did before.
     *
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return state of the feature
     */
    public Mono<Variant> getVariantAsync(String feature, Object featureContext) {
        Variant featureVariant = variantMap.get(feature);
        if (featureVariant != null) {
            return Mono.just(featureVariant);
        }

        return featureManager.getVariantAsync(feature, featureContext)
            .doOnSuccess((variant) -> variantMap.put(feature, variant));
    }

    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariant has already been called on this feature in this request, it will return the same value as it did
     * before.
     *
     * @param feature Feature being checked.
     * @return state of the feature
     */
    public Variant getVariant(String feature) {
        return getVariant(feature, null);
    }

    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariant has already been called on this feature in this request, it will return the same value as it did
     * before.
     *
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return state of the feature
     */
    public Variant getVariant(String feature, Object featureContext) {
        return getVariantAsync(feature, featureContext).block(DEFAULT_BLOCK_TIMEOUT);
    }
}
