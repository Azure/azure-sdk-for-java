// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import reactor.core.publisher.Mono;

/**
 * Entry point to Service Bus namespace API in Azure.
 */
@Fluent
public interface ServiceBusNamespaces extends
    SupportsCreating<ServiceBusNamespace.DefinitionStages.Blank>,
    SupportsBatchCreation<ServiceBusNamespace>,
    SupportsBatchDeletion,
    SupportsListing<ServiceBusNamespace>,
    SupportsListingByResourceGroup<ServiceBusNamespace>,
    SupportsGettingByResourceGroup<ServiceBusNamespace>,
    SupportsGettingById<ServiceBusNamespace>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    HasManager<ServiceBusManager> {
    /**
     * Checks if namespace name is valid and is not in use.
     *
     * @param name the account name to check
     * @return whether the name is available and other info if not
     */
    CheckNameAvailabilityResult checkNameAvailability(String name);

    /**
     * Checks if namespace name is valid and is not in use asynchronously.
     *
     * @param name the namespace name to check
     * @return a representation of the deferred computation of this call,
     * returning whether the name is available or other info if not
     */
    Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);
}
