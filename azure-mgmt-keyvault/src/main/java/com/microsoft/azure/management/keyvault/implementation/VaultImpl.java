/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.ParallelServiceCall;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.AccessPolicyEntry;
import com.microsoft.azure.management.keyvault.Sku;
import com.microsoft.azure.management.keyvault.SkuFamily;
import com.microsoft.azure.management.keyvault.SkuName;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.VaultProperties;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
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

    protected VaultImpl(String key, VaultInner innerObject, VaultsInner client, KeyVaultManager manager, GraphRbacManager graphRbacManager) {
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
    public UUID tenantId() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().tenantId();
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
    public Boolean enabledForDeployment() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().enabledForDeployment();
    }

    @Override
    public Boolean enabledForDiskEncryption() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().enabledForDiskEncryption();
    }

    @Override
    public Boolean enabledForTemplateDeployment() {
        if (inner().properties() == null) {
            return null;
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
            if (entry.objectId().toString().equals(objectId)) {
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
            if (entry.objectId().toString().equals(objectId)) {
                return entry;
            }
        }
        throw new NoSuchElementException(String.format("Identity %s not found in the access policies.", objectId));
    }

    @Override
    public VaultImpl enableDeployment() {
        inner().properties().withEnabledForDeployment(true);
        return this;
    }

    @Override
    public VaultImpl enableDiskEncryption() {
        inner().properties().withEnabledForDiskEncryption(true);
        return this;
    }

    @Override
    public VaultImpl enableTemplateDeployment() {
        inner().properties().withEnabledForTemplateDeployment(true);
        return this;
    }

    @Override
    public VaultImpl disableDeployment() {
        inner().properties().withEnabledForDeployment(false);
        return this;
    }

    @Override
    public VaultImpl disableDiskEncryption() {
        inner().properties().withEnabledForDiskEncryption(false);
        return this;
    }

    @Override
    public VaultImpl disableTemplateDeployment() {
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

    @Override
    public VaultImpl apply() throws Exception {
        return create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<Vault> callback) {
        return createAsync(callback);
    }

    @Override
    public ServiceCall createResourceAsync(final ServiceCallback<Resource> serviceCallback) {
        final ServiceCall serviceCall = new ServiceCall(null);
        final VaultImpl self = this;
        serviceCall.newCall(populateAccessPolicies(new ServiceCallback<Object>() {
            @Override
            public void failure(Throwable t) {
                serviceCallback.failure(t);
                serviceCall.failure(t);
            }

            @Override
            public void success(ServiceResponse<Object> result) {
                VaultCreateOrUpdateParametersInner parameters = new VaultCreateOrUpdateParametersInner();
                parameters.withLocation(regionName());
                parameters.withProperties(inner().properties());
                parameters.withTags(inner().getTags());
                serviceCall.newCall(client.createOrUpdateAsync(resourceGroupName(), name(), parameters, new ServiceCallback<VaultInner>() {
                    @Override
                    public void failure(Throwable t) {
                        serviceCallback.failure(t);
                        serviceCall.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<VaultInner> result) {
                        setInner(result.getBody());
                        ServiceResponse<Resource> clientResponse = new ServiceResponse<Resource>(self, result.getResponse());
                        serviceCallback.success(clientResponse);
                        serviceCall.success(clientResponse);
                    }
                }).getCall());
            }
        }).getCall());
        return serviceCall;
    }

    private ParallelServiceCall populateAccessPolicies(final ServiceCallback<?> callback) {
        final ParallelServiceCall<?> parallelServiceCall = new ParallelServiceCall();
        boolean any = false;
        for (final AccessPolicyImpl accessPolicy : accessPolicies) {
            if (accessPolicy.objectId() == null) {
                any = true;
                if (accessPolicy.userPrincipalName != null) {
                    parallelServiceCall.addCall(graphRbacManager.users().getByUserPrincipalNameAsync(accessPolicy.userPrincipalName, new ServiceCallback<User>() {
                        @Override
                        public void failure(Throwable t) {
                            if (callback != null) {
                                callback.failure(t);
                            }
                            parallelServiceCall.failure(t);
                        }

                        @Override
                        public void success(ServiceResponse<User> result) {
                            if (callback != null) {
                                callback.success(null);
                            }
                            accessPolicy.forUser(result.getBody());
                        }
                    }));
                } else if (accessPolicy.servicePrincipalName != null) {
                    parallelServiceCall.addCall(graphRbacManager.servicePrincipals().getByServicePrincipalNameAsync(accessPolicy.servicePrincipalName, new ServiceCallback<ServicePrincipal>() {
                        @Override
                        public void failure(Throwable t) {
                            if (callback != null) {
                                callback.failure(t);
                            }
                            parallelServiceCall.failure(t);
                        }

                        @Override
                        public void success(ServiceResponse<ServicePrincipal> result) {
                            if (callback != null) {
                                callback.success(null);
                            }
                            accessPolicy.forServicePrincipal(result.getBody());
                        }
                    }));
                } else {
                    throw new IllegalArgumentException("Access policy must specify object ID.");
                }
            }
        }
        if (!any) {
            parallelServiceCall.success(null);
        }
        return parallelServiceCall;
    }

    @Override
    public VaultImpl createResource() throws Exception {
        populateAccessPolicies(null).get();
        VaultCreateOrUpdateParametersInner parameters = new VaultCreateOrUpdateParametersInner();
        parameters.withLocation(regionName());
        parameters.withProperties(inner().properties());
        parameters.withTags(inner().getTags());
        parameters.properties().accessPolicies().clear();
        for (AccessPolicy accessPolicy : accessPolicies) {
            parameters.properties().accessPolicies().add(accessPolicy.inner());
        }
        this.setInner(client.createOrUpdate(resourceGroupName(), name(), parameters).getBody());
        return this;
    }

    @Override
    public VaultImpl refresh() throws Exception {
        setInner(client.get(resourceGroupName(), name()).getBody());
        return this;
    }
}
