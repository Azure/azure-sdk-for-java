// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateEndpointConnection;
import com.azure.resourcemanager.network.models.PrivateLinkServiceConnection;
import com.azure.resourcemanager.network.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.Collections;

class PrivateEndpointConnectionImpl extends
    ChildResourceImpl<PrivateLinkServiceConnection, PrivateEndpointImpl, PrivateEndpoint>
    implements
    PrivateEndpointConnection,
    PrivateEndpointConnection.Definition<PrivateEndpoint.DefinitionStages.WithCreate> {

    PrivateEndpointConnectionImpl(String name, PrivateEndpointImpl parent) {
        super(new PrivateLinkServiceConnection().withName(name), parent);
    }


    PrivateEndpointConnectionImpl(PrivateLinkServiceConnection innerModel, PrivateEndpointImpl parent) {
        super(innerModel, parent);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public PrivateLinkServiceConnectionState state() {
        return this.innerModel().privateLinkServiceConnectionState();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public PrivateEndpointImpl attach() {
        return this.parent().withPrivateEndpointConnection(this);
    }

    @Override
    public PrivateEndpointConnectionImpl withResource(Resource privateLinkServiceResource) {
        this.innerModel().withPrivateLinkServiceId(privateLinkServiceResource.id());
        return this;
    }

    @Override
    public PrivateEndpointConnectionImpl withSubResource(PrivateLinkSubResourceName subResourceName) {
        this.innerModel().withGroupIds(Collections.singletonList(subResourceName.toString()));
        return this;
    }
}
