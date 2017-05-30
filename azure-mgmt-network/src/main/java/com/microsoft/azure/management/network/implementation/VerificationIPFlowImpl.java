/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Access;
import com.microsoft.azure.management.network.Direction;
import com.microsoft.azure.management.network.Protocol;
import com.microsoft.azure.management.network.VerificationIPFlow;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

@LangDefinition
public class VerificationIPFlowImpl implements VerificationIPFlow, VerificationIPFlow.Definition {
    private final NetworkWatcherImpl parent;
    private VerificationIPFlowParametersInner parameters = new VerificationIPFlowParametersInner();
    private VerificationIPFlowResultInner result;

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
    public VerificationIPFlowImpl withProtocol(Protocol protocol) {
        parameters.withProtocol(protocol);
        return this;
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
        parameters.withLocalIPAddress(localIPAddress);
        return this;
    }

    @Override
    public VerificationIPFlowImpl withRemoteIPAddress(String remoteIPAddress) {
        parameters.withRemoteIPAddress(remoteIPAddress);
        return this;
    }

    @Override
    public VerificationIPFlow withTargetNicResourceId(String targetNicResourceId) {
        parameters.withTargetNicResourceId(targetNicResourceId);
        return this;
    }

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
    public VerificationIPFlow execute() {
        return executeAsync().toBlocking().last();
    }

    @Override
    public Observable<VerificationIPFlow> executeAsync() {
        return this.parent().manager().inner().networkWatchers()
                .verifyIPFlowAsync(parent.resourceGroupName(), parent.name(), parameters)
                .map(new Func1<VerificationIPFlowResultInner, VerificationIPFlow>() {
                    @Override
                    public VerificationIPFlow call(VerificationIPFlowResultInner verificationIPFlowResultInner) {
                        VerificationIPFlowImpl.this.result = verificationIPFlowResultInner;
                        return VerificationIPFlowImpl.this;
                    }
                });
    }

    @Override
    public ServiceFuture<VerificationIPFlow> executeAsync(ServiceCallback<VerificationIPFlow> callback) {
        return ServiceFuture.fromBody(executeAsync(), callback);
    }
}
