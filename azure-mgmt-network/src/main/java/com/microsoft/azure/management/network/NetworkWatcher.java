/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.NetworkWatcherInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Entry point for Network Watcher API in Azure.
 */
@Fluent
@Beta
public interface NetworkWatcher extends
        GroupableResource<NetworkManager, NetworkWatcherInner>,
        Refreshable<NetworkWatcher>,
        Updatable<NetworkWatcher.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * @param targetResourceGroup the name of the target resource group to perform topology on
     * @return current network topology by resource group
     */
    Topology topology(String targetResourceGroup);

    /**
     * @param vmId ID of the target VM
     * @return the configured and effective security group rules on the specified VM
     */
    SecurityGroupViewResult securityGroupViewResult(String vmId);

    /**
     * @param nsgId the name of the target resource group to perform topology on
     * @return information on the configuration of flow log
     */
    FlowLogInformation flowLogStatus(String nsgId);

    /**
     * Initiate troubleshooting on a specified resource.
     * @param targetResourceId the target resource to troubleshoot
     * @param storageId the ID for the storage account to save the troubleshoot result
     * @param storagePath the path to the blob to save the troubleshoot result in
     */
    Troubleshooting troubleshoot(String targetResourceId, String storageId, String storagePath);

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
     * The template for a domain update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<NetworkWatcher>,
            Resource.UpdateWithTags<Update> {
    }

}
