/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.ExpressRouteCircuitInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.Map;

/**
 * Entry point for Express Route Circuit management API in Azure.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface ExpressRouteCircuit extends
        GroupableResource<NetworkManager, ExpressRouteCircuitInner>,
        Refreshable<ExpressRouteCircuit>,
        Updatable<ExpressRouteCircuit.Update> {

    // Actions

    /**
     * @return entry point to manage express route peerings associated with express route circuit
     */
    @Method
    ExpressRouteCircuitPeerings peerings();

    // Getters

    /**
     * @return the SKU
     */
    ExpressRouteCircuitSku sku();

    /**
     * @return the flag indicating if classic operations allowed
     */
    boolean isAllowClassicOperations();

    /**
     * @return the CircuitProvisioningState state of the resource
     */
    String circuitProvisioningState();

    /**
     * The ServiceProviderProvisioningState state of the resource. Possible values include: 'NotProvisioned',
     * 'Provisioning', 'Provisioned', 'Deprovisioning'.
     * @return serviceProviderProvisioningState
     */
    ServiceProviderProvisioningState serviceProviderProvisioningState();

    /**
     * @return the peerings associated with this express route circuit, indexed by name
     */
    Map<String, ExpressRouteCircuitPeering> peeringsMap();

    /**
     * @return the ServiceKey
     */
    String serviceKey();

    /**
     * @return the ServiceProviderNotes
     */
    String serviceProviderNotes();

    /**
     * @return the ServiceProviderProperties
     */
    ExpressRouteCircuitServiceProviderProperties serviceProviderProperties();

    /**
     * Gets the provisioning state of the express route circuit resource. Possible values
     * are: 'Updating', 'Deleting', and 'Failed'.
     * @return provisioningState
     */
    String provisioningState();

    /**
     * The entirety of the express route circuit definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithServiceProvider,
            DefinitionStages.WithPeeringLocation,
            DefinitionStages.WithBandwidth,
            DefinitionStages.WithSkuTier,
            DefinitionStages.WithSkuFamily,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of express route circuit definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of express route circuit definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of express route circuit definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithServiceProvider> {
        }

        /**
         * The stage of express route circuit definition allowing to specify service provider name.
         */
        interface WithServiceProvider {
            WithPeeringLocation withServiceProvidet(String serviceProviderName);
        }

        /**
         * The stage of express route circuit definition allowing to specify service provider peering location.
         */
        interface WithPeeringLocation {
            WithBandwidth withPeeringLocation(String location);
        }

        /**
         * The stage of express route circuit definition allowing to specify service provider bandwidth.
         */
        interface WithBandwidth {
            WithSkuTier withBandwidthInMbps(int bandwidthInMbps);
        }

        /**
         * The stage of express route circuit definition allowing to specify SKU tier. Possible values include: 'Standard', 'Premium'.
         */
        interface WithSkuTier {
            WithSkuFamily withSkuTier(ExpressRouteCircuitSkuTier skuTier);
        }

        /**
         * The stage of express route circuit definition allowing to specify SKU family. Possible values include: 'UnlimitedData', 'MeteredData'.
         */
        interface WithSkuFamily {
            WithCreate withSkuFamily(ExpressRouteCircuitSkuFamily skuFamily);
        }

        /**
         * The stage of express route circuit definition allowing to enable/disable RDFE operations.
         */
        interface WithAllowClassicOperations {
            WithCreate enableClassicOperations();
        }

        /**
         * The stage of the express route circuit definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<ExpressRouteCircuit>,
                Resource.DefinitionWithTags<WithCreate>,
                WithAllowClassicOperations {
        }
    }

    /**
     * Grouping of express route circuit update stages.
     */
    interface UpdateStages {
        /**
         * The stage of express route circuit definition allowing to specify service provider bandwidth.
         */
        interface WithBandwidth {
            Update withBandwidthInMbps(int bandwidthInMbps);
        }

        /**
         * The stage of express route circuit definition allowing to specify SKU tier. Possible values include: 'Standard', 'Premium'.
         */
        interface WithSkuTier {
            Update withSkuTier(ExpressRouteCircuitSkuTier skuTier);
        }

        /**
         * The stage of express route circuit definition allowing to specify SKU family. Possible values include: 'UnlimitedData', 'MeteredData'.
         */
        interface WithSkuFamily {
            Update withSkuFamily(ExpressRouteCircuitSkuFamily skuFamily);
        }

        /**
         * The stage of express route circuit definition allowing to enable/disable RDFE operations.
         */
        interface WithAllowClassicOperations {
            Update enableClassicOperations();

            Update disableClassicOperations();
        }
    }

    /**
     * The template for a express route circuit update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Appliable<ExpressRouteCircuit>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithBandwidth,
            UpdateStages.WithSkuTier,
            UpdateStages.WithSkuFamily,
            UpdateStages.WithAllowClassicOperations {
    }
}
