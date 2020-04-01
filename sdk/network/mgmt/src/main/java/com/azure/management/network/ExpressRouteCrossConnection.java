/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.ExpressRouteCrossConnectionInner;
import com.azure.management.network.models.UpdatableWithTags;
import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Appliable;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Updatable;

import java.util.Map;

/**
 * Entry point for Express Route Cross Connection management API in Azure.
 */
@Fluent
public interface ExpressRouteCrossConnection extends
        GroupableResource<NetworkManager, ExpressRouteCrossConnectionInner>,
        Refreshable<ExpressRouteCrossConnection>,
        Updatable<ExpressRouteCrossConnection.Update>,
        UpdatableWithTags<ExpressRouteCrossConnection> {

    /**
     * @return entry point to manage express route peerings associated with express route circuit
     */
    ExpressRouteCrossConnectionPeerings peerings();

    /**
     * @return the name of the primary  port
     */
    String primaryAzurePort();

    /**
     * @return the name of the secondary  port
     */
    String secondaryAzurePort();

    /**
     * @return the identifier of the circuit traffic
     */
    Integer sTag();

    /**
     * @return the peering location of the ExpressRoute circuit
     */
    String peeringLocation();

    /**
     * @return the circuit bandwidth In Mbps
     */
    int bandwidthInMbps();

    /**
     * @return the ExpressRouteCircuit
     */
    ExpressRouteCircuitReference expressRouteCircuit();

    /**
     * @return the provisioning state of the circuit in the connectivity provider system
     */
    ServiceProviderProvisioningState serviceProviderProvisioningState();

    /**
     * @return additional read only notes set by the connectivity provider
     */
    String serviceProviderNotes();

    /**
     * @return the provisioning state of the express route cross connection resource
     */
    String provisioningState();

    /**
     * @return the peerings associated with this express route cross connection, indexed by name
     */
    Map<String, ExpressRouteCrossConnectionPeering> peeringsMap();

    /**
     * Grouping of express route cross connection update stages.
     */
    interface UpdateStages {
        /**
         * The stage of express route cross connection update allowing to specify service provider provisioning state.
         */
        interface WithServiceProviderProviosioningState {
            Update withServiceProviderProvisioningState(ServiceProviderProvisioningState state);
        }

        /**
         * The stage of express route cross connection update allowing to specify service provider notes.
         */
        interface WithServiceProviderNotes {
            Update withServiceProviderNotes(String notes);
        }
    }

    /**
     * The template for a express route cross connection update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Appliable<ExpressRouteCrossConnection>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithServiceProviderProviosioningState,
            UpdateStages.WithServiceProviderNotes {
    }
}
