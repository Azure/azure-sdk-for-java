// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PrivateEndpointsClient;
import com.azure.resourcemanager.network.fluent.models.PrivateEndpointInner;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateEndpoints;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

public class PrivateEndpointsImpl extends
    TopLevelModifiableResourcesImpl<
            PrivateEndpoint, PrivateEndpointImpl, PrivateEndpointInner, PrivateEndpointsClient, NetworkManager>
    implements PrivateEndpoints {

    public PrivateEndpointsImpl(NetworkManager manager) {
        super(manager.serviceClient().getPrivateEndpoints(), manager);
    }

    @Override
    protected PrivateEndpointImpl wrapModel(String name) {
        return new PrivateEndpointImpl(name, new PrivateEndpointInner(), this.manager());
    }

    @Override
    protected PrivateEndpointImpl wrapModel(PrivateEndpointInner innerModel) {
        return new PrivateEndpointImpl(innerModel.name(), innerModel, this.manager());
    }

    @Override
    public PrivateEndpointImpl define(String name) {
        return wrapModel(name);
    }
}
