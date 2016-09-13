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
import com.microsoft.azure.management.storage.implementation.AccountStatuses;
import com.microsoft.azure.management.storage.implementation.StorageAccountInner;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * An immutable client-side representation of an Azure storage account.
 */
public interface StorageAccount extends
        GroupableResource,
        Refreshable<StorageAccount>,
        Updatable<StorageAccount.Update>,
        Wrapper<StorageAccountInner> {

    /**
     * @return the status indicating whether the primary and secondary location of
     * the storage account is available or unavailable. Possible values include:
     * 'Available', 'Unavailable'
     */
    AccountStatuses accountStatuses();

    /**
     * @return the sku of this storage account. Possible names include:
     * 'Standard_LRS', 'Standard_ZRS', 'Standard_GRS', 'Standard_RAGRS',
     * 'Premium_LRS'. Possible tiers include: 'Standard', 'Premium'.
     */
    Sku sku();

    /**
     * @return the kind of the storage account. Possible values are 'Storage',
     * 'BlobStorage'.
     */
    Kind kind();

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
     * @return the encryption settings on the account. If unspecified the account
     * is unencrypted.
     */
    Encryption encryption();

    /**
     * @return access tier used for billing. Access tier cannot be changed more
     * than once every 7 days (168 hours). Access tier cannot be set for
     * StandardLRS, StandardGRS, StandardRAGRS, or PremiumLRS account types.
     * Possible values include: 'Hot', 'Cool'.
     */
    AccessTier accessTier();

    /**
     * @return the access keys for this storage account
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    List<StorageAccountKey> keys() throws CloudException, IOException;

    /**
     * Fetch the up-to-date access keys from Azure for this storage account.
     *
     * @return the access keys for this storage account
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    List<StorageAccountKey> refreshKeys() throws CloudException, IOException;

    /**
     * Regenerates the access keys for this storage account.
     *
     * @param keyName if the key name
     * @return the generated access keys for this storage account
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    List<StorageAccountKey> regenerateKey(String keyName) throws CloudException, IOException;

    /**************************************************************
     * Fluent interfaces to provision a StorageAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
        DefinitionStages.WithCreateAndAccessTier {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the storage account definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * A storage account definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * A storage account definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the sku of the storage account. This used to be called
             * account types.
             *
             * @param skuName the sku
             * @return the next stage of storage account definition
             */
            WithCreate withSku(SkuName skuName);
        }

        /**
         * A storage account definition specifying the account kind to be blob.
         */
        interface WithBlobStorageAccountKind {
            /**
             * Specifies the storage account kind to be "BlobStorage". The access
             * tier is defaulted to be "Hot".
             *
             * @return the next stage of storage account definition
             */
            WithCreateAndAccessTier withBlobStorageAccountKind();
        }

        /**
         * A storage account definition selecting the general purpose account kind.
         */
        interface WithGeneralPurposeAccountKind {
            /**
             * Specifies the storage account kind to be "Storage", the kind for
             * general purposes.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withGeneralPurposeAccountKind();
            }

        /**
         * A storage account definition specifying encryption setting.
         */
        interface WithEncryption {
            /**
             * Specifies the encryption settings on the account. The default
             * setting is unencrypted.
             *
             * @param encryption the encryption setting
             * @return the nest stage of storage account definition
             */
            WithCreate withEncryption(Encryption encryption);
        }

        /**
         * A storage account definition specifying a custom domain to associate with the account.
         */
        interface WithCustomDomain {
            /**
             * Specifies the user domain assigned to the storage account.
             *
             * @param customDomain the user domain assigned to the storage account
             * @return the next stage of storage account definition
             */
            WithCreate withCustomDomain(CustomDomain customDomain);

            /**
             * Specifies the user domain assigned to the storage account.
             *
             * @param name the custom domain name, which is the CNAME source
             * @return the next stage of storage account definition
             */
            WithCreate withCustomDomain(String name);

            /**
             * Specifies the user domain assigned to the storage account.
             *
             * @param name the custom domain name, which is the CNAME source
             * @param useSubDomain whether indirect CName validation is enabled
             * @return the next stage of storage account definition
             */
            WithCreate withCustomDomain(String name, boolean useSubDomain);
        }

        /**
         * A storage account definition with sufficient inputs to create a new
         * storage account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<StorageAccount>,
            DefinitionStages.WithSku,
            DefinitionStages.WithBlobStorageAccountKind,
            DefinitionStages.WithGeneralPurposeAccountKind,
            DefinitionStages.WithEncryption,
            DefinitionStages.WithCustomDomain,
            Resource.DefinitionWithTags<WithCreate> {
        }

        /**
         * A storage account definition allowing access tier to be set.
         */
        interface WithCreateAndAccessTier extends DefinitionStages.WithCreate {
            /**
             * Specifies the access tier used for billing.
             * <p>
             * Access tier cannot be changed more than once every 7 days (168 hours).
             * Access tier cannot be set for StandardLRS, StandardGRS, StandardRAGRS,
             * or PremiumLRS account types.
             *
             * @param accessTier the access tier value
             * @return the next stage of storage account definition
             */
            DefinitionStages.WithCreate withAccessTier(AccessTier accessTier);
        }
    }

    /**
     * Grouping of all the storage account update stages.
     */
    interface UpdateStages {
        /**
         * A storage account update stage allowing to change the parameters.
         */
        interface WithSku {
            /**
             * Specifies the sku of the storage account. This used to be called
             * account types.             *
             * @param skuName the sku
             * @return the next stage of storage account update
             */
            Update withSku(SkuName skuName);
        }

        /**
         * A storage account update stage allowing to change the parameters.
         */
        interface WithCustomDomain {
            /**
             * Specifies the user domain assigned to the storage account.
             *
             * @param customDomain the user domain assigned to the storage account
             * @return the next stage of storage account update
             */
            Update withCustomDomain(CustomDomain customDomain);

            /**
             * Specifies the user domain assigned to the storage account.
             *
             * @param name the custom domain name, which is the CNAME source
             * @return the next stage of storage account update
             */
            Update withCustomDomain(String name);

            /**
             * Specifies the user domain assigned to the storage account.
             *
             * @param name the custom domain name, which is the CNAME source
             * @param useSubDomain whether indirect CName validation is enabled
             * @return the next stage of storage account update
             */
            Update withCustomDomain(String name, boolean useSubDomain);

            /**
             * Clears the existing user domain assigned to the storage account.
             *
             * @return the next stage of storage account update
             */
            Update withoutCustomDomain();
        }

        /**
         * A storage account update allowing encryption to be specified.
         */
        interface WithEncryption {
            /**
             * Specifies the encryption setting on the account.
             * <p>
             * The default setting is unencrypted.
             *
             * @param encryption the encryption setting
             * @return the nest stage of storage account update
             */
            Update withEncryption(Encryption encryption);
        }

        /**
         * A blob storage account update stage allowing access tier to be specified.
         */
        interface WithAccessTier {
            /**
             * Specifies the access tier used for billing.
             * <p>
             * Access tier cannot be changed more than once every 7 days (168 hours).
             * Access tier cannot be set for StandardLRS, StandardGRS, StandardRAGRS,
             * or PremiumLRS account types.
             *
             * @param accessTier the access tier value
             * @return the next stage of storage account update
             */
            Update withAccessTier(AccessTier accessTier);
        }
    }

    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<StorageAccount>,
            UpdateStages.WithSku,
            UpdateStages.WithCustomDomain,
            UpdateStages.WithEncryption,
            UpdateStages.WithAccessTier,
            Resource.UpdateWithTags<Update> {
    }
}

