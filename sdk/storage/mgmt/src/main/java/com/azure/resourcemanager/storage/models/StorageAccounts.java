// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

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
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import reactor.core.publisher.Mono;

/** Entry point for storage accounts management API. */
@Fluent
public interface StorageAccounts
    extends SupportsListing<StorageAccount>,
        SupportsCreating<StorageAccount.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<StorageAccount>,
        SupportsGettingByResourceGroup<StorageAccount>,
        SupportsGettingById<StorageAccount>,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<StorageAccount>,
        SupportsBatchDeletion,
        HasManager<StorageManager>,
        HasInner<StorageAccountsClient> {
    /**
     * Checks that account name is valid and is not in use.
     *
     * @param name the account name to check
     * @return whether the name is available and other info if not
     */
    CheckNameAvailabilityResult checkNameAvailability(String name);

    /**
     * Checks that account name is valid and is not in use asynchronously.
     *
     * @param name the account name to check
     * @return a representation of the deferred computation of this call, returning whether the name is available and
     *     other info if not
     */
    Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

    /**
     * Creates an Sas token for the storage account.
     *
     * @param resourceGroupName the name of the account's resource group
     * @param accountName the account name to check
     * @param parameters the parameters to list service SAS credentials of a specific resource
     * @return the created Sas token
     */
    String createSasToken(String resourceGroupName, String accountName, ServiceSasParameters parameters);

    /**
     * Creates an Sas token for the storage account asynchronously.
     *
     * @param resourceGroupName the name of the account's resource group
     * @param accountName the account name to check
     * @param parameters the parameters to list service SAS credentials of a specific resource
     * @return an observable of the created Sas token
     */
    Mono<String> createSasTokenAsync(String resourceGroupName, String accountName, ServiceSasParameters parameters);

    /**
     * Sets a failover request that can be triggered for a storage account in case of availability issues.
     *
     * @param resourceGroupName the resource group name of the storage account
     * @param accountName the account name to check
     */
    void failover(String resourceGroupName, String accountName);

    /**
     * Sets a failover request asynchronously that can be triggered for a storage account in case of availability
     * issues.
     *
     * @param resourceGroupName the resource group name of the storage account
     * @param accountName the account name to check
     * @return a completable
     */
    Mono<Void> failoverAsync(String resourceGroupName, String accountName);
}
