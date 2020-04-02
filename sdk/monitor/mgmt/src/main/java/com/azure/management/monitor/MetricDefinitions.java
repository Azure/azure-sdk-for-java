/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.monitor.models.MetricDefinitionsInner;
import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;


/**
 * Entry point for Monitor Metric Definitions API.
 */
public interface MetricDefinitions extends
        HasManager<MonitorManager>,
        HasInner<MetricDefinitionsInner> {

    /**
     * Lists Metric Definitions for a given resource.
     *
     * @param resourceId The resource Id.
     * @return list of metric definitions.
     */
    PagedIterable<MetricDefinition> listByResource(String resourceId);

    /**
     * Lists Metric Definitions for a given resource.
     *
     * @param resourceId The resource Id.
     * @return a representation of the deferred computation of Metric Definitions list call.
     */
    PagedFlux<MetricDefinition> listByResourceAsync(String resourceId);
}
