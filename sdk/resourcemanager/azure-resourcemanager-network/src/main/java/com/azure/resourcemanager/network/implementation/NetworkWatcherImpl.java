// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.FlowLogSettings;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.SecurityGroupView;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.inner.FlowLogInformationInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkWatcherInner;
import com.azure.resourcemanager.network.fluent.inner.SecurityGroupViewResultInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

/** Implementation for Network Watcher and its create and update interfaces. */
class NetworkWatcherImpl
    extends GroupableResourceImpl<NetworkWatcher, NetworkWatcherInner, NetworkWatcherImpl, NetworkManager>
    implements NetworkWatcher, NetworkWatcher.Definition, NetworkWatcher.Update, AppliableWithTags<NetworkWatcher> {

    private PacketCapturesImpl packetCaptures;
    private ConnectionMonitorsImpl connectionMonitors;

    NetworkWatcherImpl(String name, final NetworkWatcherInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.packetCaptures = new PacketCapturesImpl(networkManager.inner().getPacketCaptures(), this);
        this.connectionMonitors = new ConnectionMonitorsImpl(networkManager.inner().getConnectionMonitors(), this);
    }

    public PacketCapturesImpl packetCaptures() {
        return packetCaptures;
    }

    @Override
    public ConnectionMonitorsImpl connectionMonitors() {
        return connectionMonitors;
    }

    // Verbs

    @Override
    public TopologyImpl topology() {
        return new TopologyImpl(this);
    }

    @Override
    public SecurityGroupView getSecurityGroupView(String vmId) {
        SecurityGroupViewResultInner securityGroupViewResultInner =
            this.manager().inner().getNetworkWatchers().getVMSecurityRules(this.resourceGroupName(), this.name(), vmId);
        return new SecurityGroupViewImpl(this, securityGroupViewResultInner, vmId);
    }

    @Override
    public Mono<SecurityGroupView> getSecurityGroupViewAsync(final String vmId) {
        return this
            .manager()
            .inner()
            .getNetworkWatchers()
            .getVMSecurityRulesAsync(this.resourceGroupName(), this.name(), vmId)
            .map(inner -> new SecurityGroupViewImpl(NetworkWatcherImpl.this, inner, vmId));
    }

    public FlowLogSettings getFlowLogSettings(String nsgId) {
        FlowLogInformationInner flowLogInformationInner =
            this.manager().inner().getNetworkWatchers().getFlowLogStatus(this.resourceGroupName(), this.name(), nsgId);
        return new FlowLogSettingsImpl(this, flowLogInformationInner, nsgId);
    }

    @Override
    public Mono<FlowLogSettings> getFlowLogSettingsAsync(final String nsgId) {
        return this
            .manager()
            .inner()
            .getNetworkWatchers()
            .getFlowLogStatusAsync(this.resourceGroupName(), this.name(), nsgId)
            .map(inner -> new FlowLogSettingsImpl(NetworkWatcherImpl.this, inner, nsgId));
    }

    public NextHopImpl nextHop() {
        return new NextHopImpl(this);
    }

    @Override
    public VerificationIPFlowImpl verifyIPFlow() {
        return new VerificationIPFlowImpl(this);
    }

    @Override
    public ConnectivityCheckImpl checkConnectivity() {
        return new ConnectivityCheckImpl(this);
    }

    @Override
    public TroubleshootingImpl troubleshoot() {
        return new TroubleshootingImpl(this);
    }

    @Override
    public AvailableProvidersImpl availableProviders() {
        return new AvailableProvidersImpl(this);
    }

    @Override
    public AzureReachabilityReportImpl azureReachabilityReport() {
        return new AzureReachabilityReportImpl(this);
    }

    @Override
    public Mono<NetworkWatcher> createResourceAsync() {
        return this
            .manager()
            .inner()
            .getNetworkWatchers()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<NetworkWatcherInner> getInnerAsync() {
        return this.manager().inner().getNetworkWatchers()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public NetworkWatcherImpl updateTags() {
        return this;
    }

    @Override
    public NetworkWatcher applyTags() {
        return applyTagsAsync().block();
    }

    @Override
    public Mono<NetworkWatcher> applyTagsAsync() {
        return this
            .manager()
            .inner()
            .getNetworkWatchers()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags())
            .flatMap(
                inner -> {
                    setInner(inner);
                    return Mono.just((NetworkWatcher) NetworkWatcherImpl.this);
                });
    }
}
