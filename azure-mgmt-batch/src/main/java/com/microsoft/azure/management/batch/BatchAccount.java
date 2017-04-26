/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.BatchAccountInner;
import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Batch account.
 */
@Fluent
public interface BatchAccount extends
        GroupableResource<BatchManager, BatchAccountInner>,
        Refreshable<BatchAccount>,
        Updatable<BatchAccount.Update> {

    /**
     * @return the provisioned state of the resource
     */
    ProvisioningState provisioningState();

    /**
     * @return Batch account endpoint
     */
    String accountEndpoint();

    /**
     * @return the properties and status of any auto storage account associated with the Batch account
     */
    AutoStorageProperties autoStorage();

    /**
     * @return the core quota for this Batch account
     */
    int coreQuota();

    /**
     * @return the pool quota for this Batch account
     */
    int poolQuota();

    /**
     * @return the active job and job schedule quota for this Batch account
     */
    int activeJobAndJobScheduleQuota();

    /**
     * @return the access keys for this Batch account
     */
    BatchAccountKeys getKeys();

    /**
     * Regenerates the access keys for the Batch account.
     *
     * @param keyType the type if key to regenerate
     * @return regenerated access keys for this Batch account
     */
    BatchAccountKeys regenerateKeys(AccountKeyType keyType);

    /**
     * Synchronizes the storage account keys for this Batch account.
     */
    void synchronizeAutoStorageKeys();

    /**
     * @return applications in this Batch account, indexed by name
     */
    Map<String, Application> applications();

    /**
     * The entirety of a Batch account definition.
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
     * Grouping of all the Batch account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Batch account definition.
         */
        interface Blank extends Resource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of a Batch account definition allowing the resource group to be specified.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreateAndApplication> {
        }

        /**
         * The stage of a Batch account definition allowing adding an application and a storage account.
         */
        interface WithApplicationAndStorage extends WithStorage, WithApplication {
        }

        /**
         * The stage of a Batch account definition allowing the creation of a Batch application.
         */
        interface WithApplication {
            /**
             * The stage of a Batch account definition allowing to add a Batch application.
             *
             * @param applicationId the id of the application to create
             * @return the next stage of the definition
             */
            Application.DefinitionStages.Blank<WithApplicationAndStorage> defineNewApplication(String applicationId);
        }

        /**
         * The stage of a Batch account definition allowing to associate storage accounts with the Batch account.
         */
        interface WithStorage {
            /**
             * Specifies an existing storage account to associate with the Batch account.
             *
             * @param storageAccount an existing storage account
             * @return the next stage of the definition
             */
            DefinitionStages.WithCreate withExistingStorageAccount(StorageAccount storageAccount);

            /**
             * Specifies a new storage account to associate with the Batch account.
             *
             * @param storageAccountCreatable a storage account to be created along with and used in the Batch account
             * @return the next stage of the definition
             */
            DefinitionStages.WithCreate withNewStorageAccount(Creatable<StorageAccount> storageAccountCreatable);

            /**
             * Specifies the name of a new storage account to be created and associated with this Batch account.
             *
             * @param storageAccountName the name of a new storage account
             * @return the next stage of the definition
             */
            DefinitionStages.WithCreate withNewStorageAccount(String storageAccountName);
        }

        /**
         * The stage of a Batch account definition allowing the adding of a Batch application or creating the Batch account.
         */
        interface WithCreateAndApplication extends
                WithCreate,
                DefinitionStages.WithApplicationAndStorage {
        }

        /**
         * A Batch account definition with sufficient inputs to create a new
         * Batch account in the cloud, but exposing additional optional inputs to specify.
         */
        interface WithCreate extends
            Creatable<BatchAccount>,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for a Batch account update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<BatchAccount>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithStorageAccount,
            UpdateStages.WithApplication {
    }

    /**
     * Grouping of all Batch account update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a Batch account update allowing to specify a storage account.
         */
        interface WithStorageAccount {
            /**
             * Specifies an existing storage account to associate with the Batch account.
             *
             * @param storageAccount an existing storage account
             * @return the next stage of the update
             */
            Update withExistingStorageAccount(StorageAccount storageAccount);

            /**
             * Specifies a new storage account to create and associate with the Batch account.
             *
             * @param storageAccountCreatable the definition of the storage account
             * @return the next stage of the update
             */
            Update withNewStorageAccount(Creatable<StorageAccount> storageAccountCreatable);

            /**
             * Specifies a new storage account to create and associate with the Batch account.
             *
             * @param storageAccountName the name of a new storage account
             * @return the next stage of the update
             */
            Update withNewStorageAccount(String storageAccountName);

            /**
             * Removes the associated storage account.
             *
             * @return the next stage of the update
             */
            Update withoutStorageAccount();
        }

        /**
         * The stage of a Batch account definition allowing the creation of a Batch application.
         */
        interface WithApplication {
            /**
             * Starts a definition of an application to be created in the Batch account.
             *
             * @param applicationId the reference name for the application
             * @return the first stage of a Batch application definition
             */
            Application.UpdateDefinitionStages.Blank<Update> defineNewApplication(String applicationId);

            /**
             * Begins the description of an update of an existing Batch application in this Batch account.
             *
             * @param applicationId the reference name of the application to be updated
             * @return the first stage of a Batch application update
             */
            Application.Update updateApplication(String applicationId);

            /**
             * Removes the specified application from the Batch account.
             *
             * @param applicationId the reference name for the application to be removed
             * @return the next stage of the update
             */
            Update withoutApplication(String applicationId);
        }
    }
}
