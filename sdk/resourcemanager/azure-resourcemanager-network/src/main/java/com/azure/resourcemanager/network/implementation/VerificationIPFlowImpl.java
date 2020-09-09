// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.Access;
import com.azure.resourcemanager.network.models.Direction;
import com.azure.resourcemanager.network.models.IpFlowProtocol;
import com.azure.resourcemanager.network.models.VerificationIPFlow;
import com.azure.resourcemanager.network.models.VerificationIpFlowParameters;
import com.azure.resourcemanager.network.fluent.inner.VerificationIpFlowResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import reactor.core.publisher.Mono;

/** Implementation of VerificationIPFlow. */
public class VerificationIPFlowImpl extends ExecutableImpl<VerificationIPFlow>
    implements VerificationIPFlow, VerificationIPFlow.Definition {
    private final NetworkWatcherImpl parent;
    private VerificationIpFlowParameters parameters = new VerificationIpFlowParameters();
    private VerificationIpFlowResultInner result;

    VerificationIPFlowImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public VerificationIPFlowImpl withTargetResourceId(String targetResourceId) {
        parameters.withTargetResourceId(targetResourceId);
        return this;
    }

    @Override
    public VerificationIPFlowImpl withDirection(Direction direction) {
        parameters.withDirection(direction);
        return this;
    }

    @Override
    public DefinitionStages.WithProtocol inbound() {
        return withDirection(Direction.INBOUND);
    }

    @Override
    public DefinitionStages.WithProtocol outbound() {
        return withDirection(Direction.OUTBOUND);
    }

    @Override
    public VerificationIPFlowImpl withProtocol(IpFlowProtocol protocol) {
        parameters.withProtocol(protocol);
        return this;
    }

    @Override
    public DefinitionStages.WithLocalIP withTCP() {
        return withProtocol(IpFlowProtocol.TCP);
    }

    @Override
    public DefinitionStages.WithLocalIP withUDP() {
        return withProtocol(IpFlowProtocol.UDP);
    }

    @Override
    public VerificationIPFlowImpl withLocalPort(String localPort) {
        parameters.withLocalPort(localPort);
        return this;
    }

    @Override
    public VerificationIPFlowImpl withRemotePort(String remotePort) {
        parameters.withRemotePort(remotePort);
        return this;
    }

    @Override
    public VerificationIPFlowImpl withLocalIPAddress(String localIPAddress) {
        parameters.withLocalIpAddress(localIPAddress);
        return this;
    }

    @Override
    public VerificationIPFlowImpl withRemoteIPAddress(String remoteIPAddress) {
        parameters.withRemoteIpAddress(remoteIPAddress);
        return this;
    }

    @Override
    public VerificationIPFlow withTargetNetworkInterfaceId(String targetNetworkInterfaceId) {
        parameters.withTargetNicResourceId(targetNetworkInterfaceId);
        return this;
    }

    @Override
    public NetworkWatcherImpl parent() {
        return parent;
    }

    @Override
    public Access access() {
        return this.result.access();
    }

    @Override
    public String ruleName() {
        return this.result.ruleName();
    }

    @Override
    public Mono<VerificationIPFlow> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getNetworkWatchers()
            .verifyIpFlowAsync(parent.resourceGroupName(), parent.name(), parameters)
            .map(
                verificationIPFlowResultInner -> {
                    VerificationIPFlowImpl.this.result = verificationIPFlowResultInner;
                    return VerificationIPFlowImpl.this;
                });
    }
}
