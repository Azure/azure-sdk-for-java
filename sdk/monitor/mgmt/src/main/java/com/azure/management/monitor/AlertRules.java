/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasManager;

/**
 *  Entry point to Alert Rules management API.
 */
@Fluent
public interface AlertRules extends
        HasManager<MonitorManager> {

    /**
     * @return the Azure Metric Alerts API entry point
     */
    MetricAlerts metricAlerts();

    /**
     * @return the Azure Activity Log Alerts API entry point
     */
    ActivityLogAlerts activityLogAlerts();
}
