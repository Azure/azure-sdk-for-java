// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.servicebus.ServiceBusManager;

/**
 * Entry point to service bus queue management API in Azure.
 */
@Fluent
public interface ServiceBusSubscriptions extends
    SupportsCreating<ServiceBusSubscription.DefinitionStages.Blank>,
    SupportsListing<ServiceBusSubscription>,
    SupportsGettingByName<ServiceBusSubscription>,
    SupportsDeletingByName,
    HasManager<ServiceBusManager> {
}
