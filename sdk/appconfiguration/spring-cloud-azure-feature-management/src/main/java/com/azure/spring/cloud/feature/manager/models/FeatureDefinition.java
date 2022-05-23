// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.azure.spring.cloud.feature.manager.implementation.models.DynamicFeature;

/**
 * The definition of a dynamic feature.
 */
@Validated
public class FeatureDefinition {

	private String name;

	private String assigner;

	private List<FeatureVariant> variants;

	/**
	 * Definition of a Dynamic Feature.
	 * 
	 * @param feature        name of the feature
	 * @param dynamicFeature dynamic feature object to be made into a definition
	 */
	public FeatureDefinition(String feature, DynamicFeature dynamicFeature) {
		this.name = feature;
		this.assigner = dynamicFeature.getAssigner();
		this.variants = new ArrayList<FeatureVariant>();

		for (int i = 0; i < dynamicFeature.getVariants().size(); i++) {
			variants.add(dynamicFeature.getVariants().get(String.valueOf(i)));
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the assigner
	 */
	public String getAssigner() {
		return assigner;
	}

	/**
	 * @return the variants
	 */
	public List<FeatureVariant> getVariants() {
		return variants;
	}
}
