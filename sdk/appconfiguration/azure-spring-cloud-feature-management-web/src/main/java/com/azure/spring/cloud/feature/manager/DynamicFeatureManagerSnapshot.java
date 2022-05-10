// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.util.HashMap;

import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled. Returns the same
 * value in the same request.
 */
@Configuration
public class DynamicFeatureManagerSnapshot {

    private DynamicFeatureManager dynamicFeatureManager;

    private HashMap<String, Object> requestMap;

    /**
     * Used to evaluate whether a feature is enabled or disabled. When setup with the <code>@RequestScope</code> it will
     * return the same value for all checks of the given feature flag.
     * 
     * @param dynamicFeatureManager DynamicFeatureManager
     */
    public DynamicFeatureManagerSnapshot(DynamicFeatureManager dynamicFeatureManager) {
        this.dynamicFeatureManager = dynamicFeatureManager;
        this.requestMap = new HashMap<>();
    }

    /**
     * Returns a feature variant of the type given.
     * <p>
     * If getVariantAsync has all ready been called on this variant, it will return the same variant as it did before.
     *
     * @param <T> Type of the feature that will be returned.
     * @param featureName name of the feature being checked.
     * @param type Type of the feature being checked.
     * @return variant of the provided type, can return Mono with a null value if the feature requested doesn't match
     * the stored type.
     * @throws FilterNotFoundException if a Filter with the given name isn't found
     */
    public <T> Mono<T> getVariantAsync(String featureName, Class<T> type) {
        if (requestMap.get(featureName) == null) {
            T variant = dynamicFeatureManager.getVariantAsync(featureName, type).block();

            if (variant != null) {
                requestMap.put(featureName, variant);
            }
            return Mono.justOrEmpty(variant);
        }

        Object variant = requestMap.get(featureName);
        if (variant != null && variant.getClass().equals(type)) {
            return Mono.just(type.cast(variant));
        }

        return Mono.justOrEmpty(null);
    }
}
