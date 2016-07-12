/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.LoadBalancer;
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

    // Setters (fluent)

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
}