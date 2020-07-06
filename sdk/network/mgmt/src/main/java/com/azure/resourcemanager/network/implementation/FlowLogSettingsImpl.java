// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.FlowLogSettings;
import com.azure.resourcemanager.network.models.RetentionPolicyParameters;
import com.azure.resourcemanager.network.fluent.inner.FlowLogInformationInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

/** Implementation for {@link FlowLogSettings} and its create and update interfaces. */
class FlowLogSettingsImpl extends RefreshableWrapperImpl<FlowLogInformationInner, FlowLogSettings>
    implements FlowLogSettings, FlowLogSettings.Update {
    private final NetworkWatcherImpl parent;
    private final String nsgId;

    FlowLogSettingsImpl(NetworkWatcherImpl parent, FlowLogInformationInner inner, String nsgId) {
        super(inner);
        this.parent = parent;
        this.nsgId = nsgId;
    }

    @Override
    public FlowLogSettings apply() {
        return applyAsync().block();
    }

    @Override
    public Mono<FlowLogSettings> applyAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getNetworkWatchers()
            .setFlowLogConfigurationAsync(parent().resourceGroupName(), parent().name(), this.inner())
            .map(
                flowLogInformationInner ->
                    new FlowLogSettingsImpl(FlowLogSettingsImpl.this.parent, flowLogInformationInner, nsgId));
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
        if (this.inner().flowAnalyticsConfiguration() != null
            && this.inner().flowAnalyticsConfiguration().networkWatcherFlowAnalyticsConfiguration() == null) {
            // Service response could have such case, which is not valid in swagger that
            // networkWatcherFlowAnalyticsConfiguration is a required field.
            this.inner().withFlowAnalyticsConfiguration(null);
        }
        return this;
    }

    @Override
    protected Mono<FlowLogInformationInner> getInnerAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getNetworkWatchers()
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
