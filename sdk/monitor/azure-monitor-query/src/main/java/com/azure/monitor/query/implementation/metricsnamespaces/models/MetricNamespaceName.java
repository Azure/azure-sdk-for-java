// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.implementation.metricsnamespaces.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The fully qualified metric namespace name. */
@Fluent
public final class MetricNamespaceName {
    /*
     * The metric namespace name.
     */
    @JsonProperty(value = "metricNamespaceName")
    private String metricNamespaceName;

    /**
     * Get the metricNamespaceName property: The metric namespace name.
     *
     * @return the metricNamespaceName value.
     */
    public String getMetricNamespaceName() {
        return this.metricNamespaceName;
    }

    /**
     * Set the metricNamespaceName property: The metric namespace name.
     *
     * @param metricNamespaceName the metricNamespaceName value to set.
     * @return the MetricNamespaceName object itself.
     */
    public MetricNamespaceName setMetricNamespaceName(String metricNamespaceName) {
        this.metricNamespaceName = metricNamespaceName;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() { }
}
