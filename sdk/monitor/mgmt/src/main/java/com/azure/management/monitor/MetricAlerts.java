/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.management.monitor.models.MetricAlertsInner;
import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for Metric Alert management API.
 */
@Fluent
public interface MetricAlerts extends
        SupportsCreating<MetricAlert.DefinitionStages.Blank>,
        SupportsListing<MetricAlert>,
        SupportsListingByResourceGroup<MetricAlert>,
        SupportsGettingById<MetricAlert>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<MetricAlert>,
        SupportsBatchDeletion,
        HasManager<MonitorManager>,
        HasInner<MetricAlertsInner> {
}
