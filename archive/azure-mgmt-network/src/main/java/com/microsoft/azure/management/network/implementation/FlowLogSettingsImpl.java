/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.FlowLogSettings;
import com.microsoft.azure.management.network.RetentionPolicyParameters;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for {@link FlowLogSettings} and its create and update interfaces.
 */
@LangDefinition
class FlowLogSettingsImpl extends RefreshableWrapperImpl<FlowLogInformationInner,
        FlowLogSettings>
        implements
        FlowLogSettings,
        FlowLogSettings.Update {
    private final NetworkWatcherImpl parent;
    private final String nsgId;

    FlowLogSettingsImpl(NetworkWatcherImpl parent, FlowLogInformationInner inner, String nsgId) {
        super(inner);
        this.parent = parent;
        this.nsgId = nsgId;
    }

    @Override
    public FlowLogSettings apply() {
        return applyAsync().toBlocking().last();
    }

    @Override
    public Observable<FlowLogSettings> applyAsync() {
        return this.parent().manager().inner().networkWatchers()
                .setFlowLogConfigurationAsync(parent().resourceGroupName(), parent().name(), this.inner())
                .map(new Func1<FlowLogInformationInner, FlowLogSettings>() {
            @Override
            public FlowLogSettings call(FlowLogInformationInner flowLogInformationInner) {
                return new FlowLogSettingsImpl(FlowLogSettingsImpl.this.parent, flowLogInformationInner, nsgId);
            }
        });
    }

    @Override
    public ServiceFuture<FlowLogSettings> applyAsync(ServiceCallback<FlowLogSettings> callback) {
        return ServiceFuture.fromBody(applyAsync(), callback);
    }

    @Override
    public Update withLogging() {
        this.inner().withEnabled(true);
        return this;
    }

    @Override
    public Update withoutLogging() {
        this.inner().withEnabled(false);
        return this;
    }

    @Override
    public Update withStorageAccount(String storageId) {
        this.inner().withStorageId(storageId);
        return this;
    }

    @Override
    public Update withRetentionPolicyEnabled() {
        ensureRetentionPolicy();
        this.inner().retentionPolicy().withEnabled(true);
        return this;
    }

    @Override
    public Update withRetentionPolicyDisabled() {
        ensureRetentionPolicy();
        this.inner().retentionPolicy().withEnabled(false);
        return this;
    }

    @Override
    public Update withRetentionPolicyDays(int days) {
        ensureRetentionPolicy();
        this.inner().retentionPolicy().withDays(days);
        return this;
    }

    private void ensureRetentionPolicy() {
        if (this.inner().retentionPolicy() == null) {
            this.inner().withRetentionPolicy(new RetentionPolicyParameters());
        }
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

    @Override
    public String targetResourceId() {
        return inner().targetResourceId();
    }

    @Override
    public String storageId() {
        return inner().storageId();
    }

    @Override
    public boolean enabled() {
        return Utils.toPrimitiveBoolean(inner().enabled());
    }

    @Override
    public boolean isRetentionEnabled() {
        // will return default values if server response for retention policy was empty
        ensureRetentionPolicy();
        return Utils.toPrimitiveBoolean(inner().retentionPolicy().enabled());
    }

    @Override
    public int retentionDays() {
        // will return default values if server response for retention policy was empty
        ensureRetentionPolicy();
        return Utils.toPrimitiveInt(inner().retentionPolicy().days());
    }

    @Override
    public String networkSecurityGroupId() {
        return nsgId;
    }
}
