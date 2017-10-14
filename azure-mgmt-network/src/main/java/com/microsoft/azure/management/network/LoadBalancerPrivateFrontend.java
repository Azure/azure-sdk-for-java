/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.model.HasPrivateIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

import java.util.Set;

/**
 * A client-side representation of a private frontend of an internal load balancer.
 */
@Fluent()
public interface LoadBalancerPrivateFrontend extends
    LoadBalancerFrontend,
    HasPrivateIPAddress,
    HasSubnet {

    /**
     * @return associated subnet
     * <p>
     * Note this makes a separate call to Azure.
     */
    Subnet getSubnet();

    /**
     * @return the availability zones assigned to private frontend.
     */
    @Beta(Beta.SinceVersion.V1_3_0)
    Set<AvailabilityZoneId> availabilityZones();

    /**
     * Grouping of private frontend definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a private frontend definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of a private frontend definition allowing to specify a subnet from the selected network.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.DefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Assigns the specified subnet to this private frontend of an internal load balancer.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a private frontend definition allowing to specify availability zone.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        @Beta(Beta.SinceVersion.V1_3_0)
        interface WithAvailabilityZone<ParentT> {
            /**
             * Specifies the availability zone for the private frontend.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_3_0)
            WithAttach<ParentT> withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /**
         * The final stage of a private frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinitionAlt<ParentT>,
            HasPrivateIPAddress.DefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>>,
            DefinitionStages.WithAvailabilityZone<ParentT> {
        }
    }

    /** The entirety of a private frontend definition.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithSubnet<ParentT> {
    }

    /**
     * Grouping of private frontend update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a private frontend update allowing to specify a subnet from the selected network.
         */
        interface WithSubnet {
            /**
             * Assigns the specified subnet to this private frontend of the internal load balancer.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            Update withExistingSubnet(Network network, String subnetName);
        }
    }

    /**
     * The entirety of a private frontend update as part of a load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithSubnet,
        HasPrivateIPAddress.UpdateStages.WithPrivateIPAddress<Update> {
    }

    /**
     * Grouping of private frontend definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a private frontend definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of a private frontend definition allowing to specify a subnet from the selected network.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSubnet<ParentT> {
            /**
             * Assigns the specified subnet to this private frontend of the internal load balancer.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a private frontend definition allowing to specify availability zone.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        @Beta(Beta.SinceVersion.V1_3_0)
        interface WithAvailabilityZone<ParentT> {
            /**
             * Specifies the availability zone for the private frontend.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_3_0)
            WithAttach<ParentT> withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /** The final stage of an internal frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdateAlt<ParentT>,
            HasPrivateIPAddress.UpdateDefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>>,
            WithAvailabilityZone<ParentT> {
        }
    }

    /** The entirety of a private frontend definition as part of a load balancer update.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithSubnet<ParentT> {
    }
}
