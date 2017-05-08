/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;

/**
 * Entry point for service bus operations management API.
 */
@Fluent
public interface ServiceBusOperations extends SupportsListing<ServiceBusOperation>, HasManager<ServiceBusManager> {
}
