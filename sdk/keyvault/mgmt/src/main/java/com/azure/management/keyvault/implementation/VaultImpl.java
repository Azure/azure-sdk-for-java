/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.keyvault.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import com.azure.core.management.CloudException;
import com.azure.management.RestClient;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.keyvault.AccessPolicy;
import com.azure.management.keyvault.AccessPolicyEntry;
import com.azure.management.keyvault.CreateMode;
import com.azure.management.keyvault.IPRule;
import com.azure.management.keyvault.Keys;
import com.azure.management.keyvault.NetworkRuleAction;
import com.azure.management.keyvault.NetworkRuleBypassOptions;
import com.azure.management.keyvault.NetworkRuleSet;
import com.azure.management.keyvault.Secrets;
import com.azure.management.keyvault.Sku;
import com.azure.management.keyvault.SkuName;
import com.azure.management.keyvault.Vault;
import com.azure.management.keyvault.VaultCreateOrUpdateParameters;
import com.azure.management.keyvault.VaultProperties;
import com.azure.management.keyvault.VirtualNetworkRule;
import com.azure.management.keyvault.models.VaultInner;
import com.azure.management.keyvault.models.VaultsInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import reactor.core.publisher.Mono;

/**
 * Implementation for Vault and its parent interfaces.
 */
class VaultImpl extends GroupableResourceImpl<Vault, VaultInner, VaultImpl, KeyVaultManager>
        implements Vault, Vault.Definition, Vault.Update {
    private GraphRbacManager graphRbacManager;
    private List<AccessPolicyImpl> accessPolicies;

    private SecretAsyncClient secretClient;
    private KeyAsyncClient keyClient;
    private RestClient vaultRestClient;

    private Keys keys;
    private Secrets secrets;

    VaultImpl(String key, VaultInner innerObject, KeyVaultManager manager, GraphRbacManager graphRbacManager) {
        super(key, innerObject, manager);
        this.graphRbacManager = graphRbacManager;
        this.accessPolicies = new ArrayList<>();
        if (innerObject != null && innerObject.getProperties() != null
                && innerObject.getProperties().getAccessPolicies() != null) {
            for (AccessPolicyEntry entry : innerObject.getProperties().getAccessPolicies()) {
                this.accessPolicies.add(new AccessPolicyImpl(entry, this));
            }
        }

        vaultRestClient = manager().newRestClientBuilder().buildClient();
        init();
    }

    private void init() {
        if (inner().getProperties().getVaultUri() != null) {
            final String vaultUrl = vaultUri();
            this.secretClient = new SecretClientBuilder()
                    .vaultUrl(vaultUrl)
                    .pipeline(vaultRestClient.getHttpPipeline())
                    .buildAsyncClient();
            this.keyClient = new KeyClientBuilder()
                    .vaultUrl(vaultUrl)
                    .pipeline(vaultRestClient.getHttpPipeline())
                    .buildAsyncClient();
        }
    }

    @Override
    public RestClient vaultRestClient() {
        return vaultRestClient;
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
        if (inner().getProperties() == null) {
            return null;
        }
        return inner().getProperties().getVaultUri();
    }

    @Override
    public String tenantId() {
        if (inner().getProperties() == null) {
            return null;
        }
        if (inner().getProperties().getTenantId() == null) {
            return null;
        }
        return inner().getProperties().getTenantId().toString();
    }

    @Override
    public Sku sku() {
        if (inner().getProperties() == null) {
            return null;
        }
        return inner().getProperties().getSku();
    }

    @Override
    public List<AccessPolicy> accessPolicies() {
        AccessPolicy[] array = new AccessPolicy[accessPolicies.size()];
        return Arrays.asList(accessPolicies.toArray(array));
    }

    @Override
    public boolean enabledForDeployment() {
        if (inner().getProperties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().getProperties().isEnabledForDeployment());
    }

    @Override
    public boolean enabledForDiskEncryption() {
        if (inner().getProperties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().getProperties().isEnabledForDiskEncryption());
    }

    @Override
    public boolean enabledForTemplateDeployment() {
        if (inner().getProperties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().getProperties().isEnabledForTemplateDeployment());
    }

    @Override
    public boolean softDeleteEnabled() {
        if (inner().getProperties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().getProperties().isEnableSoftDelete());
    }

    @Override
    public boolean purgeProtectionEnabled() {
        if (inner().getProperties() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().getProperties().isEnablePurgeProtection());
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
        throw new NoSuchElementException(String.format("Identity %s not found in the access policies.", objectId));
    }

    @Override
    public VaultImpl withDeploymentEnabled() {
        inner().getProperties().setEnabledForDeployment(true);
        return this;
    }

    @Override
    public VaultImpl withDiskEncryptionEnabled() {
        inner().getProperties().setEnabledForDiskEncryption(true);
        return this;
    }

    @Override
    public VaultImpl withTemplateDeploymentEnabled() {
        inner().getProperties().setEnabledForTemplateDeployment(true);
        return this;
    }

    @Override
    public VaultImpl withSoftDeleteEnabled() {
        inner().getProperties().setEnableSoftDelete(true);
        return this;
    }

    @Override
    public VaultImpl withPurgeProtectionEnabled() {
        inner().getProperties().setEnablePurgeProtection(true);
        return this;
    }

    @Override
    public VaultImpl withDeploymentDisabled() {
        inner().getProperties().setEnabledForDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withDiskEncryptionDisabled() {
        inner().getProperties().setEnabledForDiskEncryption(false);
        return this;
    }

    @Override
    public VaultImpl withTemplateDeploymentDisabled() {
        inner().getProperties().setEnabledForTemplateDeployment(false);
        return this;
    }

    @Override
    public VaultImpl withSku(SkuName skuName) {
        if (inner().getProperties() == null) {
            inner().setProperties(new VaultProperties());
        }
        inner().getProperties().setSku(new Sku().setName(skuName));
        return this;
    }

    private Mono<List<AccessPolicy>> populateAccessPolicies() {
        List<Mono<?>> observables = new ArrayList<>();
        for (final AccessPolicyImpl accessPolicy : accessPolicies) {
            if (accessPolicy.objectId() == null) {
                if (accessPolicy.userPrincipalName() != null) {
                    observables.add(
                            graphRbacManager.users().getByNameAsync(accessPolicy.userPrincipalName())
                                    .subscribeOn(SdkContext.getReactorScheduler())
                                    .doOnNext(user -> accessPolicy.forObjectId(user.id()))
                                    .switchIfEmpty(Mono.error(new CloudException(String.format(
                                            "User principal name %s is not found in tenant %s",
                                            accessPolicy.userPrincipalName(), graphRbacManager.tenantId()),
                                            null)))
                    );
                } else if (accessPolicy.servicePrincipalName() != null) {
                    observables.add(
                            graphRbacManager.servicePrincipals().getByNameAsync(accessPolicy.servicePrincipalName())
                                    .subscribeOn(SdkContext.getReactorScheduler())
                                    .doOnNext(sp -> accessPolicy.forObjectId(sp.id()))
                                    .switchIfEmpty(Mono.error(new CloudException(String.format(
                                            "Service principal name %s is not found in tenant %s",
                                            accessPolicy.servicePrincipalName(), graphRbacManager.tenantId()),
                                            null)))
                    );
                } else {
                    throw new IllegalArgumentException("Access policy must specify object ID.");
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
        final VaultsInner client = this.manager().inner().vaults();
        return populateAccessPolicies()
                .then(Mono.defer(() -> {
                    VaultCreateOrUpdateParameters parameters = new VaultCreateOrUpdateParameters();
                    parameters.setLocation(regionName());
                    parameters.setProperties(inner().getProperties());
                    parameters.setTags(inner().getTags());
                    parameters.getProperties().setAccessPolicies(new ArrayList<>());
                    for (AccessPolicy accessPolicy : accessPolicies) {
                        parameters.getProperties().getAccessPolicies().add(accessPolicy.inner());
                    }
                    return client.createOrUpdateAsync(resourceGroupName(), this.name(), parameters);
                })).map(inner -> {
                    this.setInner(inner);
                    init();
                    return this;
                });
    }

    @Override
    protected Mono<VaultInner> getInnerAsync() {
        return this.manager().inner().vaults().getByResourceGroupAsync(resourceGroupName(), this.name());
    }

    @Override
    public CreateMode createMode() {
        return inner().getProperties().getCreateMode();
    }


    @Override
    public NetworkRuleSet networkRuleSet() {
        return inner().getProperties().getNetworkAcls();
    }

    @Override
    public VaultImpl withAccessFromAllNetworks() {
        if (inner().getProperties().getNetworkAcls() == null) {
            inner().getProperties().setNetworkAcls(new NetworkRuleSet());
        }
        inner().getProperties().getNetworkAcls().setDefaultAction(NetworkRuleAction.ALLOW);
        return this;
    }

    @Override
    public VaultImpl withAccessFromSelectedNetworks() {
        if (inner().getProperties().getNetworkAcls() == null) {
            inner().getProperties().setNetworkAcls(new NetworkRuleSet());
        }
        inner().getProperties().getNetworkAcls().setDefaultAction(NetworkRuleAction.DENY);
        return this;
    }

    /**
     * Specifies that access to the storage account should be allowed from the given ip address or ip address range.
     *
     * @param ipAddressOrRange the ip address or ip address range in cidr format
     * @return VaultImpl
     */
    private VaultImpl withAccessAllowedFromIpAddressOrRange(String ipAddressOrRange) {
        NetworkRuleSet networkRuleSet = inner().getProperties().getNetworkAcls();
        if (networkRuleSet.getIpRules() == null) {
            networkRuleSet.setIpRules(new ArrayList<>());
        }
        boolean found = false;
        for (IPRule rule: networkRuleSet.getIpRules()) {
            if (rule.getValue().equalsIgnoreCase(ipAddressOrRange)) {
                found = true;
                break;
            }
        }
        if (!found) {
            networkRuleSet.getIpRules().add(new IPRule()
                    .setValue(ipAddressOrRange));
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
        if (inner().getProperties().getNetworkAcls() == null) {
            inner().getProperties().setNetworkAcls(new NetworkRuleSet());
        }
        inner().getProperties().getNetworkAcls().setBypass(NetworkRuleBypassOptions.AZURE_SERVICES);
        return this;
    }

    @Override
    public VaultImpl withBypass(NetworkRuleBypassOptions bypass) {
        if (inner().getProperties().getNetworkAcls() == null) {
            inner().getProperties().setNetworkAcls(new NetworkRuleSet());
        }
        inner().getProperties().getNetworkAcls().setBypass(bypass);
        return this;
    }

    @Override
    public VaultImpl withDefaultAction(NetworkRuleAction defaultAction) {
        if (inner().getProperties().getNetworkAcls() == null) {
            inner().getProperties().setNetworkAcls(new NetworkRuleSet());
        }
        inner().getProperties().getNetworkAcls().setDefaultAction(defaultAction);
        return this;
    }

    @Override
    public VaultImpl withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules) {
        if (inner().getProperties().getNetworkAcls() == null) {
            inner().getProperties().setNetworkAcls(new NetworkRuleSet());
        }
        inner().getProperties().getNetworkAcls().setVirtualNetworkRules(virtualNetworkRules);
        return this;
    }

}
