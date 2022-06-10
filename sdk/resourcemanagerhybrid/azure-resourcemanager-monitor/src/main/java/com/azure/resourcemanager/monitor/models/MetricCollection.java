// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.resourcemanager.monitor.fluent.models.ResponseInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.Duration;
import java.util.List;

/** The MetricCollection representing wrapper over ResponseInner type. */
public interface MetricCollection extends HasInnerModel<ResponseInner> {
    /**
     * Get the namespace value.
     *
     * @return the namespace value
     */
    String namespace();

    /**
     * Get the resource region value.
     *
     * @return the resource region value
     */
    String resourceRegion();

    /**
     * Get the cost value.
     *
     * @return the cost value
     */
    Double cost();

    /**
     * Get the timespan value.
     *
     * @return the timespan value
     */
    String timespan();

    /**
     * Get the interval value.
     *
     * @return the interval value
     */
    Duration interval();

    /**
     * Get the metric collection value.
     *
     * @return the metric collection value
     */
    List<Metric> metrics();
}
