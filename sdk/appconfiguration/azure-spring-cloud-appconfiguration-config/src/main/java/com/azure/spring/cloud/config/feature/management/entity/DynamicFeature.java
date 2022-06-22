// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.feature.management.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the assigner and variants of a Dynamic Feature
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFeature {

    @NotBlank
    @JsonProperty("id")
    private String name;

    @NotBlank
    @JsonProperty("assigner")
    @JsonAlias("client_assigner")
    private String assigner;

    @NotNull
    private Map<String, FeatureVariant> variants = new HashMap<>();

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

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
    public void setVariants(List<FeatureVariant> variants) {
        Map<String, FeatureVariant> map = new LinkedHashMap<>();
        if (variants != null) {
            for (int i = 0; i < variants.size(); i++) {
                map.put(String.valueOf(i), variants.get(i));
            }
        }
        this.variants = map;
    }
}
