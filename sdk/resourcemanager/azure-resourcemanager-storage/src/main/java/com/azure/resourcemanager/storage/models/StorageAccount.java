// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsUpdatingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure storage account. */
@Fluent
public interface StorageAccount
    extends GroupableResource<StorageManager, StorageAccountInner>,
    Refreshable<StorageAccount>,
    Updatable<StorageAccount.Update>,
    SupportsListingPrivateLinkResource,
    SupportsListingPrivateEndpointConnection,
    SupportsUpdatingPrivateEndpointConnection {

    /**
     * @return the status indicating whether the primary and secondary location of the storage account is available or
     *     unavailable. Possible values include: 'Available', 'Unavailable'
     */
    AccountStatuses accountStatuses();

    /** @return the sku of this storage account. */
    StorageAccountSkuType skuType();

    /** @return the kind of the storage account. Possible values are 'Storage', 'BlobStorage'. */
    Kind kind();

    /** @return the creation date and time of the storage account in UTC */
    OffsetDateTime creationTime();

    /** @return the user assigned custom domain assigned to this storage account */
    CustomDomain customDomain();

    /**
     * @return the timestamp of the most recent instance of a failover to the secondary location. Only the most recent
     *     timestamp is retained. This element is not returned if there has never been a failover instance. Only
     *     available if the accountType is StandardGRS or StandardRAGRS
     */
    OffsetDateTime lastGeoFailoverTime();

    /**
     * @return the status of the storage account at the time the operation was called. Possible values include:
     *     'Creating', 'ResolvingDNS', 'Succeeded'
     */
    ProvisioningState provisioningState();

    /**
     * @return the URLs that are used to perform a retrieval of a public blob, queue or table object. Note that
     *     StandardZRS and PremiumLRS accounts only return the blob endpoint
     */
    PublicEndpoints endPoints();

    /** @return the source of the key used for encryption. */
    StorageAccountEncryptionKeySource encryptionKeySource();

    /** @return the encryption statuses indexed by storage service type. */
    Map<StorageService, StorageAccountEncryptionStatus> encryptionStatuses();

    /** @return whether infrastructure encryption for Azure Storage data is enabled. */
    boolean infrastructureEncryptionEnabled();

    /**
     * @return access tier used for billing. Access tier cannot be changed more than once every 7 days (168 hours).
     *     Access tier cannot be set for StandardLRS, StandardGRS, StandardRAGRS, or PremiumLRS account types. Possible
     *     values include: 'Hot', 'Cool'.
     */
    AccessTier accessTier();

    /** @return the Managed Service Identity specific Active Directory tenant ID assigned to the storage account. */
    String systemAssignedManagedServiceIdentityTenantId();

    /**
     * @return the Managed Service Identity specific Active Directory service principal ID assigned to the storage
     *     account.
     */
    String systemAssignedManagedServiceIdentityPrincipalId();

    /**
     * @return true if authenticated application from any network is allowed to access the storage account, false if
     *     only application from whitelisted network (subnet, ip address, ip address range) can access the storage
     *     account.
     */
    boolean isAccessAllowedFromAllNetworks();

    /** @return the list of resource id of virtual network subnet having access to the storage account. */
    List<String> networkSubnetsWithAccess();

    /** @return the list of ip addresses having access to the storage account. */
    List<String> ipAddressesWithAccess();

    /** @return the list of ip address ranges having access to the storage account. */
    List<String> ipAddressRangesWithAccess();

    /**
     * Checks storage log entries can be read from any network.
     *
     * @return true if storage log entries can be read from any network, false otherwise
     */
    boolean canReadLogEntriesFromAnyNetwork();

    /**
     * Checks storage metrics can be read from any network.
     *
     * @return true if storage metrics can be read from any network, false otherwise
     */
    boolean canReadMetricsFromAnyNetwork();

    /**
     * Checks storage account can be accessed from applications running on azure.
     *
     * @return true if storage can be accessed from application running on azure, false otherwise
     */
    boolean canAccessFromAzureServices();

    /**
     * Checks whether Aad Integration is enabled for files on this storage account.
     *
     * @return true if Aad integration is enabled, false otherwise
     */
    boolean isAzureFilesAadIntegrationEnabled();

    /**
     * Checks whether Hns is enabled on this storage account.
     *
     * @return true if Hns is enabled, false otherwise
     */
    boolean isHnsEnabled();

    /**
     * Checks whether large file shares enabled on this storage account.
     *
     * @return true if large file shares is enabled, false otherwise
     */
    boolean isLargeFileSharesEnabled();

    /**
     * @return the minimum TLS version for HTTPS traffic.
     */
    MinimumTlsVersion minimumTlsVersion();

    /**
     * Checks whether storage account only allow HTTPS traffic.
     *
     * @return true if only allow HTTPS traffic, false otherwise
     */
    boolean isHttpsTrafficOnly();

    /**
     * Checks whether blob public access is allowed.
     *
     * @return true if blob public access is allowed, false otherwise
     */
    boolean isBlobPublicAccessAllowed();

    /**
     * Checks whether shared key access is allowed.
     *
     * @return true if shared key access is allowed, false otherwise
     */
    boolean isSharedKeyAccessAllowed();

    /**
     * Fetch the up-to-date access keys from Azure for this storage account.
     *
     * @return the access keys for this storage account
     */
    List<StorageAccountKey> getKeys();

    /**
     * Fetch the up-to-date access keys from Azure for this storage account asynchronously.
     *
     * @return a representation of the deferred computation of this call, returning the access keys
     */
    Mono<List<StorageAccountKey>> getKeysAsync();

    /**
     * Regenerates the access keys for this storage account.
     *
     * @param keyName if the key name
     * @return the generated access keys for this storage account
     */
    List<StorageAccountKey> regenerateKey(String keyName);

    /**
     * Regenerates the access keys for this storage account asynchronously.
     *
     * @param keyName if the key name
     * @return a representation of the deferred computation of this call, returning the regenerated access key
     */
    Mono<List<StorageAccountKey>> regenerateKeyAsync(String keyName);

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithCreate,
            DefinitionStages.WithCreateAndAccessTier {
    }

    /** Grouping of all the storage account definition stages. */
    interface DefinitionStages {
        /** The first stage of the storage account definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of a storage account definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /** The stage of a storage account definition allowing to specify sku. */
        interface WithSku {
            /**
             * Specifies the sku of the storage account.
             *
             * @param sku the sku
             * @return the next stage of storage account definition
             */
            WithCreate withSku(StorageAccountSkuType sku);
        }

        /** The stage of a storage account definition allowing to specify account kind as blob storage. */
        interface WithBlobStorageAccountKind {
            /**
             * Specifies the storage account kind to be "BlobStorage". The access tier is defaulted to be "Hot".
             *
             * @return the next stage of storage account definition
             */
            WithCreateAndAccessTier withBlobStorageAccountKind();
        }

        /** The stage of a storage account definition allowing to specify account kind as general purpose. */
        interface WithGeneralPurposeAccountKind {
            /**
             * Specifies the storage account kind to be "Storage", the kind for general purposes.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withGeneralPurposeAccountKind();

            /**
             * Specifies the storage account kind to be "StorageV2", the kind for general purposes.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withGeneralPurposeAccountKindV2();
        }

        /** The stage of a storage account definition allowing to specify account kind as block blob storage. */
        interface WithBlockBlobStorageAccountKind {
            /**
             * Specifies the storage account kind to be "BlockBlobStorage".
             *
             * @return The next stage of storage account definition.
             */
            WithCreate withBlockBlobStorageAccountKind();
        }

        /** The stage of a storage account definition allowing to specify account kind as file storage. */
        interface WithFileStorageAccountKind {
            /**
             * Specifies the storage account kind to be "FileStorage".
             *
             * @return the next stage of storage account definition.
             */
            WithCreate withFileStorageAccountKind();
        }

        /** The stage of a storage account definition allowing to specify encryption settings. */
        interface WithEncryption {
            /**
             * Enables the infrastructure encryption for double encryption of Azure Storage data.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withInfrastructureEncryption();

            /**
             * Specifies that encryption needs be enabled for blob service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return the next stage of storage account definition
             */
            @Deprecated
            WithCreate withBlobEncryption();

            /**
             * Disables encryption for blob service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return the next stage of storage account definition
             */
            @Deprecated
            WithCreate withoutBlobEncryption();

            /**
             * Specifies that encryption needs be enabled for file service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return the next stage of storage account definition
             */
            @Deprecated
            WithCreate withFileEncryption();

            /**
             * Disables encryption for file service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return he next stage of storage account definition
             */
            @Deprecated
            WithCreate withoutFileEncryption();

            /**
             * Specifies that table service uses an encryption key that is scoped to the account.
             * Customer-managed key can then be enabled for table service.
             *
             * Refer to {@link Update#withEncryptionKeyFromKeyVault(String, String, String)} to enable customer-managed
             * key.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withTableAccountScopedEncryptionKey();

            /**
             * Specifies that queue service uses an encryption key that is scoped to the account.
             * Customer-managed key can then be enabled for queue service.
             *
             * Refer to {@link Update#withEncryptionKeyFromKeyVault(String, String, String)} to enable customer-managed
             * key.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withQueueAccountScopedEncryptionKey();

            /**
             * Specifies the KeyVault key to be used as encryption key.
             *
             * This requires managed service identity on storage account
             * and GET, WRAP_KEY, UNWRAP_KEY access policy on key vault for the managed service identity.
             *
             * @param keyVaultUri the uri to KeyVault
             * @param keyName the KeyVault key name
             * @param keyVersion the KeyVault key version
             * @return the next stage of storage account definition
             */
            WithCreate withEncryptionKeyFromKeyVault(String keyVaultUri, String keyName, String keyVersion);
        }

        /** The stage of a storage account definition allowing to associate custom domain with the account. */
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

        /** The stage of a storage account definition allowing to enable implicit managed service identity (MSI). */
        interface WithManagedServiceIdentity {
            /**
             * Specifies that implicit managed service identity (MSI) needs to be enabled.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withSystemAssignedManagedServiceIdentity();
        }

        /** The stage of storage account definition allowing to restrict access protocol. */
        interface WithAccessTraffic {
            /**
             * Specifies that only https traffic should be allowed to storage account.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withOnlyHttpsTraffic();

            /**
             * Specifies that both http and https traffic should be allowed to storage account.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withHttpAndHttpsTraffic();

            /**
             * Specifies the minimum TLS version for HTTPS traffic.
             *
             * @param minimumTlsVersion the minimum TLS version
             * @return the next stage of storage account definition
             */
            WithCreate withMinimumTlsVersion(MinimumTlsVersion minimumTlsVersion);
        }

        /** The stage of storage account definition allowing to configure blob access. */
        interface WithBlobAccess {
            /**
             * Disables blob public access.
             *
             * Disabling in storage account overrides the public access settings for individual containers.
             *
             * @return the next stage of storage account definition
             */
            WithCreate disableBlobPublicAccess();

            /**
             * Disables shared key access.
             *
             * @return the next stage of storage account definition
             */
            WithCreate disableSharedKeyAccess();
        }

        /** The stage of storage account definition allowing to configure network access settings. */
        interface WithNetworkAccess {
            /**
             * Specifies that by default access to storage account should be allowed from all networks.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withAccessFromAllNetworks();

            /**
             * Specifies that by default access to storage account should be denied from all networks except from those
             * networks specified via {@link WithNetworkAccess#withAccessFromNetworkSubnet(String)} {@link
             * WithNetworkAccess#withAccessFromIpAddress(String)} and {@link
             * WithNetworkAccess#withAccessFromIpAddressRange(String)}.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withAccessFromSelectedNetworks();

            /**
             * Specifies that access to the storage account from the specific virtual network subnet should be allowed.
             *
             * @param subnetId the virtual network subnet id
             * @return the next stage of storage account definition
             */
            WithCreate withAccessFromNetworkSubnet(String subnetId);

            /**
             * Specifies that access to the storage account from the specific ip address should be allowed.
             *
             * @param ipAddress the ip address
             * @return the next stage of storage account definition
             */
            WithCreate withAccessFromIpAddress(String ipAddress);

            /**
             * Specifies that access to the storage account from the specific ip range should be allowed.
             *
             * @param ipAddressCidr the ip address range expressed in cidr format
             * @return the next stage of storage account definition
             */
            WithCreate withAccessFromIpAddressRange(String ipAddressCidr);

            /**
             * Specifies that read access to the storage logging should be allowed from any network.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withReadAccessToLogEntriesFromAnyNetwork();

            /**
             * Specifies that read access to the storage metrics should be allowed from any network.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withReadAccessToMetricsFromAnyNetwork();

            /**
             * Specifies that access to the storage account should be allowed from applications running on Microsoft
             * Azure services.
             *
             * @return the next stage of storage account definition
             */
            WithCreate withAccessFromAzureServices();
        }

        /**
         * The stage of storage account definition allowing to specify whether azure files aad integration will be
         * enabled.
         */
        interface WithAzureFilesAadIntegration {
            /**
             * Specifies whether Azure files aad integration will be enabled or not.
             *
             * @param enabled whether Azure files aad integration will be enabled or not
             * @return the next stage of storage account definition
             */
            WithCreate withAzureFilesAadIntegrationEnabled(boolean enabled);
        }

        /** The stage of storage account definition allowing to specify whether large file shares will be enabled. */
        interface WithLargeFileShares {
            /**
             * Allow large file shares if sets to enabled. It cannot be disabled once it is enabled.
             *
             * @param enabled whether large file shares will be enabled or not
             * @return the next stage of storage account definition
             */
            WithCreate withLargeFileShares(boolean enabled);
        }

        /** The stage of the storage account definition allowing to specify whether Hns is enabled. */
        interface WithHns {
            /**
             * Specifies whether Hns will be enabled or not.
             *
             * @param enabled whether Hns will be enabled or not
             * @return the next stage of storage account definition
             */
            WithCreate withHnsEnabled(boolean enabled);
        }

        /**
         * A storage account definition with sufficient inputs to create a new storage account in the cloud, but
         * exposing additional optional inputs to specify.
         */
        interface WithCreate
            extends Creatable<StorageAccount>,
                DefinitionStages.WithSku,
                DefinitionStages.WithBlobStorageAccountKind,
                DefinitionStages.WithGeneralPurposeAccountKind,
                DefinitionStages.WithBlockBlobStorageAccountKind,
                DefinitionStages.WithFileStorageAccountKind,
                DefinitionStages.WithEncryption,
                DefinitionStages.WithCustomDomain,
                DefinitionStages.WithManagedServiceIdentity,
                DefinitionStages.WithAccessTraffic,
                DefinitionStages.WithNetworkAccess,
                DefinitionStages.WithAzureFilesAadIntegration,
                DefinitionStages.WithLargeFileShares,
                DefinitionStages.WithHns,
                DefinitionStages.WithBlobAccess,
                Resource.DefinitionWithTags<WithCreate> {
        }

        /** The stage of storage account definition allowing to set access tier. */
        interface WithCreateAndAccessTier extends DefinitionStages.WithCreate {
            /**
             * Specifies the access tier used for billing.
             *
             * <p>Access tier cannot be changed more than once every 7 days (168 hours). Access tier cannot be set for
             * StandardLRS, StandardGRS, StandardRAGRS, or PremiumLRS account types.
             *
             * @param accessTier the access tier value
             * @return the next stage of storage account definition
             */
            DefinitionStages.WithCreate withAccessTier(AccessTier accessTier);
        }
    }

    /** Grouping of all the storage account update stages. */
    interface UpdateStages {
        /** The stage of the storage account update allowing to change the sku. */
        interface WithSku {
            /**
             * Specifies the sku of the storage account.
             *
             * @param sku the sku
             * @return the next stage of storage account update
             */
            Update withSku(StorageAccountSkuType sku);
        }

        /** The stage of the storage account update allowing to associate custom domain. */
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
        }

        /** The stage of the storage account update allowing to configure encryption settings. */
        interface WithEncryption {
            /**
             * Enables encryption for blob service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return the next stage of storage account update
             */
            @Deprecated
            Update withBlobEncryption();

            /**
             * Enables encryption for file service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return he next stage of storage account update
             */
            @Deprecated
            Update withFileEncryption();

            /**
             * Disables encryption for blob service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return the next stage of storage account update
             */
            @Deprecated
            Update withoutBlobEncryption();

            /**
             * Disables encryption for file service.
             *
             * @deprecated Azure Storage encryption cannot be disabled.
             * @return he next stage of storage account update
             */
            @Deprecated
            Update withoutFileEncryption();

            /**
             * Specifies the KeyVault key to be used as key for encryption.
             *
             * This requires managed service identity on storage account
             * (via {@link WithManagedServiceIdentity#withSystemAssignedManagedServiceIdentity()}),
             * and GET, WRAP_KEY, UNWRAP_KEY access policy on key vault for the managed service identity.
             *
             * @param keyVaultUri the uri to KeyVault
             * @param keyName the KeyVault key name
             * @param keyVersion the KeyVault key version
             * @return the next stage of storage account update
             */
            Update withEncryptionKeyFromKeyVault(String keyVaultUri, String keyName, String keyVersion);
        }

        /** A blob storage account update stage allowing access tier to be specified. */
        interface WithAccessTier {
            /**
             * Specifies the access tier used for billing.
             *
             * <p>Access tier cannot be changed more than once every 7 days (168 hours). Access tier cannot be set for
             * StandardLRS, StandardGRS, StandardRAGRS, or PremiumLRS account types.
             *
             * @param accessTier the access tier value
             * @return the next stage of storage account update
             */
            Update withAccessTier(AccessTier accessTier);
        }

        /** The stage of the storage account update allowing to enable managed service identity (MSI). */
        interface WithManagedServiceIdentity {
            /**
             * Specifies that implicit managed service identity (MSI) needs to be enabled.
             *
             * @return the next stage of storage account update
             */
            Update withSystemAssignedManagedServiceIdentity();
        }

        /** The stage of the storage account update allowing to specify the protocol to be used to access account. */
        interface WithAccessTraffic {
            /**
             * Specifies that only https traffic should be allowed to storage account.
             *
             * @return the next stage of storage account update
             */
            Update withOnlyHttpsTraffic();

            /**
             * Specifies that both http and https traffic should be allowed to storage account.
             *
             * @return the next stage of storage account update
             */
            Update withHttpAndHttpsTraffic();

            /**
             * Specifies the minimum TLS version for HTTPS traffic.
             *
             * @param minimumTlsVersion the minimum TLS version
             * @return the next stage of storage account update
             */
            Update withMinimumTlsVersion(MinimumTlsVersion minimumTlsVersion);
        }

        /** The stage of storage account update allowing to configure blob access. */
        interface WithBlobAccess {
            /**
             * Allows blob public access, configured by individual containers.
             *
             * @return the next stage of storage account update
             */
            Update enableBlobPublicAccess();

            /**
             * Disables blob public access.
             *
             * Disabling in storage account overrides the public access settings for individual containers.
             *
             * @return the next stage of storage account update
             */
            Update disableBlobPublicAccess();

            /**
             * Allows shared key access.
             *
             * @return the next stage of storage account update
             */
            Update enableSharedKeyAccess();

            /**
             * Disables shared key access.
             *
             * @return the next stage of storage account update
             */
            Update disableSharedKeyAccess();
        }

        /** The stage of storage account update allowing to configure network access. */
        interface WithNetworkAccess {
            /**
             * Specifies that by default access to storage account should be allowed from all networks.
             *
             * @return the next stage of storage account update
             */
            Update withAccessFromAllNetworks();

            /**
             * Specifies that by default access to storage account should be denied from all networks except from those
             * networks specified via {@link WithNetworkAccess#withAccessFromNetworkSubnet(String)}, {@link
             * WithNetworkAccess#withAccessFromIpAddress(String)} and {@link
             * WithNetworkAccess#withAccessFromIpAddressRange(String)}.
             *
             * @return the next stage of storage account update
             */
            Update withAccessFromSelectedNetworks();

            /**
             * Specifies that access to the storage account from a specific virtual network subnet should be allowed.
             *
             * @param subnetId the virtual network subnet id
             * @return the next stage of storage account update
             */
            Update withAccessFromNetworkSubnet(String subnetId);

            /**
             * Specifies that access to the storage account from a specific ip address should be allowed.
             *
             * @param ipAddress the ip address
             * @return the next stage of storage account update
             */
            Update withAccessFromIpAddress(String ipAddress);

            /**
             * Specifies that access to the storage account from a specific ip range should be allowed.
             *
             * @param ipAddressCidr the ip address range expressed in cidr format
             * @return the next stage of storage account update
             */
            Update withAccessFromIpAddressRange(String ipAddressCidr);

            /**
             * Specifies that read access to the storage logging should be allowed from any network.
             *
             * @return the next stage of storage account definition
             */
            Update withReadAccessToLogEntriesFromAnyNetwork();

            /**
             * Specifies that read access to the storage metrics should be allowed from any network.
             *
             * @return the next stage of storage account definition
             */
            Update withReadAccessToMetricsFromAnyNetwork();

            /**
             * Specifies that access to the storage account should be allowed from applications running on Microsoft
             * Azure services.
             *
             * @return the next stage of storage account definition
             */
            Update withAccessFromAzureServices();

            /**
             * Specifies that previously allowed access from specific virtual network subnet should be removed.
             *
             * @param subnetId the virtual network subnet id
             * @return the next stage of storage account update
             */
            Update withoutNetworkSubnetAccess(String subnetId);

            /**
             * Specifies that previously allowed access from specific ip address should be removed.
             *
             * @param ipAddress the ip address
             * @return the next stage of storage account update
             */
            Update withoutIpAddressAccess(String ipAddress);

            /**
             * Specifies that previously allowed access from specific ip range should be removed.
             *
             * @param ipAddressCidr the ip address range expressed in cidr format
             * @return the next stage of storage account update
             */
            Update withoutIpAddressRangeAccess(String ipAddressCidr);

            /**
             * Specifies that previously added read access exception to the storage logging from any network should be
             * removed.
             *
             * @return the next stage of storage account update
             */
            Update withoutReadAccessToLoggingFromAnyNetwork();

            /**
             * Specifies that previously added read access exception to the storage metrics from any network should be
             * removed.
             *
             * @return the next stage of storage account update
             */
            Update withoutReadAccessToMetricsFromAnyNetwork();

            /**
             * Specifies that previously added access exception to the storage account from application running on azure
             * should be removed.
             *
             * @return the next stage of storage account update
             */
            Update withoutAccessFromAzureServices();

            Update withAzureFilesAadIntegrationEnabled(boolean enabled);
        }

        interface WithUpgrade {
            /**
             * Specifies that the storage account should be upgraded to V2 kind.
             *
             * @return the next stage of storage account update
             */
            Update upgradeToGeneralPurposeAccountKindV2();
        }
    }

    /** The template for a storage account update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<StorageAccount>,
            UpdateStages.WithSku,
            UpdateStages.WithCustomDomain,
            UpdateStages.WithEncryption,
            UpdateStages.WithAccessTier,
            UpdateStages.WithManagedServiceIdentity,
            UpdateStages.WithAccessTraffic,
            UpdateStages.WithNetworkAccess,
            UpdateStages.WithUpgrade,
            UpdateStages.WithBlobAccess,
            Resource.UpdateWithTags<Update> {
    }
}
