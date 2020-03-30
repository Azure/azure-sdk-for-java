/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.FlowLogSettings;
import com.azure.management.network.NetworkWatcher;
import com.azure.management.network.SecurityGroupView;
import com.azure.management.network.models.AppliableWithTags;
import com.azure.management.network.models.FlowLogInformationInner;
import com.azure.management.network.models.NetworkWatcherInner;
import com.azure.management.network.models.SecurityGroupViewResultInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation for Network Watcher and its create and update interfaces.
 */
class NetworkWatcherImpl
        extends GroupableResourceImpl<
        NetworkWatcher,
        NetworkWatcherInner,
        NetworkWatcherImpl,
        NetworkManager>
        implements
        NetworkWatcher,
        NetworkWatcher.Definition,
        NetworkWatcher.Update,
        AppliableWithTags<NetworkWatcher> {

    private PacketCapturesImpl packetCaptures;
    private ConnectionMonitorsImpl connectionMonitors;

    NetworkWatcherImpl(String name,
                       final NetworkWatcherInner innerModel,
                       final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.packetCaptures = new PacketCapturesImpl(networkManager.inner().packetCaptures(), this);
        this.connectionMonitors = new ConnectionMonitorsImpl(networkManager.inner().connectionMonitors(), this);
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
        SecurityGroupViewResultInner securityGroupViewResultInner = this.manager().inner().networkWatchers()
                .getVMSecurityRules(this.resourceGroupName(), this.name(), vmId);
        return new SecurityGroupViewImpl(this, securityGroupViewResultInner, vmId);
    }

    @Override
    public Mono<SecurityGroupView> getSecurityGroupViewAsync(final String vmId) {
        return this.manager().inner().networkWatchers()
                .getVMSecurityRulesAsync(this.resourceGroupName(), this.name(), vmId)
                .map(inner -> new SecurityGroupViewImpl(NetworkWatcherImpl.this, inner, vmId));
    }

    public FlowLogSettings getFlowLogSettings(String nsgId) {
        FlowLogInformationInner flowLogInformationInner = this.manager().inner().networkWatchers()
                .getFlowLogStatus(this.resourceGroupName(), this.name(), nsgId);
        return new FlowLogSettingsImpl(this, flowLogInformationInner, nsgId);
    }

    @Override
    public Mono<FlowLogSettings> getFlowLogSettingsAsync(final String nsgId) {
        return this.manager().inner().networkWatchers()
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
        return this.manager().inner().networkWatchers().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<NetworkWatcherInner> getInnerAsync() {
        return this.manager().inner().networkWatchers().getByResourceGroupAsync(this.resourceGroupName(), this.name());
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
        return this.manager().inner().networkWatchers().updateTagsAsync(resourceGroupName(), name(), inner().getTags())
                .flatMap(inner -> {
                    setInner(inner);
                    return Mono.just((NetworkWatcher) NetworkWatcherImpl.this);
                });
    }

}
