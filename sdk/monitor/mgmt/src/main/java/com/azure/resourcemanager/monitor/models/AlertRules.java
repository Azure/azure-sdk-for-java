// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;

/** Entry point to Alert Rules management API. */
@Fluent
public interface AlertRules extends HasManager<MonitorManager> {

    /** @return the Azure Metric Alerts API entry point */
    MetricAlerts metricAlerts();

    /** @return the Azure Activity Log Alerts API entry point */
    ActivityLogAlerts activityLogAlerts();
}
