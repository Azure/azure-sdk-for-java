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
public final class ServiceStatistics {
    /*
     * Service level resource counters.
     */
    @JsonProperty(value = "counters", required = true)
    private ServiceCounters counters;

    /*
     * Service level general limits.
     */
    @JsonProperty(value = "limits", required = true)
    private ServiceLimits limits;

    /**
     * Get the counters property: Service level resource counters.
     *
     * @return the counters value.
     */
    public ServiceCounters getCounters() {
        return this.counters;
    }

    /**
     * Set the counters property: Service level resource counters.
     *
     * @param counters the counters value to set.
     * @return the ServiceStatistics object itself.
     */
    public ServiceStatistics setCounters(ServiceCounters counters) {
        this.counters = counters;
        return this;
    }

    /**
     * Get the limits property: Service level general limits.
     *
     * @return the limits value.
     */
    public ServiceLimits getLimits() {
        return this.limits;
    }

    /**
     * Set the limits property: Service level general limits.
     *
     * @param limits the limits value to set.
     * @return the ServiceStatistics object itself.
     */
    public ServiceStatistics setLimits(ServiceLimits limits) {
        this.limits = limits;
        return this;
    }
}
