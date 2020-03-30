/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.storage.AccessTier;
import com.azure.management.storage.AzureFilesIdentityBasedAuthentication;
import com.azure.management.storage.CustomDomain;
import com.azure.management.storage.DirectoryServiceOptions;
import com.azure.management.storage.Encryption;
import com.azure.management.storage.Identity;
import com.azure.management.storage.Kind;
import com.azure.management.storage.LargeFileSharesState;
import com.azure.management.storage.ProvisioningState;
import com.azure.management.storage.PublicEndpoints;
import com.azure.management.storage.Sku;
import com.azure.management.storage.SkuName;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccountCreateParameters;
import com.azure.management.storage.StorageAccountEncryptionKeySource;
import com.azure.management.storage.StorageAccountEncryptionStatus;
import com.azure.management.storage.StorageAccountKey;
import com.azure.management.storage.StorageAccountSkuType;
import com.azure.management.storage.StorageAccountUpdateParameters;
import com.azure.management.storage.StorageService;
import com.azure.management.storage.models.StorageAccountInner;
import com.azure.management.storage.models.StorageAccountsInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation for {@link StorageAccount}.
 */
class StorageAccountImpl
        extends GroupableResourceImpl<
        StorageAccount,
        StorageAccountInner,
        StorageAccountImpl,
        StorageManager>
        implements
        StorageAccount,
        StorageAccount.Definition,
        StorageAccount.Update {

    private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private StorageAccountCreateParameters createParameters;
    private StorageAccountUpdateParameters updateParameters;
    private StorageNetworkRulesHelper networkRulesHelper;
    private StorageEncryptionHelper encryptionHelper;

    StorageAccountImpl(String name,
                       StorageAccountInner innerModel,
                       final StorageManager storageManager) {
        super(name, innerModel, storageManager);
        this.createParameters = new StorageAccountCreateParameters();
        this.networkRulesHelper = new StorageNetworkRulesHelper(this.createParameters);
        this.encryptionHelper = new StorageEncryptionHelper(this.createParameters);
    }

    @Override
    public AccountStatuses accountStatuses() {
        if (accountStatuses == null) {
            accountStatuses = new AccountStatuses(this.inner().getStatusOfPrimary(), this.inner().getStatusOfSecondary());
        }
        return accountStatuses;
    }

    @Override
    public StorageAccountSkuType skuType() {
        // We deprecated the sku() getter. When we remove it we wanted to rename this
        // 'beta' getter skuType() to sku().
        //
        return StorageAccountSkuType.fromSkuName(this.inner().getSku().getName());
    }

    @Override
    public Kind kind() {
        return inner().getKind();
    }

    @Override
    public OffsetDateTime creationTime() {
        return this.inner().getCreationTime();
    }

    @Override
    public CustomDomain customDomain() {
        return this.inner().getCustomDomain();
    }

    @Override
    public OffsetDateTime lastGeoFailoverTime() {
        return this.inner().getLastGeoFailoverTime();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().getProvisioningState();
    }

    @Override
    public PublicEndpoints endPoints() {
        if (publicEndpoints == null) {
            publicEndpoints = new PublicEndpoints(this.inner().getPrimaryEndpoints(), this.inner().getSecondaryEndpoints());
        }
        return publicEndpoints;
    }

    @Override
    @Deprecated
    public Encryption encryption() {
        return inner().getEncryption();
    }

    @Override
    public StorageAccountEncryptionKeySource encryptionKeySource() {
        return StorageEncryptionHelper.encryptionKeySource(this.inner());
    }

    @Override
    public Map<StorageService, StorageAccountEncryptionStatus> encryptionStatuses() {
        return StorageEncryptionHelper.encryptionStatuses(this.inner());
    }

    @Override
    public AccessTier accessTier() {
        return inner().getAccessTier();
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        if (this.inner().getIdentity() == null) {
            return null;
        } else {
            return this.inner().getIdentity().getTenantId();
        }
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (this.inner().getIdentity() == null) {
            return null;
        } else {
            return this.inner().getIdentity().getPrincipalId();
        }
    }

    @Override
    public boolean isAccessAllowedFromAllNetworks() {
        return StorageNetworkRulesHelper.isAccessAllowedFromAllNetworks(this.inner());
    }

    @Override
    public List<String> networkSubnetsWithAccess() {
        return StorageNetworkRulesHelper.networkSubnetsWithAccess(this.inner());
    }

    @Override
    public List<String> ipAddressesWithAccess() {
        return StorageNetworkRulesHelper.ipAddressesWithAccess(this.inner());
    }

    @Override
    public List<String> ipAddressRangesWithAccess() {
        return StorageNetworkRulesHelper.ipAddressRangesWithAccess(this.inner());
    }

    @Override
    public boolean canReadLogEntriesFromAnyNetwork() {
        return StorageNetworkRulesHelper.canReadLogEntriesFromAnyNetwork(this.inner());
    }

    @Override
    public boolean canReadMetricsFromAnyNetwork() {
        return StorageNetworkRulesHelper.canReadMetricsFromAnyNetwork(this.inner());
    }

    @Override
    public boolean canAccessFromAzureServices() {
        return StorageNetworkRulesHelper.canAccessFromAzureServices(this.inner());
    }

    @Override
    public boolean isAzureFilesAadIntegrationEnabled() {
        return this.inner().getAzureFilesIdentityBasedAuthentication() != null
                && this.inner().getAzureFilesIdentityBasedAuthentication().getDirectoryServiceOptions() == DirectoryServiceOptions.AADDS;
    }

    @Override
    public boolean isHnsEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().isHnsEnabled());
    }

    @Override
    public boolean isLargeFileSharesEnabled() {
        return this.inner().getLargeFileSharesState() == LargeFileSharesState.ENABLED;
    }

    @Override
    public List<StorageAccountKey> getKeys() {
        return this.getKeysAsync().block();
    }

    @Override
    public Mono<List<StorageAccountKey>> getKeysAsync() {
        return this.manager().inner().storageAccounts().listKeysAsync(this.resourceGroupName(), this.name())
                .map(storageAccountListKeysResultInner -> storageAccountListKeysResultInner.getKeys());
    }


    @Override
    public List<StorageAccountKey> regenerateKey(String keyName) {
        return this.regenerateKeyAsync(keyName).block();
    }

    @Override
    public Mono<List<StorageAccountKey>> regenerateKeyAsync(String keyName) {
        return this.manager().inner().storageAccounts().regenerateKeyAsync(this.resourceGroupName(), this.name(), keyName)
                .map(storageAccountListKeysResultInner -> storageAccountListKeysResultInner.getKeys());
    }

    @Override
    public Mono<StorageAccount> refreshAsync() {
        return super.refreshAsync().map(storageAccount -> {
            StorageAccountImpl impl = (StorageAccountImpl) storageAccount;
            impl.clearWrapperProperties();
            return impl;
        });
    }

    @Override
    protected Mono<StorageAccountInner> getInnerAsync() {
        return this.manager().inner().storageAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    @Deprecated
    public StorageAccountImpl withSku(SkuName skuName) {
        return withSku(StorageAccountSkuType.fromSkuName(skuName));
    }

    @Override
    public StorageAccountImpl withSku(StorageAccountSkuType sku) {
        if (isInCreateMode()) {
            createParameters.setSku(new Sku().setName(sku.name()));
        } else {
            updateParameters.setSku(new Sku().setName(sku.name()));
        }
        return this;
    }

    @Override
    public StorageAccountImpl withBlobStorageAccountKind() {
        createParameters.setKind(Kind.BLOB_STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withGeneralPurposeAccountKind() {
        createParameters.setKind(Kind.STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withGeneralPurposeAccountKindV2() {
        createParameters.setKind(Kind.STORAGE_V2);
        return this;
    }

    @Override
    public StorageAccountImpl withBlockBlobStorageAccountKind() {
        createParameters.setKind(Kind.BLOCK_BLOB_STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withFileStorageAccountKind() {
        createParameters.setKind(Kind.FILE_STORAGE);
        return this;
    }

    @Override
    @Deprecated
    public StorageAccountImpl withEncryption() {
        return withBlobEncryption();
    }

    @Override
    public StorageAccountImpl withBlobEncryption() {
        this.encryptionHelper.withBlobEncryption();
        return this;
    }

    @Override
    public StorageAccountImpl withFileEncryption() {
        this.encryptionHelper.withFileEncryption();
        return this;
    }

    @Override
    public StorageAccountImpl withEncryptionKeyFromKeyVault(String keyVaultUri, String keyName, String keyVersion) {
        this.encryptionHelper.withEncryptionKeyFromKeyVault(keyVaultUri, keyName, keyVersion);
        return this;
    }

    @Override
    @Deprecated
    public StorageAccountImpl withoutEncryption() {
        return withoutBlobEncryption();
    }

    @Override
    public StorageAccountImpl withoutBlobEncryption() {
        this.encryptionHelper.withoutBlobEncryption();
        return this;
    }

    @Override
    public StorageAccountImpl withoutFileEncryption() {
        this.encryptionHelper.withoutFileEncryption();
        return this;
    }

    private void clearWrapperProperties() {
        accountStatuses = null;
        publicEndpoints = null;
    }

    @Override
    public StorageAccountImpl update() {
        createParameters = null;
        updateParameters = new StorageAccountUpdateParameters();
        this.networkRulesHelper = new StorageNetworkRulesHelper(this.updateParameters, this.inner());
        this.encryptionHelper = new StorageEncryptionHelper(this.updateParameters, this.inner());
        return super.update();
    }

    @Override
    public StorageAccountImpl withCustomDomain(CustomDomain customDomain) {
        if (isInCreateMode()) {
            createParameters.setCustomDomain(customDomain);
        } else {
            updateParameters.setCustomDomain(customDomain);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withCustomDomain(String name) {
        return withCustomDomain(new CustomDomain().setName(name));
    }

    @Override
    public StorageAccountImpl withCustomDomain(String name, boolean useSubDomain) {
        return withCustomDomain(new CustomDomain().setName(name).setUseSubDomainName(useSubDomain));
    }

    @Override
    public StorageAccountImpl withAccessTier(AccessTier accessTier) {
        if (isInCreateMode()) {
            createParameters.setAccessTier(accessTier);
        } else {
            if (this.inner().getKind() != Kind.BLOB_STORAGE) {
                throw new UnsupportedOperationException("Access tier can not be changed for general purpose storage accounts.");
            }
            updateParameters.setAccessTier(accessTier);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withSystemAssignedManagedServiceIdentity() {
        if (this.inner().getIdentity() == null) {
            if (isInCreateMode()) {
                createParameters.setIdentity(new Identity().setType("SystemAssigned"));
            } else {
                updateParameters.setIdentity(new Identity().setType("SystemAssigned"));
            }
        }
        return this;
    }

    @Override
    public StorageAccountImpl withOnlyHttpsTraffic() {
        if (isInCreateMode()) {
            createParameters.setEnableHttpsTrafficOnly(true);
        } else {
            updateParameters.setEnableHttpsTrafficOnly(true);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withHttpAndHttpsTraffic() {
        updateParameters.setEnableHttpsTrafficOnly(false);
        return this;
    }


    @Override
    public StorageAccountImpl withAccessFromAllNetworks() {
        this.networkRulesHelper.withAccessFromAllNetworks();
        return this;
    }

    @Override
    public StorageAccountImpl withAccessFromSelectedNetworks() {
        this.networkRulesHelper.withAccessFromSelectedNetworks();
        return this;
    }

    @Override
    public StorageAccountImpl withAccessFromNetworkSubnet(String subnetId) {
        this.networkRulesHelper.withAccessFromNetworkSubnet(subnetId);
        return this;
    }

    @Override
    public StorageAccountImpl withAccessFromIpAddress(String ipAddress) {
        this.networkRulesHelper.withAccessFromIpAddress(ipAddress);
        return this;
    }

    @Override
    public StorageAccountImpl withAccessFromIpAddressRange(String ipAddressCidr) {
        this.networkRulesHelper.withAccessFromIpAddressRange(ipAddressCidr);
        return this;
    }

    @Override
    public StorageAccountImpl withReadAccessToLogEntriesFromAnyNetwork() {
        this.networkRulesHelper.withReadAccessToLoggingFromAnyNetwork();
        return this;
    }

    @Override
    public StorageAccountImpl withReadAccessToMetricsFromAnyNetwork() {
        this.networkRulesHelper.withReadAccessToMetricsFromAnyNetwork();
        return this;
    }

    @Override
    public StorageAccountImpl withAccessFromAzureServices() {
        this.networkRulesHelper.withAccessAllowedFromAzureServices();
        return this;
    }

    @Override
    public StorageAccountImpl withoutNetworkSubnetAccess(String subnetId) {
        this.networkRulesHelper.withoutNetworkSubnetAccess(subnetId);
        return this;
    }

    @Override
    public StorageAccountImpl withoutIpAddressAccess(String ipAddress) {
        this.networkRulesHelper.withoutIpAddressAccess(ipAddress);
        return this;
    }

    @Override
    public StorageAccountImpl withoutIpAddressRangeAccess(String ipAddressCidr) {
        this.networkRulesHelper.withoutIpAddressRangeAccess(ipAddressCidr);
        return this;
    }

    @Override
    public Update withoutReadAccessToLoggingFromAnyNetwork() {
        this.networkRulesHelper.withoutReadAccessToLoggingFromAnyNetwork();
        return this;
    }

    @Override
    public Update withoutReadAccessToMetricsFromAnyNetwork() {
        this.networkRulesHelper.withoutReadAccessToMetricsFromAnyNetwork();
        return this;
    }

    @Override
    public Update withoutAccessFromAzureServices() {
        this.networkRulesHelper.withoutAccessFromAzureServices();
        return this;
    }

    @Override
    public Update upgradeToGeneralPurposeAccountKindV2() {
        updateParameters.setKind(Kind.STORAGE_V2);
        return this;
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Mono<StorageAccount> createResourceAsync() {
        this.networkRulesHelper.setDefaultActionIfRequired();
        createParameters.setLocation(this.regionName());
        createParameters.setTags(this.inner().getTags());
        final StorageAccountsInner client = this.manager().inner().storageAccounts();
        return this.manager().inner().storageAccounts().createAsync(
                this.resourceGroupName(), this.name(), createParameters)
                .flatMap(storageAccountInner -> client.getByResourceGroupAsync(resourceGroupName(), this.name())
                        .map(innerToFluentMap(this))
                        .doOnNext(storageAccount -> clearWrapperProperties()));
    }

    @Override
    public Mono<StorageAccount> updateResourceAsync() {
        this.networkRulesHelper.setDefaultActionIfRequired();
        updateParameters.setTags(this.inner().getTags());
        return this.manager().inner().storageAccounts().updateAsync(
                resourceGroupName(), this.name(), updateParameters)
                .map(innerToFluentMap(this))
                .doOnNext(storageAccount -> clearWrapperProperties());
    }

    @Override
    public StorageAccountImpl withAzureFilesAadIntegrationEnabled(boolean enabled) {
        if (isInCreateMode()) {
            if (enabled) {
                this.createParameters.setAzureFilesIdentityBasedAuthentication(new AzureFilesIdentityBasedAuthentication().setDirectoryServiceOptions(DirectoryServiceOptions.AADDS));
            }
        } else {
            if (this.createParameters.getAzureFilesIdentityBasedAuthentication() == null) {
                this.createParameters.setAzureFilesIdentityBasedAuthentication(new AzureFilesIdentityBasedAuthentication());
            }
            if (enabled) {
                this.updateParameters.getAzureFilesIdentityBasedAuthentication().setDirectoryServiceOptions(DirectoryServiceOptions.AADDS);
            } else {
                this.updateParameters.getAzureFilesIdentityBasedAuthentication().setDirectoryServiceOptions(DirectoryServiceOptions.NONE);
            }
        }
        return this;
    }

    @Override
    public StorageAccountImpl withLargeFileShares(boolean enabled) {
        if (isInCreateMode()) {
            if (enabled) {
                this.createParameters.setLargeFileSharesState(LargeFileSharesState.ENABLED);
            } else {
                this.createParameters.setLargeFileSharesState(LargeFileSharesState.DISABLED);
            }
        }
        return this;
    }

    @Override
    public StorageAccountImpl withHnsEnabled(boolean enabled) {
        this.createParameters.setIsHnsEnabled(enabled);
        return this;
    }
}