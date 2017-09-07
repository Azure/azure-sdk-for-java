/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ConnectivityCheck;
import com.microsoft.azure.management.network.FlowLogSettings;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.SecurityGroupView;
import com.microsoft.azure.management.network.Topology;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for Network Watcher and its create and update interfaces.
 */
@LangDefinition
class NetworkWatcherImpl
        extends GroupableResourceImpl<
        NetworkWatcher,
                NetworkWatcherInner,
                NetworkWatcherImpl,
                NetworkManager>
        implements
        NetworkWatcher,
        NetworkWatcher.Definition,
        NetworkWatcher.Update {

    private PacketCapturesImpl packetCaptures;

    NetworkWatcherImpl(String name,
                final NetworkWatcherInner innerModel,
                final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.packetCaptures = new PacketCapturesImpl(networkManager.inner().packetCaptures(), this);
    }

    public PacketCapturesImpl packetCaptures() {
        return packetCaptures;
    }

    // Verbs

    @Override
    public TopologyImpl getTopology(String targetResourceGroup) {
        TopologyInner topologyInner = this.manager().inner().networkWatchers()
                .getTopology(this.resourceGroupName(), this.name(), targetResourceGroup);
        return new TopologyImpl(this, topologyInner, targetResourceGroup);
    }

    @Override
    public Observable<Topology> getTopologyAsync(final String targetResourceGroup) {
        return this.manager().inner().networkWatchers()
                .getTopologyAsync(this.resourceGroupName(), this.name(), targetResourceGroup)
                .map(new Func1<TopologyInner, Topology>() {
                    @Override
                    public Topology call(TopologyInner inner) {
                        return new TopologyImpl(NetworkWatcherImpl.this, inner, targetResourceGroup);
                    }
                });
    }

    @Override
    public SecurityGroupView getSecurityGroupView(String vmId) {
        SecurityGroupViewResultInner securityGroupViewResultInner = this.manager().inner().networkWatchers()
                .getVMSecurityRules(this.resourceGroupName(), this.name(), vmId);
        return new SecurityGroupViewImpl(this, securityGroupViewResultInner, vmId);
    }

    @Override
    public Observable<SecurityGroupView> getSecurityGroupViewAsync(final String vmId) {
        return this.manager().inner().networkWatchers()
                .getVMSecurityRulesAsync(this.resourceGroupName(), this.name(), vmId)
                .map(new Func1<SecurityGroupViewResultInner, SecurityGroupView>() {
                    @Override
                    public SecurityGroupView call(SecurityGroupViewResultInner inner) {
                        return new SecurityGroupViewImpl(NetworkWatcherImpl.this, inner, vmId);
                    }
                });
    }

    public FlowLogSettings getFlowLogSettings(String nsgId) {
        FlowLogInformationInner flowLogInformationInner = this.manager().inner().networkWatchers()
                .getFlowLogStatus(this.resourceGroupName(), this.name(), nsgId);
        return new FlowLogSettingsImpl(this, flowLogInformationInner, nsgId);
    }

    @Override
    public Observable<FlowLogSettings> getFlowLogSettingsAsync(final String nsgId) {
        return this.manager().inner().networkWatchers()
                .getFlowLogStatusAsync(this.resourceGroupName(), this.name(), nsgId)
                .map(new Func1<FlowLogInformationInner, FlowLogSettings>() {
                    @Override
                    public FlowLogSettings call(FlowLogInformationInner inner) {
                        return new FlowLogSettingsImpl(NetworkWatcherImpl.this, inner, nsgId);
                    }
                });
    }

    public NextHopImpl nextHop() {
        return new NextHopImpl(this);
    }

    @Override
    public VerificationIPFlowImpl verifyIPFlow() {
        return new VerificationIPFlowImpl(this);
    }

    @Override
    public ConnectivityCheck.DefinitionStages.ToDestination checkConnectivity() {
        return new ConnectivityCheckImpl(this);
    }

    @Override
    public Observable<NetworkWatcher> createResourceAsync() {
        return this.manager().inner().networkWatchers().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<NetworkWatcherInner> getInnerAsync() {
        return this.manager().inner().networkWatchers().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }
}
