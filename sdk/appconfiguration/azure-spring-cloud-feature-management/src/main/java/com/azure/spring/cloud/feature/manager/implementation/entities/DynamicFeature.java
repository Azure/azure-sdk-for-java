// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation.entities;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.azure.spring.cloud.feature.manager.entities.FeatureVariant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Defines the assigner and variants of a Dynamic Feature
 */
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFeature {

    @NotBlank
    private String assigner;

    @NotNull
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