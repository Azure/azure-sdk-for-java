// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import java.util.HashMap;

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.azure.spring.cloud.feature.management.models.Variant;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled. Returns the same
 * value in the same request.
 */
public class FeatureManagerSnapshot {

    private final FeatureManager featureManager;

    private final HashMap<String, Boolean> requestMap;

    private final HashMap<String, Variant> variantMap;

    /**
     * Used to evaluate whether a feature is enabled or disabled. When setup with the <code>@RequestScope</code> it will
     * return the same value for all checks of the given feature flag.
     * @param featureManager FeatureManager
     */
    public FeatureManagerSnapshot(FeatureManager featureManager) {
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
        Boolean featureValue = requestMap.get(feature);
        if (featureValue != null) {
            return Mono.just(featureValue);
        }

        return featureManager.isEnabledAsync(feature).doOnSuccess((enabled) -> requestMap.put(feature, enabled));
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
        Boolean featureValue = requestMap.get(feature);
        if (featureValue != null) {
            return featureValue;
        }

        return featureManager.isEnabledAsync(feature).doOnSuccess((enabled) -> requestMap.put(feature, enabled))
            .block();
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
     * @param featureContext Local context
     * @return state of the feature
     */
    public Boolean isEnabled(String feature, Object featureContext) {
        Boolean featureValue = requestMap.get(feature);
        if (featureValue != null) {
            return featureValue;
        }

        return featureManager.isEnabledAsync(feature, featureContext)
            .doOnSuccess((enabled) -> requestMap.put(feature, enabled))
            .block();
    }

    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariantAsync has been already returned a result for this feature, it will return the same value as it did
     * before.
     * 
     * @param feature Feature being checked.
     * @return Assigned Variant
     */
    public Mono<Variant> getVariantAsync(String feature) {
        Variant variant = variantMap.get(feature);
        if (variant != null) {
            return Mono.just(variant);
        }
        return featureManager.getVariantAsync(feature)
            .doOnSuccess((variantObject) -> variantMap.put(feature, variantObject));
    }
    
    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariant has been already returned a result for this feature, it will return the same value as it did
     * before.
     * 
     * @param feature Feature being checked.
     * @return Assigned Variant
     */
    public Variant getVariant(String feature) {
        Variant variant = variantMap.get(feature);
        if (variant != null) {
            return variant;
        }
        return featureManager.getVariantAsync(feature)
            .doOnSuccess((variantObject) -> variantMap.put(feature, variantObject)).block();
    }
    

    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariantAsync has been already returned a result for this feature, it will return the same value as it did
     * before.
     * 
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return Assigned Variant
     */
    public Mono<Variant> getVariantAsync(String feature, Object featureContext) {
        Variant variant = variantMap.get(feature);
        if (variant != null) {
            return Mono.just(variant);
        }
        return featureManager.getVariantAsync(feature, featureContext)
            .doOnSuccess((variantObject) -> variantMap.put(feature, variantObject));
    }
    
    /**
     * Returns the variant assigned to the current context.
     * <p>
     * If getVariant has been already returned a result for this feature, it will return the same value as it did
     * before.
     * 
     * @param feature Feature being checked.
     * @param featureContext Local context
     * @return Assigned Variant
     */
    public Variant getVariant(String feature, Object featureContext) {
        Variant variant = variantMap.get(feature);
        if (variant != null) {
            return variant;
        }
        return featureManager.getVariantAsync(feature, featureContext)
            .doOnSuccess((variantObject) -> variantMap.put(feature, variantObject)).block();
    }
}
