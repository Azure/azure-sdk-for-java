/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.NamespacesInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

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
        HasManager<ServiceBusManager>,
        HasInner<NamespacesInner> {
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
     * @return a representation of the deferred computation of this call, returning whether the name is available or other info if not
     */
    Observable<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

    /**
     * Checks if namespace name is valid and is not in use asynchronously.
     *
     * @param name the namespace name to check
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback);
}
