// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.PrivateEndpointInner;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateEndpointConnection;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class PrivateEndpointImpl extends
    GroupableResourceImpl<PrivateEndpoint, PrivateEndpointInner, PrivateEndpointImpl, NetworkManager>
    implements
    PrivateEndpoint,
    PrivateEndpoint.Definition,
    PrivateEndpoint.Update {

    protected PrivateEndpointImpl(String name, PrivateEndpointInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public SubResource subnet() {
        return new SubResource().withId(this.innerModel().subnet().id());
    }

    @Override
    public List<SubResource> networkInterfaces() {
        return this.innerModel().networkInterfaces().stream()
            .map(ni -> new SubResource().withId(ni.id()))
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public Map<String, PrivateEndpointConnection> privateEndpointConnections() {
        Map<String, PrivateEndpointConnection> connections = new HashMap<>();
        if (this.innerModel().privateLinkServiceConnections() != null) {
            connections.putAll(this.innerModel().privateLinkServiceConnections().stream()
                .map(connection -> new PrivateEndpointConnectionImpl(connection, this, false))
                .collect(Collectors.toMap(PrivateEndpointConnectionImpl::name, Function.identity())));
        }
        if (this.innerModel().manualPrivateLinkServiceConnections() != null) {
            connections.putAll(this.innerModel().manualPrivateLinkServiceConnections().stream()
                .map(connection -> new PrivateEndpointConnectionImpl(connection, this, true))
                .collect(Collectors.toMap(PrivateEndpointConnectionImpl::name, Function.identity())));
        }
        return Collections.unmodifiableMap(connections);
    }

    @Override
    public PrivateEndpointImpl withSubnet(Subnet subnet) {
        this.innerModel().withSubnet(subnet.innerModel());
        return this;
    }

    @Override
    public PrivateEndpointConnectionImpl defineConnection(String name) {
        return new PrivateEndpointConnectionImpl(name, this);
    }

    PrivateEndpointImpl withPrivateEndpointConnection(PrivateEndpointConnectionImpl connection) {
        if (connection.isManualApproval()) {
            if (this.innerModel().manualPrivateLinkServiceConnections() == null) {
                this.innerModel().withManualPrivateLinkServiceConnections(new ArrayList<>());
            }
            this.innerModel().manualPrivateLinkServiceConnections().add(connection.innerModel());
        } else {
            if (this.innerModel().privateLinkServiceConnections() == null) {
                this.innerModel().withPrivateLinkServiceConnections(new ArrayList<>());
            }
            this.innerModel().privateLinkServiceConnections().add(connection.innerModel());
        }
        return this;
    }

    @Override
    public Mono<PrivateEndpoint> createResourceAsync() {
        return this.manager().serviceClient().getPrivateEndpoints()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<PrivateEndpointInner> getInnerAsync() {
        return this.manager().serviceClient().getPrivateEndpoints()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }
}
