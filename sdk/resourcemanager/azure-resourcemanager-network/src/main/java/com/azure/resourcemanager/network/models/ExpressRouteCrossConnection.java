// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCrossConnectionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Map;

/** Entry point for Express Route Cross Connection management API in Azure. */
@Fluent
public interface ExpressRouteCrossConnection extends
    GroupableResource<NetworkManager, ExpressRouteCrossConnectionInner>, Refreshable<ExpressRouteCrossConnection>,
    Updatable<ExpressRouteCrossConnection.Update>, UpdatableWithTags<ExpressRouteCrossConnection> {

    /**
     * Gets entry point to manage express route peerings associated with express route circuit.
     *
     * @return entry point to manage express route peerings associated with express route circuit
     */
    ExpressRouteCrossConnectionPeerings peerings();

    /**
     * Gets the name of the primary port.
     *
     * @return the name of the primary port
     */
    String primaryAzurePort();

    /**
     * Gets the name of the secondary port.
     *
     * @return the name of the secondary port
     */
    String secondaryAzurePort();

    /**
     * Gets the identifier of the circuit traffic.
     *
     * @return the identifier of the circuit traffic
     */
    Integer stag();

    /**
     * Gets the peering location of the ExpressRoute circuit.
     *
     * @return the peering location of the ExpressRoute circuit
     */
    String peeringLocation();

    /**
     * Gets the circuit bandwidth In Mbps.
     *
     * @return the circuit bandwidth In Mbps
     */
    int bandwidthInMbps();

    /**
     * Gets the express route circuit.
     *
     * @return the ExpressRouteCircuit
     */
    ExpressRouteCircuitReference expressRouteCircuit();

    /**
     * Gets the provisioning state of the circuit in the connectivity provider system.
     *
     * @return the provisioning state of the circuit in the connectivity provider system
     */
    ServiceProviderProvisioningState serviceProviderProvisioningState();

    /**
     * Gets additional read only notes set by the connectivity provider.
     *
     * @return additional read only notes set by the connectivity provider
     */
    String serviceProviderNotes();

    /**
     * Gets the provisioning state of the express route cross connection resource.
     *
     * @return the provisioning state of the express route cross connection resource
     */
    String provisioningState();

    /**
     * Gets the peerings associated with this express route cross connection.
     *
     * @return the peerings associated with this express route cross connection, indexed by name
     */
    Map<String, ExpressRouteCrossConnectionPeering> peeringsMap();

    /** Grouping of express route cross connection update stages. */
    interface UpdateStages {
        /**
         * The stage of express route cross connection update allowing to specify service provider provisioning state.
         */
        interface WithServiceProviderProviosioningState {
            /**
             * Specifies the service provider provisioning state for the express route cross connection.
             *
             * @param state the service provider provisioning state
             * @return next stage of update
             */
            Update withServiceProviderProvisioningState(ServiceProviderProvisioningState state);
        }

        /** The stage of express route cross connection update allowing to specify service provider notes. */
        interface WithServiceProviderNotes {
            /**
             * Specifies the service provider notes for the express route cross connection.
             *
             * @param notes the service provider notes
             * @return next stage of update
             */
            Update withServiceProviderNotes(String notes);
        }
    }

    /**
     * The template for a express route cross connection update operation, containing all the settings that can be
     * modified.
     */
    interface Update extends Appliable<ExpressRouteCrossConnection>, Resource.UpdateWithTags<Update>,
        UpdateStages.WithServiceProviderProviosioningState, UpdateStages.WithServiceProviderNotes {
    }
}
