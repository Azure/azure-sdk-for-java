// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.manager.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.manager.implementation.models.Feature;
import com.azure.spring.cloud.feature.manager.models.FeatureFilterEvaluationContext;
import com.azure.spring.cloud.feature.manager.models.IFeatureFilter;

import reactor.core.publisher.Mono;

/**
 * Holds information on Feature Management properties and can check if a given
 * feature is enabled.
 */
public class FeatureManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

	private transient ApplicationContext context;

	private FeatureManagementProperties featureManagementConfigurations;

	private transient FeatureManagementConfigProperties properties;

	/**
	 * Can be called to check if a feature is enabled or disabled.
	 * 
	 * @param context ApplicationContext
	 * @param featureManagementConfigurations Configuration Properties for Feature Flags
	 * @param properties FeatureManagementConfigProperties
	 */
	public FeatureManager(ApplicationContext context, FeatureManagementProperties featureManagementConfigurations,
			FeatureManagementConfigProperties properties) {
		this.context = context;
		this.featureManagementConfigurations = featureManagementConfigurations;
		this.properties = properties;
	}

	/**
	 * Checks to see if the feature is enabled. If enabled it check each filter,
	 * once a single filter returns true it returns true. If no filter returns true,
	 * it returns false. If there are no filters, it returns true. If feature isn't
	 * found it returns false.
	 *
	 * @param feature Feature being checked.
	 * @return state of the feature
	 * @throws FilterNotFoundException file not found
	 */
	public Mono<Boolean> isEnabledAsync(String feature) throws FilterNotFoundException {
		return Mono.just(checkFeatures(feature));
	}

	private boolean checkFeatures(String feature) throws FilterNotFoundException {
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

		return featureItem.getEnabledFor().values().stream().filter(Objects::nonNull)
				.filter(featureFilter -> featureFilter.getName() != null)
				.map(featureFilter -> isFeatureOn(featureFilter, feature)).findAny().orElse(false);
	}

	private boolean isFeatureOn(FeatureFilterEvaluationContext filter, String feature) {
		try {
			IFeatureFilter featureFilter = (IFeatureFilter) context.getBean(filter.getName());
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
