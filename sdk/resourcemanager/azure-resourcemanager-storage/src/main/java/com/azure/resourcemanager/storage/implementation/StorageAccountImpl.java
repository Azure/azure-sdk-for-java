// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpoint;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnectionProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import com.azure.resourcemanager.storage.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.AccountStatuses;
import com.azure.resourcemanager.storage.models.AzureFilesIdentityBasedAuthentication;
import com.azure.resourcemanager.storage.models.CustomDomain;
import com.azure.resourcemanager.storage.models.DirectoryServiceOptions;
import com.azure.resourcemanager.storage.models.Identity;
import com.azure.resourcemanager.storage.models.IdentityType;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.LargeFileSharesState;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.storage.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.PublicEndpoints;
import com.azure.resourcemanager.storage.models.Sku;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountCreateParameters;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionKeySource;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.storage.models.StorageAccountUpdateParameters;
import com.azure.resourcemanager.storage.models.StorageService;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

/** Implementation for {@link StorageAccount}. */
class StorageAccountImpl
    extends GroupableResourceImpl<StorageAccount, StorageAccountInner, StorageAccountImpl, StorageManager>
    implements StorageAccount, StorageAccount.Definition, StorageAccount.Update {

    private final ClientLogger logger = new ClientLogger(getClass());
    private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private StorageAccountCreateParameters createParameters;
    private StorageAccountUpdateParameters updateParameters;
    private StorageNetworkRulesHelper networkRulesHelper;
    private StorageEncryptionHelper encryptionHelper;

    StorageAccountImpl(String name, StorageAccountInner innerModel, final StorageManager storageManager) {
        super(name, innerModel, storageManager);
        this.createParameters = new StorageAccountCreateParameters();
        this.networkRulesHelper = new StorageNetworkRulesHelper(this.createParameters);
        this.encryptionHelper = new StorageEncryptionHelper(this.createParameters);
    }

    @Override
    public AccountStatuses accountStatuses() {
        if (accountStatuses == null) {
            accountStatuses = new AccountStatuses(this.innerModel().statusOfPrimary(), this.innerModel().statusOfSecondary());
        }
        return accountStatuses;
    }

    @Override
    public StorageAccountSkuType skuType() {
        // We deprecated the sku() getter. When we remove it we wanted to rename this
        // 'beta' getter skuType() to sku().
        //
        return StorageAccountSkuType.fromSkuName(this.innerModel().sku().name());
    }

    @Override
    public Kind kind() {
        return innerModel().kind();
    }

    @Override
    public OffsetDateTime creationTime() {
        return this.innerModel().creationTime();
    }

    @Override
    public CustomDomain customDomain() {
        return this.innerModel().customDomain();
    }

    @Override
    public OffsetDateTime lastGeoFailoverTime() {
        return this.innerModel().lastGeoFailoverTime();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public PublicEndpoints endPoints() {
        if (publicEndpoints == null) {
            publicEndpoints = new PublicEndpoints(this.innerModel().primaryEndpoints(), this.innerModel().secondaryEndpoints());
        }
        return publicEndpoints;
    }

    @Override
    public StorageAccountEncryptionKeySource encryptionKeySource() {
        return StorageEncryptionHelper.encryptionKeySource(this.innerModel());
    }

    @Override
    public Map<StorageService, StorageAccountEncryptionStatus> encryptionStatuses() {
        return StorageEncryptionHelper.encryptionStatuses(this.innerModel());
    }

    @Override
    public boolean infrastructureEncryptionEnabled() {
        return this.encryptionHelper.infrastructureEncryptionEnabled();
    }

    @Override
    public AccessTier accessTier() {
        return innerModel().accessTier();
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        if (this.innerModel().identity() == null) {
            return null;
        } else {
            return this.innerModel().identity().tenantId();
        }
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (this.innerModel().identity() == null) {
            return null;
        } else {
            return this.innerModel().identity().principalId();
        }
    }

    @Override
    public boolean isAccessAllowedFromAllNetworks() {
        return StorageNetworkRulesHelper.isAccessAllowedFromAllNetworks(this.innerModel());
    }

    @Override
    public List<String> networkSubnetsWithAccess() {
        return StorageNetworkRulesHelper.networkSubnetsWithAccess(this.innerModel());
    }

    @Override
    public List<String> ipAddressesWithAccess() {
        return StorageNetworkRulesHelper.ipAddressesWithAccess(this.innerModel());
    }

    @Override
    public List<String> ipAddressRangesWithAccess() {
        return StorageNetworkRulesHelper.ipAddressRangesWithAccess(this.innerModel());
    }

    @Override
    public boolean canReadLogEntriesFromAnyNetwork() {
        return StorageNetworkRulesHelper.canReadLogEntriesFromAnyNetwork(this.innerModel());
    }

    @Override
    public boolean canReadMetricsFromAnyNetwork() {
        return StorageNetworkRulesHelper.canReadMetricsFromAnyNetwork(this.innerModel());
    }

    @Override
    public boolean canAccessFromAzureServices() {
        return StorageNetworkRulesHelper.canAccessFromAzureServices(this.innerModel());
    }

    @Override
    public boolean isAzureFilesAadIntegrationEnabled() {
        return this.innerModel().azureFilesIdentityBasedAuthentication() != null
            && this.innerModel().azureFilesIdentityBasedAuthentication().directoryServiceOptions()
                == DirectoryServiceOptions.AADDS;
    }

    @Override
    public boolean isHnsEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().isHnsEnabled());
    }

    @Override
    public boolean isLargeFileSharesEnabled() {
        return this.innerModel().largeFileSharesState() == LargeFileSharesState.ENABLED;
    }

    @Override
    public MinimumTlsVersion minimumTlsVersion() {
        return this.innerModel().minimumTlsVersion();
    }

    @Override
    public boolean isHttpsTrafficOnly() {
        if (this.innerModel().enableHttpsTrafficOnly() == null) {
            return true;
        }
        return this.innerModel().enableHttpsTrafficOnly();
    }

    @Override
    public boolean isBlobPublicAccessAllowed() {
        if (this.innerModel().allowBlobPublicAccess() == null) {
            return true;
        }
        return this.innerModel().allowBlobPublicAccess();
    }

    @Override
    public boolean isSharedKeyAccessAllowed() {
        if (this.innerModel().allowSharedKeyAccess() == null) {
            return true;
        }
        return this.innerModel().allowSharedKeyAccess();
    }

    @Override
    public List<StorageAccountKey> getKeys() {
        return this.getKeysAsync().block();
    }

    @Override
    public Mono<List<StorageAccountKey>> getKeysAsync() {
        return this
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .listKeysAsync(this.resourceGroupName(), this.name())
            .map(storageAccountListKeysResultInner -> storageAccountListKeysResultInner.keys());
    }

    @Override
    public List<StorageAccountKey> regenerateKey(String keyName) {
        return this.regenerateKeyAsync(keyName).block();
    }

    @Override
    public Mono<List<StorageAccountKey>> regenerateKeyAsync(String keyName) {
        return this
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .regenerateKeyAsync(this.resourceGroupName(), this.name(), keyName)
            .map(storageAccountListKeysResultInner -> storageAccountListKeysResultInner.keys());
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        Mono<Response<List<PrivateLinkResource>>> retList = this.manager().serviceClient().getPrivateLinkResources()
            .listByStorageAccountWithResponseAsync(this.resourceGroupName(), this.name())
            .map(response -> new SimpleResponse<>(response, response.getValue().value().stream()
                .map(PrivateLinkResourceImpl::new)
                .collect(Collectors.toList())));

        return PagedConverter.convertListToPagedFlux(retList);
    }

    @Override
    public PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections() {
        return new PagedIterable<>(listPrivateEndpointConnectionsAsync());
    }

    @Override
    public PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync() {
        return PagedConverter.mapPage(this.manager().serviceClient().getPrivateEndpointConnections()
            .listAsync(this.resourceGroupName(), this.name()), PrivateEndpointConnectionImpl::new);
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager().serviceClient().getPrivateEndpointConnections()
            .putWithResponseAsync(this.resourceGroupName(), this.name(), privateEndpointConnectionName,
                null,
                new com.azure.resourcemanager.storage.models.PrivateLinkServiceConnectionState()
                    .withStatus(
                        com.azure.resourcemanager.storage.models.PrivateEndpointServiceConnectionStatus.APPROVED))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager().serviceClient().getPrivateEndpointConnections()
            .putWithResponseAsync(this.resourceGroupName(), this.name(), privateEndpointConnectionName,
                null,
                new PrivateLinkServiceConnectionState()
                    .withStatus(
                        PrivateEndpointServiceConnectionStatus.REJECTED))
            .then();
    }

    @Override
    public Mono<StorageAccount> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                storageAccount -> {
                    StorageAccountImpl impl = (StorageAccountImpl) storageAccount;
                    impl.clearWrapperProperties();
                    return impl;
                });
    }

    @Override
    protected Mono<StorageAccountInner> getInnerAsync() {
        return this.manager().serviceClient().getStorageAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public StorageAccountImpl withSku(StorageAccountSkuType sku) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(sku.name()));
        } else {
            updateParameters.withSku(new Sku().withName(sku.name()));
        }
        return this;
    }

    @Override
    public StorageAccountImpl withBlobStorageAccountKind() {
        createParameters.withKind(Kind.BLOB_STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withGeneralPurposeAccountKind() {
        createParameters.withKind(Kind.STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withGeneralPurposeAccountKindV2() {
        createParameters.withKind(Kind.STORAGE_V2);
        return this;
    }

    @Override
    public StorageAccountImpl withBlockBlobStorageAccountKind() {
        createParameters.withKind(Kind.BLOCK_BLOB_STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withFileStorageAccountKind() {
        createParameters.withKind(Kind.FILE_STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withInfrastructureEncryption() {
        this.encryptionHelper.withInfrastructureEncryption();
        return this;
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
    public StorageAccountImpl withoutBlobEncryption() {
        this.encryptionHelper.withoutBlobEncryption();
        return this;
    }

    @Override
    public StorageAccountImpl withoutFileEncryption() {
        this.encryptionHelper.withoutFileEncryption();
        return this;
    }

    @Override
    public StorageAccountImpl withTableAccountScopedEncryptionKey() {
        this.encryptionHelper.withTableEncryption();
        return this;
    }

    @Override
    public StorageAccountImpl withQueueAccountScopedEncryptionKey() {
        this.encryptionHelper.withQueueEncryption();
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
        this.networkRulesHelper = new StorageNetworkRulesHelper(this.updateParameters, this.innerModel());
        this.encryptionHelper = new StorageEncryptionHelper(this.updateParameters, this.innerModel());
        return super.update();
    }

    @Override
    public StorageAccountImpl withCustomDomain(CustomDomain customDomain) {
        if (isInCreateMode()) {
            createParameters.withCustomDomain(customDomain);
        } else {
            updateParameters.withCustomDomain(customDomain);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withCustomDomain(String name) {
        return withCustomDomain(new CustomDomain().withName(name));
    }

    @Override
    public StorageAccountImpl withCustomDomain(String name, boolean useSubDomain) {
        return withCustomDomain(new CustomDomain().withName(name).withUseSubDomainName(useSubDomain));
    }

    @Override
    public StorageAccountImpl withAccessTier(AccessTier accessTier) {
        if (isInCreateMode()) {
            createParameters.withAccessTier(accessTier);
        } else {
            if (this.innerModel().kind() != Kind.BLOB_STORAGE) {
                throw logger.logExceptionAsError(new UnsupportedOperationException(
                    "Access tier can not be changed for general purpose storage accounts."));
            }
            updateParameters.withAccessTier(accessTier);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withSystemAssignedManagedServiceIdentity() {
        if (this.innerModel().identity() == null) {
            if (isInCreateMode()) {
                createParameters.withIdentity(new Identity().withType(IdentityType.SYSTEM_ASSIGNED));
            } else {
                updateParameters.withIdentity(new Identity().withType(IdentityType.SYSTEM_ASSIGNED));
            }
        }
        return this;
    }

    @Override
    public StorageAccountImpl withOnlyHttpsTraffic() {
        if (isInCreateMode()) {
            createParameters.withEnableHttpsTrafficOnly(true);
        } else {
            updateParameters.withEnableHttpsTrafficOnly(true);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withHttpAndHttpsTraffic() {
        if (isInCreateMode()) {
            createParameters.withEnableHttpsTrafficOnly(false);
        } else {
            updateParameters.withEnableHttpsTrafficOnly(false);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withMinimumTlsVersion(MinimumTlsVersion minimumTlsVersion) {
        if (isInCreateMode()) {
            createParameters.withMinimumTlsVersion(minimumTlsVersion);
        } else {
            updateParameters.withMinimumTlsVersion(minimumTlsVersion);
        }
        return this;
    }

    @Override
    public StorageAccountImpl enableBlobPublicAccess() {
        if (isInCreateMode()) {
            createParameters.withAllowBlobPublicAccess(true);
        } else {
            updateParameters.withAllowBlobPublicAccess(true);
        }
        return this;
    }

    @Override
    public StorageAccountImpl disableBlobPublicAccess() {
        if (isInCreateMode()) {
            createParameters.withAllowBlobPublicAccess(false);
        } else {
            updateParameters.withAllowBlobPublicAccess(false);
        }
        return this;
    }

    @Override
    public StorageAccountImpl enableSharedKeyAccess() {
        if (isInCreateMode()) {
            createParameters.withAllowSharedKeyAccess(true);
        } else {
            updateParameters.withAllowSharedKeyAccess(true);
        }
        return this;
    }

    @Override
    public StorageAccountImpl disableSharedKeyAccess() {
        if (isInCreateMode()) {
            createParameters.withAllowSharedKeyAccess(false);
        } else {
            updateParameters.withAllowSharedKeyAccess(false);
        }
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
        updateParameters.withKind(Kind.STORAGE_V2);
        return this;
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Mono<StorageAccount> createResourceAsync() {
        this.networkRulesHelper.setDefaultActionIfRequired();
        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.innerModel().tags());
        final StorageAccountsClient client = this.manager().serviceClient().getStorageAccounts();
        return this
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .createAsync(this.resourceGroupName(), this.name(), createParameters)
            .flatMap(
                storageAccountInner ->
                    client
                        .getByResourceGroupAsync(resourceGroupName(), this.name())
                        .map(innerToFluentMap(this))
                        .doOnNext(storageAccount -> clearWrapperProperties()));
    }

    @Override
    public Mono<StorageAccount> updateResourceAsync() {
        this.networkRulesHelper.setDefaultActionIfRequired();
        updateParameters.withTags(this.innerModel().tags());
        return this
            .manager()
            .serviceClient()
            .getStorageAccounts()
            .updateAsync(resourceGroupName(), this.name(), updateParameters)
            .map(innerToFluentMap(this))
            .doOnNext(storageAccount -> clearWrapperProperties());
    }

    @Override
    public StorageAccountImpl withAzureFilesAadIntegrationEnabled(boolean enabled) {
        if (isInCreateMode()) {
            if (enabled) {
                this
                    .createParameters
                    .withAzureFilesIdentityBasedAuthentication(
                        new AzureFilesIdentityBasedAuthentication()
                            .withDirectoryServiceOptions(DirectoryServiceOptions.AADDS));
            }
        } else {
            if (this.createParameters.azureFilesIdentityBasedAuthentication() == null) {
                this
                    .createParameters
                    .withAzureFilesIdentityBasedAuthentication(new AzureFilesIdentityBasedAuthentication());
            }
            if (enabled) {
                this
                    .updateParameters
                    .azureFilesIdentityBasedAuthentication()
                    .withDirectoryServiceOptions(DirectoryServiceOptions.AADDS);
            } else {
                this
                    .updateParameters
                    .azureFilesIdentityBasedAuthentication()
                    .withDirectoryServiceOptions(DirectoryServiceOptions.NONE);
            }
        }
        return this;
    }

    @Override
    public StorageAccountImpl withLargeFileShares(boolean enabled) {
        if (isInCreateMode()) {
            if (enabled) {
                this.createParameters.withLargeFileSharesState(LargeFileSharesState.ENABLED);
            } else {
                this.createParameters.withLargeFileSharesState(LargeFileSharesState.DISABLED);
            }
        }
        return this;
    }

    @Override
    public StorageAccountImpl withHnsEnabled(boolean enabled) {
        this.createParameters.withIsHnsEnabled(enabled);
        return this;
    }

    private static final class PrivateLinkResourceImpl implements PrivateLinkResource {
        private final com.azure.resourcemanager.storage.models.PrivateLinkResource innerModel;

        private PrivateLinkResourceImpl(com.azure.resourcemanager.storage.models.PrivateLinkResource innerModel) {
            this.innerModel = innerModel;
        }

        @Override
        public String groupId() {
            return innerModel.groupId();
        }

        @Override
        public List<String> requiredMemberNames() {
            return Collections.unmodifiableList(innerModel.requiredMembers());
        }

        @Override
        public List<String> requiredDnsZoneNames() {
            return Collections.unmodifiableList(innerModel.requiredZoneNames());
        }
    }

    private static final class PrivateEndpointConnectionImpl implements PrivateEndpointConnection {
        private final PrivateEndpointConnectionInner innerModel;

        private final PrivateEndpoint privateEndpoint;
        private final com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState
            privateLinkServiceConnectionState;
        private final PrivateEndpointConnectionProvisioningState provisioningState;

        private PrivateEndpointConnectionImpl(PrivateEndpointConnectionInner innerModel) {
            this.innerModel = innerModel;

            this.privateEndpoint = innerModel.privateEndpoint() == null
                ? null
                : new PrivateEndpoint(innerModel.privateEndpoint().id());
            this.privateLinkServiceConnectionState = innerModel.privateLinkServiceConnectionState() == null
                ? null
                : new com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState(
                innerModel.privateLinkServiceConnectionState().status() == null
                    ? null
                    : com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus
                    .fromString(innerModel.privateLinkServiceConnectionState().status().toString()),
                innerModel.privateLinkServiceConnectionState().description(),
                innerModel.privateLinkServiceConnectionState().actionRequired());
            this.provisioningState = innerModel.provisioningState() == null
                ? null
                : PrivateEndpointConnectionProvisioningState.fromString(innerModel.provisioningState().toString());
        }

        @Override
        public String id() {
            return innerModel.id();
        }

        @Override
        public String name() {
            return innerModel.name();
        }

        @Override
        public String type() {
            return innerModel.type();
        }

        @Override
        public PrivateEndpoint privateEndpoint() {
            return privateEndpoint;
        }

        @Override
        public com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState
            privateLinkServiceConnectionState() {
            return privateLinkServiceConnectionState;
        }

        @Override
        public PrivateEndpointConnectionProvisioningState provisioningState() {
            return provisioningState;
        }
    }
}
