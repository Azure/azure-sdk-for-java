// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import reactor.core.publisher.Mono;

/** Entry point for key vaults management API. */
@Fluent
public interface Vaults
    extends SupportsCreating<Vault.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<Vault>,
        SupportsGettingByResourceGroup<Vault>,
        SupportsGettingById<Vault>,
        SupportsDeletingByResourceGroup,
        HasManager<KeyVaultManager> {

    /**
     * Gets information about the deleted vaults in a subscription.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PagedList&lt;DeletedVault&gt; object if successful.
     */
    PagedIterable<DeletedVault> listDeleted();

    /**
     * Gets information about the deleted vaults in a subscription.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;DeletedVault&gt; object
     */
    PagedFlux<DeletedVault> listDeletedAsync();

    /**
     * Gets the deleted Azure key vault.
     *
     * @param vaultName The name of the vault.
     * @param location The location of the deleted vault.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DeletedVault object if successful.
     */
    DeletedVault getDeleted(String vaultName, String location);

    /**
     * Gets the deleted Azure key vault.
     *
     * @param vaultName The name of the vault.
     * @param location The location of the deleted vault.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link Mono} object
     */
    Mono<DeletedVault> getDeletedAsync(String vaultName, String location);

    /**
     * Permanently deletes the specified vault. aka Purges the deleted Azure key vault.
     *
     * @param vaultName The name of the soft-deleted vault.
     * @param location The location of the soft-deleted vault.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void purgeDeleted(String vaultName, String location);

    /**
     * Permanently deletes the specified vault. aka Purges the deleted Azure key vault.
     *
     * @param vaultName The name of the soft-deleted vault.
     * @param location The location of the soft-deleted vault.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Mono<Void> purgeDeletedAsync(String vaultName, String location);

    /**
     * Checks that the vault name is valid and is not already in use.
     *
     * @param name The vault name.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CheckNameAvailabilityResult object if successful.
     */
    CheckNameAvailabilityResult checkNameAvailability(String name);

    /**
     * Checks that the vault name is valid and is not already in use.
     *
     * @param name The vault name.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CheckNameAvailabilityResult object
     */
    Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

    /**
     * Recovers a soft deleted vault.
     *
     * @param resourceGroupName The name of the Resource Group to which the server belongs.
     * @param vaultName Name of the vault
     * @param location The location of the deleted vault.
     * @return the recovered Vault object if successful
     */
    Vault recoverSoftDeletedVault(String resourceGroupName, String vaultName, String location);

    /**
     * Recovers a soft deleted vault.
     *
     * @param resourceGroupName The name of the Resource Group to which the server belongs.
     * @param vaultName Name of the vault
     * @param location The location of the deleted vault.
     * @return the recovered Vault object if successful
     */
    Mono<Vault> recoverSoftDeletedVaultAsync(String resourceGroupName, String vaultName, String location);
}
