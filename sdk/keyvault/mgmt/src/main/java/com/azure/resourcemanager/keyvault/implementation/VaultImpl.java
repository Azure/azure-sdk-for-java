// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.VaultsClient;
import com.azure.resourcemanager.keyvault.fluent.inner.VaultInner;
import com.azure.resourcemanager.keyvault.models.AccessPolicy;
import com.azure.resourcemanager.keyvault.models.AccessPolicyEntry;
import com.azure.resourcemanager.keyvault.models.CreateMode;
import com.azure.resourcemanager.keyvault.models.IpRule;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.NetworkRuleAction;
import com.azure.resourcemanager.keyvault.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.models.NetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.Secrets;
import com.azure.resourcemanager.keyvault.models.Sku;
import com.azure.resourcemanager.keyvault.models.SkuFamily;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.keyvault.models.VaultCreateOrUpdateParameters;
import com.azure.resourcemanager.keyvault.models.VaultProperties;
import com.azure.resourcemanager.keyvault.models.VirtualNetworkRule;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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
        if (inner().properties().vaultUri() != null) {
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

    @Override
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
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().vaultUri();
    }

    @Override
    public String tenantId() {
        if (inner().properties() == null) {
            return null;
        }
        if (inner().properties().tenantId() == null) {
            return null;
        }
        return inner().properties().tenantId().toString();
    }

    @Override
    public Sku sku() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().sku();
    }

    @Override
    public List<AccessPolicy> accessPolicies() {
        AccessPolicy[] array = new AccessPolicy[accessPolicies.size()];
        return Arrays.asList(accessPolicies.toArray(array));
    }

    @Override
    public boolean enabledForDeployment() {
        if (inner().properties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().properties().enabledForDeployment());
    }

    @Override
    public boolean enabledForDiskEncryption() {
        if (inner().properties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().properties().enabledForDiskEncryption());
    }

    @Override
    public boolean enabledForTemplateDeployment() {
        if (inner().properties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().properties().enabledForTemplateDeployment());
    }

    @Override
    public boolean softDeleteEnabled() {
        if (inner().properties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().properties().enableSoftDelete());
    }

    @Override
    public boolean purgeProtectionEnabled() {
        if (inner().properties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().properties().enablePurgeProtection());
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
        inner().properties().withEnabledForDeployment(true);
        return this;
    }

    @Override
    public VaultImpl withDiskEncryptionEnabled() {
        inner().properties().withEnabledForDiskEncryption(true);
        return this;
    }

    @Override
    public VaultImpl withTemplateDeploymentEnabled() {
        inner().properties().withEnabledForTemplateDeployment(true);
        return this;
    }

    @Override
    public VaultImpl withSoftDeleteEnabled() {
        inner().properties().withEnableSoftDelete(true);
        return this;
    }

    @Override
    public VaultImpl withPurgeProtectionEnabled() {
        inner().properties().withEnablePurgeProtection(true);
        return this;
    }

    @Override
    public VaultImpl withDeploymentDisabled() {
        inner().properties().withEnabledForDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withDiskEncryptionDisabled() {
        inner().properties().withEnabledForDiskEncryption(false);
        return this;
    }

    @Override
    public VaultImpl withTemplateDeploymentDisabled() {
        inner().properties().withEnabledForTemplateDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withSku(SkuName skuName) {
        if (inner().properties() == null) {
            inner().withProperties(new VaultProperties());
        }
        inner().properties().withSku(new Sku().withName(skuName).withFamily(SkuFamily.A));
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
                                .subscribeOn(SdkContext.getReactorScheduler())
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
                                .subscribeOn(SdkContext.getReactorScheduler())
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
        final VaultsClient client = this.manager().inner().getVaults();
        return populateAccessPolicies()
            .then(
                Mono
                    .defer(
                        () -> {
                            VaultCreateOrUpdateParameters parameters = new VaultCreateOrUpdateParameters();
                            parameters.withLocation(regionName());
                            parameters.withProperties(inner().properties());
                            parameters.withTags(inner().tags());
                            parameters.properties().withAccessPolicies(new ArrayList<>());
                            for (AccessPolicy accessPolicy : accessPolicies) {
                                parameters.properties().accessPolicies().add(accessPolicy.inner());
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
        return this.manager().inner().getVaults().getByResourceGroupAsync(resourceGroupName(), this.name());
    }

    @Override
    public CreateMode createMode() {
        return inner().properties().createMode();
    }

    @Override
    public NetworkRuleSet networkRuleSet() {
        return inner().properties().networkAcls();
    }

    @Override
    public VaultImpl withAccessFromAllNetworks() {
        if (inner().properties().networkAcls() == null) {
            inner().properties().withNetworkAcls(new NetworkRuleSet());
        }
        inner().properties().networkAcls().withDefaultAction(NetworkRuleAction.ALLOW);
        return this;
    }

    @Override
    public VaultImpl withAccessFromSelectedNetworks() {
        if (inner().properties().networkAcls() == null) {
            inner().properties().withNetworkAcls(new NetworkRuleSet());
        }
        inner().properties().networkAcls().withDefaultAction(NetworkRuleAction.DENY);
        return this;
    }

    /**
     * Specifies that access to the storage account should be allowed from the given ip address or ip address range.
     *
     * @param ipAddressOrRange the ip address or ip address range in cidr format
     * @return VaultImpl
     */
    private VaultImpl withAccessAllowedFromIpAddressOrRange(String ipAddressOrRange) {
        NetworkRuleSet networkRuleSet = inner().properties().networkAcls();
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
        if (inner().properties().networkAcls() == null) {
            inner().properties().withNetworkAcls(new NetworkRuleSet());
        }
        inner().properties().networkAcls().withBypass(NetworkRuleBypassOptions.AZURE_SERVICES);
        return this;
    }

    @Override
    public VaultImpl withBypass(NetworkRuleBypassOptions bypass) {
        if (inner().properties().networkAcls() == null) {
            inner().properties().withNetworkAcls(new NetworkRuleSet());
        }
        inner().properties().networkAcls().withBypass(bypass);
        return this;
    }

    @Override
    public VaultImpl withDefaultAction(NetworkRuleAction defaultAction) {
        if (inner().properties().networkAcls() == null) {
            inner().properties().withNetworkAcls(new NetworkRuleSet());
        }
        inner().properties().networkAcls().withDefaultAction(defaultAction);
        return this;
    }

    @Override
    public VaultImpl withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules) {
        if (inner().properties().networkAcls() == null) {
            inner().properties().withNetworkAcls(new NetworkRuleSet());
        }
        inner().properties().networkAcls().withVirtualNetworkRules(virtualNetworkRules);
        return this;
    }
}
