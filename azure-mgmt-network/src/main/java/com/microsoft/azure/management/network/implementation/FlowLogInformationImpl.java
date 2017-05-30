/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.FlowLogInformation;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for {@link FlowLogInformation} and its create and update interfaces.
 */
@LangDefinition
class FlowLogInformationImpl extends RefreshableWrapperImpl<FlowLogInformationInner,
        FlowLogInformation>
        implements
        FlowLogInformation,
        FlowLogInformation.Update {
    private final NetworkWatcherImpl parent;

    FlowLogInformationImpl(NetworkWatcherImpl parent, FlowLogInformationInner inner) {
        super(inner);
        this.parent = parent;
    }

    @Override
    public FlowLogInformation apply() {
        return applyAsync().toBlocking().last();
    }

    @Override
    public Observable<FlowLogInformation> applyAsync() {
        return this.parent().manager().inner().networkWatchers()
                .setFlowLogConfigurationAsync(parent().resourceGroupName(), parent().name(), this.inner())
                .map(new Func1<FlowLogInformationInner, FlowLogInformation>() {
            @Override
            public FlowLogInformation call(FlowLogInformationInner flowLogInformationInner) {
                return new FlowLogInformationImpl(FlowLogInformationImpl.this.parent, flowLogInformationInner);
            }
        });
    }

    @Override
    public ServiceFuture<FlowLogInformation> applyAsync(ServiceCallback<FlowLogInformation> callback) {
        return ServiceFuture.fromBody(applyAsync(), callback);
    }

    @Override
    public Update withEnabled(boolean enabled) {
        this.inner().withEnabled(enabled);
        return this;
    }

    @Override
    public Update withStorageAccount(String storageId) {
        this.inner().withStorageId(storageId);
        return this;
    }

    @Override
    public Update update() {
        return this;
    }

    @Override
    protected Observable<FlowLogInformationInner> getInnerAsync() {
        return this.parent().manager().inner().networkWatchers()
                .getFlowLogStatusAsync(parent().resourceGroupName(), parent().name(), inner().targetResourceId());
    }

    @Override
    public NetworkWatcherImpl parent() {
        return parent;
    }

    @Override
    public String key() {
        return null;
    }
}
