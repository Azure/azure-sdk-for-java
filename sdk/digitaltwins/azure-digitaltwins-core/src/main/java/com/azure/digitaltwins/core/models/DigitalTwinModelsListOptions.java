// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// This class manually copies the generated class of the same name, but also adds properties for includeModelDefinition
// and dependenciesFor since the swagger does not group those in with these options for us.

/** The optional parameters that can be set when listing models. */
@Fluent
public final class DigitalTwinModelsListOptions {
    /*
     * Identifies the request in a distributed tracing system.
     */
    @JsonProperty(value = "traceparent")
    private String traceparent;

    /*
     * Provides vendor-specific trace identification information and is a
     * companion to traceparent.
     */
    @JsonProperty(value = "tracestate")
    private String tracestate;

    /*
     * The maximum number of items to retrieve per request. The server may
     * choose to return less than the requested number.
     */
    @JsonProperty(value = "MaxItemsPerPage")
    private Integer maxItemsPerPage;

    /**
     * Get the traceparent property: Identifies the request in a distributed tracing system.
     *
     * @return the traceparent value.
     */
    public String getTraceparent() {
        return this.traceparent;
    }

    /**
     * Set the traceparent property: Identifies the request in a distributed tracing system.
     *
     * @param traceparent the traceparent value to set.
     * @return the DigitalTwinModelsListOptions object itself.
     */
    public DigitalTwinModelsListOptions setTraceparent(String traceparent) {
        this.traceparent = traceparent;
        return this;
    }

    /**
     * Get the tracestate property: Provides vendor-specific trace identification information and is a companion to
     * traceparent.
     *
     * @return the tracestate value.
     */
    public String getTracestate() {
        return this.tracestate;
    }

    /**
     * Set the tracestate property: Provides vendor-specific trace identification information and is a companion to
     * traceparent.
     *
     * @param tracestate the tracestate value to set.
     * @return the DigitalTwinModelsListOptions object itself.
     */
    public DigitalTwinModelsListOptions setTracestate(String tracestate) {
        this.tracestate = tracestate;
        return this;
    }

    /**
     * Get the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @return the maxItemsPerPage value.
     */
    public Integer getMaxItemsPerPage() {
        return this.maxItemsPerPage;
    }

    /**
     * Set the maxItemsPerPage property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested number.
     *
     * @param maxItemsPerPage the maxItemsPerPage value to set.
     * @return the DigitalTwinModelsListOptions object itself.
     */
    public DigitalTwinModelsListOptions setMaxItemsPerPage(Integer maxItemsPerPage) {
        this.maxItemsPerPage = maxItemsPerPage;
        return this;
    }

    /*
     * Whether to include the model definition in the result. If false, only the model metadata will be returned.
     * Disabled by default.
     */
    private Boolean includeModelDefinition = false;

    /**
     * Gets whether to include the model definition in the result. If false, only the model metadata will be returned.
     * @return the includeModelDefinition value.
     */
    public Boolean getIncludeModelDefinition() { return this.includeModelDefinition; }

    /**
     * Sets whether to include the model definition in the result. If false, only the model metadata will be returned.
     * @param includeModelDefinition the includeModelDefinition value to set.
     * @return the ListModelOptions object itself.
     */
    public DigitalTwinModelsListOptions setIncludeModelDefinition(Boolean includeModelDefinition) {
        this.includeModelDefinition = includeModelDefinition;
        return this;
    }

    /*
     * The model Ids to have dependencies retrieved.
     */
    private List<String> dependenciesFor;

    /**
     * Gets the model Ids that will have their dependencies retrieved.
     * @return the dependenciesFor value.
     */
    public List<String> getDependenciesFor() { return this.dependenciesFor; }

    /**
     * Sets the model Ids that will have their dependencies retrieved.
     * @param dependenciesFor the dependenciesFor value to set.
     * @return the ListModelOptions object itself.
     */
    public DigitalTwinModelsListOptions setDependenciesFor(List<String> dependenciesFor) {
        this.dependenciesFor = dependenciesFor;
        return this;
    }
}
