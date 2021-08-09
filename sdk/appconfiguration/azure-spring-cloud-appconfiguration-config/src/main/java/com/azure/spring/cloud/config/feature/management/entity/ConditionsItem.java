// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.feature.management.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object used when transforming Azure App Configuration Feature Flags to Client Feature Flags.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionsItem {

    @JsonProperty("client_filters")
    List<FeatureFilterEvaluationContext> clientFilters;

    /**
     * @return the clientFilters
     */
    public List<FeatureFilterEvaluationContext> getClientFilters() {
        return clientFilters;
    }

    /**
     * @param clientFilters the clientFilters to set
     */
    public void setClientFilters(List<FeatureFilterEvaluationContext> clientFilters) {
        this.clientFilters = clientFilters;
    }
}
