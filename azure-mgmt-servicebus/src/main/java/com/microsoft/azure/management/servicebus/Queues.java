/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByNameAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.QueuesInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;


/**
 * Entry point to service bus queue management API in Azure.
 */
@Fluent
@Beta
public interface Queues extends
        SupportsCreating<Queue.DefinitionStages.Blank>,
        SupportsListing<Queue>,
        SupportsGettingByNameAsync<Queue>,
        SupportsDeletingByName,
        HasManager<ServiceBusManager>,
        HasInner<QueuesInner> {
}