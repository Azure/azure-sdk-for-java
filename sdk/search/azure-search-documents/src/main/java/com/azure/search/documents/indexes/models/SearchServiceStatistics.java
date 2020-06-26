// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from a get service statistics request. If successful, it includes
 * service level counters and limits.
 */
@Fluent
public final class SearchServiceStatistics {
    /*
     * Service level resource counters.
     */
    @JsonProperty(value = "counters", required = true)
    private SearchServiceCounters counters;

    /*
     * Service level general limits.
     */
    @JsonProperty(value = "limits", required = true)
    private ServiceLimits limits;

    /**
     * Constructor of {@link SearchServiceStatistics}.
     *
     * @param counters Service level resource counters.
     * @param limits Service level general limits.
     */
    public SearchServiceStatistics(SearchServiceCounters counters, ServiceLimits limits) {
        this.counters = counters;
        this.limits = limits;
    }

    /**
     * Get the counters property: Service level resource counters.
     *
     * @return the counters value.
     */
    public SearchServiceCounters getCounters() {
        return this.counters;
    }

    /**
     * Get the limits property: Service level general limits.
     *
     * @return the limits value.
     */
    public ServiceLimits getLimits() {
        return this.limits;
    }
}
