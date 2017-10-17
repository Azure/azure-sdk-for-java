/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.containerregistry.implementation.RegistriesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

import java.util.Collection;

/**
 * Entry point to the registry management API.
 */
@Fluent()
@Beta(SinceVersion.V1_1_0)
public interface Registries extends
    SupportsCreating<Registry.DefinitionStages.Blank>,
    HasManager<ContainerRegistryManager>,
    HasInner<RegistriesInner>,
    SupportsBatchCreation<Registry>,
    SupportsGettingById<Registry>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsListingByResourceGroup<Registry>,
    SupportsGettingByResourceGroup<Registry>,
    SupportsListing<Registry> {

    /**
     * Gets the login credentials for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return the container registry's login credentials
     */
    @Beta(SinceVersion.V1_4_0)
    RegistryCredentials getCredentials(String resourceGroupName, String registryName);

    /**
     * Gets the login credentials for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return a representation of the future computation of this call, returning the container registry's login credentials
     */
    @Beta(SinceVersion.V1_4_0)
    Observable<RegistryCredentials> getCredentialsAsync(String resourceGroupName, String registryName);

    /**
     * Regenerates the value for one of the admin user access key for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @param accessKeyType the admin user access key name to regenerate the value for
     * @return the container registry's login credentials
     */
    @Beta(SinceVersion.V1_4_0)
    RegistryCredentials regenerateCredential(String resourceGroupName, String registryName, AccessKeyType accessKeyType);

    /**
     * Regenerates the value for one of the admin user access key for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @param accessKeyType the admin user access key name to regenerate the value for
     * @return a representation of the future computation of this call, returning the container registry's login credentials
     */
    @Beta(SinceVersion.V1_4_0)
    Observable<RegistryCredentials> regenerateCredentialAsync(String resourceGroupName, String registryName, AccessKeyType accessKeyType);

    /**
     * Lists the quota usages for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return the list of container registry's quota usages
     */
    @Beta(SinceVersion.V1_4_0)
    Collection<RegistryUsage> listQuotaUsages(String resourceGroupName, String registryName);

    /**
     * Lists the quota usages for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return a representation of the future computation of this call, returning the list of container registry's quota usages
     */
    @Beta(SinceVersion.V1_4_0)
    Observable<RegistryUsage> listQuotaUsagesAsync(String resourceGroupName, String registryName);

    /**
     * Checks if the specified container registry name is valid and available.
     *
     * @param name the container registry name to check
     * @return whether the name is available and other info if not
     */
    @Beta(SinceVersion.V1_4_0)
    CheckNameAvailabilityResult checkNameAvailability(String name);

    /**
     * Checks if container registry name is valid and is not in use asynchronously.
     *
     * @param name the container registry name to check
     * @return a representation of the future computation of this call, returning whether the name is available or other info if not
     */
    @Beta(SinceVersion.V1_4_0)
    Observable<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

    /**
     * @return returns entry point to manage container registry webhooks.
     */
    WebhooksClient webhooks();


    /**
     * Grouping of registry webhook actions.
     */
    interface WebhooksClient {
        /**
         * Gets the properties of the specified webhook.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         * @return the Webhook object if successful
         */
        @Beta(SinceVersion.V1_4_0)
        Webhook get(String resourceGroupName, String registryName, String webhookName);

        /**
         * Gets the properties of the specified webhook.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         * @return a representation of the future computation of this call, returning the Webhook object
         */
        @Beta(SinceVersion.V1_4_0)
        Observable<Webhook> getAsync(String resourceGroupName, String registryName, String webhookName);

        /**
         * Deletes a webhook from the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         */
        @Beta(SinceVersion.V1_4_0)
        void delete(String resourceGroupName, String registryName, String webhookName);

        /**
         * Deletes a webhook from the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         * @return a representation of the future computation of this call
         */
        @Beta(SinceVersion.V1_4_0)
        Completable deleteAsync(String resourceGroupName, String registryName, String webhookName);

        /**
         * Lists all the webhooks for the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @return the list of all the webhooks for the specified container registry
         */
        @Beta(SinceVersion.V1_4_0)
        PagedList<Webhook> list(String resourceGroupName, String registryName);

        /**
         * Lists all the webhooks for the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @return a representation of the future computation of this call, returning the list of all the webhooks for the specified container registry
         */
        @Beta(SinceVersion.V1_4_0)
        Observable<Webhook> listAsync(String resourceGroupName, String registryName);

    }

}
