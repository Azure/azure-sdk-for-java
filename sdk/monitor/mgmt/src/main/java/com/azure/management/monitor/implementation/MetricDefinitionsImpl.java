/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.monitor.MetricDefinition;
import com.azure.management.monitor.MetricDefinitions;
import com.azure.management.monitor.models.MetricDefinitionInner;
import com.azure.management.monitor.models.MetricDefinitionsInner;
import com.azure.management.resources.fluentcore.utils.PagedConverter;

import java.util.List;

/**
 * Implementation for {@link MetricDefinitions}.
 */
class MetricDefinitionsImpl
    implements MetricDefinitions {

    private final MonitorManager myManager;

    MetricDefinitionsImpl(final MonitorManager monitorManager) {
        this.myManager = monitorManager;
    }

    @Override
    public MonitorManager manager() {
        return this.myManager;
    }

    @Override
    public MetricDefinitionsInner inner() {
        return this.myManager.inner().metricDefinitions();
    }

    @Override
    public PagedIterable<MetricDefinition> listByResource(String resourceId) {
        return this.inner().list(resourceId).mapPage(inner -> new MetricDefinitionImpl(inner, myManager));
    }

    @Override
    public PagedFlux<MetricDefinition> listByResourceAsync(String resourceId) {
        return this.inner().listAsync(resourceId).mapPage(inner -> new MetricDefinitionImpl(inner, myManager));
    }
}
