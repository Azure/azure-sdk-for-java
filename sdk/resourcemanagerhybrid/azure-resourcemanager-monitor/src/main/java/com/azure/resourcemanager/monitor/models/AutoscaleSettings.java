// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point to autoscale management API in Azure. */
@Fluent
public interface AutoscaleSettings
    extends SupportsCreating<AutoscaleSetting.DefinitionStages.Blank>,
        SupportsListing<AutoscaleSetting>,
        SupportsListingByResourceGroup<AutoscaleSetting>,
        SupportsGettingById<AutoscaleSetting>,
        SupportsBatchCreation<AutoscaleSetting>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchDeletion,
        HasManager<MonitorManager> {
}
