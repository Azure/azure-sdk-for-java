/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.implementation.StorageAccountKeys;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import com.microsoft.azure.management.storage.implementation.api.CustomDomain;
import com.microsoft.azure.management.storage.implementation.api.ProvisioningState;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountInner;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * An immutable client-side representation of an Azure storage account.
 */
public interface StorageAccount extends
        GroupableResource,
        Refreshable<StorageAccount>,
        Updatable<StorageAccount.Update>,
        Wrapper<StorageAccountInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * @return the status indicating whether the primary and secondary location of
     * the storage account is available or unavailable. Possible values include:
     * 'Available', 'Unavailable'
     */
    AccountStatuses accountStatuses();

    /**
     * @return the type of this storage account. Possible values include:
     * 'Standard_LRS', 'Standard_ZRS', 'Standard_GRS', 'Standard_RAGRS',
     * 'Premium_LRS'
     */
    AccountType accountType();

    /**
     * @return the creation date and time of the storage account in UTC
     */
    DateTime creationTime();

    /**
     * @return the user assigned custom domain assigned to this storage account
     */
    CustomDomain customDomain();

    /**
     * @return the timestamp of the most recent instance of a failover to the
     * secondary location. Only the most recent timestamp is retained. This
     * element is not returned if there has never been a failover instance.
     * Only available if the accountType is StandardGRS or StandardRAGRS
     */
    DateTime lastGeoFailoverTime();

    /**
     * @return the status of the storage account at the time the operation was
     * called. Possible values include: 'Creating', 'ResolvingDNS',
     * 'Succeeded'
     */
    ProvisioningState provisioningState();

    /**
     * @return the URLs that are used to perform a retrieval of a public blob,
     * queue or table object. Note that StandardZRS and PremiumLRS accounts
     * only return the blob endpoint
     */
    PublicEndpoints endPoints();

    /**
     * @return the access keys for this storage account
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    StorageAccountKeys getKeys() throws CloudException, IOException;

    /**
     * Regenerates the access keys for this storage account.
     *
     * @param keyType if the key is primary or secondary
     * @return the generated access keys for this storage account
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    StorageAccountKeys regenerateKey(KeyType keyType) throws CloudException, IOException;

    /**************************************************************
     * Fluent interfaces to provision a StorageAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definitions extends
        DefinitionBlank,
        DefinitionWithGroup,
        DefinitionCreatable {
    }

    /**
     * A storage account definition allowing location to be set.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * A storage account definition allowing resource group to be set.
     */
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionCreatable> {
    }

    /**
     * A storage account definition with sufficient inputs to create a new
     * storage account in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends Creatable<StorageAccount> {
        /**
         * Specifies the type of the storage account. Possible values include:
         * 'Standard_LRS', 'Standard_ZRS', 'Standard_GRS', 'Standard_RAGRS',
         * 'Premium_LRS'.
         *
         * @param accountType the account type
         * @return the next stage of storage account definition
         */
        DefinitionCreatable withAccountType(AccountType accountType);
    }

    /**
     * A deployment update allowing to change the parameters.
     */
    interface UpdateWithAccountType {
        /**
         * Specifies the type of the storage account. Possible values include:
         * 'Standard_LRS', 'Standard_ZRS', 'Standard_GRS', 'Standard_RAGRS',
         * 'Premium_LRS'.
         *
         * @param accountType the account type
         * @return the next stage of storage account update
         */
        Update withAccountType(AccountType accountType);
    }

    /**
     * A deployment update allowing to change the parameters.
     */
    interface UpdateWithCustomDomain {
        /**
         * Specifies the user domain assigned to the storage account. Name is the CNAME source.
         * Only one custom domain is supported per storage account at this time.
         * To clear the existing custom domain, use an empty string for the
         * custom domain name property.
         *
         * @param customDomain the user domain assigned to the storage account
         * @return the next stage of storage account update
         */
        Update withCustomDomain(CustomDomain customDomain);

        /**
         * Specifies the user domain assigned to the storage account. Name is the CNAME source.
         * Only one custom domain is supported per storage account at this time.
         * To clear the existing custom domain, use an empty string for the
         * custom domain name property.
         *
         * @param name the custom domain name, which is the CNAME source
         * @return the next stage of storage account update
         */
        Update withCustomDomain(String name);

        /**
         * Specifies the user domain assigned to the storage account. Name is the CNAME source.
         * Only one custom domain is supported per storage account at this time.
         * To clear the existing custom domain, use an empty string for the
         * custom domain name property.
         *
         * @param name the custom domain name, which is the CNAME source
         * @param useSubDomain whether indirect CName validation is enabled
         * @return the next stage of storage account update
         */
        Update withCustomDomain(String name, boolean useSubDomain);
    }

    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<StorageAccount>,
            UpdateWithAccountType,
            UpdateWithCustomDomain,
            Resource.UpdateWithTags<Update> {
    }
}

