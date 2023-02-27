// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;

/**
 * The definition of a dynamic feature.
 */
@Validated
public class FeatureDefinition {

    private final String name;

    private final String assigner;

    private final List<FeatureVariant> variants;

    /**
     * Definition of a Dynamic Feature.
     * 
     * @param feature name of the feature
     * @param assigner name of the assigner used
     * @param variantMap Map of names of variants and the the FeatureVariants
     */
    public FeatureDefinition(String feature, String assigner, Map<String, FeatureVariant> variantMap) {
        this.name = feature;
        this.assigner = assigner;
        this.variants = new ArrayList<FeatureVariant>();

        for (int i = 0; i < variantMap.size(); i++) {
            variants.add(variantMap.get(String.valueOf(i)));
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
