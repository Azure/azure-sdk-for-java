/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ConnectionStatus;
import com.microsoft.azure.management.network.ConnectivityCheck;
import com.microsoft.azure.management.network.ConnectivityDestination;
import com.microsoft.azure.management.network.ConnectivityHop;
import com.microsoft.azure.management.network.ConnectivitySource;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation of ConnectivityCheck.
 */
@LangDefinition
public class ConnectivityCheckImpl extends ExecutableImpl<ConnectivityCheck>
        implements ConnectivityCheck, ConnectivityCheck.Definition {

    private final NetworkWatcherImpl parent;
    private ConnectivityParametersInner parameters = new ConnectivityParametersInner();
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
    public DefinitionStages.WithExecute fromSourcePort(int port) {
        ensureConnectivitySource().withPort(port);
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
        return Utils.toPrimitiveInt(result.avgLatencyInMs());
    }

    @Override
    public int minLatencyInMs() {
        return Utils.toPrimitiveInt(result.minLatencyInMs());
    }

    @Override
    public int maxLatencyInMs() {
        return Utils.toPrimitiveInt(result.maxLatencyInMs());
    }

    @Override
    public int probesSent() {
        return  Utils.toPrimitiveInt(result.probesSent());
    }

    @Override
    public int probesFailed() {
        return Utils.toPrimitiveInt(result.probesFailed());
    }

    @Override
    public Observable<ConnectivityCheck> executeWorkAsync() {
        return this.parent().manager().inner().networkWatchers()
                .checkConnectivityAsync(parent.resourceGroupName(), parent.name(), parameters)
                .map(new Func1<ConnectivityInformationInner, ConnectivityCheck>() {
                    @Override
                    public ConnectivityCheck call(ConnectivityInformationInner connectivityInformation) {
                        ConnectivityCheckImpl.this.result = connectivityInformation;
                        return ConnectivityCheckImpl.this;
                    }
                });
    }
}
