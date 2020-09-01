/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.resourcemanager.servicebus.models;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByNameAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.implementation.TopicsInner;


/**
 * Entry point to Service Bus topic management API in Azure.
 */
@Fluent
public interface Topics extends
        SupportsCreating<Topic.DefinitionStages.Blank>,
        SupportsListing<Topic>,
        SupportsGettingByNameAsync<Topic>,
        SupportsDeletingByName,
        HasManager<ServiceBusManager>,
        HasInner<TopicsInner> {
}
