// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.models;

import static com.azure.spring.cloud.feature.management.implementation.FeatureManagementConstants.DEFAULT_REQUIREMENT_TYPE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* Conditions for evaluating a feature flag. This class defines how feature filters
* should be evaluated to determine if a feature flag is enabled for the current request.
* It specifies both the filters to check and how their results should be combined
* (e.g., if all filters must pass or if only one needs to pass).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conditions {

    /**
     * Creates a new instance of the Conditions class.
     */
    public Conditions() {
    }

    /**
     * List of client-side feature filters to evaluate for determining if
     * a feature flag is enabled. Each filter context contains parameters
     * needed for filter evaluation.
     */
    @JsonProperty("client_filters")
    private List<FeatureFilterEvaluationContext> clientFilters = new ArrayList<>();

    /**
     * Requirement type that determines the logic for combining filter results.
     * Default is typically "All" which means all filters must pass for the
     * feature to be enabled (logical AND).
     */
    @JsonProperty("requirement_type")
    private String requirementType = DEFAULT_REQUIREMENT_TYPE;

    /**
     * Gets the requirement type that determines how feature filters are evaluated.
     * The requirement type specifies whether all filters must evaluate to true (AND logic)
     * or if only one filter needs to evaluate to true (OR logic).
     *
     * @return the requirement type for filter evaluation
     */
    public String getRequirementType() {
        return requirementType;
    }

    /**
     * Sets the requirement type that determines how feature filters are evaluated.
     * Valid values are typically "All" (AND logic) or "Any" (OR logic),
     * where "All" requires all filters to evaluate to true, and "Any" requires
     * only one filter to evaluate to true.
     *
     * @param requirementType the requirement type to set for filter evaluation
     * @return the updated Conditions object
     */
    public Conditions setRequirementType(String requirementType) {
        this.requirementType = requirementType;
        return this;
    }

    /**
     * Gets the list of client-side feature filters that should be evaluated
     * to determine if a feature flag is enabled.
     * Each filter contains its own parameters and evaluation context.
     *
     * @return the list of client-side feature filters
     */
    public List<FeatureFilterEvaluationContext> getClientFilters() {
        return clientFilters;
    }

    /**
     * Sets the list of client-side feature filters to be evaluated
     * to determine if a feature flag is enabled.
     * Each filter should contain its necessary parameters and context for evaluation.
     *
     * @param clientFilters the list of client-side feature filters to set
     * @return the updated Conditions object
     */
    public Conditions setClientFilters(List<FeatureFilterEvaluationContext> clientFilters) {
        this.clientFilters = clientFilters;
        return this;
    }

}
