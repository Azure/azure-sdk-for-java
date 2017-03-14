/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.monitor.implementation.AutoscaleSettingsInner;
import com.microsoft.azure.management.monitor.implementation.MonitorManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;


/**
 * Entry point to load balancer management API in Azure.
 */
@Fluent()
public interface AutoscaleSettings extends
    SupportsCreating<AutoscaleSetting.DefinitionStages.Blank>,
    HasManager<MonitorManager>,
    HasInner<AutoscaleSettingsInner>,
    SupportsBatchCreation<AutoscaleSetting>,
    SupportsListingByGroup<AutoscaleSetting>,
    SupportsGettingByGroup<AutoscaleSetting>,
    SupportsGettingById<AutoscaleSetting>,
    SupportsDeletingById,
    SupportsDeletingByGroup {

    MetricTrigger.StandaloneDefinitionStages.Blank defineMetricTrigger(String name);
    ScaleAction.StandaloneDefinitionStages.Blank defineScaleAction();
    Recurrence.StandaloneDefinitionStages.Blank defineRecurrence();
}
