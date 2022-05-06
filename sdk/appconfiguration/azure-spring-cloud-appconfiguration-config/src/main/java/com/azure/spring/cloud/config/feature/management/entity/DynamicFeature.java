// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.feature.management.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the assigner and variants of a Dynamic Feature
 */
@Validated
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

    public String getName() {
        return name;
    }

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
        @NotNull
        Map<String, FeatureVariant> map = new LinkedHashMap<>();
        for (int i = 0; i < variants.size(); i++) {
            map.put(String.valueOf(i), variants.get(i));
        }
        this.variants = map;
    }

    /**
     * Validates Feature Definition on construction
     */
    @PostConstruct
    public void validateAndInit() {
        Assert.isTrue(variants.size() > 0, "Assigner " + assigner + " needs at least one variant.");
    }
}
