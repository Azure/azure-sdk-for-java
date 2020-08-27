// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.resourcemanager.cosmos.fluent.PrivateEndpointConnectionsClient;
import com.azure.resourcemanager.cosmos.fluent.inner.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.PrivateEndpointConnection;
import com.azure.resourcemanager.cosmos.models.PrivateEndpointProperty;
import com.azure.resourcemanager.cosmos.models.PrivateLinkServiceConnectionStateProperty;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

/** A private endpoint connection. */
public class PrivateEndpointConnectionImpl
    extends ExternalChildResourceImpl<
        PrivateEndpointConnection, PrivateEndpointConnectionInner, CosmosDBAccountImpl, CosmosDBAccount>
    implements PrivateEndpointConnection,
        PrivateEndpointConnection.Definition<CosmosDBAccount.DefinitionStages.WithCreate>,
        PrivateEndpointConnection.UpdateDefinition<CosmosDBAccount.UpdateStages.WithOptionals>,
        PrivateEndpointConnection.Update {
    private final PrivateEndpointConnectionsClient client;

    PrivateEndpointConnectionImpl(
        String name,
        CosmosDBAccountImpl parent,
        PrivateEndpointConnectionInner inner,
        PrivateEndpointConnectionsClient client) {
        super(name, parent, inner);
        this.client = client;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public PrivateEndpointProperty privateEndpoint() {
        return inner().privateEndpoint();
    }

    @Override
    public PrivateLinkServiceConnectionStateProperty privateLinkServiceConnectionState() {
        return inner().privateLinkServiceConnectionState();
    }

    @Override
    public PrivateEndpointConnectionImpl withStateProperty(PrivateLinkServiceConnectionStateProperty property) {
        this.inner().withPrivateLinkServiceConnectionState(property);
        return this;
    }

    @Override
    public PrivateEndpointConnectionImpl withStatus(String status) {
        if (this.inner().privateLinkServiceConnectionState() == null) {
            this.inner().withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionStateProperty());
        }
        this.inner().privateLinkServiceConnectionState().withStatus(status);
        return this;
    }

    @Override
    public PrivateEndpointConnectionImpl withDescription(String description) {
        if (this.inner().privateLinkServiceConnectionState() == null) {
            this.inner().withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionStateProperty());
        }
        this.inner().privateLinkServiceConnectionState().withDescription(description);
        return this;
    }

    @Override
    public Mono<PrivateEndpointConnection> createResourceAsync() {
        final PrivateEndpointConnectionImpl self = this;
        return this
            .client
            .createOrUpdateAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), this.inner())
            .map(
                privateEndpointConnectionInner -> {
                    self.setInner(privateEndpointConnectionInner);
                    return self;
                });
    }

    @Override
    public Mono<PrivateEndpointConnection> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    protected Mono<PrivateEndpointConnectionInner> getInnerAsync() {
        return this.client.getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    public CosmosDBAccountImpl attach() {
        return this.parent().withPrivateEndpointConnection(this);
    }
}
