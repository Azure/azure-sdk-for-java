// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlerts;
import com.azure.resourcemanager.monitor.fluent.models.MetricAlertResourceInner;
import com.azure.resourcemanager.monitor.fluent.MetricAlertsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link MetricAlerts}. */
class MetricAlertsImpl
    extends TopLevelModifiableResourcesImpl<
        MetricAlert, MetricAlertImpl, MetricAlertResourceInner, MetricAlertsClient, MonitorManager>
    implements MetricAlerts {

    MetricAlertsImpl(final MonitorManager monitorManager) {
        super(monitorManager.serviceClient().getMetricAlerts(), monitorManager);
    }

    @Override
    protected MetricAlertImpl wrapModel(String name) {
        MetricAlertResourceInner inner = new MetricAlertResourceInner();
        inner.withEnabled(true);
        inner.withAutoMitigate(true);
        return new MetricAlertImpl(name, inner, this.manager());
    }

    @Override
    protected MetricAlertImpl wrapModel(MetricAlertResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new MetricAlertImpl(inner.name(), inner, this.manager());
    }

    @Override
    public MetricAlertImpl define(String name) {
        return wrapModel(name);
    }
}
