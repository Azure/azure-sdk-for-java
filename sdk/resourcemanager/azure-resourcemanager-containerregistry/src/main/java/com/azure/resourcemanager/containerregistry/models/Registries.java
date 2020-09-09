// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.RegistriesClient;
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
import reactor.core.publisher.Mono;

import java.util.Collection;

/** Entry point to the registry management API. */
@Fluent()
public interface Registries
    extends SupportsCreating<Registry.DefinitionStages.Blank>,
        HasManager<ContainerRegistryManager>,
        HasInner<RegistriesClient>,
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
    RegistryCredentials getCredentials(String resourceGroupName, String registryName);

    /**
     * Gets the login credentials for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return a representation of the future computation of this call, returning the container registry's login
     *     credentials
     */
    Mono<RegistryCredentials> getCredentialsAsync(String resourceGroupName, String registryName);

    /**
     * Regenerates the value for one of the admin user access key for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @param accessKeyType the admin user access key name to regenerate the value for
     * @return the container registry's login credentials
     */
    RegistryCredentials regenerateCredential(
        String resourceGroupName, String registryName, AccessKeyType accessKeyType);

    /**
     * Regenerates the value for one of the admin user access key for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @param accessKeyType the admin user access key name to regenerate the value for
     * @return a representation of the future computation of this call, returning the container registry's login
     *     credentials
     */
    Mono<RegistryCredentials> regenerateCredentialAsync(
        String resourceGroupName, String registryName, AccessKeyType accessKeyType);

    /**
     * Lists the quota usages for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return the list of container registry's quota usages
     */
    Collection<RegistryUsage> listQuotaUsages(String resourceGroupName, String registryName);

    /**
     * Lists the quota usages for the specified container registry.
     *
     * @param resourceGroupName the resource group name
     * @param registryName the registry name
     * @return a representation of the future computation of this call, returning the list of container registry's quota
     *     usages
     */
    PagedFlux<RegistryUsage> listQuotaUsagesAsync(String resourceGroupName, String registryName);

    /**
     * Checks if the specified container registry name is valid and available.
     *
     * @param name the container registry name to check
     * @return whether the name is available and other info if not
     */
    CheckNameAvailabilityResult checkNameAvailability(String name);

    /**
     * Checks if container registry name is valid and is not in use asynchronously.
     *
     * @param name the container registry name to check
     * @return a representation of the future computation of this call, returning whether the name is available or other
     *     info if not
     */
    Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

    /**
     * The function that gets the URL of the build source upload.
     *
     * @param rgName the name of the resource group.
     * @param acrName the name of the container.
     * @return the URL of the build source upload.
     */
    SourceUploadDefinition getBuildSourceUploadUrl(String rgName, String acrName);

    /**
     * The function that gets the URL of the build source upload ashnchronously.
     *
     * @param rgName the name of the resource group.
     * @param acrName the name of the container.
     * @return the URL of the build source upload.
     */
    Mono<SourceUploadDefinition> getBuildSourceUploadUrlAsync(String rgName, String acrName);

    /** @return returns entry point to manage container registry webhooks. */
    WebhooksClient webhooks();

    /** Grouping of registry webhook actions. */
    interface WebhooksClient {
        /**
         * Gets the properties of the specified webhook.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         * @return the Webhook object if successful
         */
        Webhook get(String resourceGroupName, String registryName, String webhookName);

        /**
         * Gets the properties of the specified webhook.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         * @return a representation of the future computation of this call, returning the Webhook object
         */
        Mono<Webhook> getAsync(String resourceGroupName, String registryName, String webhookName);

        /**
         * Deletes a webhook from the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         */
        void delete(String resourceGroupName, String registryName, String webhookName);

        /**
         * Deletes a webhook from the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @param webhookName the name of the webhook
         * @return a representation of the future computation of this call
         */
        Mono<Void> deleteAsync(String resourceGroupName, String registryName, String webhookName);

        /**
         * Lists all the webhooks for the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @return the list of all the webhooks for the specified container registry
         */
        PagedIterable<Webhook> list(String resourceGroupName, String registryName);

        /**
         * Lists all the webhooks for the container registry.
         *
         * @param resourceGroupName the resource group name
         * @param registryName the registry name
         * @return a representation of the future computation of this call, returning the list of all the webhooks for
         *     the specified container registry
         */
        PagedFlux<Webhook> listAsync(String resourceGroupName, String registryName);
    }
}
