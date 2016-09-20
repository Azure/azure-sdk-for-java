/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.batch.implementation.AccountResourceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

/**
 * An immutable client-side representation of an Azure batch account.
 */
@LangDefinition(ContainerName = "~/")
public interface BatchAccount extends
        GroupableResource,
        Refreshable<BatchAccount>,
        Updatable<BatchAccount.Update>,
        Wrapper<BatchAccountInner> {

    /**
     * @return the provisioned state of the resource. Possible values include:
     * 'Invalid', 'Creating', 'Deleting', 'Succeeded', 'Failed', 'Cancelled'
     */
    ProvisioningState provisioningState();

    /**
     * @return Get the accountEndpoint value.
     */
    String accountEndpoint();

    /**
     * @return the properties and status of any auto storage account associated with
     * the account
     */
    AutoStorageProperties autoStorage();

    /**
     * @return the core quota for this BatchAccount account
     */
    int coreQuota();

    /**
     * @return the pool quota for this BatchAccount account
     */
    int poolQuota();

    /**
     * @return the active job and job schedule quota for this BatchAccount account
     */
    int activeJobAndJobScheduleQuota();

    /**
     * @return the access keys for this batch account
     */
    @LangMethodDefinition(AsType = LangMethodType.Method)
    BatchAccountKeys keys();

    /**
     * @return the access keys for this batch account
     */
    @LangMethodDefinition(AsType = LangMethodType.Method)
    BatchAccountKeys refreshKeys();

    /**
     * Regenerates the access keys for batch account.
     *
     * @param keyType either primary or secondary key to be regenerated
     * @return the access keys for this batch account
     */
    @LangMethodDefinition(AsType = LangMethodType.Method)
    BatchAccountKeys regenerateKeys(AccountKeyType keyType);

    /**
     * Synchronize the storage account keys for batch account.
     */
    void synchronizeAutoStorageKeys();

    /**************************************************************
     * Fluent interfaces to provision a BatchAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    @LangDefinition(ContainerName = "~/BatchAccount.Definition")
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    @LangDefinition(ContainerName = "~/BatchAccount.Definition", ContainerFileName = "IDefinition", IsContainerOnly = true)
    interface DefinitionStages {
        /**
         * The first stage of the batch account definition.
         */
        interface Blank extends Resource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * A batch account definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * A batch account definition with sufficient inputs to create a new
         * batch account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<BatchAccount>,
            Resource.DefinitionWithTags<WithCreate> {

            /**
             * Specifies that an existing storage account to be attached with the batch account.
             *
             * @param storageAccount existing storage account to be used
             * @return the stage representing creatable batch account definition
             */
            DefinitionStages.WithCreate withStorageAccount(StorageAccount storageAccount);

            /**
             * Specifies that a storage account to be attached with the batch account.
             *
             * @param storageAccountCreatable storage account to be created along with and used in batch
             * @return the stage representing creatable batch account definition
             */
            DefinitionStages.WithCreate withNewStorageAccount(Creatable<StorageAccount> storageAccountCreatable);

            /**
             * Specifies that an existing storage account to be attached with the batch account.
             *
             * @param storageAccountName name of new storage account to be created and used in batch account
             * @return the stage representing creatable batch account definition
             */
            DefinitionStages.WithCreate withNewStorageAccount(String storageAccountName);

        }
    }
    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    @LangDefinition(ContainerName = "~/BatchAccount.Update")
    interface Update extends
            Appliable<BatchAccount>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithStorageAccount {
    }

    /**
     * Grouping of all the storage account update stages.
     */
    @LangDefinition(ContainerName = "~/BatchAccount.Update", ContainerFileName = "IUpdate", IsContainerOnly = true)
    interface UpdateStages {
        /**
         * The stage of the batch account update definition allowing to specify storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies that an existing storage account to be attached with the batch account.
             *
             * @param storageAccount existing storage account to be used
             * @return the stage representing updatable batch account definition
             */
            Update withStorageAccount(StorageAccount storageAccount);

            /**
             * Specifies that a storage account to be attached with the batch account.
             *
             * @param storageAccountCreatable storage account to be created along with and used in batch
             * @return the stage representing updatable batch account definition
             */
            Update withNewStorageAccount(Creatable<StorageAccount> storageAccountCreatable);

            /**
             * Specifies that an existing storage account to be attached with the batch account.
             *
             * @param storageAccountName name of new storage account to be created and used in batch account
             * @return the stage representing updatable batch account definition
             */
            Update withNewStorageAccount(String storageAccountName);

            /**
             * Specifies that storage account should be removed from the batch account.
             *
             * @return the stage representing updatable batch account definition
             */
            Update withoutStorageAccount();
        }
    }
}

