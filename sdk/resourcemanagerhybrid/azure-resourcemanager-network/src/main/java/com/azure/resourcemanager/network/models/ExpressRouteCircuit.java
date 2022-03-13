// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Map;

/** Entry point for Express Route Circuit management API in Azure. */
@Fluent
public interface ExpressRouteCircuit
    extends GroupableResource<NetworkManager, ExpressRouteCircuitInner>,
        Refreshable<ExpressRouteCircuit>,
        Updatable<ExpressRouteCircuit.Update>,
        UpdatableWithTags<ExpressRouteCircuit> {

    // Actions

    /** @return entry point to manage express route peerings associated with express route circuit */
    ExpressRouteCircuitPeerings peerings();

    // Getters

    /** @return the SKU type */
    ExpressRouteCircuitSkuType sku();

    /** @return the flag indicating if classic operations allowed */
    boolean isAllowClassicOperations();

    /** @return the CircuitProvisioningState state of the resource */
    String circuitProvisioningState();

    /**
     * The ServiceProviderProvisioningState state of the resource.
     *
     * @return serviceProviderProvisioningState
     */
    ServiceProviderProvisioningState serviceProviderProvisioningState();

    /** @return the peerings associated with this express route circuit, indexed by name */
    Map<String, ExpressRouteCircuitPeering> peeringsMap();

    /** @return the ServiceKey */
    String serviceKey();

    /** @return the ServiceProviderNotes */
    String serviceProviderNotes();

    /** @return the ServiceProviderProperties */
    ExpressRouteCircuitServiceProviderProperties serviceProviderProperties();

    /**
     * Gets the provisioning state of the express route circuit resource.
     *
     * @return provisioningState
     */
    String provisioningState();

    /** The entirety of the express route circuit definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithServiceProvider,
            DefinitionStages.WithPeeringLocation,
            DefinitionStages.WithBandwidth,
            DefinitionStages.WithSku,
            DefinitionStages.WithCreate {
    }

    /** Grouping of express route circuit definition stages. */
    interface DefinitionStages {
        /** The first stage of express route circuit definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of express route circuit definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithServiceProvider> {
        }

        /** The stage of express route circuit definition allowing to specify service provider name. */
        interface WithServiceProvider {
            /**
             * Specifies the service provider name for the express route circuit.
             *
             * @param serviceProviderName the service provider name
             * @return the next stage of the definition
             */
            WithPeeringLocation withServiceProvider(String serviceProviderName);
        }

        /** The stage of express route circuit definition allowing to specify service provider peering location. */
        interface WithPeeringLocation {
            /**
             * Specifies the location for the express route circuit.
             *
             * @param location the location
             * @return the next stage of the definition
             */
            WithBandwidth withPeeringLocation(String location);
        }

        /** The stage of express route circuit definition allowing to specify service provider bandwidth. */
        interface WithBandwidth {
            /**
             * Specifies the bandwidth in Mbps for the express route circuit.
             *
             * @param bandwidthInMbps the bandwidth in Mbps
             * @return the next stage of the definition
             */
            WithSku withBandwidthInMbps(int bandwidthInMbps);
        }

        /** The stage of express route circuit definition allowing to specify SKU tier and family. */
        interface WithSku {
            /**
             * Specifies the SKU type for the express route circuit.
             *
             * @param skuType the SKU type
             * @return the next stage of the definition
             */
            WithCreate withSku(ExpressRouteCircuitSkuType skuType);
        }

        /** The stage of express route circuit definition allowing to enable/disable classic operations. */
        interface WithAllowClassicOperations {
            /**
             * Specifies whether classic operations are enabled.
             *
             * @return the next stage of the definition
             */
            WithCreate withClassicOperations();
        }

        /** The stage of definition allowing to add authorization. */
        interface WithAuthorization {
            /**
             * Specifies the authorization name for the express route circuit.
             *
             * @param authorizationName the authorization name
             * @return the next stage of the definition
             */
            WithCreate withAuthorization(String authorizationName);
        }

        /**
         * The stage of the express route circuit definition which contains all the minimum required inputs for the
         * resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<ExpressRouteCircuit>,
                Resource.DefinitionWithTags<WithCreate>,
                WithAllowClassicOperations,
                WithAuthorization {
        }
    }

    /** Grouping of express route circuit update stages. */
    interface UpdateStages {
        /** The stage of express route circuit update allowing to specify service provider bandwidth. */
        interface WithBandwidth {
            /**
             * Specifies the bandwidth in Mbps for the express route circuit.
             *
             * @param bandwidthInMbps the bandwidth in Mbps
             * @return the next stage of the update
             */
            Update withBandwidthInMbps(int bandwidthInMbps);
        }

        /** The stage of express route circuit update allowing to specify SKU tier and family. */
        interface WithSku {
            /**
             * Specifies the SKU for the express route circuit.
             *
             * @param sku the SKI
             * @return the next stage of the update
             */
            Update withSku(ExpressRouteCircuitSkuType sku);
        }

        /** The stage of express route circuit update allowing to enable/disable classic operations. */
        interface WithAllowClassicOperations {
            /**
             * Specifies whether classic operations are enabled.
             *
             * @return the next stage of the update
             */
            Update withClassicOperations();

            /**
             * Specifies whether classic operations are disabled.
             *
             * @return the next stage of the update
             */
            Update withoutClassicOperations();
        }

        /** The stage of express route circuit update allowing to add authorization. */
        interface WithAuthorization {
            /**
             * Specifies the authorization name for the express route circuit.
             *
             * @param authorizationName the authorization name
             * @return the next stage of the update
             */
            Update withAuthorization(String authorizationName);
        }
    }

    /** The template for a express route circuit update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<ExpressRouteCircuit>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithBandwidth,
            UpdateStages.WithSku,
            UpdateStages.WithAllowClassicOperations,
            UpdateStages.WithAuthorization {
    }
}
