/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.containerregistry.implementation.RegistryInner;
import com.microsoft.azure.management.containerregistry.implementation.RegistryListCredentialsResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import org.joda.time.DateTime;
import rx.Observable;

/**
 */
@Fluent
@Beta()
public interface Registry extends
    GroupableResource<ContainerRegistryManager, RegistryInner>,
    Refreshable<Registry>,
    Updatable<Registry.Update> {

    /**
     * The SKU of the container registry.
     */
    Sku sku();

    /**
     * The URL that can be used to log into the container registry.
     */
    String loginServer();
    /**
     * The creation date of the container registry in ISO8601 format.
     */
    DateTime creationDate();
    /**
     * The value that indicates whether the admin user is enabled. This value is false by default.
     */
    boolean adminUserEnabled();
    /**
     * The properties of the storage account for the container registry.
     */
    StorageAccountProperties storageAccount();

    /**
     * Lists the login credentials for the specified container registry.
     */
    RegistryListCredentialsResultInner listCredentials();

    /**
     * Lists the login credentials for the specified container registry.
     */
    Observable<RegistryListCredentialsResultInner> listCredentialsAsync();

    /**
     * Regenerates one of the login credentials for the specified container registry.
     */
    RegistryListCredentialsResultInner regenerateCredential(PasswordName passwordName);

    /**
     * Regenerates one of the login credentials for the specified container registry.
     */
    Observable<RegistryListCredentialsResultInner> regenerateCredentialAsync(PasswordName passwordName);

    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithSku,
        DefinitionStages.WithStorageAccount,
        DefinitionStages.WithCreate {
    }

    interface DefinitionStages {

        interface WithSku {
            /**
             * The SKU of a container registry.
             *
             * @return the next stage
             */
            WithStorageAccount withSku(Sku sku);
        }

        interface WithAdminUserEnabled {
            Definition withAdminUserEnabled();
            Definition withoutAdminUserEnabled();
        }

        interface WithStorageAccount {
            /**
             * The parameters of a storage account for the container registry. If specified, the storage account must be in the same physical location as the container registry.
             *
             * @return the next stage
             */
            WithCreate withStorageAccount(String name, String accessKey);
        }

        interface WithCreate extends
            Creatable<Registry>,
            WithAdminUserEnabled {
        }

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
                GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }
    }

    interface Update extends
        UpdateStages.WithAdminUserEnabled{
    }

    interface UpdateStages {

        interface WithAdminUserEnabled {
            Update withAdminUserEnabled();
            Update withoutAdminUserEnabled();
        }
    }

}
