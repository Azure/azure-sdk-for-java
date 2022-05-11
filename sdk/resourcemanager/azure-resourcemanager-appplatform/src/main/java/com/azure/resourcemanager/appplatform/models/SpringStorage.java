// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.StorageResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** An immutable client-side representation of an Azure Spring Storage. */
@Fluent
public interface SpringStorage
    extends ExternalChildResource<SpringStorage, SpringService>,
    HasManager<AppPlatformManager>,
    HasInnerModel<StorageResourceInner>,
    Updatable<SpringStorage.Update> {
    /** @return account name of Azure Storage Account */
    String storageAccountName();

    /**
     * Container interface for all the definitions that need to be implemented.
     * @param <T> the return type of the final stage,
     *            usually {@link DefinitionStages.WithCreate}
     */
    interface Definition<T>
        extends DefinitionStages.Blank<T>,
        DefinitionStages.WithStorageAccount<T>,
        DefinitionStages.WithStorageAccountKey<T>,
        DefinitionStages.WithCreate<T> { }

    /** Grouping of all the spring storage definition stages. */
    interface DefinitionStages<T> {
        /** The first stage of the spring storage definition. */
        interface Blank<T> extends WithStorageAccount<T> { }

        /** The stage of a spring storage definition allowing to specify an Azure Storage Account. */
        interface WithStorageAccount<T> {
            /**
             * Specifies an existing Azure Storage Account by its name.
             * @param accountName name of the Azure Storage Account
             * @return the next stage of spring storage definition
             */
            WithStorageAccountKey<T> withExistingStorageAccount(String accountName);
        }

        /** The stage of a spring storage definition allowing to specify an Azure Storage Account key. */
        interface WithStorageAccountKey<T> {
            /**
             * Specifies an Azure Storage Account key to associate with the spring storage.
             * @param accountKey the Azure storage account key
             * @return the next stage of spring storage definition
             */
            T withAccountKey(String accountKey);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface Final<T>
            extends WithStorageAccountKey<T> { }

        /** The final stage of the definition allowing to create a spring storage */
        interface WithCreate<T>
            extends Creatable<SpringStorage>,
            Final<T> { }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<SpringStorage>,
        UpdateStages.WithStorageAccountKey { }

    /** Grouping of storage update stages. */
    interface UpdateStages {
        /** The stage of a spring storage update allowing to specify storage settings. */
        interface WithStorageAccountKey {
            /**
             * Specifies an Azure Storage Account key of the storage.
             * @param key azure storage account key
             * @return the next stage of storage definition
             */
            Update withAccountKey(String key);
        }
    }
}
