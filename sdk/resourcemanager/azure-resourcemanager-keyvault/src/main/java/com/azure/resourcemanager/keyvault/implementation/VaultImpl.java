// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.VaultsClient;
import com.azure.resourcemanager.keyvault.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.keyvault.fluent.models.VaultInner;
import com.azure.resourcemanager.keyvault.models.AccessPolicy;
import com.azure.resourcemanager.keyvault.models.AccessPolicyEntry;
import com.azure.resourcemanager.keyvault.models.CreateMode;
import com.azure.resourcemanager.keyvault.models.IpRule;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.NetworkRuleAction;
import com.azure.resourcemanager.keyvault.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.models.NetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.keyvault.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.keyvault.models.Secrets;
import com.azure.resourcemanager.keyvault.models.Sku;
import com.azure.resourcemanager.keyvault.models.SkuFamily;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.keyvault.models.VaultCreateOrUpdateParameters;
import com.azure.resourcemanager.keyvault.models.VaultProperties;
import com.azure.resourcemanager.keyvault.models.VirtualNetworkRule;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** Implementation for Vault and its parent interfaces. */
class VaultImpl extends GroupableResourceImpl<Vault, VaultInner, VaultImpl, KeyVaultManager>
    implements Vault, Vault.Definition, Vault.Update {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    private AuthorizationManager authorizationManager;
    private List<AccessPolicyImpl> accessPolicies;

    private SecretAsyncClient secretClient;
    private KeyAsyncClient keyClient;
    private HttpPipeline vaultHttpPipeline;

    private Keys keys;
    private Secrets secrets;

    VaultImpl(String key, VaultInner innerObject, KeyVaultManager manager, AuthorizationManager authorizationManager) {
        super(key, innerObject, manager);
        this.authorizationManager = authorizationManager;
        this.accessPolicies = new ArrayList<>();
        if (innerObject != null
            && innerObject.properties() != null
            && innerObject.properties().accessPolicies() != null) {
            for (AccessPolicyEntry entry : innerObject.properties().accessPolicies()) {
                this.accessPolicies.add(new AccessPolicyImpl(entry, this));
            }
        }

        vaultHttpPipeline = manager().httpPipeline();
        init();
    }

    private void init() {
        if (innerModel().properties().vaultUri() != null) {
            final String vaultUrl = vaultUri();
            this.secretClient =
                new SecretClientBuilder()
                    .vaultUrl(vaultUrl)
                    .pipeline(vaultHttpPipeline)
                    .buildAsyncClient();
            this.keyClient =
                new KeyClientBuilder()
                    .vaultUrl(vaultUrl)
                    .pipeline(vaultHttpPipeline)
                    .buildAsyncClient();
        }
    }

    @Override
    public HttpPipeline vaultHttpPipeline() {
        return vaultHttpPipeline;
    }

    public SecretAsyncClient secretClient() {
        return secretClient;
    }

    @Override
    public KeyAsyncClient keyClient() {
        return keyClient;
    }

    @Override
    public Keys keys() {
        if (keys == null) {
            keys = new KeysImpl(keyClient, this);
        }
        return keys;
    }

    @Override
    public Secrets secrets() {
        if (secrets == null) {
            secrets = new SecretsImpl(secretClient, this);
        }
        return secrets;
    }

    @Override
    public String vaultUri() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().vaultUri();
    }

    @Override
    public String tenantId() {
        if (innerModel().properties() == null) {
            return null;
        }
        if (innerModel().properties().tenantId() == null) {
            return null;
        }
        return innerModel().properties().tenantId().toString();
    }

    @Override
    public Sku sku() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().sku();
    }

    @Override
    public List<AccessPolicy> accessPolicies() {
        AccessPolicy[] array = new AccessPolicy[accessPolicies.size()];
        return Arrays.asList(accessPolicies.toArray(array));
    }

    @Override
    public boolean roleBasedAccessControlEnabled() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enableRbacAuthorization());
    }

    @Override
    public boolean enabledForDeployment() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enabledForDeployment());
    }

    @Override
    public boolean enabledForDiskEncryption() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enabledForDiskEncryption());
    }

    @Override
    public boolean enabledForTemplateDeployment() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enabledForTemplateDeployment());
    }

    @Override
    public boolean softDeleteEnabled() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enableSoftDelete());
    }

    @Override
    public boolean purgeProtectionEnabled() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enablePurgeProtection());
    }

    @Override
    public VaultImpl withEmptyAccessPolicy() {
        this.accessPolicies = new ArrayList<>();
        return this;
    }

    @Override
    public VaultImpl withoutAccessPolicy(String objectId) {
        for (AccessPolicyImpl entry : this.accessPolicies) {
            if (entry.objectId().equals(objectId)) {
                accessPolicies.remove(entry);
                break;
            }
        }
        return this;
    }

    @Override
    public VaultImpl withAccessPolicy(AccessPolicy accessPolicy) {
        accessPolicies.add((AccessPolicyImpl) accessPolicy);
        return this;
    }

    @Override
    public AccessPolicyImpl defineAccessPolicy() {
        return new AccessPolicyImpl(new AccessPolicyEntry(), this);
    }

    @Override
    public VaultImpl withRoleBasedAccessControl() {
        innerModel().properties().withEnableRbacAuthorization(true);
        return this;
    }

    @Override
    public VaultImpl withoutRoleBasedAccessControl() {
        innerModel().properties().withEnableRbacAuthorization(false);
        return this;
    }

    @Override
    public AccessPolicyImpl updateAccessPolicy(String objectId) {
        for (AccessPolicyImpl entry : this.accessPolicies) {
            if (entry.objectId().equals(objectId)) {
                return entry;
            }
        }
        throw logger.logExceptionAsError(
            new NoSuchElementException(String.format("Identity %s not found in the access policies.", objectId)));
    }

    @Override
    public VaultImpl withDeploymentEnabled() {
        innerModel().properties().withEnabledForDeployment(true);
        return this;
    }

    @Override
    public VaultImpl withDiskEncryptionEnabled() {
        innerModel().properties().withEnabledForDiskEncryption(true);
        return this;
    }

    @Override
    public VaultImpl withTemplateDeploymentEnabled() {
        innerModel().properties().withEnabledForTemplateDeployment(true);
        return this;
    }

    @Override
    public VaultImpl withSoftDeleteEnabled() {
        innerModel().properties().withEnableSoftDelete(true);
        return this;
    }

    @Override
    public VaultImpl withPurgeProtectionEnabled() {
        innerModel().properties().withEnablePurgeProtection(true);
        return this;
    }

    @Override
    public VaultImpl withDeploymentDisabled() {
        innerModel().properties().withEnabledForDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withDiskEncryptionDisabled() {
        innerModel().properties().withEnabledForDiskEncryption(false);
        return this;
    }

    @Override
    public VaultImpl withTemplateDeploymentDisabled() {
        innerModel().properties().withEnabledForTemplateDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withSku(SkuName skuName) {
        if (innerModel().properties() == null) {
            innerModel().withProperties(new VaultProperties());
        }
        innerModel().properties().withSku(new Sku().withName(skuName).withFamily(SkuFamily.A));
        return this;
    }

    private Mono<List<AccessPolicy>> populateAccessPolicies() {
        List<Mono<?>> observables = new ArrayList<>();
        for (final AccessPolicyImpl accessPolicy : accessPolicies) {
            if (accessPolicy.objectId() == null) {
                if (accessPolicy.userPrincipalName() != null) {
                    observables
                        .add(
                            authorizationManager
                                .users()
                                .getByNameAsync(accessPolicy.userPrincipalName())
                                .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler())
                                .doOnNext(user -> accessPolicy.forObjectId(user.id()))
                                .switchIfEmpty(
                                    Mono
                                        .error(
                                            new ManagementException(
                                                String
                                                    .format(
                                                        "User principal name %s is not found in tenant %s",
                                                        accessPolicy.userPrincipalName(),
                                                        authorizationManager.tenantId()),
                                                null))));
                } else if (accessPolicy.servicePrincipalName() != null) {
                    observables
                        .add(
                            authorizationManager
                                .servicePrincipals()
                                .getByNameAsync(accessPolicy.servicePrincipalName())
                                .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler())
                                .doOnNext(sp -> accessPolicy.forObjectId(sp.id()))
                                .switchIfEmpty(
                                    Mono
                                        .error(
                                            new ManagementException(
                                                String
                                                    .format(
                                                        "Service principal name %s is not found in tenant %s",
                                                        accessPolicy.servicePrincipalName(),
                                                        authorizationManager.tenantId()),
                                                null))));
                } else {
                    throw logger.logExceptionAsError(
                        new IllegalArgumentException("Access policy must specify object ID."));
                }
            }
        }
        if (observables.isEmpty()) {
            return Mono.just(accessPolicies());
        } else {
            return Mono.zip(observables, args -> accessPolicies());
        }
    }

    @Override
    public Mono<Vault> createResourceAsync() {
        final VaultsClient client = this.manager().serviceClient().getVaults();
        return populateAccessPolicies()
            .then(
                Mono
                    .defer(
                        () -> {
                            VaultCreateOrUpdateParameters parameters = new VaultCreateOrUpdateParameters();
                            parameters.withLocation(regionName());
                            parameters.withProperties(innerModel().properties());
                            parameters.withTags(innerModel().tags());
                            parameters.properties().withAccessPolicies(new ArrayList<>());
                            for (AccessPolicy accessPolicy : accessPolicies) {
                                parameters.properties().accessPolicies().add(accessPolicy.innerModel());
                            }
                            return client.createOrUpdateAsync(resourceGroupName(), this.name(), parameters);
                        }))
            .map(
                inner -> {
                    this.setInner(inner);
                    init();
                    return this;
                });
    }

    @Override
    protected Mono<VaultInner> getInnerAsync() {
        return this.manager().serviceClient().getVaults().getByResourceGroupAsync(resourceGroupName(), this.name());
    }

    @Override
    public CreateMode createMode() {
        return innerModel().properties().createMode();
    }

    @Override
    public NetworkRuleSet networkRuleSet() {
        return innerModel().properties().networkAcls();
    }

    @Override
    public VaultImpl withAccessFromAllNetworks() {
        if (innerModel().properties().networkAcls() == null) {
            innerModel().properties().withNetworkAcls(new NetworkRuleSet());
        }
        innerModel().properties().networkAcls().withDefaultAction(NetworkRuleAction.ALLOW);
        return this;
    }

    @Override
    public VaultImpl withAccessFromSelectedNetworks() {
        if (innerModel().properties().networkAcls() == null) {
            innerModel().properties().withNetworkAcls(new NetworkRuleSet());
        }
        innerModel().properties().networkAcls().withDefaultAction(NetworkRuleAction.DENY);
        return this;
    }

    /**
     * Specifies that access to the storage account should be allowed from the given ip address or ip address range.
     *
     * @param ipAddressOrRange the ip address or ip address range in cidr format
     * @return VaultImpl
     */
    private VaultImpl withAccessAllowedFromIpAddressOrRange(String ipAddressOrRange) {
        NetworkRuleSet networkRuleSet = innerModel().properties().networkAcls();
        if (networkRuleSet.ipRules() == null) {
            networkRuleSet.withIpRules(new ArrayList<>());
        }
        boolean found = false;
        for (IpRule rule : networkRuleSet.ipRules()) {
            if (rule.value().equalsIgnoreCase(ipAddressOrRange)) {
                found = true;
                break;
            }
        }
        if (!found) {
            networkRuleSet.ipRules().add(new IpRule().withValue(ipAddressOrRange));
        }
        return this;
    }

    @Override
    public VaultImpl withAccessFromIpAddress(String ipAddress) {
        return withAccessAllowedFromIpAddressOrRange(ipAddress);
    }

    @Override
    public VaultImpl withAccessFromIpAddressRange(String ipAddressCidr) {
        return withAccessAllowedFromIpAddressOrRange(ipAddressCidr);
    }

    @Override
    public VaultImpl withAccessFromAzureServices() {
        if (innerModel().properties().networkAcls() == null) {
            innerModel().properties().withNetworkAcls(new NetworkRuleSet());
        }
        innerModel().properties().networkAcls().withBypass(NetworkRuleBypassOptions.AZURE_SERVICES);
        return this;
    }

    @Override
    public VaultImpl withBypass(NetworkRuleBypassOptions bypass) {
        if (innerModel().properties().networkAcls() == null) {
            innerModel().properties().withNetworkAcls(new NetworkRuleSet());
        }
        innerModel().properties().networkAcls().withBypass(bypass);
        return this;
    }

    @Override
    public VaultImpl withDefaultAction(NetworkRuleAction defaultAction) {
        if (innerModel().properties().networkAcls() == null) {
            innerModel().properties().withNetworkAcls(new NetworkRuleSet());
        }
        innerModel().properties().networkAcls().withDefaultAction(defaultAction);
        return this;
    }

    @Override
    public VaultImpl withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules) {
        if (innerModel().properties().networkAcls() == null) {
            innerModel().properties().withNetworkAcls(new NetworkRuleSet());
        }
        innerModel().properties().networkAcls().withVirtualNetworkRules(virtualNetworkRules);
        return this;
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        Mono<Response<List<PrivateLinkResource>>> retList = this.manager().serviceClient().getPrivateLinkResources()
            .listByVaultWithResponseAsync(this.resourceGroupName(), this.name())
            .map(response -> new SimpleResponse<>(response, response.getValue().value().stream()
                .map(PrivateLinkResourceImpl::new)
                .collect(Collectors.toList())));

        return PagedConverter.convertListToPagedFlux(retList);
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return manager().serviceClient().getPrivateEndpointConnections().putAsync(
            this.resourceGroupName(), this.name(), privateEndpointConnectionName,
            new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return manager().serviceClient().getPrivateEndpointConnections().putAsync(
            this.resourceGroupName(), this.name(), privateEndpointConnectionName,
            new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.REJECTED)))
            .then();
    }

    private static final class PrivateLinkResourceImpl implements PrivateLinkResource {
        private final com.azure.resourcemanager.keyvault.models.PrivateLinkResource innerModel;

        private PrivateLinkResourceImpl(com.azure.resourcemanager.keyvault.models.PrivateLinkResource innerModel) {
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
}
