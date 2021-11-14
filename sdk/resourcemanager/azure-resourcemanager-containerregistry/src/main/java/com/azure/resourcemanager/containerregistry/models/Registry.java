// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.models.RegistryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
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
     * @return the state of public network access for the container registry.
     */
    PublicNetworkAccess publicNetworkAccess();

    /**
     * Begins the definition of the task run.
     *
     * @return the first stage of the task run definition.
     */
    RegistryTaskRun.DefinitionStages.BlankFromRegistry scheduleRun();

    /** Container interface for all the definitions related to a registry. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithSku,
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

        /** The stage of the container registry definition allowing to disable public network access. */
        interface WithPublicNetworkAccess {
            /**
             * Disables public network access for the container registry, for private link feature.
             *
             * @return the next stage of the definition
             */
            WithCreate disablePublicNetworkAccess();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<Registry>,
            WithAdminUserEnabled,
            WithWebhook,
            WithPublicNetworkAccess,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Resource.UpdateWithTags<Update>,
            Appliable<Registry>,
            UpdateStages.WithAdminUserEnabled,
            UpdateStages.WithSku,
            UpdateStages.WithWebhook,
            UpdateStages.WithPublicNetworkAccess {
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

        /** The stage of the container registry definition allowing to change public network access. */
        interface WithPublicNetworkAccess {
            /**
             * Enables public network access for the container registry.
             *
             * @return the next stage of the update
             */
            Update enablePublicNetworkAccess();

            /**
             * Disables public network access for the container registry, for private link feature.
             *
             * @return the next stage of the update
             */
            Update disablePublicNetworkAccess();
        }
    }
}
