/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.containerregistry.implementation.RegistryInner;
import com.microsoft.azure.management.containerregistry.implementation.RegistryListCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.storage.StorageAccount;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * An immutable client-side representation of an Azure registry.
 */
@Fluent
@Beta(SinceVersion.V1_1_0)
public interface Registry extends
    GroupableResource<ContainerRegistryManager, RegistryInner>,
    Refreshable<Registry>,
    Updatable<Registry.Update> {

    /**
     * @return the SKU of the container registry.
     */
    Sku sku();

    /**
     * @return the URL that can be used to log into the container registry.
     */
    String loginServerUrl();

    /**
     * @return the creation date of the container registry in ISO8601 format.
     */
    DateTime creationDate();

    /**
     * @return the value that indicates whether the admin user is enabled. This value is false by default.
     */
    boolean adminUserEnabled();

    /**
     * @return the name of the storage account for the container registry.
     */
    String storageAccountName();

    /**
     * @return the login credentials for the specified container registry.
     */
    RegistryListCredentials listCredentials();

    /**
     * @return the login credentials for the specified container registry.
     */
    Observable<RegistryListCredentials> listCredentialsAsync();

    /**
     * Regenerates one of the login credentials for the specified container registry.
     * @param passwordName the password name
     * @return the result of the regeneration
     */
    RegistryListCredentials regenerateCredential(PasswordName passwordName);

    /**
     * Regenerates one of the login credentials for the specified container registry.
     * @param passwordName the password name
     * @return the result of the regeneration
     */
    Observable<RegistryListCredentials> regenerateCredentialAsync(PasswordName passwordName);

    /**
     * Container interface for all the definitions related to a registry.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithStorageAccount,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of registry definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a container service definition.
         */
        interface Blank extends
                GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the container service definition allowing to specify the resource group.
         */
        interface WithGroup extends
                GroupableResource.DefinitionStages.WithGroup<WithStorageAccount> {
        }

        /**
         * The stage of the registry definition allowing to enable admin user.
         */
        interface WithAdminUserEnabled {
            /**
             * Enable admin user.
             * @return the next stage of the definition
             */
            WithCreate withRegistryNameAsAdminUser();
        }

        /**
         * The stage of the registry definition allowing to specify the storage account.
         */
        interface WithStorageAccount {
            /**
             * The parameters of a storage account for the container registry.
             * If specified, the storage account must be in the same physical location as the container registry.
             * @param storageAccount the storage account
             * @return the next stage
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);

            /**
             * The parameters for a storage account for the container registry.
             * If specified, the storage account must be in the same physical location as the container registry.
             * @param storageAccountName the name of the storage account
             * @return the next stage
             */
            WithCreate withNewStorageAccount(String storageAccountName);

            /**
             * The parameters for a storage account for the container registry.
             * If specified, the storage account must be in the same physical location as the container registry.
             * @param creatable the storage account to create
             * @return the next stage
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> creatable);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Registry>,
                Resource.DefinitionWithTags<WithCreate>,
                WithAdminUserEnabled {
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Resource.UpdateWithTags<Update>,
            Appliable<Registry>,
            UpdateStages.WithAdminUserEnabled {
    }

    /**
     * Grouping of container service update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the registry update allowing to enable admin user.
         */
        interface WithAdminUserEnabled {
            /**
             * Enable admin user.
             * @return the next stage of the definition
             */
            Update withRegistryNameAsAdminUser();

            /**
             * Disable admin user.
             * @return the next stage of the definition
             */
            Update withoutRegistryNameAsAdminUser();
        }
    }

}
