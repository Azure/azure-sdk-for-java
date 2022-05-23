// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation.models;

import java.util.HashMap;
import java.util.Map;

import com.azure.spring.cloud.feature.manager.models.FeatureVariant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Defines the assigner and variants of a Dynamic Feature
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFeature {

    private String assigner;

    private Map<String, FeatureVariant> variants = new HashMap<>();

    /**
     * @return the assigner
     */
    public String getAssigner() {
        return assigner;
    }

    /**
     * @param assigner the assigner to set
     */
    public void setAssigner(String assigner) {
        this.assigner = assigner;
    }

    /**
     * @return the variants
     */
    public Map<String, FeatureVariant> getVariants() {
        return variants;
    }

    /**
     * @param variants the variants to set
     */
    public void setVariants(Map<String, FeatureVariant> variants) {
        this.variants = variants;
    }
}