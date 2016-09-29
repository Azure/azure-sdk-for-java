/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.BatchAccountInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure batch account.
 */
@Fluent
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
    BatchAccountKeys getKeys();

    /**
     * Regenerates the access keys for batch account.
     *
     * @param keyType either primary or secondary key to be regenerated
     * @return the access keys for this batch account
     */
    BatchAccountKeys regenerateKeys(AccountKeyType keyType);

    /**
     * Synchronize the storage account keys for batch account.
     */
    void synchronizeAutoStorageKeys();

    /**
     * @return the application in this batch account.
     */
    Map<String, Application> applications();

    /**************************************************************
     * Fluent interfaces to provision a BatchAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
        DefinitionStages.WithApplicationAndStorage,
        DefinitionStages.WithCreateAndApplication,
        DefinitionStages.WithApplication,
        DefinitionStages.WithStorage {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the batch account definition.
         */
        interface Blank extends Resource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * A batch account definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreateAndApplication> {
        }

        /**
         * A batch account definition allowing defining application and storage account.
         */
        interface WithApplicationAndStorage extends WithStorage, WithApplication {
        }

        /**
         * A batch account definition to allow creation of application.
         */
        interface WithApplication {
            /**
             * First stage to create new application in Batch account.
             *
             * @param applicationId id of the application to create
             * @return next stage to create the Batch account.
             */
            Application.DefinitionStages.Blank<WithApplicationAndStorage> defineNewApplication(String applicationId);
        }

        /**
         * A batch account definition to allow attaching storage accounts.
         */
        interface WithStorage {
            /**
             * Specifies that an existing storage account to be attached with the batch account.
             *
             * @param storageAccount existing storage account to be used
             * @return the stage representing creatable batch account definition
             */
            DefinitionStages.WithCreate withExistingStorageAccount(StorageAccount storageAccount);

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

        /**
         * A batch account definition allowing creation of application and batch account.
         */
        interface WithCreateAndApplication extends
                WithCreate,
                DefinitionStages.WithApplicationAndStorage {
        }

        /**
         * A batch account definition with sufficient inputs to create a new
         * batch account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<BatchAccount>,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }
    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<BatchAccount>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithStorageAccount,
            UpdateStages.WithApplication {
    }

    /**
     * Grouping of all the storage account update stages.
     */
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
            Update withExistingStorageAccount(StorageAccount storageAccount);

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

        /**
         * A batch account definition to allow creation of application.
         */
        interface WithApplication {
            /**
             * Specifies definition of an application to be created in a batch account.
             *
             * @param applicationId the reference name for application
             * @return the stage representing configuration for the extension
             */
            Application.UpdateDefinitionStages.Blank<Update> defineNewApplication(String applicationId);

            /**
             * Begins the description of an update of an existing application of this batch account.
             *
             * @param applicationId the reference name for the application to be updated
             * @return the stage representing updatable application.
             */
            Application.Update updateApplication(String applicationId);

            /**
             * Deletes specified application from the batch account.
             *
             * @param applicationId the reference name for the application to be removed
             * @return the stage representing updatable batch account definition.
             */
            Update withoutApplication(String applicationId);
        }
    }
}

