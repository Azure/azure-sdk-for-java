/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.FrontendIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Implementation of the LoadBalancer interface.
 */
class LoadBalancerImpl
    extends GroupableResourceImpl<
        LoadBalancer,
        LoadBalancerInner,
        LoadBalancerImpl,
        NetworkManager>
    implements
        LoadBalancer,
        LoadBalancer.Definition,
        LoadBalancer.Update {

    private final LoadBalancersInner innerCollection;

    LoadBalancerImpl(String name,
            final LoadBalancerInner innerModel,
            final LoadBalancersInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
    }

    // Verbs

    @Override
    public LoadBalancerImpl refresh() throws Exception {
        ServiceResponse<LoadBalancerInner> response =
            this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        return this;
    }

    @Override
    public LoadBalancerImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<LoadBalancer> callback) {
        return createAsync(callback);
    }

    // Helpers

    NetworkManager myManager() {
        return super.myManager;
    }

    private FrontendIPConfiguration createFrontendIPConfig(String name) {
        List<FrontendIPConfiguration> frontendIpConfigs = this.inner().frontendIPConfigurations();
        if (frontendIpConfigs == null) {
            frontendIpConfigs = new ArrayList<FrontendIPConfiguration>();
            this.inner().withFrontendIPConfigurations(frontendIpConfigs);
        }

        FrontendIPConfiguration frontendIpConfig = new FrontendIPConfiguration();
        frontendIpConfigs.add(frontendIpConfig);
        if (name == null) {
            name = "frontend" + frontendIpConfigs.size() + 1;
        }
        frontendIpConfig.withName(name);

        return frontendIpConfig;
    }

    // Withers (fluent)

    @Override
    public LoadBalancerImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        return this.withExistingPublicIpAddress(publicIpAddress.id());
    }

    @Override
    public LoadBalancerImpl withExistingPublicIpAddress(String resourceId) {
        FrontendIPConfiguration frontendIpConfig = createFrontendIPConfig(null);
        SubResource pip = new SubResource();
        pip.withId(resourceId);
        frontendIpConfig.withPublicIPAddress(pip);
        return this;
    }

    // Getters

    private void ensureCreationPrerequisites() {
    }

    @Override
    protected void createResource() throws Exception {
        ensureCreationPrerequisites();

        ServiceResponse<LoadBalancerInner> response =
                this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback) {
        ensureCreationPrerequisites();

        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                Utils.fromVoidCallback(this, new ServiceCallback<Void>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<Void> result) {
                        callback.success(result);
                    }
                }));
    }

    @Override
    public List<String> publicIpAddressIds() {
        List<String> publicIpAddressIds = new ArrayList<>();
        if (this.inner().frontendIPConfigurations() != null) {
            for (FrontendIPConfiguration frontEndIpConfig : this.inner().frontendIPConfigurations()) {
                publicIpAddressIds.add(frontEndIpConfig.publicIPAddress().id());
            }
        }
        return Collections.unmodifiableList(publicIpAddressIds);
    }
}