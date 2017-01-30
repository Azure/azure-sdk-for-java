/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.AccessPolicyEntry;
import com.microsoft.azure.management.keyvault.Sku;
import com.microsoft.azure.management.keyvault.SkuName;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.VaultProperties;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation for Vault and its parent interfaces.
 */
@LangDefinition
class VaultImpl
        extends GroupableResourceImpl<
            Vault,
            VaultInner,
            VaultImpl,
            KeyVaultManager>
        implements
        Vault,
        Vault.Definition,
        Vault.Update {
    private VaultsInner client;
    private GraphRbacManager graphRbacManager;
    private List<AccessPolicyImpl> accessPolicies;

    VaultImpl(String key, VaultInner innerObject, VaultsInner client, KeyVaultManager manager, GraphRbacManager graphRbacManager) {
        super(key, innerObject, manager);
        this.client = client;
        this.graphRbacManager = graphRbacManager;
        this.accessPolicies = new ArrayList<>();
        if (innerObject != null && innerObject.properties() != null && innerObject.properties().accessPolicies() != null) {
            for (AccessPolicyEntry entry : innerObject.properties().accessPolicies()) {
                this.accessPolicies.add(new AccessPolicyImpl(entry, this));
            }
        }
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
        if (inner().properties() == null || inner().properties().enabledForDeployment() == null) {
            return false;
        }
        return inner().properties().enabledForDeployment();
    }

    @Override
    public boolean enabledForDiskEncryption() {
        if (inner().properties() == null || inner().properties().enabledForDiskEncryption() == null) {
            return false;
        }
        return inner().properties().enabledForDiskEncryption();
    }

    @Override
    public boolean enabledForTemplateDeployment() {
        if (inner().properties() == null || inner().properties().enabledForTemplateDeployment()) {
            return false;
        }
        return inner().properties().enabledForTemplateDeployment();
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
        inner().properties().withSku(new Sku().withName(skuName));
        return this;
    }

    private Observable<List<AccessPolicy>> populateAccessPolicies() {
        List<Observable<?>>observables = new ArrayList<>();
        for (final AccessPolicyImpl accessPolicy : accessPolicies) {
            if (accessPolicy.objectId() == null) {
                if (accessPolicy.userPrincipalName() != null) {
                    observables.add(graphRbacManager.users().getByUserPrincipalNameAsync(accessPolicy.userPrincipalName())
                            .subscribeOn(SdkContext.getRxScheduler())
                            .doOnNext(new Action1<User>() {
                                @Override
                                public void call(User user) {
                                    accessPolicy.forObjectId(user.objectId());
                                }
                            }));
                } else if (accessPolicy.servicePrincipalName() != null) {
                    observables.add(graphRbacManager.servicePrincipals().getByServicePrincipalNameAsync(accessPolicy.servicePrincipalName())
                            .subscribeOn(SdkContext.getRxScheduler())
                            .doOnNext(new Action1<ServicePrincipal>() {
                                @Override
                                public void call(ServicePrincipal sp) {
                                    accessPolicy.forObjectId(sp.objectId());
                                }
                            }));
                } else {
                    throw new IllegalArgumentException("Access policy must specify object ID.");
                }
            }
        }
        if (observables.isEmpty()) {
            return Observable.just(accessPolicies());
        } else {
            return Observable.zip(observables, new FuncN<List<AccessPolicy>>() {
                @Override
                public List<AccessPolicy> call(Object... args) {
                    return accessPolicies();
                }
            });
        }
    }

    @Override
    public Observable<Vault> createResourceAsync() {
        return populateAccessPolicies()
                .flatMap(new Func1<Object, Observable<VaultInner>>() {
                    @Override
                    public Observable<VaultInner> call(Object o) {
                        VaultCreateOrUpdateParametersInner parameters = new VaultCreateOrUpdateParametersInner();
                        parameters.withLocation(regionName());
                        parameters.withProperties(inner().properties());
                        parameters.withTags(inner().getTags());
                        parameters.properties().withAccessPolicies(new ArrayList<AccessPolicyEntry>());
                        for (AccessPolicy accessPolicy : accessPolicies) {
                            parameters.properties().accessPolicies().add(accessPolicy.inner());
                        }
                        return client.createOrUpdateAsync(resourceGroupName(), name(), parameters);
                    }
                })
                .map(innerToFluentMap(this));
    }

    @Override
    public VaultImpl refresh() {
        setInner(client.get(resourceGroupName(), name()));
        return this;
    }
}
