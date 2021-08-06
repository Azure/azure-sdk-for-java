// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.resourcemanager.network.models.FlowLogSettings;
import com.azure.resourcemanager.network.models.RetentionPolicyParameters;
import com.azure.resourcemanager.network.fluent.models.FlowLogInformationInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
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
        return applyAsync(Context.NONE);
    }

    @Override
    public FlowLogSettings apply(Context context) {
        return applyAsync(context).block();
    }

    @Override
    public Mono<FlowLogSettings> applyAsync(Context context) {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .setFlowLogConfigurationAsync(parent().resourceGroupName(), parent().name(), this.innerModel())
            .contextWrite(c -> c.putAll(FluxUtil.toReactorContext(context).readOnly()))
            .map(
                flowLogInformationInner ->
                    new FlowLogSettingsImpl(FlowLogSettingsImpl.this.parent, flowLogInformationInner, nsgId));
    }

    @Override
    public Update withLogging() {
        this.innerModel().withEnabled(true);
        return this;
    }

    @Override
    public Update withoutLogging() {
        this.innerModel().withEnabled(false);
        return this;
    }

    @Override
    public Update withStorageAccount(String storageId) {
        this.innerModel().withStorageId(storageId);
        return this;
    }

    @Override
    public Update withRetentionPolicyEnabled() {
        ensureRetentionPolicy();
        this.innerModel().retentionPolicy().withEnabled(true);
        return this;
    }

    @Override
    public Update withRetentionPolicyDisabled() {
        ensureRetentionPolicy();
        this.innerModel().retentionPolicy().withEnabled(false);
        return this;
    }

    @Override
    public Update withRetentionPolicyDays(int days) {
        ensureRetentionPolicy();
        this.innerModel().retentionPolicy().withDays(days);
        return this;
    }

    private void ensureRetentionPolicy() {
        if (this.innerModel().retentionPolicy() == null) {
            this.innerModel().withRetentionPolicy(new RetentionPolicyParameters());
        }
    }

    @Override
    public Update update() {
        if (this.innerModel().flowAnalyticsConfiguration() != null
            && this.innerModel().flowAnalyticsConfiguration().networkWatcherFlowAnalyticsConfiguration() == null) {
            // Service response could have such case, which is not valid in swagger that
            // networkWatcherFlowAnalyticsConfiguration is a required field.
            this.innerModel().withFlowAnalyticsConfiguration(null);
        }
        return this;
    }

    @Override
    protected Mono<FlowLogInformationInner> getInnerAsync() {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .getFlowLogStatusAsync(parent().resourceGroupName(), parent().name(), innerModel().targetResourceId());
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
        return innerModel().targetResourceId();
    }

    @Override
    public String storageId() {
        return innerModel().storageId();
    }

    @Override
    public boolean enabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().enabled());
    }

    @Override
    public boolean isRetentionEnabled() {
        // will return default values if server response for retention policy was empty
        ensureRetentionPolicy();
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().retentionPolicy().enabled());
    }

    @Override
    public int retentionDays() {
        // will return default values if server response for retention policy was empty
        ensureRetentionPolicy();
        return ResourceManagerUtils.toPrimitiveInt(innerModel().retentionPolicy().days());
    }

    @Override
    public String networkSecurityGroupId() {
        return nsgId;
    }
}
