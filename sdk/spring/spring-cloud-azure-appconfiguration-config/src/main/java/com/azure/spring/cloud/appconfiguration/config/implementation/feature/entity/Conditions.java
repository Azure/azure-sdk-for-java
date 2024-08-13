// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;

import java.util.ArrayList;
import java.util.List;

import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conditions {
    @JsonProperty("client_filters")
    private final List<FeatureFlagFilter> clientFilters;

    @JsonProperty("requirement_type")
    private String requirementType = DEFAULT_REQUIREMENT_TYPE;

    public Conditions(List<FeatureFlagFilter> featureFilters, String requirementType) {
        clientFilters = new ArrayList<>();
        clientFilters.addAll(featureFilters);
        this.requirementType = requirementType;
    }

    /**
     * @return the requirementType
     */
    public String getRequirementType() {
        return requirementType;
    }

    /**
     * @return the clientFilters
     */
    public List<FeatureFlagFilter> getClientFilters() {
        return clientFilters;
    }

}
