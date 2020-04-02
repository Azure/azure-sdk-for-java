/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.MetricAlert;
import com.azure.management.monitor.MetricAlerts;
import com.azure.management.monitor.models.MetricAlertResourceInner;
import com.azure.management.monitor.models.MetricAlertsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for {@link MetricAlerts}.
 */
class MetricAlertsImpl
        extends TopLevelModifiableResourcesImpl<
        MetricAlert,
        MetricAlertImpl,
        MetricAlertResourceInner,
        MetricAlertsInner,
        MonitorManager>
        implements MetricAlerts {

    MetricAlertsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().metricAlerts(), monitorManager);
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
        if (inner ==  null) {
            return null;
        }
        return new MetricAlertImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public MetricAlertImpl define(String name) {
        return wrapModel(name);
    }
}
