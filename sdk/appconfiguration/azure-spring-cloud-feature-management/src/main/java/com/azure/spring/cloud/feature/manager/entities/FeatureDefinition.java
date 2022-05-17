// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.entities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.azure.spring.cloud.feature.manager.implementation.entities.DynamicFeature;

/**
 * The definition of a dynamic feature.
 */
@Validated
public class FeatureDefinition {

    @NotBlank
    private String name;

    @NotBlank
    private String assigner;

    @NotNull
    private List<FeatureVariant> variants;

    /**
     * Definition of a Dynamic Feature.
     * @param feature name of the feature
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
    
    /**
     * Validates Feature Definition on construction
     */
    @PostConstruct
    public void validateAndInit() {
        Assert.isTrue(variants.size() > 0, "Feature " + name + " needs at least one variant.");
    }
}
