// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation.models;

import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.feature.management.implementation.FeatureManagementConstants.DEFAULT_REQUIREMENT_TYPE;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conditions {
    @JsonProperty("client_filters")
    private List<FeatureFilterEvaluationContext> clientFilters;

    @JsonProperty("requirement_type")
    private String requirementType = DEFAULT_REQUIREMENT_TYPE;

    /**
     * @return the requirementType
     */
    public String getRequirementType() {
        return requirementType;
    }

    /**
     * @param requirementType the requirementType to set
     */
    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    /**
     * @return the clientFilters
     */
    public List<FeatureFilterEvaluationContext> getClientFilters() {
        return clientFilters;
    }

    /**
     * @return the clientFilters in Map format using index of list as key
     */
    public Map<Integer, FeatureFilterEvaluationContext> getClientFiltersAsMap() {
        final Map<Integer, FeatureFilterEvaluationContext> clientFiltersMap = new HashMap<>();
        for (int i=0; i< clientFilters.size(); i++) {
            clientFiltersMap.put(i, clientFilters.get(i));
        }
        return clientFiltersMap;
    }

    /**
     * @param clientFilters the clientFilters to set
     */
    public void setClientFilters(List<FeatureFilterEvaluationContext> clientFilters) {
        this.clientFilters = clientFilters;
    }

}
