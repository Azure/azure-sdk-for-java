// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.feature.manager.DynamicFeatureManager;
import com.azure.spring.cloud.feature.manager.FeatureManager;
import com.azure.spring.cloud.feature.manager.IDynamicFeatureProperties;

/**
 * Configuration for setting up FeatureManager
 */
@Configuration
@EnableConfigurationProperties({ FeatureManagementConfigProperties.class, FeatureManagementProperties.class })
public class FeatureManagementConfiguration {

	/**
	 * Creates Feature Manager
	 * 
	 * @param context ApplicationContext
	 * @param featureManagementConfigurations Configuration Properties for Feature Flags
	 * @param properties Feature Management configuration properties
	 * @return FeatureManager
	 */
	@Bean
	public FeatureManager featureManager(ApplicationContext context,
			FeatureManagementProperties featureManagementConfigurations, FeatureManagementConfigProperties properties) {
		return new FeatureManager(context, featureManagementConfigurations, properties);
	}

	/**
	 * Creates Dynamic Feature Manager
	 * 
	 * @param context ApplicationContext
	 * @param propertiesProvider Object Provider for accessing client IDynamicFeatureProperties
	 * @param featureManagementConfigurations Configuration Properties for Feature Flags
	 * @return DynamicFeatureManager
	 */
	@Bean
	public DynamicFeatureManager dynamicFeatureManager(ApplicationContext context,
			ObjectProvider<IDynamicFeatureProperties> propertiesProvider,
			FeatureManagementProperties featureManagementConfigurations) {
		return new DynamicFeatureManager(context, propertiesProvider, featureManagementConfigurations);
	}

}
