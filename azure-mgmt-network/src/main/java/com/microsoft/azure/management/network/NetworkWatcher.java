/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.NetworkWatcherInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Observable;

/**
 * Entry point for Network Watcher API in Azure.
 */
@Fluent
@Beta
public interface NetworkWatcher extends
        GroupableResource<NetworkManager, NetworkWatcherInner>,
        Refreshable<NetworkWatcher>,
        Updatable<NetworkWatcher.Update> {

    /**
     * @return entry point to manage packet captures associated with network watcher
     */
    PacketCaptures packetCaptures();

    /**
     * Gets network topology of a given resource group.
     * @param targetResourceGroup the name of the target resource group to perform getTopology on
     * @return current network topology by resource group
     */
    Topology getTopology(String targetResourceGroup);

    /**
     * Gets network topology of a given resource group asynchronously.
     * @param targetResourceGroup the name of the target resource group to perform getTopology on
     * @return current network topology by resource group
     */
    Observable<Topology> getTopologyAsync(String targetResourceGroup);

    /**
     * Gets the configured and effective security group rules on the specified VM.
     * @param vmId ID of the target VM
     * @return the configured and effective security group rules on the specified VM
     */
    SecurityGroupView getSecurityGroupView(String vmId);

    /**
     * Gets the configured and effective security group rules on the specified VM asynchronously.
     * @param vmId ID of the target VM
     * @return the configured and effective security group rules on the specified VM
     */
    Observable<SecurityGroupView> getSecurityGroupViewAsync(String vmId);

    /**
     * Gets the information on the configuration of flow log.
     * @param nsgId the name of the target resource group to get flow log status for
     * @return information on the configuration of flow log
     */
    FlowLogSettings getFlowLogSettings(String nsgId);

    /**
     * Gets the information on the configuration of flow log asynchronously.
     * @param nsgId the name of the target resource group to get flow log status for
     * @return information on the configuration of flow log
     */
    Observable<FlowLogSettings> getFlowLogSettingsAsync(String nsgId);

    /**
     * First step specifying the parameters to get next hop for the VM.
     * @return a stage to specify parameters for next hop
     */
    @Method
    NextHop.DefinitionStages.WithTargetResource nextHop();

    /**
     * Verify IP flow from the specified VM to a location given the currently configured NSG rules.
     * @return a stage to specify parameters for ip flow verification
     */
    @Method
    VerificationIPFlow.DefinitionStages.WithTargetResource verifyIPFlow();

    /**
     * Verifies the possibility of establishing a direct TCP connection from a virtual machine to a given endpoint
     * including another virtual machine or an arbitrary remote server.
     * @return a stage to specify parameters for connectivity check
     */
    ConnectivityCheck.DefinitionStages.ToDestination checkConnectivity();

    /**
     * Container interface for all the definitions.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of network watcher definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a network watcher definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the network watcher definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * A network watcher with sufficient inputs to create a new network watcher in the cloud,
         * but exposing additional optional inputs to specify.
         */
        interface WithCreate extends
                Creatable<NetworkWatcher>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<NetworkWatcher>,
            Resource.UpdateWithTags<Update> {
    }
}
