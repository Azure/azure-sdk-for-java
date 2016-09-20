/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.model.HasPrivateIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

/**
 * An immutable client-side representation of a private frontend of an internal load balancer.
 */
@Fluent()
public interface PrivateFrontend extends
    Frontend,
    HasPrivateIpAddress,
    HasSubnet {

    /**
     * @return the private IP allocation method within the associated subnet for this private frontend
     */
    IPAllocationMethod privateIpAllocationMethod();

    /**
     * Grouping of private frontend definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a private frontend definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of a private frontend definition allowing to specify a subnet from the selected network.
         * @param <ParentT> the next stage of the parent definition
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
         * The final stage of a private frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinitionAlt<ParentT>,
            HasPrivateIpAddress.DefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
        }
    }

    /** The entirety of a private frontend definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
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
        HasPrivateIpAddress.UpdateStages.WithPrivateIpAddress<Update> {
    }

    /**
     * Grouping of private frontend definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a private frontend definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of a private frontend definition allowing to specify a subnet from the selected network.
         * @param <ParentT> the next stage of the parent definition
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

        /** The final stage of an internal frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdateAlt<ParentT>,
            HasPrivateIpAddress.UpdateDefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
        }
    }

    /** The entirety of a private frontend definition as part of a load balancer update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithSubnet<ParentT> {
    }
}
