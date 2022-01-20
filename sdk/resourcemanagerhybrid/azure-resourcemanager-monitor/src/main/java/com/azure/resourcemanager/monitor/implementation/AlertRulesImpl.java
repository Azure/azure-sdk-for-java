// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.ActivityLogAlerts;
import com.azure.resourcemanager.monitor.models.AlertRules;
import com.azure.resourcemanager.monitor.models.MetricAlerts;

/** Implementation for {@link MetricAlerts}. */
public class AlertRulesImpl implements AlertRules {

    private final MetricAlerts metricAlerts;
    private final ActivityLogAlerts activityLogAlerts;

    public AlertRulesImpl(final MonitorManager monitorManager) {
        metricAlerts = new MetricAlertsImpl(monitorManager);
        activityLogAlerts = new ActivityLogAlertsImpl(monitorManager);
    }

    @Override
    public MetricAlerts metricAlerts() {
        return metricAlerts;
    }

    @Override
    public ActivityLogAlerts activityLogAlerts() {
        return activityLogAlerts;
    }

    @Override
    public MonitorManager manager() {
        return this.metricAlerts.manager();
    }
}
