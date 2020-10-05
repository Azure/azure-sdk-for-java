// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ConnectionStatus;
import com.azure.resourcemanager.network.models.ConnectivityCheck;
import com.azure.resourcemanager.network.models.ConnectivityDestination;
import com.azure.resourcemanager.network.models.ConnectivityHop;
import com.azure.resourcemanager.network.models.ConnectivityParameters;
import com.azure.resourcemanager.network.models.ConnectivitySource;
import com.azure.resourcemanager.network.models.Protocol;
import com.azure.resourcemanager.network.fluent.models.ConnectivityInformationInner;
import com.azure.resourcemanager.network.models.HasNetworkInterfaces;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.List;
import reactor.core.publisher.Mono;

/** Implementation of ConnectivityCheck. */
public class ConnectivityCheckImpl extends ExecutableImpl<ConnectivityCheck>
    implements ConnectivityCheck, ConnectivityCheck.Definition {

    private final NetworkWatcherImpl parent;
    private ConnectivityParameters parameters = new ConnectivityParameters();
    private ConnectivityInformationInner result;

    ConnectivityCheckImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public ConnectivityCheckImpl fromSourceVirtualMachine(String sourceResourceId) {
        ensureConnectivitySource().withResourceId(sourceResourceId);
        return this;
    }

    @Override
    public DefinitionStages.WithExecute fromSourceVirtualMachine(HasNetworkInterfaces vm) {
        ensureConnectivitySource().withResourceId(vm.id());
        return this;
    }

    @Override
    public ConnectivityCheckImpl toDestinationResourceId(String resourceId) {
        ensureConnectivityDestination().withResourceId(resourceId);
        return this;
    }

    @Override
    public ConnectivityCheckImpl toDestinationAddress(String address) {
        ensureConnectivityDestination().withAddress(address);
        return this;
    }

    @Override
    public ConnectivityCheckImpl toDestinationPort(int port) {
        ensureConnectivityDestination().withPort(port);
        return this;
    }

    @Override
    public ConnectivityCheckImpl fromSourcePort(int port) {
        ensureConnectivitySource().withPort(port);
        return this;
    }

    @Override
    public ConnectivityCheckImpl withProtocol(Protocol protocol) {
        parameters.withProtocol(protocol);
        return this;
    }

    private ConnectivitySource ensureConnectivitySource() {
        if (parameters.source() == null) {
            parameters.withSource(new ConnectivitySource());
        }
        return parameters.source();
    }

    private ConnectivityDestination ensureConnectivityDestination() {
        if (parameters.destination() == null) {
            parameters.withDestination(new ConnectivityDestination());
        }
        return parameters.destination();
    }

    @Override
    public NetworkWatcherImpl parent() {
        return parent;
    }

    @Override
    public List<ConnectivityHop> hops() {
        return result.hops();
    }

    @Override
    public ConnectionStatus connectionStatus() {
        return result.connectionStatus();
    }

    @Override
    public int avgLatencyInMs() {
        return ResourceManagerUtils.toPrimitiveInt(result.avgLatencyInMs());
    }

    @Override
    public int minLatencyInMs() {
        return ResourceManagerUtils.toPrimitiveInt(result.minLatencyInMs());
    }

    @Override
    public int maxLatencyInMs() {
        return ResourceManagerUtils.toPrimitiveInt(result.maxLatencyInMs());
    }

    @Override
    public int probesSent() {
        return ResourceManagerUtils.toPrimitiveInt(result.probesSent());
    }

    @Override
    public int probesFailed() {
        return ResourceManagerUtils.toPrimitiveInt(result.probesFailed());
    }

    @Override
    public Mono<ConnectivityCheck> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .checkConnectivityAsync(parent.resourceGroupName(), parent.name(), parameters)
            .map(
                connectivityInformation -> {
                    ConnectivityCheckImpl.this.result = connectivityInformation;
                    return ConnectivityCheckImpl.this;
                });
    }
}
