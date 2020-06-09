// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.inner.RegistryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.time.OffsetDateTime;
import java.util.Collection;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure registry. */
@Fluent
public interface Registry
    extends GroupableResource<ContainerRegistryManager, RegistryInner>,
        Refreshable<Registry>,
        Updatable<Registry.Update> {

    /** @return the SKU of the container registry. */
    Sku sku();

    /** @return the URL that can be used to log into the container registry */
    String loginServerUrl();

    /** @return the creation date of the container registry in ISO8601 format */
    OffsetDateTime creationDate();

    /** @return the value that indicates whether the admin user is enabled */
    boolean adminUserEnabled();

    /**
     * @return the name of the storage account for the container registry; 'null' if container register SKU a managed
     *     tier
     */
    String storageAccountName();

    /**
     * @return the ID of the storage account for the container registry; 'null' if container register SKU a managed tier
     */
    String storageAccountId();

    /** @return the login credentials for the specified container registry */
    RegistryCredentials getCredentials();

    /** @return a representation of the future computation of this call */
    Mono<RegistryCredentials> getCredentialsAsync();

    /**
     * Regenerates one of the login credentials for the specified container registry.
     *
     * @param accessKeyType the admin user access key name to regenerate the value for
     * @return the result of the regeneration
     */
    RegistryCredentials regenerateCredential(AccessKeyType accessKeyType);

    /**
     * Regenerates one of the login credentials for the specified container registry.
     *
     * @param accessKeyType the admin user access key name to regenerate the value for
     * @return a representation of the future computation of this call
     */
    Mono<RegistryCredentials> regenerateCredentialAsync(AccessKeyType accessKeyType);

    /**
     * Lists the quota usages for the specified container registry.
     *
     * @return the list of container registry's quota usages
     */
    Collection<RegistryUsage> listQuotaUsages();

    /**
     * Lists the quota usages for the specified container registry.
     *
     * @return a representation of the future computation of this call
     */
    PagedFlux<RegistryUsage> listQuotaUsagesAsync();

    /** @return returns the upload location for the user to be able to upload the source. */
    SourceUploadDefinition getBuildSourceUploadUrl();

    /**
     * Gets the upload location for the user to be able to upload the source asynchronously.
     *
     * @return a representation of the future computation of this call
     */
    Mono<SourceUploadDefinition> getBuildSourceUploadUrlAsync();

    /** @return returns entry point to manage container registry webhooks. */
    WebhookOperations webhooks();

    /**
     * @return returns entry point to manage the builds such as queued quick builds and queued build tasks for the
     *     container registry.
     */
    //    @Beta(SinceVersion.V1_14_0)
    //    QueuedBuildOperations queuedBuilds();

    /** @return returns entry point to manage the build tasks for the container registry. */
    //    @Beta(SinceVersion.V1_14_0)
    //    BuildTaskOperations buildTasks();

    RegistryTaskRun.DefinitionStages.BlankFromRegistry scheduleRun();

    /** Container interface for all the definitions related to a registry. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
            DefinitionStages.WithStorageAccount,
            DefinitionStages.WithCreate {
    }

    /** Grouping of registry definition stages. */
    interface DefinitionStages {
        /** The first stage of a container registry definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the container service definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /** The stage of the registry definition allowing to specify the SKU type. */
        interface WithSku {
            /**
             * Creates a container registry with a 'Classic' SKU type.
             *
             * @return the next stage of the definition
             */
            WithStorageAccount withClassicSku();

            /**
             * Creates a 'managed' registry with a 'Basic' SKU type.
             *
             * @return the next stage of the definition
             */
            WithCreate withBasicSku();

            /**
             * Creates a 'managed' registry with a 'Standard' SKU type.
             *
             * @return the next stage of the definition
             */
            WithCreate withStandardSku();

            /**
             * Creates a 'managed' registry with a 'Premium' SKU type.
             *
             * @return the next stage of the definition
             */
            WithCreate withPremiumSku();
        }

        /** The stage of the registry definition allowing to specify the storage account. */
        interface WithStorageAccount {
            /**
             * The parameters of a storage account for the container registry.
             *
             * <p>If specified, the storage account must be in the same physical location as the container registry.
             *
             * @param storageAccount the storage account
             * @return the next stage
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);

            /**
             * The ID of an existing storage account for the container registry.
             *
             * <p>If specified, the storage account must be in the same physical location as the container registry.
             *
             * @param id the resource ID of the storage account; must be in the same physical location as the container
             *     registry
             * @return the next stage
             */
            WithCreate withExistingStorageAccount(String id);

            /**
             * The parameters for a storage account for the container registry.
             *
             * <p>A new storage account with default setting and specified name will be created.
             *
             * @param storageAccountName the name of the storage account
             * @return the next stage
             */
            WithCreate withNewStorageAccount(String storageAccountName);

            /**
             * The parameters for a storage account for the container registry.
             *
             * <p>If specified, the storage account must be in the same physical location as the container registry.
             *
             * @param creatable the storage account to create
             * @return the next stage
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);
        }

        /** The stage of the registry definition allowing to enable admin user. */
        interface WithAdminUserEnabled {
            /**
             * Enable admin user.
             *
             * @return the next stage of the definition
             */
            WithCreate withRegistryNameAsAdminUser();
        }

        /** The stage of the container registry definition allowing to add or remove a webhook. */
        interface WithWebhook {
            /**
             * Begins the definition of a new webhook to be added to this container registry.
             *
             * @param name the name of the new webhook
             * @return the first stage of the new webhook definition
             */
            Webhook.DefinitionStages.Blank<WithCreate> defineWebhook(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<Registry>, WithAdminUserEnabled, WithWebhook, Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Resource.UpdateWithTags<Update>,
            Appliable<Registry>,
            UpdateStages.WithAdminUserEnabled,
            UpdateStages.WithSku,
            UpdateStages.WithWebhook {
    }

    /** Grouping of container service update stages. */
    interface UpdateStages {
        /** The stage of the registry update allowing to enable admin user. */
        interface WithAdminUserEnabled {
            /**
             * Enable admin user.
             *
             * @return the next stage of the definition
             */
            Update withRegistryNameAsAdminUser();

            /**
             * Disable admin user.
             *
             * @return the next stage of the definition
             */
            Update withoutRegistryNameAsAdminUser();
        }

        /** The stage of the registry definition allowing to specify the SKU type. */
        interface WithSku {
            /**
             * Updates the current container registry to a 'managed' registry with a 'Basic' SKU type.
             *
             * @return the next stage of the definition
             */
            Update withBasicSku();

            /**
             * Updates the current container registry to a 'managed' registry with a 'Standard' SKU type.
             *
             * @return the next stage of the definition
             */
            Update withStandardSku();

            /**
             * Updates the current container registry to a 'managed' registry with a 'Premium' SKU type.
             *
             * @return the next stage of the definition
             */
            Update withPremiumSku();
        }

        /** The stage of the container registry update allowing to add or remove a webhook. */
        interface WithWebhook {
            /**
             * Begins the definition of a new webhook to be added to this container registry.
             *
             * @param name the name of the new webhook
             * @return the first stage of the new webhook definition
             */
            Webhook.UpdateDefinitionStages.Blank<Update> defineWebhook(String name);

            /**
             * Removes a webhook from the container registry.
             *
             * @param name name of the webhook to remove
             * @return the next stage of the container registry update
             */
            Update withoutWebhook(String name);

            /**
             * Begins the description of an update of an existing webhook of this container registry.
             *
             * @param name the name of an existing webhook
             * @return the first stage of the webhook update description
             */
            Webhook.UpdateResourceStages.Blank<Update> updateWebhook(String name);
        }
    }
}
