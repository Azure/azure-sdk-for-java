// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.FlowLogSettings;
import com.azure.resourcemanager.network.models.FlowLogStatusParameters;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.SecurityGroupView;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.models.FlowLogInformationInner;
import com.azure.resourcemanager.network.fluent.models.NetworkWatcherInner;
import com.azure.resourcemanager.network.fluent.models.SecurityGroupViewResultInner;
import com.azure.resourcemanager.network.models.SecurityGroupViewParameters;
import com.azure.resourcemanager.network.models.TagsObject;
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
        this.packetCaptures = new PacketCapturesImpl(networkManager.serviceClient().getPacketCaptures(), this);
        this.connectionMonitors =
            new ConnectionMonitorsImpl(networkManager.serviceClient().getConnectionMonitors(), this);
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
            this
                .manager()
                .serviceClient()
                .getNetworkWatchers()
                .getVMSecurityRules(this.resourceGroupName(), this.name(),
                    new SecurityGroupViewParameters().withTargetResourceId(vmId));
        return new SecurityGroupViewImpl(this, securityGroupViewResultInner, vmId);
    }

    @Override
    public Mono<SecurityGroupView> getSecurityGroupViewAsync(final String vmId) {
        return this
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .getVMSecurityRulesAsync(this.resourceGroupName(), this.name(),
                new SecurityGroupViewParameters().withTargetResourceId(vmId))
            .map(inner -> new SecurityGroupViewImpl(NetworkWatcherImpl.this, inner, vmId));
    }

    public FlowLogSettings getFlowLogSettings(String nsgId) {
        FlowLogInformationInner flowLogInformationInner =
            this
                .manager()
                .serviceClient()
                .getNetworkWatchers()
                .getFlowLogStatus(this.resourceGroupName(), this.name(),
                    new FlowLogStatusParameters().withTargetResourceId(nsgId));
        return new FlowLogSettingsImpl(this, flowLogInformationInner, nsgId);
    }

    @Override
    public Mono<FlowLogSettings> getFlowLogSettingsAsync(final String nsgId) {
        return this
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .getFlowLogStatusAsync(this.resourceGroupName(), this.name(),
                new FlowLogStatusParameters().withTargetResourceId(nsgId))
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
            .serviceClient()
            .getNetworkWatchers()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<NetworkWatcherInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getNetworkWatchers()
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
            .serviceClient()
            .getNetworkWatchers()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()))
            .flatMap(
                inner -> {
                    setInner(inner);
                    return Mono.just((NetworkWatcher) NetworkWatcherImpl.this);
                });
    }
}
