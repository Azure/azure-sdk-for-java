// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * App Configuration Feature defines the feature name and a Map of FeatureFilterEvaluationContexts.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {
    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("conditions")
    private Conditions conditions = new Conditions();

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public Feature setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public Feature setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return the description
     * */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     * */
    public Feature setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the conditions
     * */
    public Conditions getConditions() {
        return conditions;
    }

    /**
     * @param conditions the conditions to set
     * */
    public Feature setConditions(Conditions conditions) {
        this.conditions = conditions;
        return this;
    }

}
