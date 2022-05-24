// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given
 * feature is enabled. Returns the same value in the same request.
 */
public class DynamicFeatureManagerSnapshot {

	private final DynamicFeatureManager dynamicFeatureManager;

	private final Map<String, Object> requestMap;

	/**
	 * Used to evaluate whether a feature is enabled or disabled. When setup with
	 * the <code>@RequestScope</code> it will return the same value for all checks
	 * of the given feature flag.
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
	 * If getVariantAsync has all ready been called on this variant, it will return
	 * the same variant as it did before.
	 *
	 * @param <T>         Type of the feature that will be returned.
	 * @param variantName name of the variant being checked.
	 * @param type        Type of the feature being checked.
	 * @return variant of the provided type, can return Mono with a null value if
	 *         the feature requested doesn't match the stored type.
	 * @throws FilterNotFoundException if a Filter with the given name isn't found
	 */
	public <T> Mono<T> getVariantAsync(String variantName, Class<T> type) {
		if (!requestMap.containsKey(variantName)) {
			return dynamicFeatureManager.getVariantAsync(variantName, type).doOnSuccess(newVariant -> {
				requestMap.put(variantName, newVariant);
			});
		}

		T variant = null;
		Object o = requestMap.get(variantName);
		if (o.getClass().equals(type)) {
			variant = type.cast(o);
		}

		return Mono.justOrEmpty(variant);
	}
}
